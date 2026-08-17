import json
import joblib
import numpy as np
import pandas as pd

from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "models" / "crop_model.joblib"
METADATA_PATH = BASE_DIR / "models" / "crop_model_metadata.json"

print("=" * 72)
print("GrowX Crop Recommendation V2 - Reliability Validation")
print("=" * 72)

artifact = joblib.load(MODEL_PATH)

if not isinstance(artifact, dict) or artifact.get("artifact_type") != "GrowXCropRecommendationV2":
    raise RuntimeError("V2 crop model artifact not found.")

with open(METADATA_PATH, "r", encoding="utf-8") as file:
    metadata = json.load(file)

validation = metadata["validation"]
reliability = metadata["reliability"]

print("\nMODEL")
print("Version :", metadata["version"])
print("Classes :", metadata["dataset_classes"])
print("Rows    :", metadata["dataset_rows"])

print("\nCROSS-VALIDATION")
print(f"Top-1 accuracy : {validation['top1_accuracy'] * 100:.2f}%")
print(f"Macro F1       : {validation['macro_f1'] * 100:.2f}%")
print(f"Top-2 accuracy : {validation['top2_accuracy'] * 100:.2f}%")
print(f"Top-3 accuracy : {validation['top3_accuracy'] * 100:.2f}%")
print(f"Wrong top-1    : {validation['wrong_predictions']}")

print("\nRELIABILITY GATES")
print(
    "High confidence :",
    round(reliability["high_confidence_threshold"] * 100, 2),
    "%"
)
print(
    "Moderate        :",
    round(reliability["moderate_confidence_threshold"] * 100, 2),
    "%"
)
print("OOD threshold   :", round(reliability["ood_threshold"], 4))

print("\nSanity checks complete.")
print("=" * 72)
