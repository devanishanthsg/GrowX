import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth.js";
import { getMe, updateProfile } from "../api/userApi.js";
import { getInitials } from "../utils/formatters.js";

function Profile() {
  const { user, currentFarm, refreshUser, refreshFarm } = useAuth();

  // Form mirrors UpdateProfileRequest fields (backend DTO)
  // IMPORTANT: email is always required by the backend
  const [profile, setProfile] = useState({
    name: "",
    email: "",
    phone: "",
    farmName: "",
    location: "",
    landArea: "",
    mainCrop: "",
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  // Populate form from API on mount
  useEffect(() => {
    async function loadProfile() {
      setLoading(true);
      try {
        // Fetch fresh profile from API (not just from AuthContext)
        const userData = await getMe();
        setProfile({
          name: userData.name ?? "",
          email: userData.email ?? "",
          phone: userData.phone ?? "",
          // Farm fields from currentFarm if available
          farmName: currentFarm?.farmName ?? "",
          location: currentFarm?.location ?? "",
          landArea: currentFarm?.area != null ? String(currentFarm.area) : "",
          mainCrop: currentFarm?.mainCrop ?? "",
        });
      } catch (err) {
        setError(err.message ?? "Failed to load profile.");
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;
    setProfile((prev) => ({ ...prev, [name]: value }));
    setSaved(false);
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    }
    setError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    // Basic frontend validation
    if (!profile.name.trim()) {
      setFieldErrors({ name: "Name is required." });
      return;
    }
    if (!profile.email.trim()) {
      setFieldErrors({ email: "Email is required." });
      return;
    }

    setSaving(true);
    setSaved(false);
    setError("");
    setFieldErrors({});

    try {
      // PUT /api/users/me — email is required by UpdateProfileRequest
      await updateProfile({
        name: profile.name.trim(),
        email: profile.email.trim(),
        phone: profile.phone.trim() || null,
        farmName: profile.farmName.trim() || null,
        location: profile.location.trim() || null,
        landArea: profile.landArea ? Number(profile.landArea) : null,
        mainCrop: profile.mainCrop.trim() || null,
      });

      setSaved(true);
      // Refresh AuthContext so Sidebar/Dashboard reflect updated name
      await refreshUser();
      await refreshFarm();
    } catch (err) {
      if (err.data && typeof err.data === "object") {
        setFieldErrors(err.data);
      } else {
        setError(err.message ?? "Failed to save profile. Please try again.");
      }
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div>
        <header className="page-header">
          <div>
            <p className="page-label">ACCOUNT</p>
            <h1>Farmer Profile</h1>
          </div>
        </header>
        <div className="auth-loading" style={{ minHeight: "50vh" }}>
          <div className="auth-loading-spinner" />
          <p>Loading profile…</p>
        </div>
      </div>
    );
  }

  const initials = getInitials(profile.name || user?.name);

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">ACCOUNT</p>
          <h1>Farmer Profile</h1>
          <p>Update your personal and farm information.</p>
        </div>
      </header>

      <form
        className="panel profile-form"
        onSubmit={handleSubmit}
        noValidate
      >
        <div className="profile-top">
          <div className="large-avatar">{initials}</div>

          <div>
            <h2>{profile.name || "—"}</h2>
            <p>{profile.farmName || currentFarm?.farmName || "—"}</p>
          </div>
        </div>

        {saved && (
          <p className="form-success">
            ✓ Profile updated successfully.
          </p>
        )}

        {error && <p className="page-error">{error}</p>}

        <div className="form-grid">
          <label>
            Full name
            <input
              name="name"
              id="profile-name"
              value={profile.name}
              onChange={handleChange}
              disabled={saving}
            />
            {fieldErrors.name && (
              <span className="field-error">{fieldErrors.name}</span>
            )}
          </label>

          <label>
            Email address
            <input
              type="email"
              name="email"
              id="profile-email"
              value={profile.email}
              onChange={handleChange}
              disabled={saving}
            />
            {fieldErrors.email && (
              <span className="field-error">{fieldErrors.email}</span>
            )}
          </label>

          <label>
            Phone number
            <input
              name="phone"
              id="profile-phone"
              value={profile.phone}
              onChange={handleChange}
              placeholder="Optional"
              disabled={saving}
            />
            {fieldErrors.phone && (
              <span className="field-error">{fieldErrors.phone}</span>
            )}
          </label>

          <label>
            Farm name
            <input
              name="farmName"
              id="profile-farmName"
              value={profile.farmName}
              onChange={handleChange}
              disabled={saving}
            />
          </label>

          <label>
            Farm location
            <input
              name="location"
              id="profile-location"
              value={profile.location}
              onChange={handleChange}
              disabled={saving}
            />
          </label>

          <label>
            Land area in acres
            <input
              type="number"
              name="landArea"
              id="profile-landArea"
              value={profile.landArea}
              onChange={handleChange}
              placeholder="Optional"
              min="0"
              disabled={saving}
            />
          </label>

          <label className="full-field">
            Main crop
            <input
              name="mainCrop"
              id="profile-mainCrop"
              value={profile.mainCrop}
              onChange={handleChange}
              disabled={saving}
            />
          </label>
        </div>

        <div className="form-buttons">
          <button
            type="submit"
            className="primary-button"
            id="profile-save-btn"
            disabled={saving}
          >
            {saving ? (
              <>
                <span className="inline-spinner" />
                Saving…
              </>
            ) : (
              "Save Changes"
            )}
          </button>
        </div>
      </form>
    </div>
  );
}

export default Profile;