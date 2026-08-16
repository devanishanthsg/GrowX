import pandas as pd
import numpy as np
from pathlib import Path

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import (
    StratifiedKFold,
    cross_val_score,
    cross_val_predict
)
from sklearn.metrics import (
    classification_report,
    confusion_matrix
)

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "data" / "Crop_recommendation.csv"

FEATURES = [
    "N",
    "P",
    "K",
    "temperature",
    "humidity",
    "ph",
    "rainfall"
]

TARGET = "label"

print("=" * 65)
print("GrowX - Crop AI Reliability Validation")
print("=" * 65)

df = pd.read_csv(DATA_PATH)

X = df[FEATURES]
y = df[TARGET]

model = RandomForestClassifier(
    n_estimators=500,
    random_state=42,
    n_jobs=-1,
    class_weight="balanced"
)

# --------------------------------------------------
# 5-Fold Stratified Cross Validation
# --------------------------------------------------

cv = StratifiedKFold(
    n_splits=5,
    shuffle=True,
    random_state=42
)

scores = cross_val_score(
    model,
    X,
    y,
    cv=cv,
    scoring="accuracy",
    n_jobs=-1
)

print("\n5-FOLD CROSS VALIDATION")
print("-" * 40)

for i, score in enumerate(scores, start=1):
    print(
        f"Fold {i}: "
        f"{score:.4f} "
        f"({score * 100:.2f}%)"
    )

print("\nAverage Accuracy:")
print(f"{scores.mean() * 100:.2f}%")

print("\nStandard Deviation:")
print(f"{scores.std() * 100:.4f}%")

# --------------------------------------------------
# Cross-validated predictions
# --------------------------------------------------

predictions = cross_val_predict(
    model,
    X,
    y,
    cv=cv,
    n_jobs=-1
)

print("\n" + "=" * 65)
print("CLASSIFICATION REPORT")
print("=" * 65)

print(
    classification_report(
        y,
        predictions,
        zero_division=0
    )
)

print("\n" + "=" * 65)
print("CONFUSION MATRIX")
print("=" * 65)

print(
    confusion_matrix(
        y,
        predictions
    )
)

# --------------------------------------------------
# Train final model on entire dataset
# --------------------------------------------------

print("\nTraining final Random Forest on entire dataset...")

model.fit(X, y)

print("Final model trained.")

print("\nClasses:")
print(model.classes_)

print("\n" + "=" * 65)
print("VALIDATION COMPLETED")
print("=" * 65)