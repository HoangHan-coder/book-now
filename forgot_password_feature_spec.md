# Forgot Password Feature

## 1. Overview
This document describes the **Forgot Password** feature for a project built with:

- Java Spring Boot
- Thymeleaf (MVC architecture)
- Redis (store OTP and reset token temporarily)
- Relational Database (store user password)

Flow:

```
User enters email
        ↓
Server sends OTP to email
        ↓
User enters OTP
        ↓
If OTP valid → Server generates Reset Password Token
        ↓
Redirect to New Password page
        ↓
User enters new password + confirm password
        ↓
Server updates password in DB
```

---

## 2. Architecture (MVC)

### Model
- User
- OTP (stored in Redis)
- ResetPasswordToken (stored in Redis)

### View (Thymeleaf Pages)
- forgot-password.html (enter email)
- verify-otp.html (enter OTP)
- reset-password.html (enter new password)

### Controller
- AuthController

### Service
- AuthService
- EmailService
- OTPService
- RedisService

---

## 3. Detailed Flow

## Step 1: User Enters Email

### UI: forgot-password.html
Fields:
- email

### Endpoint
```
POST /forgot-password
```

### Server Logic
1. Validate email format.
2. Check if email exists in DB.
3. Generate 6-digit OTP.
4. Store OTP in Redis with expiration (e.g., 5 minutes).
   - Key: OTP:{email}
   - Value: 6-digit code
5. Send OTP to user's email.
6. Redirect to verify-otp page.

### Redis Configuration
- TTL: 5 minutes

---

## Step 2: User Enters OTP

### UI: verify-otp.html
Fields:
- email (hidden field)
- otp

### Endpoint
```
POST /verify-otp
```

### Server Logic
1. Get OTP from Redis using key OTP:{email}.
2. Compare with user input.
3. If invalid → return error message.
4. If valid:
   - Delete OTP from Redis.
   - Generate reset password token (UUID).
   - Store token in Redis:
     - Key: RESET_TOKEN:{token}
     - Value: email
     - TTL: 10 minutes
   - Redirect to:
     ```
     /reset-password?token=xxxxx
     ```

---

## Step 3: User Resets Password

### UI: reset-password.html
Fields:
- newPassword
- confirmPassword

### Endpoint
```
POST /reset-password
```

### Server Logic
1. Get token from request.
2. Retrieve email from Redis using key RESET_TOKEN:{token}.
3. If token not found or expired → return error.
4. Validate:
   - Password length
   - Password strength
   - newPassword == confirmPassword
5. Encode password (BCryptPasswordEncoder).
6. Update password in database.
7. Delete reset token from Redis.
8. Redirect to login page with success message.

---

## 4. Redis Key Design

| Purpose | Key | Value | TTL |
|----------|------|--------|------|
| Store OTP | OTP:{email} | 6-digit OTP | 5 minutes |
| Store Reset Token | RESET_TOKEN:{token} | email | 10 minutes |

---

## 5. Security Considerations

- Use BCryptPasswordEncoder.
- Limit OTP attempts (store attempt count in Redis).
- Invalidate OTP after successful verification.
- Invalidate reset token after password update.
- Do not reveal whether email exists.
- Use HTTPS.
- Add CSRF protection (Spring Security).

---

## 6. Example Package Structure

```
controller/
    AuthController.java

service/
    AuthService.java
    OTPService.java
    EmailService.java

repository/
    UserRepository.java

config/
    RedisConfig.java
    SecurityConfig.java

entity/
    User.java
```

---

## 7. Sequence Diagram (Textual)

```
User → Controller: Submit email
Controller → Service: Generate OTP
Service → Redis: Save OTP
Service → EmailService: Send OTP

User → Controller: Submit OTP
Controller → Redis: Validate OTP
Controller → Redis: Save Reset Token

User → Controller: Submit new password
Controller → Redis: Validate token
Controller → DB: Update password
Controller → Redis: Delete token
```

---

## 8. Error Cases

- Email not found
- OTP expired
- OTP incorrect
- Too many OTP attempts
- Token expired
- Password mismatch

---

## 9. Enhancements (Optional)

- Add resend OTP (with cooldown 60s)
- Add rate limiting per IP
- Log security events
- Use JWT instead of random token

---

End of Document.

