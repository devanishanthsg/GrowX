import json
import joblib
import pandas as pd
from app.season_engine import get_crop_season
from app.crop_explainer import generate_explanation
from pathlib import Path
from app.yield_engine import get_expected_yield

# --------------------------------------------------
# Paths
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent.parent

MODEL_PATH = (
    BASE_DIR
    / "models"
    / "crop_model.joblib"
)

METADATA_PATH = (
    BASE_DIR
    / "models"
    / "crop_model_metadata.json"
)


# --------------------------------------------------
# Load model and metadata
# --------------------------------------------------

model = joblib.load(MODEL_PATH)

with open(
    METADATA_PATH,
    "r",
    encoding="utf-8"
) as file:
    metadata = json.load(file)


FEATURES = metadata["features"]


# --------------------------------------------------
# Safe input limits
# --------------------------------------------------

SAFE_LIMITS = {
    "N": (0, 200),
    "P": (0, 200),
    "K": (0, 300),
    "temperature": (0, 55),
    "humidity": (0, 100),
    "ph": (2.5, 11),
    "rainfall": (0, 500)
}


# --------------------------------------------------
# Validate input
# --------------------------------------------------

def validate_input(values):
    errors = []

    for feature in FEATURES:
        value = values.get(feature)

        if value is None:
            errors.append(
                f"{feature} is required"
            )
            continue

        try:
            numeric_value = float(value)
        except (TypeError, ValueError):
            errors.append(
                f"{feature} must be numeric"
            )
            continue

        minimum, maximum = SAFE_LIMITS[feature]

        if not minimum <= numeric_value <= maximum:
            errors.append(
                f"{feature} must be between "
                f"{minimum} and {maximum}"
            )

    return errors


# --------------------------------------------------
# Predict crop
# --------------------------------------------------

def predict_crop(
    values,
    district=None,
    sowing_month=None
):
    errors = validate_input(values)

    if errors:
        return {
            "status": "INVALID_INPUT",
            "errors": errors
        }

    # Keep the same column names used during training
    row = pd.DataFrame(
        [[
            float(values[feature])
            for feature in FEATURES
        ]],
        columns=FEATURES
    )

    # Get probability for every crop
    probabilities = model.predict_proba(row)[0]

    classes = model.classes_

    ranked_predictions = sorted(
        zip(classes, probabilities),
        key=lambda item: item[1],
        reverse=True
    )

    # Top 3 crops
    top_three = [
        {
            "crop": crop,
            "confidence": round(
                float(probability) * 100,
                2
            )
        }
        for crop, probability
        in ranked_predictions[:3]
    ]

    best = top_three[0]

    confidence = best["confidence"]

    explanation = generate_explanation(
    best["crop"],
    values,
    confidence
    )

    season_result = get_crop_season(
    crop=best["crop"],
    district=district,
    sowing_month=sowing_month
    )

    yield_result = get_expected_yield(
    crop=best["crop"],
    district=district,
    season=season_result["season"]
    )
    # --------------------------------------------------
    # Confidence status
    # --------------------------------------------------

    if confidence < 50:
        status = "UNCERTAIN"

    elif confidence < 70:
        status = "LOW_CONFIDENCE"

    else:
        status = "CONFIDENT"

    # --------------------------------------------------
    # Result
    # --------------------------------------------------

    return {
    "status": status,
    "recommendedCrop": best["crop"],
    "confidence": best["confidence"],

    "season": season_result["season"],
    "seasonStatus": season_result["status"],
    "seasonMessage": season_result["message"],

    "expectedYield": yield_result["expectedYield"],
    "yieldStatus": yield_result["status"],
    "yieldMessage": yield_result["message"],

    "explanation": explanation,

    "alternatives": top_three[1:],

    "modelVersion": metadata["version"]
    }