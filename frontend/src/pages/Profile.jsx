import { useState } from "react";

function Profile() {
  const [profile, setProfile] = useState({
    name: "Devanishanth",
    email: "farmer@example.com",
    phone: "+91 98765 43210",
    farmName: "Green Valley Farm",
    location: "Coimbatore, Tamil Nadu",
    landArea: "3",
    mainCrop: "Coconut",
  });

  const [saved, setSaved] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;

    setProfile((previousProfile) => ({
      ...previousProfile,
      [name]: value,
    }));

    setSaved(false);
  }

  function handleSubmit(event) {
    event.preventDefault();
    setSaved(true);
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">
            ACCOUNT
          </p>

          <h1>Farmer Profile</h1>

          <p>
            Update your personal and farm information.
          </p>
        </div>
      </header>

      <form
        className="panel profile-form"
        onSubmit={handleSubmit}
      >
        <div className="profile-top">
          <div className="large-avatar">DS</div>

          <div>
            <h2>{profile.name}</h2>
            <p>{profile.farmName}</p>
            <button
              type="button"
              className="text-button"
            >
              Change profile photo
            </button>
          </div>
        </div>

        {saved && (
          <p className="form-success">
            Profile updated successfully.
          </p>
        )}

        <div className="form-grid">
          <label>
            Full name
            <input
              name="name"
              value={profile.name}
              onChange={handleChange}
            />
          </label>

          <label>
            Email address
            <input
              type="email"
              name="email"
              value={profile.email}
              onChange={handleChange}
            />
          </label>

          <label>
            Phone number
            <input
              name="phone"
              value={profile.phone}
              onChange={handleChange}
            />
          </label>

          <label>
            Farm name
            <input
              name="farmName"
              value={profile.farmName}
              onChange={handleChange}
            />
          </label>

          <label>
            Farm location
            <input
              name="location"
              value={profile.location}
              onChange={handleChange}
            />
          </label>

          <label>
            Land area in acres
            <input
              type="number"
              name="landArea"
              value={profile.landArea}
              onChange={handleChange}
            />
          </label>

          <label className="full-field">
            Main crop
            <input
              name="mainCrop"
              value={profile.mainCrop}
              onChange={handleChange}
            />
          </label>
        </div>

        <div className="form-buttons">
          <button
            type="submit"
            className="primary-button"
          >
            Save Changes
          </button>
        </div>
      </form>
    </div>
  );
}

export default Profile;