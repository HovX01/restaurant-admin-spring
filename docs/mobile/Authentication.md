# Authentication Guide

Complete guide to implementing JWT authentication in your mobile application.

## Table of Contents

1. [Overview](#overview)
2. [JWT Token Details](#jwt-token-details)
3. [Authentication Flow](#authentication-flow)
4. [Implementation Guide](#implementation-guide)
5. [Token Storage](#token-storage)
6. [Token Management](#token-management)
7. [Error Handling](#error-handling)
8. [Security Best Practices](#security-best-practices)
9. [Code Examples](#code-examples)

---

## Overview

The Restaurant Admin System uses **JWT (JSON Web Token)** authentication with the following characteristics:

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Expiration**: 24 hours
- **Token Type**: Bearer token
- **Password Encryption**: BCrypt
- **Session Management**: Stateless (no server-side sessions)

### Authentication Endpoints

- **Login**: `POST /api/auth/login` (public)
- **Register**: `POST /api/auth/register` (public)
- **Get User Info**: `GET /api/auth/info` (authenticated)
- **Change Password**: `POST /api/auth/change-password` (authenticated)

---

## JWT Token Details

### Token Structure

A JWT token consists of three parts separated by dots:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzOTQ4NjQwMCwiZXhwIjoxNjM5NTcyODAwfQ.signature
│                                      │                                                              │
└─────────── Header ───────────────────┴─────────────────────── Payload ──────────────────────────────┴─ Signature
```

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "username",
  "iat": 1639486400,  // Issued at (Unix timestamp)
  "exp": 1639572800   // Expiration (Unix timestamp)
}
```

### Token Properties

- **Expiration**: 24 hours (86400000 milliseconds)
- **Subject (sub)**: Username of the authenticated user
- **Issued At (iat)**: Token creation timestamp
- **Expiration (exp)**: Token expiry timestamp

### Using the Token

Include the JWT token in the Authorization header of all protected API requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Authentication Flow

### Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     App Launch                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │  Check for Stored    │
              │  JWT Token           │
              └──────────┬───────────┘
                         │
           ┌─────────────┴──────────────┐
           │                            │
      No Token                      Token Exists
           │                            │
           ▼                            ▼
    ┌────────────┐            ┌──────────────────┐
    │   Show     │            │  Validate Token  │
    │   Login    │            │  - Check Format  │
    │   Screen   │            │  - Not Expired?  │
    └─────┬──────┘            └────────┬─────────┘
          │                            │
          │              ┌─────────────┴───────────────┐
          │              │                             │
          │           Invalid                       Valid
          │              │                             │
          │              ▼                             ▼
          │      ┌──────────────┐            ┌─────────────────┐
          │      │  Clear Token │            │ Verify on Server│
          │      │  Show Login  │            │ GET /auth/info  │
          │      └──────────────┘            └────────┬────────┘
          │                                            │
          │                          ┌─────────────────┴────────────┐
          │                          │                              │
          │                     200 OK                         401 Error
          │                          │                              │
          │                          ▼                              ▼
          │                  ┌──────────────┐            ┌──────────────┐
          │                  │   Go to Main │            │ Clear Token  │
          │                  │   Dashboard  │            │  Show Login  │
          │                  └──────────────┘            └──────────────┘
          │                                                       │
          └───────────────────────────────────────────────────────┘
                                     │
                                     ▼
                          ┌──────────────────────┐
                          │  User Enters         │
                          │  Username & Password │
                          └──────────┬───────────┘
                                     │
                                     ▼
                          ┌──────────────────────┐
                          │  POST /auth/login    │
                          │  {username, password}│
                          └──────────┬───────────┘
                                     │
                       ┌─────────────┴─────────────┐
                       │                           │
                  200 OK                      401 Error
                       │                           │
                       ▼                           ▼
            ┌──────────────────┐        ┌──────────────────┐
            │  Receive Token   │        │  Show Error      │
            │  & User Data     │        │  "Invalid        │
            └─────────┬────────┘        │  Credentials"    │
                      │                 └──────────────────┘
                      ▼
            ┌──────────────────┐
            │  Store Token     │
            │  Securely        │
            │  - iOS: Keychain │
            │  - Android: ESPref│
            └─────────┬────────┘
                      │
                      ▼
            ┌──────────────────┐
            │  Store User Data │
            │  (optional cache)│
            └─────────┬────────┘
                      │
                      ▼
            ┌──────────────────┐
            │  Navigate to     │
            │  Main Dashboard  │
            └──────────────────┘
```

---

## Implementation Guide

### Step 1: Create Auth Service/Manager

Create a centralized authentication manager to handle all auth operations:

**Responsibilities**:
- Store and retrieve JWT tokens
- Make login/logout requests
- Check authentication status
- Manage user session
- Handle token expiration

### Step 2: Login Implementation

**Process**:
1. Collect username and password from user
2. Validate input (non-empty, minimum length)
3. Send POST request to `/api/auth/login`
4. On success:
   - Extract JWT token from response
   - Store token securely
   - Store user data (optional)
   - Navigate to main screen
5. On failure:
   - Display error message
   - Allow retry

**Request**:
```json
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "[email protected]",
      "fullName": "System Administrator",
      "role": "ADMIN",
      "enabled": true,
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-01-01T00:00:00"
    }
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Invalid credentials",
  "error": "Bad credentials",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

### Step 3: Token Storage

Store the JWT token securely using platform-specific secure storage:

**iOS**: Keychain Services
**Android**: EncryptedSharedPreferences
**Flutter**: flutter_secure_storage
**React Native**: react-native-keychain

**Never**:
- Store tokens in UserDefaults/SharedPreferences (unencrypted)
- Log tokens in production
- Store tokens in plain text files

### Step 4: HTTP Client Configuration

Configure your HTTP client to automatically include the JWT token:

**Requirements**:
1. Retrieve token from secure storage
2. Add Authorization header to all requests
3. Handle 401 responses (token expired/invalid)
4. Implement token refresh logic

**Header Format**:
```
Authorization: Bearer {your-jwt-token}
```

### Step 5: Token Validation

**On App Launch**:
1. Check if token exists in secure storage
2. Validate token format (not empty, not corrupted)
3. Check expiration time (decode JWT and check 'exp' claim)
4. Verify with server by calling `/api/auth/info`
5. If valid, proceed to main screen
6. If invalid, clear token and show login

### Step 6: Logout Implementation

**Process**:
1. Clear JWT token from secure storage
2. Clear any cached user data
3. Reset app state
4. Disconnect WebSocket connection
5. Navigate to login screen

**Note**: This is a client-side only operation (stateless JWT)

---

## Token Storage

### iOS (Swift) - Keychain

```swift
import Security

class KeychainManager {
    static let shared = KeychainManager()

    private let service = "com.yourapp.resadmin"
    private let tokenKey = "jwt_token"

    func saveToken(_ token: String) -> Bool {
        let data = token.data(using: .utf8)!

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: tokenKey,
            kSecValueData as String: data
        ]

        SecItemDelete(query as CFDictionary) // Delete existing

        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }

    func getToken() -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: tokenKey,
            kSecReturnData as String: true
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let token = String(data: data, encoding: .utf8) else {
            return nil
        }

        return token
    }

    func deleteToken() -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: tokenKey
        ]

        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess
    }
}
```

### Android (Kotlin) - EncryptedSharedPreferences

```kotlin
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val TOKEN_KEY = "jwt_token"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun deleteToken() {
        sharedPreferences.edit()
            .remove(TOKEN_KEY)
            .apply()
    }

    fun hasToken(): Boolean {
        return sharedPreferences.contains(TOKEN_KEY)
    }
}
```

### Flutter - flutter_secure_storage

```dart
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SecureTokenManager {
  static const _storage = FlutterSecureStorage();
  static const _tokenKey = 'jwt_token';

  static Future<void> saveToken(String token) async {
    await _storage.write(key: _tokenKey, value: token);
  }

  static Future<String?> getToken() async {
    return await _storage.read(key: _tokenKey);
  }

  static Future<void> deleteToken() async {
    await _storage.delete(key: _tokenKey);
  }

  static Future<bool> hasToken() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }
}
```

### React Native - react-native-keychain

```javascript
import * as Keychain from 'react-native-keychain';

export const SecureTokenManager = {
  saveToken: async (token) => {
    try {
      await Keychain.setGenericPassword('jwt_token', token, {
        service: 'com.yourapp.resadmin',
      });
      return true;
    } catch (error) {
      console.error('Error saving token:', error);
      return false;
    }
  },

  getToken: async () => {
    try {
      const credentials = await Keychain.getGenericPassword({
        service: 'com.yourapp.resadmin',
      });
      if (credentials) {
        return credentials.password;
      }
      return null;
    } catch (error) {
      console.error('Error getting token:', error);
      return null;
    }
  },

  deleteToken: async () => {
    try {
      await Keychain.resetGenericPassword({
        service: 'com.yourapp.resadmin',
      });
      return true;
    } catch (error) {
      console.error('Error deleting token:', error);
      return false;
    }
  },

  hasToken: async () => {
    const token = await SecureTokenManager.getToken();
    return token !== null && token.length > 0;
  },
};
```

---

## Token Management

### Token Expiration

JWT tokens expire after **24 hours**. You need to handle expiration gracefully:

**Option 1: Proactive Check** (Recommended)
- Decode JWT and check `exp` claim
- If token expires within next hour, prompt user to re-login
- Show warning before expiration

**Option 2: Reactive Handling**
- Wait for 401 error from API
- Clear token and redirect to login
- Show "Session expired" message

### Decoding JWT Token

**iOS (Swift)**:
```swift
import Foundation

func decodeJWT(_ token: String) -> [String: Any]? {
    let segments = token.components(separatedBy: ".")
    guard segments.count > 1 else { return nil }

    let payloadSegment = segments[1]
    var base64 = payloadSegment
        .replacingOccurrences(of: "-", with: "+")
        .replacingOccurrences(of: "_", with: "/")

    let paddingLength = 4 - base64.count % 4
    if paddingLength < 4 {
        base64 += String(repeating: "=", count: paddingLength)
    }

    guard let data = Data(base64Encoded: base64),
          let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
        return nil
    }

    return json
}

func isTokenExpired(_ token: String) -> Bool {
    guard let payload = decodeJWT(token),
          let exp = payload["exp"] as? TimeInterval else {
        return true
    }

    return Date() >= Date(timeIntervalSince1970: exp)
}
```

**Android (Kotlin)**:
```kotlin
import android.util.Base64
import org.json.JSONObject
import java.util.Date

fun decodeJWT(token: String): JSONObject? {
    try {
        val parts = token.split(".")
        if (parts.size < 2) return null

        val payload = String(
            Base64.decode(parts[1], Base64.URL_SAFE),
            Charsets.UTF_8
        )
        return JSONObject(payload)
    } catch (e: Exception) {
        return null
    }
}

fun isTokenExpired(token: String): Boolean {
    val payload = decodeJWT(token) ?: return true
    val exp = payload.optLong("exp", 0) * 1000
    return Date().time >= exp
}
```

### Token Refresh Strategy

Since there's no refresh token endpoint, implement these strategies:

**Strategy 1: Re-authentication Before Expiration**
```
If token expires in < 1 hour:
  - Show subtle notification
  - Prompt user to re-login
  - Allow continued use until expiration
```

**Strategy 2: Background Re-authentication**
```
If user has saved credentials (with permission):
  - Automatically login in background
  - Replace old token with new token
  - Show success notification
```

**Strategy 3: Session Extension**
```
If user is actively using app:
  - Prompt for re-authentication
  - Use same screen overlay
  - Maintain app state
```

---

## Error Handling

### Common Authentication Errors

#### 401 Unauthorized - Invalid Credentials

**Scenario**: Wrong username or password

**Response**:
```json
{
  "success": false,
  "message": "Invalid credentials",
  "error": "Bad credentials",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

**Handling**:
- Show error message: "Incorrect username or password"
- Clear password field
- Allow user to retry
- Implement rate limiting after multiple failed attempts

#### 401 Unauthorized - Token Expired

**Scenario**: JWT token has expired (> 24 hours old)

**Response**:
```json
{
  "success": false,
  "message": "TOKEN_EXPIRED",
  "error": "JWT token has expired",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

**Handling**:
- Clear stored token
- Show message: "Your session has expired. Please login again."
- Redirect to login screen
- Preserve navigation stack for return

#### 401 Unauthorized - Invalid Token

**Scenario**: Malformed or invalid JWT token

**Response**:
```json
{
  "success": false,
  "message": "INVALID_TOKEN",
  "error": "JWT token is invalid",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

**Handling**:
- Clear stored token
- Redirect to login screen
- Log error for debugging

#### 403 Forbidden - Insufficient Permissions

**Scenario**: User doesn't have required role for endpoint

**Response**:
```json
{
  "success": false,
  "message": "Forbidden",
  "error": "Access denied",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

**Handling**:
- Show error: "You don't have permission to perform this action"
- Hide/disable UI elements that user can't access
- Check user role on app launch and configure UI accordingly

#### Network Errors

**Scenarios**: No internet, server down, timeout

**Handling**:
- Show user-friendly message: "Unable to connect. Please check your internet connection."
- Implement retry button
- Cache data for offline use
- Show offline indicator

---

## Security Best Practices

### DO ✅

1. **Secure Storage**
   - Use Keychain (iOS) or EncryptedSharedPreferences (Android)
   - Never store tokens in plain text
   - Use platform-specific secure storage solutions

2. **Token Transmission**
   - Always use HTTPS in production
   - Implement certificate pinning
   - Validate SSL certificates

3. **Token Handling**
   - Clear token on logout
   - Clear token on 401 errors
   - Implement token expiration checks
   - Handle token lifecycle properly

4. **Password Security**
   - Never log passwords
   - Clear password fields after submission
   - Implement secure password input (masked)
   - Support biometric authentication for re-login

5. **Error Messages**
   - Use generic messages for failed logins
   - Don't reveal if username exists
   - Log detailed errors server-side only

6. **Network Security**
   - Validate SSL certificates
   - Use certificate pinning in production
   - Implement timeout handling
   - Use trusted certificate authorities

### DON'T ❌

1. **Storage**
   - Don't store tokens in UserDefaults/SharedPreferences
   - Don't store tokens in local files
   - Don't commit tokens to version control
   - Don't cache tokens in memory longer than necessary

2. **Logging**
   - Don't log tokens in production
   - Don't log passwords ever
   - Don't log full API responses with tokens
   - Don't use analytics tools that capture sensitive data

3. **Token Usage**
   - Don't send tokens in URL parameters
   - Don't share tokens between apps
   - Don't use tokens after logout
   - Don't ignore certificate validation errors

4. **Code Security**
   - Don't hardcode credentials
   - Don't disable certificate validation
   - Don't use HTTP in production
   - Don't store secrets in code

---

## Code Examples

### Complete Authentication Manager (iOS Swift)

```swift
import Foundation

class AuthenticationManager: ObservableObject {
    static let shared = AuthenticationManager()

    @Published var isAuthenticated = false
    @Published var currentUser: User?

    private let baseURL = "http://localhost:8080/api"
    private let keychainManager = KeychainManager.shared

    init() {
        checkAuthentication()
    }

    func checkAuthentication() {
        guard let token = keychainManager.getToken() else {
            isAuthenticated = false
            return
        }

        if isTokenExpired(token) {
            logout()
            return
        }

        verifyToken()
    }

    func login(username: String, password: String, completion: @escaping (Result<User, Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/auth/login") else {
            completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])))
            return
        }

        let body: [String: String] = [
            "username": username,
            "password": password
        ]

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)

        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            if let error = error {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
                return
            }

            guard let data = data else {
                DispatchQueue.main.async {
                    completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data received"])))
                }
                return
            }

            do {
                let loginResponse = try JSONDecoder().decode(LoginResponse.self, from: data)

                if loginResponse.success, let data = loginResponse.data {
                    // Save token
                    _ = self?.keychainManager.saveToken(data.token)

                    // Update state
                    DispatchQueue.main.async {
                        self?.currentUser = data.user
                        self?.isAuthenticated = true
                        completion(.success(data.user))
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: loginResponse.message])))
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
            }
        }.resume()
    }

    func logout() {
        _ = keychainManager.deleteToken()
        DispatchQueue.main.async {
            self.currentUser = nil
            self.isAuthenticated = false
        }
    }

    func verifyToken() {
        guard let token = keychainManager.getToken(),
              let url = URL(string: "\(baseURL)/auth/info") else {
            logout()
            return
        }

        var request = URLRequest(url: url)
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                DispatchQueue.main.async {
                    self?.logout()
                }
                return
            }

            do {
                let userResponse = try JSONDecoder().decode(UserInfoResponse.self, from: data)
                if userResponse.success, let user = userResponse.data {
                    DispatchQueue.main.async {
                        self?.currentUser = user
                        self?.isAuthenticated = true
                    }
                } else {
                    DispatchQueue.main.async {
                        self?.logout()
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    self?.logout()
                }
            }
        }.resume()
    }

    private func isTokenExpired(_ token: String) -> Bool {
        // Implementation from Token Management section
        guard let payload = decodeJWT(token),
              let exp = payload["exp"] as? TimeInterval else {
            return true
        }
        return Date() >= Date(timeIntervalSince1970: exp)
    }

    private func decodeJWT(_ token: String) -> [String: Any]? {
        // Implementation from Token Management section
        let segments = token.components(separatedBy: ".")
        guard segments.count > 1 else { return nil }

        let payloadSegment = segments[1]
        var base64 = payloadSegment
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")

        let paddingLength = 4 - base64.count % 4
        if paddingLength < 4 {
            base64 += String(repeating: "=", count: paddingLength)
        }

        guard let data = Data(base64Encoded: base64),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return nil
        }

        return json
    }
}

// Models
struct LoginResponse: Codable {
    let success: Bool
    let message: String
    let data: LoginData?
}

struct LoginData: Codable {
    let token: String
    let user: User
}

struct UserInfoResponse: Codable {
    let success: Bool
    let message: String
    let data: User?
}

struct User: Codable {
    let id: Int
    let username: String
    let email: String?
    let fullName: String?
    let role: String
    let enabled: Bool
    let createdAt: String
    let updatedAt: String
}
```

### Complete Authentication Manager (Android Kotlin)

```kotlin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class AuthenticationManager(private val context: Context) {
    private val tokenManager = SecureTokenManager(context)
    private val apiService: AuthApiService

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(AuthApiService::class.java)
        checkAuthentication()
    }

    private fun checkAuthentication() {
        val token = tokenManager.getToken()
        if (token == null) {
            _isAuthenticated.value = false
            return
        }

        if (isTokenExpired(token)) {
            logout()
            return
        }

        // Verify token with server
        GlobalScope.launch {
            verifyToken()
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                tokenManager.saveToken(data.token)
                _currentUser.value = data.user
                _isAuthenticated.value = true
                Result.success(data.user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.deleteToken()
        _currentUser.value = null
        _isAuthenticated.value = false
    }

    private suspend fun verifyToken() {
        try {
            val token = tokenManager.getToken() ?: return
            val response = apiService.getUserInfo("Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                _currentUser.value = response.body()!!.data
                _isAuthenticated.value = true
            } else {
                logout()
            }
        } catch (e: Exception) {
            logout()
        }
    }

    fun getAuthToken(): String? {
        return tokenManager.getToken()
    }

    private fun isTokenExpired(token: String): Boolean {
        val payload = decodeJWT(token) ?: return true
        val exp = payload.optLong("exp", 0) * 1000
        return System.currentTimeMillis() >= exp
    }

    companion object {
        @Volatile
        private var instance: AuthenticationManager? = null

        fun getInstance(context: Context): AuthenticationManager {
            return instance ?: synchronized(this) {
                instance ?: AuthenticationManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

// API Service
interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @GET("auth/info")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<ApiResponse<User>>

    @POST("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Nothing>>
}

// Models
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginData(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String?,
    val fullName: String?,
    val role: String,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val error: String?,
    val timestamp: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
```

---

**Last Updated**: 2025-11-13
**Version**: 1.0.1
