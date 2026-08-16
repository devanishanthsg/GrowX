from fastapi import FastAPI
from pydantic import BaseModel

from app.crop_predictor import predict_crop


app = FastAPI(
    title="GrowX AI Service",
    version="1.0.0",
    description="AI service for GrowX precision farming"
)


class CropPredictionRequest(BaseModel):
    N: float
    P: float
    K: float
    temperature: float
    humidity: float
    ph: float
    rainfall: float

    district: str | None = None
    sowingMonth: int | None = None


@app.get("/")
def root():
    return {
        "service": "GrowX AI Service",
        "status": "running",
        "version": "1.0.0"
    }


@app.get("/health")
def health():
    return {
        "status": "UP"
    }


@app.post("/predict/crop")
def predict_crop_endpoint(
    request: CropPredictionRequest
):
    data = request.model_dump()

    values = {
        "N": data["N"],
        "P": data["P"],
        "K": data["K"],
        "temperature": data["temperature"],
        "humidity": data["humidity"],
        "ph": data["ph"],
        "rainfall": data["rainfall"]
    }

    return predict_crop(
        values=values,
        district=data.get("district"),
        sowing_month=data.get("sowingMonth")
    )