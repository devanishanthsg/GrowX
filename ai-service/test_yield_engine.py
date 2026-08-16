from app.yield_engine import get_expected_yield


print("=" * 60)
print("GrowX Yield Engine Test")
print("=" * 60)


tests = [
    {
        "crop": "rice",
        "district": "Coimbatore",
        "season": "Navarai"
    },

    {
        "crop": "rice",
        "district": "Coimbatore",
        "season": "Kar"
    },

    {
        "crop": "rice",
        "district": "Coimbatore",
        "season": "Early Samba"
    },

    {
        "crop": "banana",
        "district": "Coimbatore",
        "season": "Kar"
    },

    {
        "crop": "rice",
        "district": "Chennai",
        "season": "Navarai"
    }
]


for test in tests:

    print("\nInput:")
    print(test)

    result = get_expected_yield(
        crop=test["crop"],
        district=test["district"],
        season=test["season"]
    )

    print("Result:")
    print(result)


print("\n" + "=" * 60)
print("Yield engine testing completed.")
print("=" * 60)