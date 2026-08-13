# GrowX Spring Boot Backend — Implementation Plan

## Frontend Analysis Summary

### Pages Discovered
| Page | Route | Auth Required | Backend Modules Needed |
|---|---|---|---|
| Home | `/` | No | None (static landing) |
| Login | `/login` | No | `POST /api/auth/login` → JWT |
| Register | `/register` | No | `POST /api/auth/register` → JWT |
| Dashboard | `/dashboard` | ✅ Yes | Aggregated data: weather, stats, recent activity |
| Crop Recommendation | `/crop-recommendation` | ✅ Yes | ML input (N, P, K, temp, humidity, pH, rainfall) → crop, confidence, season, yield |
| Disease Detection | `/disease-detection` | ✅ Yes | Multipart image upload → disease, confidence, severity, treatment, prevention |
| Weather | `/weather` | ✅ Yes | Current weather + 5-day forecast + alerts by farm location |
| Reports | `/reports` | ✅ Yes | List reports by type (AI Prediction, Disease Detection, Farm Report), download |
| Profile | `/profile` | ✅ Yes | Get/update: name, email, phone, farmName, location, landArea, mainCrop |

### Key Frontend Observations
- **Auth**: Uses `localStorage("growx-user")` with `{ name, email, farmName, location }` — currently fake/mock. Backend will replace this with real JWT.
- **ProtectedLayout**: Checks `localStorage("growx-user")` — redirects to `/login` if missing. Will need JWT token stored instead.
- **Register fields**: `name`, `email`, `location`, `password`
- **Login fields**: `email`, `password`
- **Profile fields**: `name`, `email`, `phone`, `farmName`, `location`, `landArea` (acres), `mainCrop`
- **Crop Recommendation inputs**: nitrogen, phosphorus, potassium, temperature, humidity, ph, rainfall (all numbers)
- **Crop Recommendation output**: crop name, confidence %, season, yield (string), explanation
- **Disease Detection**: PNG/JPG multipart upload → disease name, confidence %, severity (Moderate/etc), treatment (text), prevention (text)
- **Weather page**: current temp, condition, feelsLike, humidity, windSpeed, rainProbability, uvIndex + 5-day forecast (day, icon, condition, temp, rain%)  + alerts
- **Dashboard**: soil moisture %, temperature, humidity %, crop health %, weather summary, AI recommendations list, recent activity list
- **Reports page**: list with name, date, type (AI Prediction / Disease Detection / Farm Report), status, download action; plus aggregate counts (total, AI predictions, disease analyses)

---

## Recommended Backend Modules & Entities

### Entities
```
User           → id, name, email, phone, passwordHash, role, createdAt, updatedAt
Farm           → id, farmName, location, latitude, longitude, area, areaUnit, soilType, mainCrop, user_id, createdAt, updatedAt
CropRecommendation → id, nitrogen, phosphorus, potassium, temperature, humidity, ph, rainfall, recommendedCrop, confidence, season, expectedYield, explanation, status, farm_id, createdAt
DiseaseDetection   → id, imagePath, diseaseName, confidence, severity, treatment, prevention, farm_id, detectedAt
Report             → id, title, type (enum), status, farm_id, createdAt
```

### Relationships
```
User (1) ——< (many) Farm
Farm (1) ——< (many) CropRecommendation
Farm (1) ——< (many) DiseaseDetection
Farm (1) ——< (many) Report
```

---

## Final Backend Structure to Create

```
GrowX/backend/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/growx/
    │   │   ├── GrowXApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── CorsConfig.java
    │   │   │   └── AppConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── UserController.java
    │   │   │   ├── FarmController.java
    │   │   │   ├── CropRecommendationController.java
    │   │   │   ├── DiseaseDetectionController.java
    │   │   │   ├── WeatherController.java
    │   │   │   ├── DashboardController.java
    │   │   │   └── ReportController.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── UserService.java
    │   │   │   ├── FarmService.java
    │   │   │   ├── CropRecommendationService.java
    │   │   │   ├── DiseaseDetectionService.java
    │   │   │   ├── WeatherService.java
    │   │   │   ├── DashboardService.java
    │   │   │   ├── ReportService.java
    │   │   │   └── FileStorageService.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── FarmRepository.java
    │   │   │   ├── CropRecommendationRepository.java
    │   │   │   ├── DiseaseDetectionRepository.java
    │   │   │   └── ReportRepository.java
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   ├── Farm.java
    │   │   │   ├── CropRecommendation.java
    │   │   │   ├── DiseaseDetection.java
    │   │   │   └── Report.java
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── UpdateProfileRequest.java
    │   │   │   │   ├── CreateFarmRequest.java
    │   │   │   │   ├── UpdateFarmRequest.java
    │   │   │   │   └── CropRecommendationRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── UserResponse.java
    │   │   │       ├── FarmResponse.java
    │   │   │       ├── DashboardResponse.java
    │   │   │       ├── CropRecommendationResponse.java
    │   │   │       ├── DiseaseDetectionResponse.java
    │   │   │       ├── WeatherResponse.java
    │   │   │       └── ReportResponse.java
    │   │   ├── security/
    │   │   │   ├── JwtService.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   └── UserPrincipal.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── UnauthorizedException.java
    │   │   │   ├── EmailAlreadyExistsException.java
    │   │   │   └── InvalidCredentialsException.java
    │   │   ├── enums/
    │   │   │   ├── Role.java
    │   │   │   ├── ReportType.java
    │   │   │   ├── DiseaseSeverity.java
    │   │   │   └── RecommendationStatus.java
    │   │   ├── mapper/
    │   │   │   ├── UserMapper.java
    │   │   │   ├── FarmMapper.java
    │   │   │   ├── CropRecommendationMapper.java
    │   │   │   ├── DiseaseDetectionMapper.java
    │   │   │   └── ReportMapper.java
    │   │   ├── client/
    │   │   │   ├── WeatherApiClient.java
    │   │   │   ├── CropMlClient.java
    │   │   │   └── DiseaseMlClient.java
    │   │   ├── util/
    │   │   │   └── ApiConstants.java
    │   │   └── constants/
    │   │       └── SecurityConstants.java
    │   └── resources/
    │       ├── application.properties
    │       ├── application-dev.properties
    │       └── application-prod.properties
    └── test/
        └── java/com/growx/
            ├── controller/
            ├── service/
            └── repository/
```

## Verification Plan

### Automated
- `mvn clean compile` — verify zero compile errors
- `mvn test` — verify test context loads

### Configuration to Request from User
After creation, user must supply:
- MySQL database name (e.g., `growx_db`)
- MySQL username
- MySQL password
- JWT secret key (256-bit Base64 string)
- Weather API key (OpenWeatherMap or similar — for future use)
