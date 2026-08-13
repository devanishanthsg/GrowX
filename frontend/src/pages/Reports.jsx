import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth.js";
import {
  getReports,
  getReportStats,
  generateReport,
} from "../api/reportApi.js";
import { formatDate, formatReportType } from "../utils/formatters.js";

const REPORT_TYPES = [
  { value: "CROP_RECOMMENDATION", label: "Crop Recommendation" },
  { value: "DISEASE_DETECTION", label: "Disease Detection" },
  { value: "FARM_SUMMARY", label: "Farm Summary" },
  { value: "WEATHER", label: "Weather" },
];

function Reports() {
  const { currentFarm } = useAuth();

  const [reports, setReports] = useState([]);
  const [stats, setStats] = useState(null);
  const [loadingReports, setLoadingReports] = useState(true);
  const [error, setError] = useState("");

  // Generate modal state
  const [showModal, setShowModal] = useState(false);
  const [selectedType, setSelectedType] = useState("CROP_RECOMMENDATION");
  const [generating, setGenerating] = useState(false);
  const [generateError, setGenerateError] = useState("");

  // Search filter
  const [search, setSearch] = useState("");

  async function loadReports(farmId) {
    setLoadingReports(true);
    setError("");
    try {
      const [reportList, statsData] = await Promise.all([
        getReports(farmId),
        getReportStats(farmId),
      ]);
      setReports(Array.isArray(reportList) ? reportList : []);
      setStats(statsData);
    } catch (err) {
      setError(err.message ?? "Failed to load reports.");
    } finally {
      setLoadingReports(false);
    }
  }

  useEffect(() => {
    if (currentFarm) {
      loadReports(currentFarm.id);
    } else {
      setLoadingReports(false);
    }
  }, [currentFarm]);

  async function handleGenerate() {
    if (!currentFarm) return;
    setGenerating(true);
    setGenerateError("");

    try {
      const newReport = await generateReport(currentFarm.id, selectedType);
      // Prepend to list and refresh stats
      setReports((prev) => [newReport, ...prev]);
      setShowModal(false);
      // Refresh stats after generating
      const statsData = await getReportStats(currentFarm.id);
      setStats(statsData);
    } catch (err) {
      setGenerateError(err.message ?? "Failed to generate report.");
    } finally {
      setGenerating(false);
    }
  }

  // Filter reports by search term
  const filteredReports = reports.filter(
    (r) =>
      !search ||
      (r.title ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.typeLabel ?? "").toLowerCase().includes(search.toLowerCase()),
  );

  const totalReports = stats?.total ?? reports.length;
  const cropReports = stats?.CROP_RECOMMENDATION ?? 0;
  const diseaseReports = stats?.DISEASE_DETECTION ?? 0;

  if (!currentFarm && !loadingReports) {
    return (
      <div>
        <header className="page-header">
          <div>
            <p className="page-label">FARM RECORDS</p>
            <h1>Reports</h1>
          </div>
        </header>
        <p className="page-error">
          ⚠️ No farm linked to your account. Please update your Farm Profile first.
        </p>
      </div>
    );
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">FARM RECORDS</p>
          <h1>Reports</h1>
          <p>
            View and manage generated farm reports and AI predictions.
          </p>
        </div>

        <button
          className="primary-button"
          id="reports-generate-btn"
          onClick={() => {
            setGenerateError("");
            setShowModal(true);
          }}
          disabled={!currentFarm || loadingReports}
        >
          Generate Report
        </button>
      </header>

      {/* Stat boxes — from /api/reports/farm/{farmId}/stats */}
      <section className="stat-grid report-stats">
        <StatBox
          title="Total Reports"
          value={loadingReports ? "…" : String(totalReports)}
          icon="📄"
        />
        <StatBox
          title="Crop Recommendations"
          value={loadingReports ? "…" : String(cropReports)}
          icon="🌾"
        />
        <StatBox
          title="Disease Analyses"
          value={loadingReports ? "…" : String(diseaseReports)}
          icon="🍃"
        />
      </section>

      {error && <p className="page-error">{error}</p>}

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Recent reports</h2>
            <p>Reports generated through the GrowX platform</p>
          </div>

          <input
            className="table-search"
            placeholder="Search reports"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {loadingReports ? (
          <div className="auth-loading" style={{ minHeight: "180px" }}>
            <div className="auth-loading-spinner" />
            <p>Loading reports…</p>
          </div>
        ) : filteredReports.length === 0 ? (
          <div className="empty-state">
            <span style={{ fontSize: "36px" }}>📄</span>
            <p>
              {reports.length === 0
                ? "No reports generated yet. Click Generate Report to create your first one."
                : "No reports match your search."}
            </p>
          </div>
        ) : (
          <div className="table-wrapper">
            <table className="reports-table">
              <thead>
                <tr>
                  <th>Report</th>
                  <th>Type</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {filteredReports.map((report) => (
                  <tr key={report.id}>
                    <td>
                      <strong>{report.title ?? "—"}</strong>
                    </td>

                    <td>
                      {report.typeLabel ??
                        formatReportType(report.type)}
                    </td>

                    <td>{formatDate(report.createdAt)}</td>

                    <td>
                      <span className="status-completed">
                        {report.status ?? "—"}
                      </span>
                    </td>

                    <td>
                      {/* Download is disabled until backend implements PDF export */}
                      <button
                        type="button"
                        className="table-button"
                        disabled={!report.downloadable}
                        title={
                          report.downloadable
                            ? "Download report"
                            : "Download not yet available"
                        }
                      >
                        {report.downloadable ? "Download" : "Unavailable"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Generate Report Modal */}
      {showModal && (
        <div
          className="modal-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="generate-modal-title"
        >
          <div className="modal-card">
            <h2 id="generate-modal-title">Generate Report</h2>
            <p>Choose the type of report to generate for your farm.</p>

            <select
              className="modal-select"
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              disabled={generating}
              id="report-type-select"
            >
              {REPORT_TYPES.map((rt) => (
                <option key={rt.value} value={rt.value}>
                  {rt.label}
                </option>
              ))}
            </select>

            {generateError && (
              <p className="form-error" style={{ marginBottom: "12px" }}>
                {generateError}
              </p>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="outline-button"
                onClick={() => setShowModal(false)}
                disabled={generating}
              >
                Cancel
              </button>

              <button
                type="button"
                className="primary-button"
                id="confirm-generate-btn"
                onClick={handleGenerate}
                disabled={generating}
              >
                {generating ? (
                  <>
                    <span className="inline-spinner" />
                    Generating…
                  </>
                ) : (
                  "Generate"
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatBox({ title, value, icon }) {
  return (
    <article className="stat-card">
      <div className="stat-icon stat-success">{icon}</div>
      <div>
        <p className="stat-title">{title}</p>
        <h3 className="stat-value">{value}</h3>
      </div>
    </article>
  );
}

export default Reports;