import { useState } from "react";
import { useAuth } from "../hooks/useAuth.js";
import { submitRecommendation } from "../api/cropApi.js";
import { validateCropForm } from "../utils/validators.js";
import "../styles/cropRecommendationV2.css";

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

const FEATURE_LABELS = {
  N: "Nitrogen",
  P: "Phosphorus",
  K: "Potassium",
  temperature: "Temperature",
  humidity: "Humidity",
  ph: "Soil pH",
  rainfall: "Rainfall",
};

function formatStatus(value) {
  if (!value) return "";

  return String(value)
    .toLowerCase()
    .split("_")
    .map(
      (word) =>
        word.charAt(0).toUpperCase() + word.slice(1),
    )
    .join(" ");
}

function formatScore(value) {
  const number = Number(value);

  if (!Number.isFinite(number)) {
    return "—";
  }

  return `${number.toFixed(1)}%`;
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
    value: "Not available",
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
    value: "Not available",
    status:
      formatStatus(result.yieldStatus) ||
      "Unavailable",
  };
}

function CandidateList({
  candidates = [],
  approvedCrop = null,
  rankOffset = 0,
}) {
  if (!Array.isArray(candidates) || candidates.length === 0) {
    return null;
  }

  const hasApprovedCrop = Boolean(approvedCrop);

  return (
    <div className="crop-v2-candidate-section">
      <div className="crop-v2-section-heading">
        <div>
          <span className="crop-v2-eyebrow">
            MODEL RANKING
          </span>

          <h3>
            {hasApprovedCrop
              ? "Other model candidates"
              : "Possible candidates"}
          </h3>
        </div>

        <span className="crop-v2-reference-badge">
          Reference only
        </span>
      </div>

      <p className="crop-v2-section-description">
        {hasApprovedCrop
          ? `${approvedCrop} is the approved recommendation. These lower-ranked candidates are shown only for comparison.`
          : "These are the model's highest scores. They are not approved crop recommendations for this input."}
      </p>

      <div className="crop-v2-candidate-list">
        {candidates.map((candidate, index) => (
          <div
            className="crop-v2-candidate-row"
            key={`${candidate.crop}-${index}`}
          >
            <div className="crop-v2-candidate-rank">
              {index + 1 + rankOffset}
            </div>

            <div className="crop-v2-candidate-main">
              <div className="crop-v2-candidate-name">
                {candidate.crop || "Unknown"}
              </div>

              {!hasApprovedCrop && index === 0 && (
                <span className="crop-v2-top-candidate-note">
                  Highest model score
                </span>
              )}
            </div>

            <strong className="crop-v2-candidate-score">
              {formatScore(candidate.confidence)}
            </strong>
          </div>
        ))}
      </div>
    </div>
  );
}

function ReliableRecommendation({ result, onReset }) {
  const seasonDisplay = getSeasonDisplay(result);
  const yieldDisplay = getYieldDisplay(result);

  return (
    <div className="crop-v2-result-content">
      <div className="crop-v2-result-header">
        <p className="result-label">
          RECOMMENDED CROP
        </p>

        <span className="crop-v2-reliability-badge crop-v2-reliability-good">
          Reliable recommendation
        </span>
      </div>

      <div className="crop-result-icon">
        🌾
      </div>

      <h2 className="recommended-crop-name">
        {result.recommendedCrop}
      </h2>

      {result.recommendationMessage && (
        <p className="crop-v2-primary-message">
          {result.recommendationMessage}
        </p>
      )}

      {result.confidence != null && (
        <div className="confidence crop-v2-confidence">
          <div>
            <span>Model score</span>

            <strong>
              {formatScore(result.confidence)}
            </strong>
          </div>

          <div className="confidence-track">
            <div
              className="confidence-fill"
              style={{
                width: `${Math.min(
                  Math.max(
                    Number(result.confidence) || 0,
                    0,
                  ),
                  100,
                )}%`,
              }}
            />
          </div>
        </div>
      )}

      {result.reliabilityMessage && (
        <div className="crop-v2-reliability-note crop-v2-note-good">
          <strong>Reliability check passed</strong>
          <p>{result.reliabilityMessage}</p>
        </div>
      )}

      <div className="result-information crop-v2-info-grid">
        <div>
          <span>Suitable season</span>
          <strong>{seasonDisplay.value}</strong>
          <small>{seasonDisplay.status}</small>
        </div>

        <div>
          <span>Expected yield reference</span>
          <strong>{yieldDisplay.value}</strong>
          <small>{yieldDisplay.status}</small>
        </div>

        <div>
          <span>Model domain</span>
          <strong>
            {formatStatus(result.domainStatus) ||
              "Not reported"}
          </strong>

          {result.domainScore != null && (
            <small>
              Domain score: {Number(result.domainScore).toFixed(3)}
            </small>
          )}
        </div>

        <div>
          <span>Model version</span>
          <strong>
            {result.modelVersion || "Not reported"}
          </strong>

          {result.confidenceMargin != null && (
            <small>
              Confidence margin:{" "}
              {formatScore(result.confidenceMargin)}
            </small>
          )}
        </div>
      </div>

      {result.seasonMessage && (
        <div className="result-note">
          <strong>Season information:</strong>{" "}
          {result.seasonMessage}
        </div>
      )}

      {result.yieldMessage && (
        <div className="result-note">
          <strong>Yield information:</strong>{" "}
          {result.yieldMessage}
        </div>
      )}

      {result.explanation && (
        <div className="result-explanation">
          <h3>Why this crop?</h3>
          <p>{result.explanation}</p>
        </div>
      )}

      <CandidateList
        candidates={
          Array.isArray(result.candidates)
            ? result.candidates.slice(1)
            : Array.isArray(result.alternatives)
              ? result.alternatives
              : []
        }
        approvedCrop={result.recommendedCrop}
        rankOffset={1}
      />

      {result.expectedYield && (
        <div className="result-note">
          <strong>Important:</strong>{" "}
          The yield value shown is a reference range,
          not a guaranteed harvest. Actual yield can vary
          with crop variety, irrigation, fertilizer, soil
          condition, pests, diseases and farm management.
        </div>
      )}

      <div className="result-actions">
        <button
          type="button"
          className="outline-button"
          onClick={onReset}
        >
          Try Another Recommendation
        </button>
      </div>
    </div>
  );
}

function UnreliableRecommendation({ result, onReset }) {
  const candidates =
    Array.isArray(result.candidates) &&
    result.candidates.length > 0
      ? result.candidates
      : result.topCandidate
        ? [
            result.topCandidate,
            ...(Array.isArray(result.alternatives)
              ? result.alternatives
              : []),
          ]
        : [];

  const outsideFeatures =
    Array.isArray(result.domainOutsideFeatures)
      ? result.domainOutsideFeatures
      : [];

  const isOutOfDistribution =
    result.reliabilityStatus === "OUT_OF_DISTRIBUTION" ||
    result.domainStatus === "OUT_OF_DISTRIBUTION";

  return (
    <div className="crop-v2-result-content crop-v2-unreliable">
      <div className="crop-v2-result-header">
        <p className="result-label">
          AI RELIABILITY CHECK
        </p>

        <span className="crop-v2-reliability-badge crop-v2-reliability-warning">
          {isOutOfDistribution
            ? "Outside reliable model range"
            : "Recommendation withheld"}
        </span>
      </div>

      <div
        className="crop-v2-warning-icon"
        aria-hidden="true"
      >
        !
      </div>

      <h2 className="crop-v2-unavailable-title">
        No reliable recommendation
      </h2>

      <p className="crop-v2-unavailable-message">
        {result.recommendationMessage ||
          "GrowX could not issue a sufficiently reliable crop recommendation for these conditions."}
      </p>

      {result.reliabilityMessage && (
        <div className="crop-v2-reliability-note crop-v2-note-warning">
          <strong>
            {isOutOfDistribution
              ? "Why GrowX withheld the recommendation"
              : "Reliability check"}
          </strong>

          <p>{result.reliabilityMessage}</p>
        </div>
      )}

      {outsideFeatures.length > 0 && (
        <div className="crop-v2-domain-section">
          <span className="crop-v2-domain-label">
            Values outside the model&apos;s observed training range
          </span>

          <div className="crop-v2-feature-tags">
            {outsideFeatures.map((feature) => (
              <span key={feature}>
                {FEATURE_LABELS[feature] || feature}
              </span>
            ))}
          </div>
        </div>
      )}

      <CandidateList candidates={candidates} />

      {result.explanation && (
        <div className="crop-v2-explanation-warning">
          <strong>Model interpretation</strong>
          <p>{result.explanation}</p>
        </div>
      )}

      <div className="crop-v2-unreliable-meta">
        <div>
          <span>Reliability status</span>
          <strong>
            {formatStatus(result.reliabilityStatus) ||
              "Not reliable"}
          </strong>
        </div>

        <div>
          <span>Model domain</span>
          <strong>
            {formatStatus(result.domainStatus) ||
              "Not reported"}
          </strong>
        </div>

        {result.domainScore != null && (
          <div>
            <span>Domain score</span>
            <strong>
              {Number(result.domainScore).toFixed(3)}
            </strong>
          </div>
        )}

        {result.modelVersion && (
          <div>
            <span>Model version</span>
            <strong>{result.modelVersion}</strong>
          </div>
        )}
      </div>

      <div className="crop-v2-skipped-note">
        <strong>
          Season and yield were intentionally not estimated.
        </strong>

        <p>
          GrowX only provides those details after a crop
          recommendation passes the reliability gate.
        </p>
      </div>

      {result.id && (
        <p className="crop-v2-request-id">
          Request ID: #{result.id}
        </p>
      )}

      <div className="result-actions">
        <button
          type="button"
          className="outline-button"
          onClick={onReset}
        >
          Review Inputs
        </button>
      </div>
    </div>
  );
}

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
      <span className="field-error">
        {fieldErrors[name]}
      </span>
    ) : null;
  }

  const hasReliableRecommendation =
    result != null &&
    result.recommendationAvailable === true &&
    Boolean(result.recommendedCrop);

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
          ⚠️ No farm linked to your account. Please update your
          Farm Profile before using this tool.
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
            <div className="farm-context-card">
              <div>
                <span>Selected farm</span>

                <strong>
                  {currentFarm.farmName}
                </strong>
              </div>

              {currentFarm.location && (
                <div>
                  <span>Location</span>

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
                min="0"
                max="14"
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
                <option value="">Select month</option>
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
          ) : hasReliableRecommendation ? (
            <ReliableRecommendation
              result={result}
              onReset={handleReset}
            />
          ) : (
            <UnreliableRecommendation
              result={result}
              onReset={handleReset}
            />
          )}
        </article>
      </section>
    </div>
  );
}

export default CropRecommendation;
