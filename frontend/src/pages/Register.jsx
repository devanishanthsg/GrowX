import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/authApi.js";
import { useAuth } from "../hooks/useAuth.js";
import { validateRegister } from "../utils/validators.js";

function Register() {
  const navigate = useNavigate();
  const { loginSuccess } = useAuth();

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    location: "",
    password: "",
  });

  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
    setApiError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    // Frontend validation
    const validationErrors = validateRegister(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setApiError("");

    try {
      const authData = await register({
        name: formData.name.trim(),
        email: formData.email.trim(),
        location: formData.location.trim(),
        password: formData.password,
      });

      // Registration also returns a JWT — log in immediately
      await loginSuccess(authData);
      navigate("/dashboard", { replace: true });
    } catch (error) {
      // Map field-level validation errors from backend if present
      if (error.data && typeof error.data === "object") {
        setErrors(error.data);
      } else {
        setApiError(
          error.message ?? "Registration failed. Please try again.",
        );
      }
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
          <p className="auth-label">JOIN GROWX</p>

          <h1>Begin your smart farming journey.</h1>

          <p>
            Create your account and start using
            AI-powered tools for modern agriculture.
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
            <h2>Create account</h2>
            <p>Register as a farmer on GrowX.</p>
          </div>

          {apiError && (
            <p className="form-error">{apiError}</p>
          )}

          <label>
            Full name
            <input
              name="name"
              id="register-name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter your name"
              disabled={loading}
              autoComplete="name"
            />
            {errors.name && (
              <span className="field-error">{errors.name}</span>
            )}
          </label>

          <label>
            Email address
            <input
              type="email"
              name="email"
              id="register-email"
              value={formData.email}
              onChange={handleChange}
              placeholder="farmer@example.com"
              disabled={loading}
              autoComplete="email"
            />
            {errors.email && (
              <span className="field-error">{errors.email}</span>
            )}
          </label>

          <label>
            Farm location
            <input
              name="location"
              id="register-location"
              value={formData.location}
              onChange={handleChange}
              placeholder="Example: Coimbatore"
              disabled={loading}
            />
            {errors.location && (
              <span className="field-error">{errors.location}</span>
            )}
          </label>

          <label>
            Password
            <input
              type="password"
              name="password"
              id="register-password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Create a strong password"
              disabled={loading}
              autoComplete="new-password"
            />
            {errors.password && (
              <span className="field-error">{errors.password}</span>
            )}
          </label>

          <button
            type="submit"
            className="full-primary-button"
            id="register-submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="inline-spinner" />
                Creating account…
              </>
            ) : (
              "Create Account"
            )}
          </button>

          <p className="auth-switch">
            Already have an account?{" "}
            <Link to="/login">Login</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default Register;