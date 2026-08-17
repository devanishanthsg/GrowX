import json
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent
METADATA_PATH = BASE_DIR / "models" / "crop_model_metadata.json"

FEATURES = [
    "N",
    "P",
    "K",
    "temperature",
    "humidity",
    "ph",
    "rainfall",
]

FEATURE_LABELS = {
    "N": "nitrogen",
    "P": "phosphorus",
    "K": "potassium",
    "temperature": "temperature",
    "humidity": "humidity",
    "ph": "soil pH",
    "rainfall": "rainfall",
}


def _load_metadata():
    with open(METADATA_PATH, "r", encoding="utf-8") as file:
        return json.load(file)


def _join_labels(labels):
    if not labels:
        return ""

    if len(labels) == 1:
        return labels[0]

    return ", ".join(labels[:-1]) + f" and {labels[-1]}"


def generate_explanation(
    crop: str,
    values: dict,
    confidence: float,
    status: str | None = None,
    alternative: str | None = None,
):
    metadata = _load_metadata()
    crop_profiles = metadata.get("class_profiles", {})
    profile = crop_profiles.get(crop)

    if not profile:
        return (
            f"{crop.title()} received the highest model score "
            f"({confidence:.2f}%) for the supplied soil and weather inputs."
        )

    comparisons = []

    for feature in FEATURES:
        stats = profile.get(feature)

        if not stats:
            continue

        user_value = float(values[feature])
        mean = float(stats["mean"])
        std = float(stats["std"])

        if std <= 1e-12:
            continue

        z_score = abs(user_value - mean) / std

        comparisons.append({
            "feature": feature,
            "z": z_score,
        })

    comparisons.sort(key=lambda item: item["z"])

    close_features = [
        FEATURE_LABELS[item["feature"]]
        for item in comparisons[:3]
    ]

    unusual_features = [
        FEATURE_LABELS[item["feature"]]
        for item in comparisons
        if item["z"] >= 2.0
    ][:2]

    if status == "OUT_OF_DISTRIBUTION":
        return (
            f"{crop.title()} has the highest model score ({confidence:.2f}%), "
            "but the complete input combination is outside the part of the "
            "training dataset represented reliably enough for a firm recommendation."
        )

    if status in {"LOW_CONFIDENCE", "UNCERTAIN"} and alternative:
        return (
            f"{crop.title()} currently ranks first at {confidence:.2f}%, "
            f"but {alternative.title()} is a meaningful alternative. "
            "The recommendation should be treated as uncertain rather than final."
        )

    close_text = _join_labels(close_features)

    explanation = (
        f"{crop.title()} was recommended with a {confidence:.2f}% model score. "
        f"Within the training data, the supplied {close_text} values are among "
        f"the closest matches to the typical {crop.title()} profile."
    )

    if unusual_features:
        explanation += (
            f" The supplied {_join_labels(unusual_features)} value(s) are less "
            f"typical for that crop profile, so they should be reviewed."
        )

    return explanation
