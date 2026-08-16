from app.season_engine import get_crop_season


print("=" * 60)
print("GrowX Season Engine Test")
print("=" * 60)


tests = [
    {
        "crop": "rice",
        "district": "Coimbatore",
        "month": 1
    },
    {
        "crop": "rice",
        "district": "Coimbatore",
        "month": 6
    },
    {
        "crop": "rice",
        "district": "Coimbatore",
        "month": 8
    },
    {
        "crop": "rice",
        "district": "Coimbatore",
        "month": 11
    },
    {
        "crop": "banana",
        "district": "Coimbatore",
        "month": 6
    }
]


for test in tests:

    print("\nInput:")
    print(test)

    result = get_crop_season(
        crop=test["crop"],
        district=test["district"],
        sowing_month=test["month"]
    )

    print("Result:")
    print(result)


print("\n" + "=" * 60)
print("Season engine testing completed.")
print("=" * 60)