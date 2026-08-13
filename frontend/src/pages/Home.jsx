import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";

const features = [
  {
    icon: "🌾",
    title: "Crop Recommendation",
    description:
      "Get AI-powered crop recommendations using soil nutrients and weather information.",
    path: "/crop-recommendation",
    linkText: "Try Crop AI",
  },
  {
    icon: "🍃",
    title: "Disease Detection",
    description:
      "Upload plant leaf images and identify possible diseases using artificial intelligence.",
    path: "/disease-detection",
    linkText: "Detect Disease",
  },
  {
    icon: "💧",
    title: "Irrigation Guidance",
    description:
      "Receive smart watering suggestions based on soil and environmental conditions.",
    path: "/dashboard",
    linkText: "View Guidance",
  },
  {
    icon: "🌦️",
    title: "Weather Intelligence",
    description:
      "View weather forecasts and important alerts for better farm planning.",
    path: "/weather",
    linkText: "View Weather",
  },
  {
    icon: "🧪",
    title: "Soil Analysis",
    description:
      "Record and analyse nitrogen, phosphorus, potassium and soil pH information.",
    path: "/crop-recommendation",
    linkText: "Analyse Soil",
  },
  {
    icon: "📊",
    title: "Farm Reports",
    description:
      "View crop performance, predictions and farm activity in one dashboard.",
    path: "/reports",
    linkText: "View Reports",
  },
];

function Home() {
  return (
    <div>
      <Navbar />

      <section className="hero-section" id="home">
        <div className="hero-overlay" />

        <div className="hero-content">
          <p className="hero-label">
            AI-POWERED PRECISION FARMING
          </p>

          <h1>
            Grow smarter.
            <span> Harvest better.</span>
          </h1>

          <p className="hero-description">
            GrowX helps farmers make better decisions
            using intelligent crop recommendations,
            disease detection, weather analysis and
            farm insights.
          </p>

          <div className="hero-actions">
            <Link
              to="/register"
              className="primary-action"
            >
              Start Farming Smarter
            </Link>

            <Link
              to="/dashboard"
              className="secondary-action"
            >
              View Dashboard
            </Link>
          </div>

          <div className="hero-stats">
            <div>
              <strong>AI</strong>
              <span>Crop Intelligence</span>
            </div>

            <div>
              <strong>24/7</strong>
              <span>Farm Monitoring</span>
            </div>

            <div>
              <strong>Smart</strong>
              <span>Decision Support</span>
            </div>
          </div>
        </div>
      </section>

      <section
        className="features-section"
        id="features"
      >
        <div className="section-heading">
          <p>OUR FEATURES</p>

          <h2>
            Everything required for smarter farming
          </h2>

          <span>
            Manage farm information, analyse crops and
            make informed agricultural decisions from
            one platform.
          </span>
        </div>
          <div className="home-feature-grid">
            {features.map((feature) => (
              <article
                className="home-feature-card"
                key={feature.title}
              >
                <div className="feature-icon">
                  {feature.icon}
                </div>

                <h3>{feature.title}</h3>

                <p>{feature.description}</p>

                <Link
                  className="feature-link"
                  to={feature.path}
                >
                  {feature.linkText} →
                </Link>
              </article>
            ))}
          </div>
      </section>

      <section className="about-section" id="about">
        <div className="about-content">
          <p className="section-small-label">
            ABOUT GROWX
          </p>

          <h2>
            Technology designed for modern agriculture
          </h2>

          <p>
            GrowX combines artificial intelligence,
            weather information and farm records to
            provide useful recommendations for farmers.
          </p>

          <div className="about-points">
            <p>✓ Easy-to-use farmer dashboard</p>
            <p>✓ AI-based recommendations</p>
            <p>✓ Secure farm data management</p>
            <p>✓ Detailed reports and insights</p>
          </div>
        </div>

        <div className="about-visual">
          <div className="about-main-card">
            <span>🌱</span>
            <h3>GrowX Intelligence</h3>
            <p>
              Better information leads to better
              agricultural decisions.
            </p>
          </div>
        </div>
      </section>

      <footer className="footer">
        <div>
          <h3>🌱 GrowX</h3>
          <p>Smart Farming Powered by AI</p>
        </div>

        <p>© 2026 GrowX. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default Home;