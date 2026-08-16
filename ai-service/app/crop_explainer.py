import pandas as pd
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent

DATA_PATH = (
    BASE_DIR
    / "data"
    / "Crop_recommendation.csv"
)

FEATURES = [
    "N",
    "P",
    "K",
    "temperature",
    "humidity",
    "ph",
    "rainfall"
]

FEATURE_LABELS = {
    "N": "nitrogen",
    "P": "phosphorus",
    "K": "potassium",
    "temperature": "temperature",
    "humidity": "humidity",
    "ph": "soil pH",
    "rainfall": "rainfall"
}


df = pd.read_csv(DATA_PATH)


def generate_explanation(
    crop: str,
    values: dict,
    confidence: float
):

    crop_data = df[
        df["label"].str.lower()
        == crop.lower()
    ]

    if crop_data.empty:
        return (
            f"{crop.title()} was recommended "
            f"with {confidence:.2f}% confidence "
            "based on the supplied soil and "
            "weather parameters."
        )

    matches = []

    for feature in FEATURES:

        user_value = float(
            values[feature]
        )

        mean = crop_data[
            feature
        ].mean()

        std = crop_data[
            feature
        ].std()

        if std == 0:
            continue

        z_score = abs(
            user_value - mean
        ) / std

        matches.append(
            (
                feature,
                z_score,
                user_value,
                mean
            )
        )

    # Smaller z-score =
    # closer to typical crop values
    matches.sort(
        key=lambda item: item[1]
    )

    strongest = matches[:3]

    factors = []

    for (
        feature,
        z_score,
        user_value,
        mean
    ) in strongest:

        label = FEATURE_LABELS[
            feature
        ]

        factors.append(label)

    if factors:

        factor_text = ", ".join(
            factors[:-1]
        )

        if len(factors) > 1:
            factor_text += (
                f" and {factors[-1]}"
            )
        else:
            factor_text = factors[0]

        explanation = (
            f"{crop.title()} was recommended "
            f"with {confidence:.2f}% confidence "
            f"because the supplied {factor_text} "
            "values closely match the conditions "
            f"represented by {crop.title()} "
            "samples in the trained dataset."
        )

    else:

        explanation = (
            f"{crop.title()} was recommended "
            f"with {confidence:.2f}% confidence "
            "based on the supplied soil nutrient "
            "and weather parameters."
        )

    return explanation