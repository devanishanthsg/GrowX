from typing import Optional


# Verified initial season calendar.
# Start small and expand only with trusted agricultural sources.
#
# Structure:
# crop -> district -> list of possible sowing windows
#
# Months are numeric:
# 1 = January
# 12 = December

SEASON_CALENDAR = {
    "rice": {
        "coimbatore": [
            {
                "season": "Navarai",
                "months": [12, 1]
            },
            {
                "season": "Kar",
                "months": [5, 6]
            },
            {
                "season": "Early Samba",
                "months": [7, 8]
            },
            {
                "season": "Late Samba / Thaladi / Pishanam",
                "months": [9, 10]
            }
        ]
    }
}


def normalize_text(value: Optional[str]) -> Optional[str]:
    if value is None:
        return None

    value = value.strip().lower()

    return value if value else None


def get_crop_season(
    crop: str,
    district: Optional[str],
    sowing_month: Optional[int]
):
    """
    Finds a verified crop season using:
    - predicted crop
    - district
    - intended sowing month

    Returns a structured result instead of guessing.
    """

    crop_key = normalize_text(crop)
    district_key = normalize_text(district)

    if not crop_key:
        return {
            "status": "UNKNOWN",
            "season": None,
            "message": "Crop is required."
        }

    if not district_key:
        return {
            "status": "LOCATION_REQUIRED",
            "season": None,
            "message": (
                "Farm district is required for a reliable "
                "season recommendation."
            )
        }

    if sowing_month is None:
        return {
            "status": "MONTH_REQUIRED",
            "season": None,
            "message": (
                "Intended sowing month is required for a "
                "reliable season recommendation."
            )
        }

    try:
        sowing_month = int(sowing_month)
    except (TypeError, ValueError):
        return {
            "status": "INVALID_MONTH",
            "season": None,
            "message": "Sowing month must be a number from 1 to 12."
        }

    if sowing_month < 1 or sowing_month > 12:
        return {
            "status": "INVALID_MONTH",
            "season": None,
            "message": "Sowing month must be between 1 and 12."
        }

    crop_calendar = SEASON_CALENDAR.get(crop_key)

    if not crop_calendar:
        return {
            "status": "NOT_VERIFIED",
            "season": None,
            "message": (
                f"No verified season calendar is currently "
                f"available for {crop.title()}."
            )
        }

    district_calendar = crop_calendar.get(district_key)

    if not district_calendar:
        return {
            "status": "NOT_VERIFIED",
            "season": None,
            "message": (
                f"No verified {crop.title()} season calendar "
                f"is currently available for {district.title()}."
            )
        }

    matches = []

    for season_info in district_calendar:
        if sowing_month in season_info["months"]:
            matches.append(season_info["season"])

    if not matches:
        return {
            "status": "NO_MATCH",
            "season": None,
            "message": (
                f"No verified {crop.title()} sowing season "
                f"was found for {district.title()} in month "
                f"{sowing_month}."
            )
        }

    return {
        "status": "VERIFIED",
        "season": " / ".join(matches),
        "message": (
            f"Season matched using the verified crop calendar "
            f"for {crop.title()} in {district.title()}."
        )
    }