import { useState } from "react";
import { useAuth } from "../hooks/useAuth.js";
import { analyzeImage } from "../api/diseaseApi.js";
import { isAcceptedImage, isFileSizeOk } from "../utils/validators.js";

function DiseaseDetection() {
  const { currentFarm } = useAuth();

  const [selectedFile, setSelectedFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [fileError, setFileError] = useState("");
  const [apiError, setApiError] = useState("");

  function handleImageChange(event) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    if (!isAcceptedImage(file)) {
      setFileError("Please select a JPG, JPEG, or PNG image.");
      return;
    }

    // Validate file size (max 10 MB)
    if (!isFileSizeOk(file, 10)) {
      setFileError("Image must be smaller than 10 MB.");
      return;
    }

    setFileError("");
    setSelectedFile(file);
    setImagePreview(URL.createObjectURL(file));
    setResult(null);
    setApiError("");
  }

  async function handleAnalyse() {
    if (!selectedFile) {
      setFileError("Please select an image first.");
      return;
    }

    if (!currentFarm) {
      setApiError(
        "No farm linked to your account. Please update your Farm Profile first.",
      );
      return;
    }

    setLoading(true);
    setApiError("");
    setResult(null);

    try {
      // Multipart field names confirmed from DiseaseDetectionController:
      // farmId → text, image → file
      const data = await analyzeImage(currentFarm.id, selectedFile);
      setResult(data);
    } catch (error) {
      setApiError(
        error.message ?? "Upload failed. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  }

  function handleReset() {
    setSelectedFile(null);
    setImagePreview(null);
    setResult(null);
    setFileError("");
    setApiError("");
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">COMPUTER VISION</p>
          <h1>Plant Disease Detection</h1>
          <p>
            Upload a clear image of a plant leaf to identify
            possible diseases.
          </p>
        </div>
      </header>

      {!currentFarm && (
        <p className="page-error">
          ⚠️ No farm linked to your account. Please update your Farm Profile
          before using this tool.
        </p>
      )}

      <section className="form-result-layout">
        <article className="panel image-upload-panel">
          <div className="panel-heading">
            <div>
              <h2>Upload leaf image</h2>
              <p>
                Use a well-lit image with the affected leaf clearly
                visible.
              </p>
            </div>
          </div>

          <label className="image-drop-zone">
            {imagePreview ? (
              <img
                src={imagePreview}
                alt="Uploaded plant leaf"
              />
            ) : (
              <>
                <span>📷</span>
                <h3>Select a plant image</h3>
                <p>PNG, JPG or JPEG · Max 10 MB</p>
              </>
            )}

            <input
              type="file"
              accept="image/png,image/jpeg,image/jpg"
              onChange={handleImageChange}
              disabled={loading}
              id="disease-image-input"
            />
          </label>

          {fileError && (
            <p className="form-error" style={{ marginTop: "10px" }}>
              {fileError}
            </p>
          )}

          {apiError && (
            <p className="form-error" style={{ marginTop: "10px" }}>
              {apiError}
            </p>
          )}

          <div
            style={{
              display: "flex",
              gap: "10px",
              marginTop: "16px",
            }}
          >
            {imagePreview && (
              <button
                type="button"
                className="outline-button"
                onClick={handleReset}
                disabled={loading}
                style={{ flex: 1 }}
              >
                Clear
              </button>
            )}

            <button
              type="button"
              className="full-primary-button"
              id="disease-analyse-btn"
              onClick={handleAnalyse}
              disabled={!selectedFile || loading || !currentFarm}
              style={{ flex: 2 }}
            >
              {loading ? (
                <>
                  <span className="inline-spinner" />
                  Uploading image…
                </>
              ) : (
                "Analyse Plant Image"
              )}
            </button>
          </div>
        </article>

        <article className="panel disease-result">
          {loading ? (
            <div className="service-unavailable">
              <div className="auth-loading-spinner" />
              <h2>Uploading image…</h2>
              <p>Analysis initiated. Please wait.</p>
            </div>
          ) : !result ? (
            <div className="empty-result">
              <span>🍃</span>
              <h2>Disease analysis</h2>
              <p>Upload an image and start the analysis.</p>
            </div>
          ) : result.disease ? (
            /* ML result available */
            <div>
              <div className="disease-status">
                <span>⚠️</span>
                <div>
                  <p>DISEASE DETECTED</p>
                  <h2>{result.disease}</h2>
                </div>
              </div>

              <div className="result-information">
                {result.confidence != null && (
                  <div>
                    <span>Confidence</span>
                    <strong>
                      {Number(result.confidence).toFixed(1)}%
                    </strong>
                  </div>
                )}
                {result.severityLabel && (
                  <div>
                    <span>Severity</span>
                    <strong>{result.severityLabel}</strong>
                  </div>
                )}
              </div>

              {result.treatment && (
                <div className="advice-box">
                  <h3>Suggested treatment</h3>
                  <p>{result.treatment}</p>
                </div>
              )}

              {result.prevention && (
                <div className="advice-box">
                  <h3>Prevention guidance</h3>
                  <p>{result.prevention}</p>
                </div>
              )}
            </div>
          ) : (
            /* ML not yet implemented — pending state */
            <div className="service-unavailable">
              <span className="service-icon">🍃</span>
              <h2>Image uploaded successfully</h2>
              <span className="pending-badge">⏳ Analysis Pending</span>
              <p>
                Your image has been saved. The disease analysis
                will appear here once the AI service is available.
              </p>
              <p style={{ fontSize: "12px", marginTop: "4px" }}>
                Detection ID: #{result.id}
              </p>
            </div>
          )}
        </article>
      </section>
    </div>
  );
}

export default DiseaseDetection;