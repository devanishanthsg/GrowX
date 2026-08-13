const reports = [
  {
    name: "Crop Recommendation Report",
    date: "July 31, 2026",
    type: "AI Prediction",
    status: "Completed",
  },
  {
    name: "Plant Disease Analysis",
    date: "July 29, 2026",
    type: "Disease Detection",
    status: "Completed",
  },
  {
    name: "Monthly Farm Summary",
    date: "July 1, 2026",
    type: "Farm Report",
    status: "Completed",
  },
];

function Reports() {
  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">
            FARM RECORDS
          </p>

          <h1>Reports</h1>

          <p>
            View and manage generated farm reports and AI
            predictions.
          </p>
        </div>

        <button className="primary-button">
          Generate Report
        </button>
      </header>

      <section className="stat-grid report-stats">
        <StatBox
          title="Total Reports"
          value="12"
          icon="📄"
        />

        <StatBox
          title="AI Predictions"
          value="7"
          icon="🤖"
        />

        <StatBox
          title="Disease Analyses"
          value="5"
          icon="🍃"
        />
      </section>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Recent reports</h2>
            <p>
              Reports generated through the GrowX
              platform
            </p>
          </div>

          <input
            className="table-search"
            placeholder="Search reports"
          />
        </div>

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
              {reports.map((report) => (
                <tr key={report.name}>
                  <td>
                    <strong>{report.name}</strong>
                  </td>

                  <td>{report.type}</td>
                  <td>{report.date}</td>

                  <td>
                    <span className="status-completed">
                      {report.status}
                    </span>
                  </td>

                  <td>
                    <button
                      type="button"
                      className="table-button"
                    >
                      Download
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function StatBox({ title, value, icon }) {
  return (
    <article className="stat-card">
      <div className="stat-icon stat-success">
        {icon}
      </div>

      <div>
        <p className="stat-title">{title}</p>
        <h3 className="stat-value">{value}</h3>
      </div>
    </article>
  );
}

export default Reports;