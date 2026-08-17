from app.crop_predictor import predict_crop


def show(name, values, district="coimbatore", month=8):
    result = predict_crop(
        values=values,
        district=district,
        sowing_month=month,
    )

    print("\n" + "=" * 72)
    print(name)
    print("=" * 72)
    print("status             :", result.get("status"))
    print("recommendationAvail:", result.get("recommendationAvailable"))
    print("recommendedCrop    :", result.get("recommendedCrop"))
    print("topCandidate       :", result.get("topCandidate"))
    print("confidence         :", result.get("confidence"))
    print("confidenceMargin   :", result.get("confidenceMargin"))
    print("domainStatus       :", result.get("domainStatus"))
    print("domainScore        :", result.get("domainScore"))
    print("alternatives       :", result.get("alternatives"))
    print("seasonStatus       :", result.get("seasonStatus"))
    print("yieldStatus        :", result.get("yieldStatus"))
    print("explanation        :", result.get("explanation"))
    return result


rice = show(
    "Known in-domain rice example",
    {
        "N": 90,
        "P": 42,
        "K": 43,
        "temperature": 20.88,
        "humidity": 82,
        "ph": 6.5,
        "rainfall": 202.9,
    },
)

banana = show(
    "Known in-domain banana example",
    {
        "N": 100,
        "P": 80,
        "K": 50,
        "temperature": 27,
        "humidity": 80,
        "ph": 6.0,
        "rainfall": 100,
    },
)

ood = show(
    "Out-of-distribution example",
    {
        "N": 180,
        "P": 180,
        "K": 250,
        "temperature": 50,
        "humidity": 95,
        "ph": 10.5,
        "rainfall": 450,
    },
)

invalid = predict_crop(
    values={
        "N": 90,
        "P": 42,
        "K": 43,
        "temperature": 20,
        "humidity": 150,
        "ph": 6.5,
        "rainfall": 200,
    }
)

print("\nInvalid-input test:")
print(invalid)

if rice.get("recommendedCrop") != "rice" or rice.get("status") != "CONFIDENT":
    raise SystemExit("Rice test failed.")

if banana.get("recommendedCrop") != "banana" or banana.get("status") != "CONFIDENT":
    raise SystemExit("Banana test failed.")

if ood.get("status") != "OUT_OF_DISTRIBUTION":
    raise SystemExit("OOD gate test failed.")

if ood.get("recommendationAvailable") is not False:
    raise SystemExit("OOD recommendation gate failed.")

if ood.get("recommendedCrop") is not None:
    raise SystemExit(
        "OOD safety test failed: unreliable top candidate was exposed as a recommendation."
    )

if not ood.get("topCandidate") or not ood.get("candidates"):
    raise SystemExit("OOD transparency test failed: candidate ranking missing.")

if invalid.get("status") != "INVALID_INPUT":
    raise SystemExit("Invalid-input gate test failed.")

print("\nPASS - Crop Recommendation V2 reliability tests passed.")
