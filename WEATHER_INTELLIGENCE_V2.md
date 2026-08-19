# GrowX Agro Weather Intelligence V2

## What was found in the uploaded project

The uploaded backend still had a placeholder `WeatherService` that threw `UnsupportedOperationException`, no concrete `WeatherApiClient` implementation, and Dashboard weather values were returned as `null`. The uploaded React Weather page used the old flat response/5-day layout and could remain indefinitely in the loading state when no current farm existed.

This patch completes the weather module without retraining or replacing the existing Crop Recommendation model.

## Architecture

```text
Farm (stored latitude/longitude preferred)
        |
        v
WeatherController
        |
        v
WeatherService  <---- short-lived in-memory cache / stale fallback
        |
        +----> WeatherApiClient
        |          |
        |          +----> OpenMeteoWeatherClient
        |
        +----> AgroWeatherService
        +----> WeatherAdvisoryService
        +----> WeatherFeatureService
```

Provider-specific Open-Meteo JSON is isolated inside `OpenMeteoWeatherClient`. The rest of GrowX consumes provider-neutral DTOs.

## Provider

Primary provider: **Open-Meteo Forecast API**.

- Farm WGS84 latitude/longitude are used first.
- Saved location text is geocoded only as a fallback when coordinates are absent.
- Provider traffic occurs only in Spring Boot; React never contacts the provider directly.
- The standard public Open-Meteo endpoint does not require a key; commercial reserved endpoints may use provider credentials under Open-Meteo's terms.

## Weather response

`GET /api/weather/farm/{farmId}` returns a normalized response containing:

- farm/location/timezone metadata
- current conditions
- next 24 hours
- 7-day forecast
- agricultural weather indicators
- farm-operation advisories
- overall farm weather risk
- crop-recommendation helper values
- provider/freshness metadata
- warnings

`POST /api/weather/farm/{farmId}/refresh` requests a manual refresh, subject to the configured minimum refresh interval.

Both endpoints require the existing JWT authentication, and the backend verifies that the requested farm belongs to the authenticated user.

## Agricultural indicators

The UI can display values when the provider returns them:

- FAO-56 reference ET0
- modelled evapotranspiration
- **Estimated / Modelled Soil Moisture**
- modelled near-surface soil temperature
- vapour pressure deficit

Modelled soil moisture is explicitly kept separate from physical farm sensor data. Dashboard's existing soil-moisture statistic remains `null` unless a real sensor source is added.

## Advisory engine

The deterministic, rule-based `WeatherAdvisoryService` produces decision-support signals for:

- irrigation
- spraying conditions
- environmental disease-weather risk
- heat stress
- rain alerts
- overall farm weather risk

These are weather-based decision aids, not farming guarantees or disease diagnoses.

## Rainfall semantics

Rain/precipitation probability (%) and amount (mm) remain separate throughout the implementation.

- Hourly/current total precipitation uses Open-Meteo precipitation amount.
- The daily "Rain amount" uses Open-Meteo daily `rain_sum`.
- Probability values are never converted to millimetres.

## Crop Recommendation integration

The Crop Recommendation model itself is unchanged.

The repository's crop CSV contains the features:

```text
N, P, K, temperature, humidity, ph, rainfall
```

However, the repository does not document the accumulation period represented by the model's `rainfall` feature. Therefore:

- **Rainfall auto-fill is disabled.**
- "Use Farm Weather" may fill only a 7-day representative temperature and 7-day average humidity.
- The UI tells the user these are convenience estimates and should be reviewed.
- Existing ML feature ordering/preprocessing is untouched.

## Reliability and caching

Configurable properties:

```properties
growx.weather.connection-timeout-ms=3000
growx.weather.read-timeout-ms=5000
growx.weather.retry-count=1
growx.weather.cache-ttl-minutes=10
growx.weather.stale-after-minutes=60
growx.weather.min-refresh-interval-seconds=60
```

Response states:

- `LIVE` - newly fetched provider data
- `CACHED` - recent cached provider data
- `STALE` - older cached data returned because live retrieval failed
- `UNAVAILABLE` - no live or usable cached data

No fake weather values are generated on provider failure.

The cache is intentionally an understandable in-process cache for this student project. It resets if the backend process restarts and is not distributed between multiple backend instances.

## Environment variables

No weather API secret is required for the standard provider endpoint.

Optional configuration overrides:

```text
GROWX_WEATHER_BASE_URL
GROWX_WEATHER_GEOCODING_URL
GROWX_WEATHER_CONNECT_TIMEOUT_MS
GROWX_WEATHER_READ_TIMEOUT_MS
GROWX_WEATHER_RETRY_COUNT
GROWX_WEATHER_CACHE_TTL_MINUTES
GROWX_WEATHER_STALE_AFTER_MINUTES
GROWX_WEATHER_MIN_REFRESH_SECONDS
```

Existing database/JWT/AI environment variables continue to apply.

## Run locally

### 1. AI service

```cmd
cd D:\GrowX\ai-service
python -m uvicorn app.main:app --reload --port 8000
```

### 2. Backend

CMD example:

```cmd
cd D:\GrowX\backend
set GROWX_DB_PASSWORD=YOUR_MYSQL_PASSWORD
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Frontend

```cmd
cd D:\GrowX\frontend
npm run dev
```

## Verification commands

Backend:

```cmd
cd D:\GrowX\backend
mvn test
mvn clean package
```

Frontend:

```cmd
cd D:\GrowX\frontend
npm run lint
npm run build
```

## End-to-end test

1. Log in.
2. Open Profile and confirm the farm location.
3. Add latitude and longitude if known.
4. Open Weather Intelligence.
5. Confirm current conditions and freshness badge.
6. Confirm next 24 hours.
7. Confirm 7-day forecast and separate rain chance / rain amount.
8. Confirm Farm Actions advisories.
9. Open Dashboard and confirm it uses the same centralized weather data.
10. Open Crop Recommendation and select **Use Farm Weather**.
11. Confirm temperature/humidity can be populated but rainfall remains manual.
12. Temporarily disconnect the provider/network after one successful fetch to verify cached fallback behavior.

## Known limitations

- Weather values are model forecasts and can change.
- Soil moisture is a weather-model estimate, not a farm sensor or root-zone measurement.
- Disease-weather risk is environmental risk only; it is separate from Disease Detection AI.
- Advisory thresholds are general deterministic heuristics, not crop-specific agronomic prescriptions.
- Location-name geocoding is a fallback and may be less precise than saved coordinates.
- Cache is process-local and is cleared on backend restart.
- No circuit breaker was added because it was optional and would add complexity beyond the project's current needs; bounded timeouts, limited retry and cache fallback are implemented.
- Crop rainfall auto-fill remains disabled until the training dataset's rainfall time basis can be verified.
