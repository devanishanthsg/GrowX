from app.crop_predictor import predict_crop


print("=" * 60)
print("GrowX Crop Predictor Test")
print("=" * 60)


# --------------------------------------------------
# Valid input test
# --------------------------------------------------

valid_sample = {
    "N": 90,
    "P": 42,
    "K": 43,
    "temperature": 20.88,
    "humidity": 82.0,
    "ph": 6.5,
    "rainfall": 202.9
}


print("\nVALID INPUT TEST")
print("-" * 40)

valid_result = predict_crop(valid_sample)

print(valid_result)


# --------------------------------------------------
# Invalid input test
# --------------------------------------------------

invalid_sample = {
    "N": -900,
    "P": 5000,
    "K": 20,
    "temperature": 300,
    "humidity": -10,
    "ph": 25,
    "rainfall": 9000
}

print("\nINVALID INPUT TEST")
print("-" * 40)

invalid_result = predict_crop(invalid_sample)

print(invalid_result)


print("\n" + "=" * 60)
print("Testing completed.")
print("=" * 60)