import json
import joblib
import numpy as np
import pandas as pd

from pathlib import Path

from app.season_engine import get_crop_season
from app.crop_explainer import generate_explanation
from app.yield_engine import get_expected_yield


BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "models" / "crop_model.joblib"
METADATA_PATH = BASE_DIR / "models" / "crop_model_metadata.json"

artifact = joblib.load(MODEL_PATH)

if not isinstance(artifact, dict) or artifact.get("artifact_type") != "GrowXCropRecommendationV2":
    raise RuntimeError(
        "GrowX Crop V2 model artifact was not found. "
        "Run training/build_crop_model.py before starting the API."
    )

model = artifact["model"]
scaler = artifact["scaler"]
neighbor_model = artifact["neighbor_model"]
OOD_THRESHOLD = float(artifact["ood_threshold"])
FEATURES = artifact["features"]
THRESHOLDS = artifact["thresholds"]

with open(METADATA_PATH, "r", encoding="utf-8") as file:
    metadata = json.load(file)


SAFE_LIMITS = {
    "N": (0, 200),
    "P": (0, 200),
    "K": (0, 300),
    "temperature": (0, 55),
    "humidity": (0, 100),
    "ph": (2.5, 11),
    "rainfall": (0, 500),
}


def validate_input(values):
    errors = []

    for feature in FEATURES:
        value = values.get(feature)

        if value is None:
            errors.append(f"{feature} is required")
            continue

        try:
            numeric_value = float(value)
        except (TypeError, ValueError):
            errors.append(f"{feature} must be numeric")
            continue

        minimum, maximum = SAFE_LIMITS[feature]

        if not minimum <= numeric_value <= maximum:
            errors.append(
                f"{feature} must be between {minimum} and {maximum}"
            )

    return errors


def _calculate_domain_result(row: pd.DataFrame):
    scaled = scaler.transform(row)

    distances, _ = neighbor_model.kneighbors(
        scaled,
        n_neighbors=6,
    )

    nearest = distances[0]

    # If this input exactly matches a training sample, ignore that zero-distance
    # neighbour so the score is comparable with the training threshold.
    if nearest[0] < 1e-12:
        usable = nearest[1:6]
    else:
        usable = nearest[:5]

    distance = float(np.mean(usable))
    ratio = distance / OOD_THRESHOLD if OOD_THRESHOLD > 0 else float("inf")

    outside_features = []

    for feature in FEATURES:
        value = float(row.iloc[0][feature])
        trained = metadata["feature_ranges"][feature]

        if value < trained["min"] or value > trained["max"]:
            outside_features.append(feature)

    is_ood = ratio > 1.0 or len(outside_features) >= 2

    if ratio <= 0.60 and not outside_features:
        domain_status = "IN_DISTRIBUTION"
    elif not is_ood:
        domain_status = "EDGE_OF_DISTRIBUTION"
    else:
        domain_status = "OUT_OF_DISTRIBUTION"

    return {
        "status": domain_status,
        "distance": round(distance, 4),
        "threshold": round(OOD_THRESHOLD, 4),
        "ratio": round(ratio, 4),
        "outsideFeatures": outside_features,
    }


def _status_from_reliability(confidence, margin, domain_result):
    if domain_result["status"] == "OUT_OF_DISTRIBUTION":
        return "OUT_OF_DISTRIBUTION"

    high = float(THRESHOLDS["high_confidence"])
    moderate = float(THRESHOLDS["moderate_confidence"])
    minimum_margin = float(THRESHOLDS["minimum_margin"])

    if confidence >= high:
        return "CONFIDENT"

    if confidence >= moderate and margin >= minimum_margin:
        return "LOW_CONFIDENCE"

    return "UNCERTAIN"


def predict_crop(values, district=None, sowing_month=None):
    errors = validate_input(values)

    if errors:
        return {
            "status": "INVALID_INPUT",
            "errors": errors,
            "modelVersion": metadata["version"],
        }

    row = pd.DataFrame(
        [[float(values[feature]) for feature in FEATURES]],
        columns=FEATURES,
    )

    probabilities = model.predict_proba(row)[0]
    classes = model.classes_

    ranked_indices = np.argsort(probabilities)[::-1]

    top_three = [
        {
            "crop": str(classes[index]),
            "confidence": round(float(probabilities[index]) * 100, 2),
        }
        for index in ranked_indices[:3]
    ]

    best = top_three[0]
    second = top_three[1]

    confidence_probability = float(probabilities[ranked_indices[0]])
    second_probability = float(probabilities[ranked_indices[1]])
    margin_probability = confidence_probability - second_probability

    domain_result = _calculate_domain_result(row)

    status = _status_from_reliability(
        confidence_probability,
        margin_probability,
        domain_result,
    )

    explanation = generate_explanation(
        crop=best["crop"],
        values=values,
        confidence=best["confidence"],
        status=status,
        alternative=second["crop"],
    )

    if status == "CONFIDENT":
        season_result = get_crop_season(
            crop=best["crop"],
            district=district,
            sowing_month=sowing_month,
        )

        yield_result = get_expected_yield(
            crop=best["crop"],
            district=district,
            season=season_result["season"],
        )
    else:
        season_result = {
            "status": "SKIPPED_UNCERTAIN",
            "season": None,
            "message": (
                "Season matching was skipped because the crop recommendation "
                "did not pass the high-reliability gate."
            ),
        }

        yield_result = {
            "status": "SKIPPED_UNCERTAIN",
            "expectedYield": None,
            "message": (
                "Yield reference was skipped because the crop recommendation "
                "did not pass the high-reliability gate."
            ),
        }

    if domain_result["status"] == "OUT_OF_DISTRIBUTION":
        reliability_message = (
            "The supplied combination is outside the region represented well "
            "by the training dataset. Treat the crop ranking as exploratory "
            "rather than a dependable recommendation."
        )
    elif status == "CONFIDENT":
        reliability_message = (
            "The input is within the trained data domain and the top crop "
            "cleared the cross-validated confidence threshold."
        )
    elif status == "LOW_CONFIDENCE":
        reliability_message = (
            "The model found a likely crop, but the top candidates are not "
            "separated strongly enough for a high-reliability recommendation."
        )
    else:
        reliability_message = (
            "The model cannot make a sufficiently reliable single-crop "
            "recommendation from these inputs."
        )

    recommendation_available = status == "CONFIDENT"

    if recommendation_available:
        recommendation_message = (
            f"{best['crop'].title()} passed the reliability checks and is "
            "returned as the recommended crop."
        )
    elif status == "OUT_OF_DISTRIBUTION":
        recommendation_message = (
            "No reliable crop recommendation was issued because the complete "
            "input combination is outside the model's reliable training domain. "
            "The candidate ranking is shown only for reference."
        )
    elif status == "LOW_CONFIDENCE":
        recommendation_message = (
            "No firm crop recommendation was issued because the model did not "
            "clear the high-confidence reliability gate. Review the candidate "
            "ranking instead."
        )
    else:
        recommendation_message = (
            "No reliable single-crop recommendation was issued because the "
            "model could not separate the leading candidates strongly enough."
        )

    return {
        "status": status,

        # A crop is called a recommendation ONLY after it clears every
        # reliability gate. This prevents an OOD/uncertain top guess from
        # being presented to farmers as an endorsed recommendation.
        "recommendationAvailable": recommendation_available,
        "recommendedCrop": best["crop"] if recommendation_available else None,
        "recommendationMessage": recommendation_message,

        # Keep the raw model ranking available for transparency/debugging.
        # topCandidate is NOT equivalent to recommendedCrop when reliability
        # gates fail.
        "topCandidate": best,
        "candidates": top_three,

        "confidence": best["confidence"],
        "confidenceMargin": round(margin_probability * 100, 2),

        "reliabilityStatus": status,
        "reliabilityMessage": reliability_message,
        "domainStatus": domain_result["status"],
        "domainScore": round(domain_result["ratio"], 4),
        "domainOutsideFeatures": domain_result["outsideFeatures"],

        "season": season_result["season"],
        "seasonStatus": season_result["status"],
        "seasonMessage": season_result["message"],

        "expectedYield": yield_result["expectedYield"],
        "yieldStatus": yield_result["status"],
        "yieldMessage": yield_result["message"],

        "explanation": explanation,
        "alternatives": top_three[1:],
        "modelVersion": metadata["version"],
    }
