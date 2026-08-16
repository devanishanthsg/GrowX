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
  sowingMonth: "",
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

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }

    setApiError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    // Clear previous recommendation immediately
    // so stale results are never shown for new invalid inputs.
    setResult(null);
    setApiError("");

    if (!currentFarm) {
      setApiError(
        "No farm found for your account. Please complete your farm profile first.",
      );
      return;
    }

    const errors = validateCropForm(formData);

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setLoading(true);

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
        sowingMonth: Number(formData.sowingMonth),
      });

      setResult(data);
    } catch (error) {
      if (error.data && typeof error.data === "object") {
        setFieldErrors(error.data);
      } else {
        setApiError(
          error.message ??
            "Unable to generate crop recommendation. Please try again.",
        );
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

  function FieldError({ name }) {
    return fieldErrors[name] ? (
      <span className="field-error">{fieldErrors[name]}</span>
    ) : null;
  }

  function getConfidenceInfo(confidence) {
    const value = Number(confidence);

    if (!Number.isFinite(value)) {
      return {
        label: "Unknown confidence",
        className: "confidence-unknown",
        message:
          "The AI service did not return a valid confidence score.",
      };
    }

    if (value >= 70) {
      return {
        label: "High confidence",
        className: "confidence-high",
        message:
          "The model has strong confidence in this crop recommendation.",
      };
    }

    if (value >= 50) {
      return {
        label: "Moderate confidence",
        className: "confidence-medium",
        message:
          "The recommendation is reasonably supported, but other crops may also be suitable.",
      };
    }

    return {
      label: "Low confidence",
      className: "confidence-low",
      message:
        "The model is uncertain about this recommendation. Review the input values before making a planting decision.",
    };
  }

  function formatStatus(value) {
    if (!value) return "";

    return value
      .toLowerCase()
      .split("_")
      .map(
        (word) =>
          word.charAt(0).toUpperCase() +
          word.slice(1),
      )
      .join(" ");
  }

  function getSeasonDisplay(result) {
    if (result.season) {
      return {
        value: result.season,
        status:
          result.seasonStatus === "VERIFIED"
            ? "Verified"
            : formatStatus(result.seasonStatus),
      };
    }

    return {
      value: "Not verified",
      status:
        formatStatus(result.seasonStatus) ||
        "Unavailable",
    };
  }

  function getYieldDisplay(result) {
    if (result.expectedYield) {
      return {
        value: result.expectedYield,
        status:
          result.yieldStatus === "REFERENCE_AVAILABLE"
            ? "Reference available"
            : formatStatus(result.yieldStatus),
      };
    }

    return {
      value: "Unavailable",
      status:
        formatStatus(result.yieldStatus) ||
        "Unavailable",
    };
  }

  const confidenceInfo =
    result?.confidence != null
      ? getConfidenceInfo(result.confidence)
      : null;

  const seasonDisplay =
    result?.recommendedCrop
      ? getSeasonDisplay(result)
      : null;

  const yieldDisplay =
    result?.recommendedCrop
      ? getYieldDisplay(result)
      : null;

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">AI TOOL</p>

          <h1>Crop Recommendation</h1>

          <p>
            Enter soil and weather information to receive an
            AI-powered crop recommendation for your farm.
          </p>
        </div>
      </header>

      {!currentFarm && (
        <p className="page-error">
          ⚠️ No farm linked to your account. Please update your Farm
          Profile before using this tool.
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
                Use values from your latest soil test and farm
                observations for better recommendations.
              </p>
            </div>
          </div>

          {apiError && (
            <p className="form-error">{apiError}</p>
          )}

          {currentFarm && (
            <div
              className="farm-context-card"
              style={{
                display: "grid",
                gridTemplateColumns:
                  "repeat(2, minmax(0, 1fr))",
                gap: "14px",
                padding: "14px 16px",
                marginBottom: "18px",
              }}
            >
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "4px",
                }}
              >
                <span
                  style={{
                    fontSize: "12px",
                    opacity: 0.7,
                  }}
                >
                  Selected farm
                </span>

                <strong>
                  {currentFarm.farmName}
                </strong>
              </div>

              {currentFarm.location && (
                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "4px",
                  }}
                >
                  <span
                    style={{
                      fontSize: "12px",
                      opacity: 0.7,
                    }}
                  >
                    Location
                  </span>

                  <strong>
                    {currentFarm.location}
                  </strong>
                </div>
              )}
            </div>
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
                step="0.01"
                name="temperature"
                id="crop-temperature"
                value={formData.temperature}
                onChange={handleChange}
                placeholder="Example: 20.88"
                disabled={loading}
              />

              <FieldError name="temperature" />
            </label>

            <label>
              Humidity (%)

              <input
                type="number"
                step="0.01"
                name="humidity"
                id="crop-humidity"
                value={formData.humidity}
                onChange={handleChange}
                placeholder="Example: 82"
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
                step="0.01"
                name="ph"
                id="crop-ph"
                value={formData.ph}
                onChange={handleChange}
                placeholder="Example: 6.5"
                disabled={loading}
                min="2.5"
                max="11"
              />

              <FieldError name="ph" />
            </label>

            <label>
              Rainfall (mm)

              <input
                type="number"
                step="0.01"
                name="rainfall"
                id="crop-rainfall"
                value={formData.rainfall}
                onChange={handleChange}
                placeholder="Example: 202.9"
                disabled={loading}
                min="0"
                max="500"
              />

              <FieldError name="rainfall" />
            </label>

            <label>
              Intended sowing month

              <select
                name="sowingMonth"
                id="crop-sowing-month"
                value={formData.sowingMonth}
                onChange={handleChange}
                disabled={loading}
              >
                <option value="">
                  Select month
                </option>

                <option value="1">January</option>
                <option value="2">February</option>
                <option value="3">March</option>
                <option value="4">April</option>
                <option value="5">May</option>
                <option value="6">June</option>
                <option value="7">July</option>
                <option value="8">August</option>
                <option value="9">September</option>
                <option value="10">October</option>
                <option value="11">November</option>
                <option value="12">December</option>
              </select>

              <FieldError name="sowingMonth" />
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
                  Analyzing…
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

              <h2>Analyzing farm conditions…</h2>

              <p>
                GrowX AI is evaluating your soil nutrients,
                weather conditions, location and sowing month.
              </p>
            </div>
          ) : !result ? (
            <div className="empty-result">
              <span>🌾</span>

              <h2>
                Your recommendation will appear here
              </h2>

              <p>
                Complete the form and select Generate
                Recommendation.
              </p>
            </div>
          ) : result.recommendedCrop ? (
            <div>
              <p className="result-label">
                RECOMMENDED CROP
              </p>

              <div className="crop-result-icon">
                🌾
              </div>

              <h2 className="recommended-crop-name">
                {result.recommendedCrop}
              </h2>

              {result.confidence != null && (
                <>
                  <div className="confidence">
                    <div>
                      <span>AI confidence</span>

                      <strong>
                        {Number(
                          result.confidence,
                        ).toFixed(1)}
                        %
                      </strong>
                    </div>

                    <div className="confidence-track">
                      <div
                        className="confidence-fill"
                        style={{
                          width: `${Math.min(
                            Math.max(
                              Number(result.confidence),
                              0,
                            ),
                            100,
                          )}%`,
                        }}
                      />
                    </div>
                  </div>

                  {confidenceInfo && (
                    <div
                      className={`confidence-status ${confidenceInfo.className}`}
                      style={{
                        marginTop: "12px",
                        marginBottom: "18px",
                      }}
                    >
                      <strong>
                        {confidenceInfo.label}
                      </strong>

                      <p>
                        {confidenceInfo.message}
                      </p>
                    </div>
                  )}
                </>
              )}

              <div
                className="result-information"
                style={{
                  display: "grid",
                  gridTemplateColumns:
                    "repeat(2, minmax(0, 1fr))",
                  gap: "12px",
                }}
              >
                <div>
                  <span>
                    Suitable season
                  </span>

                  <strong>
                    {seasonDisplay?.value}
                  </strong>

                  <small>
                    {seasonDisplay?.status}
                  </small>
                </div>

                <div>
                  <span>
                    Expected yield
                  </span>

                  <strong>
                    {yieldDisplay?.value}
                  </strong>

                  <small>
                    {yieldDisplay?.status}
                  </small>
                </div>

                {result.status && (
                  <div>
                    <span>
                      Analysis status
                    </span>

                    <strong>
                      {formatStatus(result.status)}
                    </strong>
                  </div>
                )}

                {result.modelVersion && (
                  <div>
                    <span>
                      Model version
                    </span>

                    <strong>
                      {result.modelVersion}
                    </strong>
                  </div>
                )}
              </div>

              {(result.seasonMessage ||
                result.yieldMessage) && (
                <div
                  style={{
                    display: "grid",
                    gap: "10px",
                    marginTop: "16px",
                  }}
                >
                  {result.seasonMessage && (
                    <div
                      className="result-note"
                      style={{
                        padding: "12px 14px",
                        borderRadius: "10px",
                      }}
                    >
                      <strong>
                        🌱 Season information
                      </strong>

                      <p
                        style={{
                          margin: "6px 0 0",
                        }}
                      >
                        {result.seasonMessage}
                      </p>
                    </div>
                  )}

                  {result.yieldMessage && (
                    <div
                      className="result-note"
                      style={{
                        padding: "12px 14px",
                        borderRadius: "10px",
                      }}
                    >
                      <strong>
                        📈 Yield information
                      </strong>

                      <p
                        style={{
                          margin: "6px 0 0",
                        }}
                      >
                        {result.yieldMessage}
                      </p>
                    </div>
                  )}
                </div>
              )}

              {result.explanation && (
                <div
                  className="result-explanation"
                  style={{
                    marginTop: "16px",
                  }}
                >
                  <h3>
                    Why this crop?
                  </h3>

                  <p>
                    {result.explanation}
                  </p>
                </div>
              )}

              {result.expectedYield && (
                <div
                  className="result-note"
                  style={{
                    marginTop: "12px",
                  }}
                >
                  <strong>
                    Important:
                  </strong>{" "}
                  This yield is a reference range, not a
                  guaranteed harvest. Actual yield can vary
                  with variety, irrigation, fertilizer,
                  soil condition, pests, diseases and farm
                  management.
                </div>
              )}

              {result.confidence != null &&
                Number(result.confidence) < 50 && (
                  <div
                    className="result-note"
                    style={{
                      marginTop: "12px",
                    }}
                  >
                    <strong>
                      ⚠ Recommendation caution:
                    </strong>{" "}
                    This prediction has low confidence. Do not
                    rely on this result alone for planting
                    decisions.
                  </div>
                )}

              <div
                className="result-actions"
                style={{
                  marginTop: "18px",
                }}
              >
                <button
                  type="button"
                  className="outline-button"
                  onClick={handleReset}
                >
                  Try Another Recommendation
                </button>
              </div>
            </div>
          ) : (
            <div className="service-unavailable">
              <span className="service-icon">
                🌱
              </span>

              <h2>
                Recommendation unavailable
              </h2>

              <p>
                GrowX could not generate a completed crop
                recommendation for these conditions.
              </p>

              {result.id && (
                <p
                  style={{
                    fontSize: "12px",
                    marginTop: "4px",
                  }}
                >
                  Request ID: #{result.id}
                </p>
              )}
            </div>
          )}
        </article>
      </section>
    </div>
  );
}

export default CropRecommendation;