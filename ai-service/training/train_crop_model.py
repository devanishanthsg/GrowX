import pandas as pd
import joblib
from pathlib import Path

from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.svm import SVC
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix
)

# --------------------------------------------------
# Paths
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "data" / "Crop_recommendation.csv"
MODEL_DIR = BASE_DIR / "models"

MODEL_DIR.mkdir(exist_ok=True)

# --------------------------------------------------
# Load dataset
# --------------------------------------------------

print("=" * 60)
print("GrowX - Crop Recommendation Model Training")
print("=" * 60)

df = pd.read_csv(DATA_PATH)

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

X = df[FEATURES]
y = df[TARGET]

# --------------------------------------------------
# Train / Test split
# Stratify keeps crop distribution balanced
# --------------------------------------------------

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

print("\nTraining samples:", len(X_train))
print("Testing samples :", len(X_test))

# --------------------------------------------------
# Models
# --------------------------------------------------

models = {
    "Logistic Regression": Pipeline([
        ("scaler", StandardScaler()),
        ("model", LogisticRegression(
            max_iter=3000,
            random_state=42
        ))
    ]),

    "Decision Tree": DecisionTreeClassifier(
        random_state=42
    ),

    "Random Forest": RandomForestClassifier(
        n_estimators=300,
        random_state=42,
        n_jobs=-1
    ),

    "SVM": Pipeline([
        ("scaler", StandardScaler()),
        ("model", SVC(
            probability=True,
            random_state=42
        ))
    ])
}

results = {}

best_name = None
best_model = None
best_accuracy = -1

# --------------------------------------------------
# Train and compare
# --------------------------------------------------

for name, model in models.items():

    print("\n" + "=" * 60)
    print("Training:", name)
    print("=" * 60)

    model.fit(X_train, y_train)

    predictions = model.predict(X_test)

    accuracy = accuracy_score(y_test, predictions)

    results[name] = accuracy

    print(f"Accuracy: {accuracy:.4f} ({accuracy * 100:.2f}%)")

    if accuracy > best_accuracy:
        best_accuracy = accuracy
        best_name = name
        best_model = model

# --------------------------------------------------
# Results
# --------------------------------------------------

print("\n" + "=" * 60)
print("MODEL COMPARISON")
print("=" * 60)

for name, accuracy in sorted(
    results.items(),
    key=lambda x: x[1],
    reverse=True
):
    print(f"{name:25} : {accuracy * 100:.2f}%")

# --------------------------------------------------
# Detailed evaluation of best model
# --------------------------------------------------

print("\n" + "=" * 60)
print("BEST MODEL")
print("=" * 60)

print("Model   :", best_name)
print(f"Accuracy: {best_accuracy * 100:.2f}%")

best_predictions = best_model.predict(X_test)

print("\nCLASSIFICATION REPORT")
print(
    classification_report(
        y_test,
        best_predictions,
        zero_division=0
    )
)

print("\nCONFUSION MATRIX")
print(confusion_matrix(y_test, best_predictions))

# --------------------------------------------------
# Save model
# --------------------------------------------------

MODEL_PATH = MODEL_DIR / "crop_model.joblib"

joblib.dump(best_model, MODEL_PATH)

print("\nModel saved to:")
print(MODEL_PATH)

print("\nGrowX Crop AI v1 training completed.")