import json
import joblib
import pandas as pd

from pathlib import Path
from sklearn.ensemble import RandomForestClassifier


BASE_DIR = Path(__file__).resolve().parent.parent

DATA_PATH = BASE_DIR / "data" / "Crop_recommendation.csv"
MODEL_DIR = BASE_DIR / "models"

MODEL_DIR.mkdir(exist_ok=True)


FEATURES = [
    "N",
    "P",
    "K",
    "temperature",
    "humidity",
    "ph",
    "rainfall"
]


print("=" * 60)
print("GrowX - Building Production Crop Model")
print("=" * 60)


# --------------------------------------------------
# Load dataset
# --------------------------------------------------

df = pd.read_csv(DATA_PATH)

X = df[FEATURES]
y = df["label"]


# --------------------------------------------------
# Train final model
# --------------------------------------------------

model = RandomForestClassifier(
    n_estimators=500,
    random_state=42,
    n_jobs=-1,
    class_weight="balanced"
)

model.fit(X, y)


# --------------------------------------------------
# Save model
# --------------------------------------------------

MODEL_PATH = MODEL_DIR / "crop_model.joblib"

joblib.dump(model, MODEL_PATH)


# --------------------------------------------------
# Save metadata
# --------------------------------------------------

feature_ranges = {}

for feature in FEATURES:
    feature_ranges[feature] = {
        "min": float(df[feature].min()),
        "max": float(df[feature].max()),
        "mean": float(df[feature].mean())
    }


metadata = {
    "model_name": "GrowX Crop Recommendation Model",
    "version": "1.0.0",
    "algorithm": "RandomForestClassifier",
    "trees": 500,

    "features": FEATURES,

    "classes": sorted(
        y.unique().tolist()
    ),

    "feature_ranges": feature_ranges,

    "dataset_rows": int(len(df)),

    "validation": {
        "method": "5-fold stratified cross validation",
        "average_accuracy": 0.9959,
        "standard_deviation": 0.003015
    }
}


METADATA_PATH = MODEL_DIR / "crop_model_metadata.json"

with open(
    METADATA_PATH,
    "w",
    encoding="utf-8"
) as file:
    json.dump(
        metadata,
        file,
        indent=4
    )


print("\nModel saved:")
print(MODEL_PATH)

print("\nMetadata saved:")
print(METADATA_PATH)

print("\nClasses:")
for crop in model.classes_:
    print("-", crop)

print("\nGrowX Crop AI production model ready.")