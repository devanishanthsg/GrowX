import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/authApi.js";
import { useAuth } from "../hooks/useAuth.js";
import { validateLogin } from "../utils/validators.js";

function Login() {
  const navigate = useNavigate();
  const { loginSuccess } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear field error on change
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
    setApiError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    // Frontend validation
    const validationErrors = validateLogin(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setApiError("");

    try {
      const authData = await login({
        email: formData.email.trim(),
        password: formData.password,
      });

      await loginSuccess(authData);
      navigate("/dashboard", { replace: true });
    } catch (error) {
      setApiError(
        error.message ?? "Login failed. Please check your credentials.",
      );
    } finally {
      setLoading(false);
    }
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
          noValidate
        >
          <div className="auth-form-heading">
            <h2>Welcome back</h2>
            <p>Sign in to your GrowX account.</p>
          </div>

          {apiError && (
            <p className="form-error">{apiError}</p>
          )}

          <label>
            Email address
            <input
              type="email"
              name="email"
              id="login-email"
              placeholder="farmer@example.com"
              value={formData.email}
              onChange={handleChange}
              disabled={loading}
              autoComplete="email"
            />
            {errors.email && (
              <span className="field-error">{errors.email}</span>
            )}
          </label>

          <label>
            Password
            <input
              type="password"
              name="password"
              id="login-password"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              disabled={loading}
              autoComplete="current-password"
            />
            {errors.password && (
              <span className="field-error">{errors.password}</span>
            )}
          </label>

          <button
            type="submit"
            className="full-primary-button"
            id="login-submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="inline-spinner" />
                Signing in…
              </>
            ) : (
              "Login"
            )}
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