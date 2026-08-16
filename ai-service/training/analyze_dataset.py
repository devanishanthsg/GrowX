import pandas as pd
from pathlib import Path

# Dataset location
BASE_DIR = Path(__file__).resolve().parent.parent
DATASET_PATH = BASE_DIR / "data" / "Crop_recommendation.csv"

print("=" * 60)
print("GrowX - Crop Recommendation Dataset Analysis")
print("=" * 60)

# Load dataset
df = pd.read_csv(DATASET_PATH)

# 1. Basic information
print("\n1. DATASET SHAPE")
print("Rows:", df.shape[0])
print("Columns:", df.shape[1])

# 2. Column names
print("\n2. COLUMNS")
print(df.columns.tolist())

# 3. First five records
print("\n3. FIRST 5 ROWS")
print(df.head())

# 4. Missing values
print("\n4. MISSING VALUES")
print(df.isnull().sum())

# 5. Duplicate rows
print("\n5. DUPLICATE ROWS")
print("Duplicates:", df.duplicated().sum())

# 6. Data types
print("\n6. DATA TYPES")
print(df.dtypes)

# 7. Crop classes
print("\n7. CROP CLASSES")

if "label" in df.columns:
    print("Number of crops:", df["label"].nunique())
    print("\nSamples per crop:")
    print(df["label"].value_counts().sort_index())
else:
    print("ERROR: 'label' column not found.")

# 8. Numerical statistics
print("\n8. NUMERICAL STATISTICS")
print(df.describe().T)

# 9. Check important feature ranges
expected_features = [
    "N",
    "P",
    "K",
    "temperature",
    "humidity",
    "ph",
    "rainfall"
]

print("\n9. FEATURE RANGES")

for column in expected_features:
    if column in df.columns:
        print(
            f"{column:15} "
            f"min={df[column].min():.2f} "
            f"max={df[column].max():.2f} "
            f"mean={df[column].mean():.2f}"
        )
    else:
        print(f"WARNING: Missing expected column: {column}")

print("\n" + "=" * 60)
print("Analysis completed.")
print("=" * 60)