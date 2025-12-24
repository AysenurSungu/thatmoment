# Auth & JWT Test Scenarios

## 0) Ready-to-paste JSON (Swagger friendly)

Register:
```json
{
  "email": "test@example.com"
}
```

Verify email:
```json
{
  "email": "test@example.com",
  "code": "000000"
}
```

Resend code:
```json
{
  "email": "test@example.com"
}
```

Login (request code):
```json
{
  "email": "test@example.com"
}
```

Login verify:
```json
{
  "email": "test@example.com",
  "code": "LOGIN_CODE_HERE"
}
```

Refresh:
```json
{
  "refreshToken": "REFRESH_TOKEN_HERE"
}
```

Logout (current session):
```json
{}
```

Logout (all devices):
```json
{
  "allDevices": true
}
```

---

## 1) Register and Verify Email (curl)

Register a new user:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

Verify with an invalid code (expect 400):
```bash
curl -X POST "http://localhost:8080/api/v1/auth/verify-email" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "000000"
  }'
```

Verify with the real code (from DB or email):
```bash
curl -X POST "http://localhost:8080/api/v1/auth/verify-email" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "REAL_CODE_HERE"
  }'
```

Resend verification code:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/resend-code" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

---

## 2) Login Flow (OTP)

Request login code:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

Login with OTP (expect access + refresh tokens):
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "LOGIN_CODE_HERE"
  }'
```

---

## 3) Protected Endpoints

No token (expect 401):
```bash
curl "http://localhost:8080/api/v1/auth/sessions"
```

Valid token (expect 200):
```bash
curl "http://localhost:8080/api/v1/auth/sessions" \
  -H "Authorization: Bearer ACCESS_TOKEN_HERE"
```

Invalid token (expect 401):
```bash
curl "http://localhost:8080/api/v1/auth/sessions" \
  -H "Authorization: Bearer invalid.token.here"
```

---

## 4) Refresh Token

Refresh tokens (expect a new access + refresh token pair):
```bash
curl -X POST "http://localhost:8080/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "REFRESH_TOKEN_HERE"
  }'
```

Reuse old refresh token (expect 401 + reuse detection):
```bash
curl -X POST "http://localhost:8080/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "OLD_REFRESH_TOKEN"
  }'
```

---

## 5) Logout

Logout current session:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/logout" \
  -H "Authorization: Bearer ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{}'
```

After logout, the token should be invalid (expect 401):
```bash
curl "http://localhost:8080/api/v1/auth/sessions" \
  -H "Authorization: Bearer ACCESS_TOKEN_HERE"
```

Logout all devices:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/logout" \
  -H "Authorization: Bearer ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "allDevices": true
  }'
```

---

## 6) Public Endpoints (No Token Required)

These should return 200:
```bash
curl "http://localhost:8080/api/v1/health"
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "public@test.com"
  }'
```

---

## Expected Results Summary

| Scenario | Expected Result |
| --- | --- |
| Protected endpoint without token | 401 Unauthorized |
| Protected endpoint with valid token | 200 OK |
| Invalid/expired access token | 401 + errorCode |
| Refresh token rotation | New access + refresh token |
| Refresh token reuse | 401 + all sessions revoked |
| Logout current session | Access token becomes invalid |
| Public endpoints | 200 OK |
