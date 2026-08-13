import { useState } from "react";

function DiseaseDetection() {
  const [image, setImage] = useState(null);
  const [result, setResult] = useState(null);

  function handleImageChange(event) {
    const selectedFile = event.target.files[0];

    if (!selectedFile) {
      return;
    }

    const imageUrl =
      URL.createObjectURL(selectedFile);

    setImage(imageUrl);
    setResult(null);
  }

  function analyseImage() {
    if (!image) {
      return;
    }

    setResult({
      disease: "Leaf Blight",
      confidence: 94,
      severity: "Moderate",
      treatment:
        "Remove heavily affected leaves and use an appropriate fungicide after consulting an agricultural expert.",
      prevention:
        "Avoid excessive moisture on leaves and maintain proper spacing between plants.",
    });
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">
            COMPUTER VISION
          </p>

          <h1>Plant Disease Detection</h1>

          <p>
            Upload a clear image of a plant leaf to
            identify possible diseases.
          </p>
        </div>
      </header>

      <section className="form-result-layout">
        <article className="panel image-upload-panel">
          <div className="panel-heading">
            <div>
              <h2>Upload leaf image</h2>
              <p>
                Use a well-lit image with the affected
                leaf clearly visible.
              </p>
            </div>
          </div>

          <label className="image-drop-zone">
            {image ? (
              <img
                src={image}
                alt="Uploaded plant leaf"
              />
            ) : (
              <>
                <span>📷</span>
                <h3>Select a plant image</h3>
                <p>PNG, JPG or JPEG</p>
              </>
            )}

            <input
              type="file"
              accept="image/png,image/jpeg"
              onChange={handleImageChange}
            />
          </label>

          <button
            type="button"
            className="full-primary-button"
            onClick={analyseImage}
            disabled={!image}
          >
            Analyse Plant Image
          </button>
        </article>

        <article className="panel disease-result">
          {!result ? (
            <div className="empty-result">
              <span>🍃</span>
              <h2>Disease analysis</h2>
              <p>
                Upload an image and start the analysis.
              </p>
            </div>
          ) : (
            <div>
              <div className="disease-status">
                <span>⚠️</span>

                <div>
                  <p>DISEASE DETECTED</p>
                  <h2>{result.disease}</h2>
                </div>
              </div>

              <div className="result-information">
                <div>
                  <span>Confidence</span>
                  <strong>
                    {result.confidence}%
                  </strong>
                </div>

                <div>
                  <span>Severity</span>
                  <strong>{result.severity}</strong>
                </div>
              </div>

              <div className="advice-box">
                <h3>Suggested treatment</h3>
                <p>{result.treatment}</p>
              </div>

              <div className="advice-box">
                <h3>Prevention guidance</h3>
                <p>{result.prevention}</p>
              </div>
            </div>
          )}
        </article>
      </section>
    </div>
  );
}

export default DiseaseDetection;