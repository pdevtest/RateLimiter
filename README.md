# Rate Limiter Service

A token bucket rate limiting service built with Spring Boot that supports per-client rate limit configurations, concurrency safety, in-memory bucket stores with idle eviction, and HTTP request filtering.

---

## 🚀 How to Run the Application

### Option 1: Run with Maven Plugin (Development)
```powershell
mvn spring-boot:run
```

### Option 2: Build JAR and Run (Production-ready)
1. Build the executable package:
   ```powershell
   mvn clean package
   ```
2. Run the JAR:
   ```powershell
   java -jar target/demo-0.0.1-SNAPSHOT.jar
   ```

The application starts by default on port `8080`: `http://localhost:8080`.

---

## 📬 Manual HTTP Testing Guide (Postman, cURL, PowerShell)

### Workflow Overview
```
1. Check Health   --> GET /health (Verify app is UP)
2. Configure Limit --> PUT /admin/clients/{clientId}/rate-limit (Set capacity & period)
3. Access API      --> GET /api/example with Header X-Client-Id (Observe 200 OK -> 429 Too Many Requests)
4. Delete Limit    --> DELETE /admin/clients/{clientId}/rate-limit (Reset limit)
```

---

### Step 1: Health Check (Unfiltered)

#### In Postman:
1. Create a new request.
2. Set Method to **`GET`**.
3. Enter URL: `http://localhost:8080/health`
4. Click **Send**.
5. **Expected Response**: Status `200 OK`
   ```json
   {
     "status": "UP"
   }
   ```

#### Alternative HTTP Requests:
- **cURL**:
  ```bash
  curl -X GET http://localhost:8080/health
  ```
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/health" -Method Get
  ```

---

### Step 2: Configure Rate Limit for a Client

Configure a rate limit of **2 requests per 60 seconds** for client `client-123`.

#### In Postman:
1. Create a new request tab.
2. Set Method to **`PUT`**.
3. Enter URL: `http://localhost:8080/admin/clients/client-123/rate-limit`
4. Go to the **Headers** tab:
   - Key: `Content-Type`, Value: `application/json`
5. Go to the **Body** tab:
   - Select **raw** radio button.
   - Select **JSON** from the dropdown.
   - Enter payload:
     ```json
     {
       "capacity": 2,
       "periodSeconds": 60
     }
     ```
6. Click **Send**.
7. **Expected Response**: Status `204 No Content` (Empty response body).

#### Alternative HTTP Requests:
- **cURL**:
  ```bash
  curl -X PUT http://localhost:8080/admin/clients/client-123/rate-limit \
    -H "Content-Type: application/json" \
    -d "{\"capacity\": 2, \"periodSeconds\": 60}"
  ```
- **PowerShell**:
  ```powershell
  $body = @{ capacity = 2; periodSeconds = 60 } | ConvertTo-Json
  Invoke-RestMethod -Uri "http://localhost:8080/admin/clients/client-123/rate-limit" -Method Put -Body $body -ContentType "application/json"
  ```

---

### Step 3: Test Protected API (`/api/example`)

#### In Postman:
1. Create a new request tab.
2. Set Method to **`GET`**.
3. Enter URL: `http://localhost:8080/api/example`
4. Go to the **Headers** tab:
   - Key: `X-Client-Id`
   - Value: `client-123`
5. Click **Send** multiple times in succession to observe rate limiting behavior:

#### Observations:
- **1st Click**: Status `200 OK`
  ```json
  { "message": "Request accepted" }
  ```
- **2nd Click**: Status `200 OK`
  ```json
  { "message": "Request accepted" }
  ```
- **3rd Click (Limit Exceeded)**: Status `429 Too Many Requests`
  - In Postman, check the **Headers** tab in the lower response section:
    - `Retry-After: 1`
  - In the response **Body**:
    ```json
    { "error": "Rate limit exceeded" }
    ```

#### Testing Header Validation:
- In Postman, uncheck or delete the `X-Client-Id` header and click **Send**:
- **Expected Response**: Status `400 Bad Request`
  ```json
  { "error": "X-Client-Id header is required" }
  ```

#### Alternative HTTP Requests:
- **cURL (Request with client header)**:
  ```bash
  curl -i -X GET http://localhost:8080/api/example -H "X-Client-Id: client-123"
  ```
- **cURL (Missing client header)**:
  ```bash
  curl -i -X GET http://localhost:8080/api/example
  ```
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/example" -Headers @{ "X-Client-Id" = "client-123" }
  ```

---

### Step 4: Reset / Delete Client Limit

#### In Postman:
1. Create a new request tab.
2. Set Method to **`DELETE`**.
3. Enter URL: `http://localhost:8080/admin/clients/client-123/rate-limit`
4. Click **Send**.
5. **Expected Response**: Status `204 No Content`

#### Alternative HTTP Requests:
- **cURL**:
  ```bash
  curl -X DELETE http://localhost:8080/admin/clients/client-123/rate-limit
  ```
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/admin/clients/client-123/rate-limit" -Method Delete
  ```

---

## 🔬 Running Automated Tests

> **Note**: Use the `mvn` command directly with the `test` lifecycle phase.

### 1. Run the Entire Test Suite
Executes all unit, concurrency, and integration tests:
```powershell
mvn test
```

To clean previous build artifacts before testing:
```powershell
mvn clean test
```

---

### 2. Run Individual Test Classes
Execute a specific test class using the `-Dtest` property:

- **Token Bucket Rate Limiter Unit Tests:**
  ```powershell
  mvn test -Dtest=TokenBucketRateLimiterTest
  ```

- **Concurrency & Thread-Safety Tests:**
  ```powershell
  mvn test -Dtest=TokenBucketRateLimiterConcurrencyTest
  ```

- **In-Memory Bucket Store & Eviction Tests:**
  ```powershell
  mvn test -Dtest=InMemoryClientBucketStoreTest
  ```

- **End-to-End Web Integration Tests:**
  ```powershell
  mvn test -Dtest=RateLimitingIntegrationTest
  ```

---

## 📋 Test Suite Overview

### 1. `TokenBucketRateLimiterTest`
- **Location**: `src/test/java/com/ratelimiter/demo/service/TokenBucketRateLimiterTest.java`
- **Purpose**: Unit tests verifying core token bucket algorithm behaviors using a simulated clock (`MutableClock`):
  - **Capacity Enforcement**: Allows requests up to the configured limit and blocks subsequent requests once tokens are depleted.
  - **Token Refill**: Verifies tokens regenerate accurately across elapsed time based on the defined rate.
  - **Client Isolation**: Ensures multiple clients maintain independent bucket states and limits.
  - **Unconfigured Clients**: Verifies requests from clients without rate limits are rejected.
  - **Dynamic Configuration**: Validates that updating a client's limit takes effect immediately.

---

### 2. `TokenBucketRateLimiterConcurrencyTest`
- **Location**: `src/test/java/com/ratelimiter/demo/service/TokenBucketRateLimiterConcurrencyTest.java`
- **Purpose**: Multi-threaded concurrency testing:
  - Dispatches 1,000 concurrent requests across a 16-thread pool against a configured rate limiter.
  - Guarantees thread safety and race-condition prevention by asserting that the total number of allowed requests strictly never exceeds the configured capacity.

---

### 3. `InMemoryClientBucketStoreTest`
- **Location**: `src/test/java/com/ratelimiter/demo/store/InMemoryClientBucketStoreTest.java`
- **Purpose**: Validates bucket lifecycle and cleanup mechanisms:
  - Tests the idle bucket eviction routine (`evictIdleBefore`).
  - Verifies that buckets idle prior to a given cutoff timestamp are evicted to free memory, while active buckets remain untouched.

---

### 4. `RateLimitingIntegrationTest`
- **Location**: `src/test/java/com/ratelimiter/demo/web/RateLimitingIntegrationTest.java`
- **Purpose**: Full Spring Boot integration testing using `MockMvc`:
  - **Admin Configuration API**: Tests configuring client limits via `PUT /admin/clients/{clientId}/rate-limit`.
  - **HTTP Rate Limiting Filter**: Tests that protected endpoints (`/api/example`) return `200 OK` within limits and `429 Too Many Requests` (including the `Retry-After` response header) when exhausted.
  - **Header Validation**: Verifies `400 Bad Request` is returned when the mandatory `X-Client-Id` HTTP header is missing.
