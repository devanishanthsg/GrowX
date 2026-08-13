import { useState } from "react";
import {
  Link,
  useNavigate,
} from "react-router-dom";

function Login() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: value,
    }));
  }

    function handleSubmit(event) {
    event.preventDefault();
    setError("");

    const email = formData.email.trim();
    const password = formData.password.trim();

    if (!email || !password) {
      setError("Please enter your email and password.");
      return;
    }

    const user = {
      name: "GrowX Farmer",
      email,
      farmName: "Green Valley Farm",
      location: "Tamil Nadu",
    };

    localStorage.setItem(
      "growx-user",
      JSON.stringify(user)
    );

    navigate("/dashboard");
  }

  return (
    <main className="auth-page">
      <section className="auth-presentation">
        <Link to="/" className="auth-logo">
          🌱 GrowX
        </Link>

        <div>
          <p className="auth-label">
            SMART FARMING PLATFORM
          </p>

          <h1>
            Make better farming decisions with AI.
          </h1>

          <p>
            Access crop recommendations, plant disease
            detection, weather information and farm
            reports.
          </p>
        </div>
      </section>

      <section className="auth-form-section">
        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          <div className="auth-form-heading">
            <h2>Welcome back</h2>
            <p>Sign in to your GrowX account.</p>
          </div>

          {error && (
            <p className="form-error">{error}</p>
          )}

          <label>
            Email address

            <input
              type="email"
              name="email"
              placeholder="farmer@example.com"
              value={formData.email}
              onChange={handleChange}
            />
          </label>

          <label>
            Password

            <input
              type="password"
              name="password"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
            />
          </label>

          <div className="form-options">
            <label className="remember-option">
              <input type="checkbox" />
              Remember me
            </label>

            <button
              className="text-button"
              type="button"
            >
              Forgot password?
            </button>
          </div>

          <button
            type="submit"
            className="full-primary-button"
          >
            Login
          </button>

          <p className="auth-switch">
            Don't have an account?{" "}
            <Link to="/register">Create account</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default Login;