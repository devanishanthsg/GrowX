import { useState } from "react";

const initialForm = {
  nitrogen: "",
  phosphorus: "",
  potassium: "",
  temperature: "",
  humidity: "",
  ph: "",
  rainfall: "",
};

function CropRecommendation() {
  const [formData, setFormData] =
    useState(initialForm);

  const [prediction, setPrediction] =
    useState(null);

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: value,
    }));
  }

  function handleSubmit(event) {
    event.preventDefault();

    setPrediction({
      crop: "Rice",
      confidence: 96,
      season: "Kharif",
      yield: "5.8 tons/hectare",
      explanation:
        "The provided nutrient values, humidity and rainfall are suitable for rice cultivation.",
    });
  }

  function handleReset() {
    setFormData(initialForm);
    setPrediction(null);
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">AI TOOL</p>
          <h1>Crop Recommendation</h1>
          <p>
            Enter soil and weather information to
            receive an AI-powered crop suggestion.
          </p>
        </div>
      </header>

      <section className="form-result-layout">
        <form
          className="panel prediction-form"
          onSubmit={handleSubmit}
        >
          <div className="panel-heading">
            <div>
              <h2>Farm and soil information</h2>
              <p>
                All values should come from your latest
                soil test or farm observation.
              </p>
            </div>
          </div>

          <div className="form-grid">
            <label>
              Nitrogen (N)
              <input
                type="number"
                name="nitrogen"
                value={formData.nitrogen}
                onChange={handleChange}
                placeholder="Example: 90"
                required
              />
            </label>

            <label>
              Phosphorus (P)
              <input
                type="number"
                name="phosphorus"
                value={formData.phosphorus}
                onChange={handleChange}
                placeholder="Example: 42"
                required
              />
            </label>

            <label>
              Potassium (K)
              <input
                type="number"
                name="potassium"
                value={formData.potassium}
                onChange={handleChange}
                placeholder="Example: 43"
                required
              />
            </label>

            <label>
              Temperature (°C)
              <input
                type="number"
                name="temperature"
                value={formData.temperature}
                onChange={handleChange}
                placeholder="Example: 28"
                required
              />
            </label>

            <label>
              Humidity (%)
              <input
                type="number"
                name="humidity"
                value={formData.humidity}
                onChange={handleChange}
                placeholder="Example: 80"
                required
              />
            </label>

            <label>
              Soil pH
              <input
                type="number"
                step="0.1"
                name="ph"
                value={formData.ph}
                onChange={handleChange}
                placeholder="Example: 6.5"
                required
              />
            </label>

            <label className="full-field">
              Rainfall (mm)
              <input
                type="number"
                name="rainfall"
                value={formData.rainfall}
                onChange={handleChange}
                placeholder="Example: 200"
                required
              />
            </label>
          </div>

          <div className="form-buttons">
            <button
              type="button"
              className="outline-button"
              onClick={handleReset}
            >
              Clear
            </button>

            <button
              type="submit"
              className="primary-button"
            >
              Generate Recommendation
            </button>
          </div>
        </form>

        <article className="panel prediction-result">
          {!prediction ? (
            <div className="empty-result">
              <span>🌾</span>
              <h2>Your result will appear here</h2>
              <p>
                Complete the form and select Generate
                Recommendation.
              </p>
            </div>
          ) : (
            <div>
              <p className="result-label">
                RECOMMENDED CROP
              </p>

              <div className="crop-result-icon">
                🌾
              </div>

              <h2>{prediction.crop}</h2>

              <div className="confidence">
                <div>
                  <span>AI confidence</span>
                  <strong>
                    {prediction.confidence}%
                  </strong>
                </div>

                <div className="confidence-track">
                  <div
                    className="confidence-fill"
                    style={{
                      width: `${prediction.confidence}%`,
                    }}
                  />
                </div>
              </div>

              <div className="result-information">
                <div>
                  <span>Suitable season</span>
                  <strong>{prediction.season}</strong>
                </div>

                <div>
                  <span>Expected yield</span>
                  <strong>{prediction.yield}</strong>
                </div>
              </div>

              <div className="result-explanation">
                <h3>Why this crop?</h3>
                <p>{prediction.explanation}</p>
              </div>
            </div>
          )}
        </article>
      </section>
    </div>
  );
}

export default CropRecommendation;