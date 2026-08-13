import { useState } from "react";
import { useAuth } from "../hooks/useAuth.js";
import { submitRecommendation } from "../api/cropApi.js";
import { validateCropForm } from "../utils/validators.js";

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
  const { currentFarm } = useAuth();

  const [formData, setFormData] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    }
    setApiError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (!currentFarm) {
      setApiError(
        "No farm found for your account. Please complete your farm profile first.",
      );
      return;
    }

    // Frontend validation
    const errors = validateCropForm(formData);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setLoading(true);
    setApiError("");
    setResult(null);

    try {
      const data = await submitRecommendation({
        farmId: currentFarm.id,
        nitrogen: Number(formData.nitrogen),
        phosphorus: Number(formData.phosphorus),
        potassium: Number(formData.potassium),
        temperature: Number(formData.temperature),
        humidity: Number(formData.humidity),
        ph: Number(formData.ph),
        rainfall: Number(formData.rainfall),
      });
      setResult(data);
    } catch (error) {
      if (error.data && typeof error.data === "object") {
        setFieldErrors(error.data);
      } else {
        setApiError(error.message ?? "Submission failed. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  }

  function handleReset() {
    setFormData(initialForm);
    setFieldErrors({});
    setResult(null);
    setApiError("");
  }

  // Helper for field error display
  function FieldError({ name }) {
    return fieldErrors[name] ? (
      <span className="field-error">{fieldErrors[name]}</span>
    ) : null;
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">AI TOOL</p>
          <h1>Crop Recommendation</h1>
          <p>
            Enter soil and weather information to receive
            an AI-powered crop suggestion.
          </p>
        </div>
      </header>

      {!currentFarm && (
        <p className="page-error">
          ⚠️ No farm linked to your account. Please update your Farm Profile before using this tool.
        </p>
      )}

      <section className="form-result-layout">
        <form
          className="panel prediction-form"
          onSubmit={handleSubmit}
          noValidate
        >
          <div className="panel-heading">
            <div>
              <h2>Farm and soil information</h2>
              <p>
                All values should come from your latest soil test
                or farm observation.
              </p>
            </div>
          </div>

          {apiError && (
            <p className="form-error">{apiError}</p>
          )}

          <div className="form-grid">
            <label>
              Nitrogen (N)
              <input
                type="number"
                name="nitrogen"
                id="crop-nitrogen"
                value={formData.nitrogen}
                onChange={handleChange}
                placeholder="Example: 90"
                disabled={loading}
                min="0"
              />
              <FieldError name="nitrogen" />
            </label>

            <label>
              Phosphorus (P)
              <input
                type="number"
                name="phosphorus"
                id="crop-phosphorus"
                value={formData.phosphorus}
                onChange={handleChange}
                placeholder="Example: 42"
                disabled={loading}
                min="0"
              />
              <FieldError name="phosphorus" />
            </label>

            <label>
              Potassium (K)
              <input
                type="number"
                name="potassium"
                id="crop-potassium"
                value={formData.potassium}
                onChange={handleChange}
                placeholder="Example: 43"
                disabled={loading}
                min="0"
              />
              <FieldError name="potassium" />
            </label>

            <label>
              Temperature (°C)
              <input
                type="number"
                name="temperature"
                id="crop-temperature"
                value={formData.temperature}
                onChange={handleChange}
                placeholder="Example: 28"
                disabled={loading}
              />
              <FieldError name="temperature" />
            </label>

            <label>
              Humidity (%)
              <input
                type="number"
                name="humidity"
                id="crop-humidity"
                value={formData.humidity}
                onChange={handleChange}
                placeholder="Example: 80"
                disabled={loading}
                min="0"
                max="100"
              />
              <FieldError name="humidity" />
            </label>

            <label>
              Soil pH
              <input
                type="number"
                step="0.1"
                name="ph"
                id="crop-ph"
                value={formData.ph}
                onChange={handleChange}
                placeholder="Example: 6.5"
                disabled={loading}
                min="0"
                max="14"
              />
              <FieldError name="ph" />
            </label>

            <label className="full-field">
              Rainfall (mm)
              <input
                type="number"
                name="rainfall"
                id="crop-rainfall"
                value={formData.rainfall}
                onChange={handleChange}
                placeholder="Example: 200"
                disabled={loading}
                min="0"
              />
              <FieldError name="rainfall" />
            </label>
          </div>

          <div className="form-buttons">
            <button
              type="button"
              className="outline-button"
              onClick={handleReset}
              disabled={loading}
            >
              Clear
            </button>

            <button
              type="submit"
              className="primary-button"
              id="crop-submit"
              disabled={loading || !currentFarm}
            >
              {loading ? (
                <>
                  <span className="inline-spinner" />
                  Submitting…
                </>
              ) : (
                "Generate Recommendation"
              )}
            </button>
          </div>
        </form>

        <article className="panel prediction-result">
          {loading ? (
            <div className="service-unavailable">
              <div className="auth-loading-spinner" />
              <h2>Submitting farm conditions…</h2>
              <p>Your request is being processed.</p>
            </div>
          ) : !result ? (
            <div className="empty-result">
              <span>🌾</span>
              <h2>Your result will appear here</h2>
              <p>
                Complete the form and select Generate
                Recommendation.
              </p>
            </div>
          ) : result.recommendedCrop ? (
            /* ML result available */
            <div>
              <p className="result-label">RECOMMENDED CROP</p>

              <div className="crop-result-icon">🌾</div>

              <h2>{result.recommendedCrop}</h2>

              {result.confidence != null && (
                <div className="confidence">
                  <div>
                    <span>AI confidence</span>
                    <strong>{Number(result.confidence).toFixed(1)}%</strong>
                  </div>
                  <div className="confidence-track">
                    <div
                      className="confidence-fill"
                      style={{ width: `${result.confidence}%` }}
                    />
                  </div>
                </div>
              )}

              <div className="result-information">
                {result.season && (
                  <div>
                    <span>Suitable season</span>
                    <strong>{result.season}</strong>
                  </div>
                )}
                {result.expectedYield && (
                  <div>
                    <span>Expected yield</span>
                    <strong>{result.expectedYield}</strong>
                  </div>
                )}
              </div>

              {result.explanation && (
                <div className="result-explanation">
                  <h3>Why this crop?</h3>
                  <p>{result.explanation}</p>
                </div>
              )}
            </div>
          ) : (
            /* ML not yet implemented — show pending state */
            <div className="service-unavailable">
              <span className="service-icon">🌱</span>
              <h2>Request submitted</h2>
              <span className="pending-badge">⏳ Analysis Pending</span>
              <p>
                Your soil data has been saved. The AI crop
                recommendation will appear here once the
                analysis service is available.
              </p>
              <p style={{ fontSize: "12px", marginTop: "4px" }}>
                Request ID: #{result.id}
              </p>
            </div>
          )}
        </article>
      </section>
    </div>
  );
}

export default CropRecommendation;