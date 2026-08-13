function StatCard({
  icon,
  title,
  value,
  description,
  status = "normal",
}) {
  return (
    <article className="stat-card">
      <div className={`stat-icon stat-${status}`}>
        {icon}
      </div>

      <div>
        <p className="stat-title">{title}</p>

        <h3 className="stat-value">{value}</h3>

        <p className="stat-description">
          {description}
        </p>
      </div>
    </article>
  );
}

export default StatCard;