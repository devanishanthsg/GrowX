import json
import math
import joblib
import numpy as np
import pandas as pd

from pathlib import Path
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import StratifiedKFold, cross_val_predict
from sklearn.metrics import accuracy_score, f1_score
from sklearn.preprocessing import StandardScaler
from sklearn.neighbors import NearestNeighbors


BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "data" / "Crop_recommendation.csv"
MODEL_DIR = BASE_DIR / "models"
MODEL_DIR.mkdir(exist_ok=True)

MODEL_PATH = MODEL_DIR / "crop_model.joblib"
METADATA_PATH = MODEL_DIR / "crop_model_metadata.json"

FEATURES = [
    "N",
    "P",
    "K",
    "temperature",
    "humidity",
    "ph",
    "rainfall",
]
TARGET = "label"
RANDOM_STATE = 42


def _round_up(value: float, step: float = 0.01) -> float:
    return math.ceil(value / step) * step


print("=" * 72)
print("GrowX Crop Recommendation V2 - Production Model Builder")
print("=" * 72)

df = pd.read_csv(DATA_PATH)

required_columns = FEATURES + [TARGET]
missing = [column for column in required_columns if column not in df.columns]

if missing:
    raise RuntimeError(f"Dataset is missing required columns: {missing}")

if df[required_columns].isnull().any().any():
    raise RuntimeError("Dataset contains missing values. Clean the dataset before training.")

X = df[FEATURES].astype(float)
y = df[TARGET].astype(str)

print(f"\nRows    : {len(df)}")
print(f"Classes : {y.nunique()}")

base_model = RandomForestClassifier(
    n_estimators=500,
    random_state=RANDOM_STATE,
    n_jobs=-1,
    class_weight="balanced",
)

cv = StratifiedKFold(
    n_splits=5,
    shuffle=True,
    random_state=RANDOM_STATE,
)

print("\n[1/4] Generating 5-fold out-of-fold probabilities...")

oof_probabilities = cross_val_predict(
    base_model,
    X,
    y,
    cv=cv,
    method="predict_proba",
    n_jobs=-1,
)

classes = np.array(sorted(y.unique()))
predicted_indices = np.argmax(oof_probabilities, axis=1)
predictions = classes[predicted_indices]

confidence = np.max(oof_probabilities, axis=1)
sorted_probabilities = np.sort(oof_probabilities, axis=1)
margin = sorted_probabilities[:, -1] - sorted_probabilities[:, -2]

correct = predictions == y.to_numpy()

top2_indices = np.argsort(oof_probabilities, axis=1)[:, -2:]
top3_indices = np.argsort(oof_probabilities, axis=1)[:, -3:]

class_to_index = {label: index for index, label in enumerate(classes)}
truth_indices = np.array([class_to_index[label] for label in y])

top2_accuracy = float(
    np.mean([
        truth_indices[index] in top2_indices[index]
        for index in range(len(truth_indices))
    ])
)

top3_accuracy = float(
    np.mean([
        truth_indices[index] in top3_indices[index]
        for index in range(len(truth_indices))
    ])
)

accuracy = float(accuracy_score(y, predictions))
macro_f1 = float(f1_score(y, predictions, average="macro"))

wrong_confidence = confidence[~correct]

if len(wrong_confidence):
    highest_wrong_confidence = float(np.max(wrong_confidence))
else:
    highest_wrong_confidence = 0.0

# A recommendation must beat the strongest cross-validated wrong prediction
# with a small safety margin. A 0.75 floor prevents over-trusting weak votes.
high_confidence_threshold = max(
    0.75,
    _round_up(highest_wrong_confidence + 0.03, 0.01),
)
high_confidence_threshold = min(high_confidence_threshold, 0.90)

moderate_confidence_threshold = 0.55
minimum_margin = 0.10

print(f"Top-1 CV accuracy : {accuracy * 100:.2f}%")
print(f"Macro F1          : {macro_f1 * 100:.2f}%")
print(f"Top-2 CV accuracy : {top2_accuracy * 100:.2f}%")
print(f"Top-3 CV accuracy : {top3_accuracy * 100:.2f}%")
print(f"High-confidence threshold: {high_confidence_threshold:.2f}")

print("\n[2/4] Building input-domain detector...")

scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

neighbor_model = NearestNeighbors(
    n_neighbors=6,
    metric="euclidean",
)
neighbor_model.fit(X_scaled)

distances, _ = neighbor_model.kneighbors(
    X_scaled,
    n_neighbors=6,
)

# First neighbor is the sample itself.
training_ood_scores = distances[:, 1:6].mean(axis=1)
ood_threshold = float(np.quantile(training_ood_scores, 0.995))

print(f"OOD distance threshold: {ood_threshold:.4f}")

print("\n[3/4] Training final Random Forest on all data...")

final_model = RandomForestClassifier(
    n_estimators=500,
    random_state=RANDOM_STATE,
    n_jobs=-1,
    class_weight="balanced",
)
final_model.fit(X, y)

feature_ranges = {}
feature_quantiles = {}

for feature in FEATURES:
    feature_ranges[feature] = {
        "min": float(df[feature].min()),
        "max": float(df[feature].max()),
        "mean": float(df[feature].mean()),
        "std": float(df[feature].std()),
    }
    feature_quantiles[feature] = {
        "q01": float(df[feature].quantile(0.01)),
        "q05": float(df[feature].quantile(0.05)),
        "q50": float(df[feature].quantile(0.50)),
        "q95": float(df[feature].quantile(0.95)),
        "q99": float(df[feature].quantile(0.99)),
    }

class_profiles = {}

for crop, crop_data in df.groupby(TARGET):
    profile = {}
    for feature in FEATURES:
        profile[feature] = {
            "mean": float(crop_data[feature].mean()),
            "std": float(crop_data[feature].std()),
            "min": float(crop_data[feature].min()),
            "max": float(crop_data[feature].max()),
        }
    class_profiles[str(crop)] = profile

bundle = {
    "artifact_type": "GrowXCropRecommendationV2",
    "version": "2.0.0",
    "model": final_model,
    "scaler": scaler,
    "neighbor_model": neighbor_model,
    "ood_threshold": ood_threshold,
    "features": FEATURES,
    "classes": final_model.classes_.tolist(),
    "thresholds": {
        "high_confidence": high_confidence_threshold,
        "moderate_confidence": moderate_confidence_threshold,
        "minimum_margin": minimum_margin,
    },
}

joblib.dump(bundle, MODEL_PATH)

metadata = {
    "model_name": "GrowX Crop Recommendation Model",
    "version": "2.0.0",
    "algorithm": "RandomForestClassifier",
    "trees": 500,
    "features": FEATURES,
    "classes": final_model.classes_.tolist(),
    "dataset_rows": int(len(df)),
    "dataset_classes": int(y.nunique()),
    "feature_ranges": feature_ranges,
    "feature_quantiles": feature_quantiles,
    "class_profiles": class_profiles,
    "reliability": {
        "high_confidence_threshold": high_confidence_threshold,
        "moderate_confidence_threshold": moderate_confidence_threshold,
        "minimum_margin": minimum_margin,
        "ood_method": "StandardScaler + 5-nearest-neighbour mean distance",
        "ood_percentile": 99.5,
        "ood_threshold": ood_threshold,
    },
    "validation": {
        "method": "5-fold stratified out-of-fold prediction",
        "top1_accuracy": accuracy,
        "macro_f1": macro_f1,
        "top2_accuracy": top2_accuracy,
        "top3_accuracy": top3_accuracy,
        "wrong_predictions": int((~correct).sum()),
        "highest_wrong_confidence": highest_wrong_confidence,
    },
}

with open(METADATA_PATH, "w", encoding="utf-8") as file:
    json.dump(metadata, file, indent=4)

print("\n[4/4] Artifacts saved")
print("Model   :", MODEL_PATH)
print("Metadata:", METADATA_PATH)
print("\nGrowX Crop Recommendation V2 model is ready.")
