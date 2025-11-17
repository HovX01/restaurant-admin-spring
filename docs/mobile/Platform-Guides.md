# Platform-Specific Implementation Guides

Step-by-step guides for implementing the Restaurant Admin API in different mobile platforms.

## Table of Contents

1. [iOS (Swift/SwiftUI)](#ios-swiftswiftui)
2. [Android (Kotlin)](#android-kotlin)
3. [Flutter](#flutter)
4. [React Native](#react-native)

---

## iOS (Swift/SwiftUI)

### Project Setup

**Requirements**:
- Xcode 14+
- iOS 15+
- Swift 5.5+

**Dependencies** (Package.swift or SPM):
```swift
dependencies: [
    .package(url: "https://github.com/Alamofire/Alamofire.git", from: "5.8.0"),
    .package(url: "https://github.com/daltoniam/Starscream.git", from: "4.0.0")
]
```

---

### Project Structure

```
ResAdminApp/
├── App/
│   ├── ResAdminApp.swift
│   └── ContentView.swift
├── Models/
│   ├── User.swift
│   ├── Order.swift
│   ├── Product.swift
│   ├── Category.swift
│   └── Delivery.swift
├── Services/
│   ├── APIService.swift
│   ├── AuthenticationManager.swift
│   ├── WebSocketManager.swift
│   └── KeychainManager.swift
├── ViewModels/
│   ├── LoginViewModel.swift
│   ├── OrderListViewModel.swift
│   └── DeliveryViewModel.swift
├── Views/
│   ├── Auth/
│   │   ├── LoginView.swift
│   │   └── RegisterView.swift
│   ├── Orders/
│   │   ├── OrderListView.swift
│   │   └── OrderDetailView.swift
│   └── Deliveries/
│       ├── DeliveryListView.swift
│       └── DeliveryDetailView.swift
└── Utilities/
    ├── Constants.swift
    └── Extensions.swift
```

---

### Core Implementation

#### 1. Configuration (Constants.swift)

```swift
import Foundation

struct APIConfig {
    static let baseURL = "http://localhost:8080/api"
    static let wsURL = "ws://localhost:8080/ws"
    static let timeout: TimeInterval = 30
}

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case patch = "PATCH"
    case delete = "DELETE"
}
```

#### 2. Models (User.swift)

```swift
import Foundation

struct User: Codable, Identifiable {
    let id: Int
    let username: String
    let email: String?
    let fullName: String?
    let role: UserRole
    let enabled: Bool
    let createdAt: Date
    let updatedAt: Date
}

enum UserRole: String, Codable {
    case admin = "ADMIN"
    case manager = "MANAGER"
    case kitchenStaff = "KITCHEN_STAFF"
    case deliveryStaff = "DELIVERY_STAFF"
}

// API Response Wrapper
struct APIResponse<T: Codable>: Codable {
    let success: Bool
    let message: String
    let data: T?
    let error: String?
    let timestamp: Date
}

// Login Response
struct LoginData: Codable {
    let token: String
    let user: User
}
```

#### 3. API Service (APIService.swift)

```swift
import Foundation

class APIService {
    static let shared = APIService()

    private let session: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = APIConfig.timeout
        session = URLSession(configuration: config)

        decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601

        encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
    }

    func request<T: Codable>(
        endpoint: String,
        method: HTTPMethod = .get,
        body: Encodable? = nil,
        authenticated: Bool = true
    ) async throws -> T {
        guard let url = URL(string: "\(APIConfig.baseURL)\(endpoint)") else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        // Add authentication token if required
        if authenticated, let token = KeychainManager.shared.getToken() {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        // Add request body if provided
        if let body = body {
            request.httpBody = try encoder.encode(body)
        }

        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw APIError.serverError(statusCode: httpResponse.statusCode)
        }

        return try decoder.decode(T.self, from: data)
    }
}

enum APIError: LocalizedError {
    case invalidURL
    case invalidResponse
    case serverError(statusCode: Int)
    case decodingError

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .invalidResponse:
            return "Invalid response from server"
        case .serverError(let code):
            return "Server error: \(code)"
        case .decodingError:
            return "Failed to decode response"
        }
    }
}
```

#### 4. Authentication (AuthenticationManager.swift)

```swift
import Foundation
import Combine

class AuthenticationManager: ObservableObject {
    static let shared = AuthenticationManager()

    @Published var isAuthenticated = false
    @Published var currentUser: User?

    private let keychainManager = KeychainManager.shared
    private let apiService = APIService.shared

    init() {
        checkAuthentication()
    }

    func checkAuthentication() {
        guard let token = keychainManager.getToken(),
              !isTokenExpired(token) else {
            logout()
            return
        }

        Task {
            await verifyToken()
        }
    }

    func login(username: String, password: String) async throws {
        let request = ["username": username, "password": password]

        let response: APIResponse<LoginData> = try await apiService.request(
            endpoint: "/auth/login",
            method: .post,
            body: request,
            authenticated: false
        )

        guard response.success, let data = response.data else {
            throw AuthError.loginFailed(message: response.message)
        }

        // Save token
        _ = keychainManager.saveToken(data.token)

        // Update state
        await MainActor.run {
            self.currentUser = data.user
            self.isAuthenticated = true
        }
    }

    func logout() {
        _ = keychainManager.deleteToken()
        DispatchQueue.main.async {
            self.currentUser = nil
            self.isAuthenticated = false
        }
    }

    private func verifyToken() async {
        do {
            let response: APIResponse<User> = try await apiService.request(
                endpoint: "/auth/info"
            )

            if response.success, let user = response.data {
                await MainActor.run {
                    self.currentUser = user
                    self.isAuthenticated = true
                }
            } else {
                logout()
            }
        } catch {
            logout()
        }
    }

    private func isTokenExpired(_ token: String) -> Bool {
        // Decode JWT and check expiration
        let segments = token.components(separatedBy: ".")
        guard segments.count > 1 else { return true }

        let payloadSegment = segments[1]
        var base64 = payloadSegment
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")

        let paddingLength = 4 - base64.count % 4
        if paddingLength < 4 {
            base64 += String(repeating: "=", count: paddingLength)
        }

        guard let data = Data(base64Encoded: base64),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let exp = json["exp"] as? TimeInterval else {
            return true
        }

        return Date() >= Date(timeIntervalSince1970: exp)
    }
}

enum AuthError: LocalizedError {
    case loginFailed(message: String)
    case tokenExpired

    var errorDescription: String? {
        switch self {
        case .loginFailed(let message):
            return message
        case .tokenExpired:
            return "Your session has expired. Please login again."
        }
    }
}
```

#### 5. UI Implementation (LoginView.swift)

```swift
import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel = LoginViewModel()
    @EnvironmentObject var authManager: AuthenticationManager

    var body: some View {
        VStack(spacing: 20) {
            Text("Restaurant Admin")
                .font(.largeTitle)
                .fontWeight(.bold)

            TextField("Username", text: $viewModel.username)
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)
                .padding(.horizontal)

            SecureField("Password", text: $viewModel.password)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal)

            if let error = viewModel.error {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }

            Button(action: {
                Task {
                    await viewModel.login()
                }
            }) {
                if viewModel.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("Login")
                        .fontWeight(.semibold)
                }
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(10)
            .padding(.horizontal)
            .disabled(viewModel.isLoading)
        }
        .padding()
    }
}

class LoginViewModel: ObservableObject {
    @Published var username = ""
    @Published var password = ""
    @Published var isLoading = false
    @Published var error: String?

    func login() async {
        guard !username.isEmpty, !password.isEmpty else {
            error = "Please enter username and password"
            return
        }

        await MainActor.run {
            isLoading = true
            error = nil
        }

        do {
            try await AuthenticationManager.shared.login(
                username: username,
                password: password
            )
        } catch {
            await MainActor.run {
                self.error = error.localizedDescription
            }
        }

        await MainActor.run {
            isLoading = false
        }
    }
}
```

#### 6. Main App (ResAdminApp.swift)

```swift
import SwiftUI

@main
struct ResAdminApp: App {
    @StateObject private var authManager = AuthenticationManager.shared

    var body: some Scene {
        WindowGroup {
            if authManager.isAuthenticated {
                MainTabView()
                    .environmentObject(authManager)
            } else {
                LoginView()
                    .environmentObject(authManager)
            }
        }
    }
}

struct MainTabView: View {
    var body: some View {
        TabView {
            OrderListView()
                .tabItem {
                    Label("Orders", systemImage: "list.bullet")
                }

            DeliveryListView()
                .tabItem {
                    Label("Deliveries", systemImage: "shippingbox")
                }

            ProfileView()
                .tabItem {
                    Label("Profile", systemImage: "person")
                }
        }
    }
}
```

---

## Android (Kotlin)

### Project Setup

**Requirements**:
- Android Studio Hedgehog+
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34
- Kotlin 1.9+

**Dependencies** (build.gradle.kts):
```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WebSocket
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
}
```

---

### Project Structure

```
app/src/main/java/com/yourapp/resadmin/
├── MainActivity.kt
├── ResAdminApplication.kt
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Order.kt
│   │   ├── Product.kt
│   │   └── Delivery.kt
│   ├── api/
│   │   ├── ApiService.kt
│   │   ├── AuthApi.kt
│   │   ├── OrderApi.kt
│   │   └── DeliveryApi.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── OrderRepository.kt
│   │   └── DeliveryRepository.kt
│   └── local/
│       └── SecureTokenManager.kt
├── ui/
│   ├── theme/
│   │   └── Theme.kt
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   ├── orders/
│   │   ├── OrderListScreen.kt
│   │   └── OrderListViewModel.kt
│   └── deliveries/
│       ├── DeliveryListScreen.kt
│       └── DeliveryListViewModel.kt
├── network/
│   ├── AuthInterceptor.kt
│   └── NetworkModule.kt
├── websocket/
│   └── WebSocketManager.kt
└── utils/
    └── Constants.kt
```

---

### Core Implementation

#### 1. Configuration (Constants.kt)

```kotlin
package com.yourapp.resadmin.utils

object Constants {
    const val BASE_URL = "http://10.0.2.2:8080/api/"  // Use 10.0.2.2 for emulator
    const val WS_URL = "ws://10.0.2.2:8080/ws"
    const val TIMEOUT = 30L
}
```

#### 2. Models (User.kt)

```kotlin
package com.yourapp.resadmin.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    val username: String,
    val email: String?,
    val fullName: String?,
    val role: UserRole,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)

enum class UserRole {
    @SerializedName("ADMIN")
    ADMIN,
    @SerializedName("MANAGER")
    MANAGER,
    @SerializedName("KITCHEN_STAFF")
    KITCHEN_STAFF,
    @SerializedName("DELIVERY_STAFF")
    DELIVERY_STAFF
}

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val error: String?,
    val timestamp: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginData(
    val token: String,
    val user: User
)
```

#### 3. API Service (AuthApi.kt)

```kotlin
package com.yourapp.resadmin.data.api

import com.yourapp.resadmin.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @GET("auth/info")
    suspend fun getUserInfo(): Response<ApiResponse<User>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Nothing>>
}

interface OrderApi {
    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("status") status: String? = null
    ): Response<ApiResponse<PagedResponse<Order>>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<ApiResponse<Order>>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<Order>>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<ApiResponse<Order>>
}
```

#### 4. Network Module (NetworkModule.kt)

```kotlin
package com.yourapp.resadmin.network

import com.yourapp.resadmin.data.api.AuthApi
import com.yourapp.resadmin.data.api.OrderApi
import com.yourapp.resadmin.data.local.SecureTokenManager
import com.yourapp.resadmin.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun authInterceptor(tokenManager: SecureTokenManager) = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    fun provideOkHttpClient(tokenManager: SecureTokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor(tokenManager))
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    fun provideOrderApi(retrofit: Retrofit): OrderApi {
        return retrofit.create(OrderApi::class.java)
    }
}
```

#### 5. Repository (AuthRepository.kt)

```kotlin
package com.yourapp.resadmin.data.repository

import com.yourapp.resadmin.data.api.AuthApi
import com.yourapp.resadmin.data.local.SecureTokenManager
import com.yourapp.resadmin.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: SecureTokenManager
) {
    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(LoginRequest(username, password))

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                tokenManager.saveToken(data.token)
                Result.success(data.user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.getUserInfo()

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to get user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.deleteToken()
    }

    fun hasToken(): Boolean = tokenManager.hasToken()
}
```

#### 6. ViewModel (LoginViewModel.kt)

```kotlin
package com.yourapp.resadmin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.resadmin.data.model.User
import com.yourapp.resadmin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = authRepository.login(username, password)

            _uiState.value = if (result.isSuccess) {
                LoginUiState.Success(result.getOrNull()!!)
            } else {
                LoginUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

#### 7. UI (LoginScreen.kt) - Jetpack Compose

```kotlin
package com.yourapp.resadmin.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Restaurant Admin",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (uiState is LoginUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
    }
}
```

---

## Flutter

### Project Setup

**pubspec.yaml**:
```yaml
name: resadmin_app
description: Restaurant Admin Mobile App

dependencies:
  flutter:
    sdk: flutter

  # HTTP
  dio: ^5.4.0

  # WebSocket
  stomp_dart_client: ^1.0.0

  # Storage
  flutter_secure_storage: ^9.0.0
  shared_preferences: ^2.2.2

  # State Management
  provider: ^6.1.1

  # UI
  flutter_svg: ^2.0.9
  cached_network_image: ^3.3.1

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.0
```

### Core Implementation

#### config.dart

```dart
class ApiConfig {
  static const String baseUrl = 'http://10.0.2.2:8080/api';
  static const String wsUrl = 'ws://10.0.2.2:8080/ws';
  static const Duration timeout = Duration(seconds: 30);
}
```

#### user.dart (Model)

```dart
class User {
  final int id;
  final String username;
  final String? email;
  final String? fullName;
  final UserRole role;
  final bool enabled;
  final DateTime createdAt;
  final DateTime updatedAt;

  User({
    required this.id,
    required this.username,
    this.email,
    this.fullName,
    required this.role,
    required this.enabled,
    required this.createdAt,
    required this.updatedAt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      username: json['username'],
      email: json['email'],
      fullName: json['fullName'],
      role: UserRole.values.firstWhere(
        (e) => e.toString() == 'UserRole.${json['role']}'
      ),
      enabled: json['enabled'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }
}

enum UserRole { ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF }
```

#### api_service.dart

```dart
import 'package:dio/dio.dart';

class ApiService {
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  final Dio dio = Dio(BaseOptions(
    baseUrl: ApiConfig.baseUrl,
    connectTimeout: ApiConfig.timeout,
    receiveTimeout: ApiConfig.timeout,
    headers: {'Content-Type': 'application/json'},
  ));

  Future<void> setAuthToken(String token) async {
    dio.options.headers['Authorization'] = 'Bearer $token';
  }

  Future<T> request<T>({
    required String path,
    required String method,
    Map<String, dynamic>? data,
    Map<String, dynamic>? queryParameters,
  }) async {
    try {
      Response response;

      switch (method.toUpperCase()) {
        case 'GET':
          response = await dio.get(path, queryParameters: queryParameters);
          break;
        case 'POST':
          response = await dio.post(path, data: data);
          break;
        case 'PUT':
          response = await dio.put(path, data: data);
          break;
        case 'PATCH':
          response = await dio.patch(path, data: data);
          break;
        case 'DELETE':
          response = await dio.delete(path);
          break;
        default:
          throw Exception('Unsupported HTTP method: $method');
      }

      return response.data;
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Exception _handleError(DioException error) {
    if (error.response != null) {
      return Exception(error.response!.data['message'] ?? 'Server error');
    } else {
      return Exception('Network error: ${error.message}');
    }
  }
}
```

#### auth_provider.dart

```dart
import 'package:flutter/foundation.dart';

class AuthProvider with ChangeNotifier {
  User? _currentUser;
  bool _isAuthenticated = false;

  User? get currentUser => _currentUser;
  bool get isAuthenticated => _isAuthenticated;

  final ApiService _apiService = ApiService();
  final SecureTokenManager _tokenManager = SecureTokenManager();

  Future<void> login(String username, String password) async {
    try {
      final response = await _apiService.request<Map<String, dynamic>>(
        path: '/auth/login',
        method: 'POST',
        data: {'username': username, 'password': password},
      );

      if (response['success']) {
        final token = response['data']['token'];
        final user = User.fromJson(response['data']['user']);

        await _tokenManager.saveToken(token);
        await _apiService.setAuthToken(token);

        _currentUser = user;
        _isAuthenticated = true;
        notifyListeners();
      } else {
        throw Exception(response['message']);
      }
    } catch (e) {
      rethrow;
    }
  }

  Future<void> logout() async {
    await _tokenManager.deleteToken();
    _currentUser = null;
    _isAuthenticated = false;
    notifyListeners();
  }

  Future<void> checkAuthentication() async {
    final token = await _tokenManager.getToken();
    if (token == null) {
      _isAuthenticated = false;
      return;
    }

    try {
      await _apiService.setAuthToken(token);
      final response = await _apiService.request<Map<String, dynamic>>(
        path: '/auth/info',
        method: 'GET',
      );

      if (response['success']) {
        _currentUser = User.fromJson(response['data']);
        _isAuthenticated = true;
        notifyListeners();
      } else {
        await logout();
      }
    } catch (e) {
      await logout();
    }
  }
}
```

---

## React Native

### Project Setup

```bash
npx react-native init ResAdminApp --template react-native-template-typescript
cd ResAdminApp
npm install axios @stomp/stompjs react-native-keychain @react-navigation/native @react-navigation/native-stack
```

**package.json** dependencies:
```json
{
  "dependencies": {
    "react": "18.2.0",
    "react-native": "0.73.0",
    "axios": "^1.6.5",
    "@stomp/stompjs": "^7.0.0",
    "sockjs-client": "^1.6.1",
    "react-native-keychain": "^8.1.2",
    "@react-navigation/native": "^6.1.9",
    "@react-navigation/native-stack": "^6.9.17",
    "react-native-screens": "^3.29.0",
    "react-native-safe-area-context": "^4.8.2"
  }
}
```

### Core Implementation

#### config.ts

```typescript
export const API_CONFIG = {
  baseURL: 'http://localhost:8080/api',
  wsURL: 'ws://localhost:8080/ws',
  timeout: 30000,
};
```

#### types.ts

```typescript
export interface User {
  id: number;
  username: string;
  email?: string;
  fullName?: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  MANAGER = 'MANAGER',
  KITCHEN_STAFF = 'KITCHEN_STAFF',
  DELIVERY_STAFF = 'DELIVERY_STAFF',
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
  timestamp: string;
}
```

#### apiService.ts

```typescript
import axios, { AxiosInstance } from 'axios';
import { API_CONFIG } from './config';
import * as Keychain from 'react-native-keychain';

class ApiService {
  private axiosInstance: AxiosInstance;

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: API_CONFIG.baseURL,
      timeout: API_CONFIG.timeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.axiosInstance.interceptors.request.use(
      async (config) => {
        const credentials = await Keychain.getGenericPassword();
        if (credentials) {
          config.headers.Authorization = `Bearer ${credentials.password}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );
  }

  async get<T>(url: string, params?: any): Promise<T> {
    const response = await this.axiosInstance.get<T>(url, { params });
    return response.data;
  }

  async post<T>(url: string, data: any): Promise<T> {
    const response = await this.axiosInstance.post<T>(url, data);
    return response.data;
  }

  async put<T>(url: string, data: any): Promise<T> {
    const response = await this.axiosInstance.put<T>(url, data);
    return response.data;
  }

  async patch<T>(url: string, data: any): Promise<T> {
    const response = await this.axiosInstance.patch<T>(url, data);
    return response.data;
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.axiosInstance.delete<T>(url);
    return response.data;
  }
}

export default new ApiService();
```

#### LoginScreen.tsx

```typescript
import React, { useState } from 'react';
import { View, TextInput, Button, Text, StyleSheet, ActivityIndicator } from 'react-native';
import apiService from '../services/apiService';
import * as Keychain from 'react-native-keychain';

export const LoginScreen = ({ navigation }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async () => {
    if (!username || !password) {
      setError('Please enter username and password');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await apiService.post('/auth/login', {
        username,
        password,
      });

      if (response.success) {
        await Keychain.setGenericPassword('jwt_token', response.data.token);
        navigation.replace('Main');
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Restaurant Admin</Text>

      <TextInput
        style={styles.input}
        placeholder="Username"
        value={username}
        onChangeText={setUsername}
        autoCapitalize="none"
      />

      <TextInput
        style={styles.input}
        placeholder="Password"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Button
        title={loading ? 'Logging in...' : 'Login'}
        onPress={handleLogin}
        disabled={loading}
      />

      {loading && <ActivityIndicator style={styles.loader} />}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 5,
    padding: 10,
    marginBottom: 10,
  },
  error: {
    color: 'red',
    marginBottom: 10,
  },
  loader: {
    marginTop: 20,
  },
});
```

---

**Last Updated**: 2025-11-13
**Version**: 1.0.1
