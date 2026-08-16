from typing import Optional


# --------------------------------------------------
# Verified reference yield knowledge
# --------------------------------------------------
#
# IMPORTANT:
# These are reference ranges, NOT exact AI predictions.
#
# Structure:
# crop -> district/region -> season -> yield range
#
# Unit:
# tonnes per hectare
#

YIELD_REFERENCE = {
    "rice": {
        "coimbatore": {
            "Navarai": {
                "min": 3.5,
                "max": 5.5
            },
            "Kar": {
                "min": 3.5,
                "max": 5.5
            },
            "Early Samba": {
                "min": 4.0,
                "max": 6.0
            },
            "Samba": {
                "min": 4.0,
                "max": 6.0
            },
            "Late Samba / Thaladi / Pishanam": {
                "min": 4.0,
                "max": 6.0
            }
        }
    }
}


def normalize_text(value: Optional[str]) -> Optional[str]:
    if value is None:
        return None

    value = value.strip().lower()

    return value if value else None


def get_expected_yield(
    crop: str,
    district: Optional[str],
    season: Optional[str]
):
    """
    Returns a reference yield range based on:
    - crop
    - district
    - verified season

    This is NOT a trained yield prediction model.
    """

    crop_key = normalize_text(crop)
    district_key = normalize_text(district)

    if not crop_key:
        return {
            "status": "UNKNOWN",
            "expectedYield": None,
            "message": "Crop is required."
        }

    if not district_key:
        return {
            "status": "LOCATION_REQUIRED",
            "expectedYield": None,
            "message": (
                "Farm district is required for a reliable "
                "yield reference."
            )
        }

    if not season:
        return {
            "status": "SEASON_REQUIRED",
            "expectedYield": None,
            "message": (
                "A verified crop season is required before "
                "estimating yield."
            )
        }

    crop_reference = YIELD_REFERENCE.get(crop_key)

    if not crop_reference:
        return {
            "status": "NOT_VERIFIED",
            "expectedYield": None,
            "message": (
                f"No verified yield reference is currently "
                f"available for {crop.title()}."
            )
        }

    district_reference = crop_reference.get(district_key)

    if not district_reference:
        return {
            "status": "NOT_VERIFIED",
            "expectedYield": None,
            "message": (
                f"No verified {crop.title()} yield reference "
                f"is currently available for {district.title()}."
            )
        }

    season_reference = district_reference.get(season)

    if not season_reference:
        return {
            "status": "NOT_VERIFIED",
            "expectedYield": None,
            "message": (
                f"No verified yield range is currently available "
                f"for {crop.title()} during {season} "
                f"in {district.title()}."
            )
        }

    minimum = season_reference["min"]
    maximum = season_reference["max"]

    return {
        "status": "REFERENCE_AVAILABLE",

        "expectedYield": (
            f"{minimum:.1f}-{maximum:.1f} tonnes/hectare"
        ),

        "minimumYield": minimum,
        "maximumYield": maximum,

        "message": (
            "This is a reference yield range based on crop, "
            "location and season. Actual yield may vary with "
            "variety, irrigation, fertilizer, soil condition, "
            "pests, diseases and farm management."
        )
    }