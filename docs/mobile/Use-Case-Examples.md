# Use Case Examples

Complete implementation examples for common workflows in the Restaurant Admin System.

## Table of Contents

1. [Complete Authentication Flow](#complete-authentication-flow)
2. [Order Creation and Tracking](#order-creation-and-tracking)
3. [Kitchen Display System](#kitchen-display-system)
4. [Delivery Driver Workflow](#delivery-driver-workflow)
5. [Manager Dashboard](#manager-dashboard)
6. [Real-time Notifications](#real-time-notifications)

---

## Complete Authentication Flow

### Use Case Description

Implement a complete authentication system with login, token storage, automatic re-authentication, and logout.

### Requirements

- Store JWT token securely
- Validate token on app launch
- Handle token expiration
- Provide seamless user experience

---

### Implementation Steps

#### Step 1: Check Authentication on Launch

**iOS (Swift)**:
```swift
@main
struct ResAdminApp: App {
    @StateObject private var authManager = AuthenticationManager.shared

    init() {
        authManager.checkAuthentication()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(authManager)
        }
    }
}
```

**Android (Kotlin)**:
```kotlin
class MainActivity : ComponentActivity() {
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (authRepository.hasToken()) {
                val result = authRepository.getUserInfo()
                if (result.isFailure) {
                    authRepository.logout()
                }
            }
        }

        setContent {
            ResAdminApp(authRepository)
        }
    }
}
```

#### Step 2: Login Screen

**Flutter**:
```dart
class LoginScreen extends StatefulWidget {
  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;
  String? _errorMessage;

  Future<void> _handleLogin() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      await authProvider.login(
        _usernameController.text,
        _passwordController.text,
      );

      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => HomeScreen()),
      );
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Login')),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                'Restaurant Admin',
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              SizedBox(height: 32),
              TextFormField(
                controller: _usernameController,
                decoration: InputDecoration(
                  labelText: 'Username',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter username';
                  }
                  return null;
                },
              ),
              SizedBox(height: 16),
              TextFormField(
                controller: _passwordController,
                decoration: InputDecoration(
                  labelText: 'Password',
                  border: OutlineInputBorder(),
                ),
                obscureText: true,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter password';
                  }
                  if (value.length < 6) {
                    return 'Password must be at least 6 characters';
                  }
                  return null;
                },
              ),
              if (_errorMessage != null)
                Padding(
                  padding: EdgeInsets.only(top: 8),
                  child: Text(
                    _errorMessage!,
                    style: TextStyle(color: Colors.red),
                  ),
                ),
              SizedBox(height: 24),
              ElevatedButton(
                onPressed: _isLoading ? null : _handleLogin,
                child: _isLoading
                    ? CircularProgressIndicator()
                    : Text('Login'),
                style: ElevatedButton.styleFrom(
                  minimumSize: Size(double.infinity, 50),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
}
```

#### Step 3: Handle Token Expiration

**React Native**:
```typescript
import axios, { AxiosError } from 'axios';
import * as Keychain from 'react-native-keychain';
import { Alert } from 'react-native';

// Setup axios interceptor for 401 errors
axios.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      await Keychain.resetGenericPassword();

      Alert.alert(
        'Session Expired',
        'Your session has expired. Please login again.',
        [
          {
            text: 'OK',
            onPress: () => {
              // Navigate to login screen
              // This depends on your navigation setup
            },
          },
        ]
      );
    }
    return Promise.reject(error);
  }
);
```

---

## Order Creation and Tracking

### Use Case Description

Manager creates a new order with multiple items, receives confirmation, and tracks the order status in real-time.

### Workflow

```
1. Manager selects products from menu
2. Adds products to cart with quantities
3. Enters customer information
4. Calculates total price
5. Creates order via API
6. Receives order confirmation
7. Subscribes to order updates via WebSocket
8. Displays order in order list
9. Updates order status in real-time
```

---

### Implementation

#### Step 1: Product Selection Screen

**iOS (SwiftUI)**:
```swift
struct ProductSelectionView: View {
    @StateObject private var viewModel = ProductSelectionViewModel()
    @State private var selectedProducts: [CartItem] = []

    var body: some View {
        NavigationView {
            VStack {
                if viewModel.isLoading {
                    ProgressView()
                } else {
                    List {
                        ForEach(viewModel.products) { product in
                            ProductRow(
                                product: product,
                                quantity: getQuantity(for: product.id),
                                onAdd: { addToCart(product) },
                                onRemove: { removeFromCart(product.id) }
                            )
                        }
                    }
                }

                CartSummaryView(
                    items: selectedProducts,
                    onCheckout: {
                        showCreateOrderSheet = true
                    }
                )
            }
            .navigationTitle("Products")
            .sheet(isPresented: $showCreateOrderSheet) {
                CreateOrderView(items: selectedProducts)
            }
        }
        .onAppear {
            viewModel.loadProducts()
        }
    }

    private func getQuantity(for productId: Int) -> Int {
        selectedProducts.first(where: { $0.product.id == productId })?.quantity ?? 0
    }

    private func addToCart(_ product: Product) {
        if let index = selectedProducts.firstIndex(where: { $0.product.id == product.id }) {
            selectedProducts[index].quantity += 1
        } else {
            selectedProducts.append(CartItem(product: product, quantity: 1))
        }
    }

    private func removeFromCart(_ productId: Int) {
        if let index = selectedProducts.firstIndex(where: { $0.product.id == productId }) {
            if selectedProducts[index].quantity > 1 {
                selectedProducts[index].quantity -= 1
            } else {
                selectedProducts.remove(at: index)
            }
        }
    }
}

class ProductSelectionViewModel: ObservableObject {
    @Published var products: [Product] = []
    @Published var isLoading = false
    @Published var error: String?

    private let apiService = APIService.shared

    func loadProducts() {
        isLoading = true
        Task {
            do {
                let response: APIResponse<[Product]> = try await apiService.request(
                    endpoint: "/products/available"
                )

                await MainActor.run {
                    self.products = response.data ?? []
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.error = error.localizedDescription
                    self.isLoading = false
                }
            }
        }
    }
}
```

#### Step 2: Create Order

**Android (Kotlin)**:
```kotlin
class CreateOrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderCreationState>(OrderCreationState.Idle)
    val orderState: StateFlow<OrderCreationState> = _orderState.asStateFlow()

    fun createOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        orderType: OrderType,
        items: List<CartItem>,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _orderState.value = OrderCreationState.Loading

            // Calculate total
            val totalAmount = items.sumOf { it.product.price * it.quantity.toBigDecimal() }

            val orderItems = items.map { cartItem ->
                CreateOrderItemRequest(
                    productId = cartItem.product.id,
                    quantity = cartItem.quantity,
                    price = cartItem.product.price
                )
            }

            val request = CreateOrderRequest(
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                orderType = orderType,
                totalAmount = totalAmount,
                items = orderItems,
                notes = notes
            )

            val result = orderRepository.createOrder(request)

            _orderState.value = if (result.isSuccess) {
                OrderCreationState.Success(result.getOrNull()!!)
            } else {
                OrderCreationState.Error(result.exceptionOrNull()?.message ?: "Failed to create order")
            }
        }
    }
}

sealed class OrderCreationState {
    object Idle : OrderCreationState()
    object Loading : OrderCreationState()
    data class Success(val order: Order) : OrderCreationState()
    data class Error(val message: String) : OrderCreationState()
}

@Composable
fun CreateOrderScreen(
    viewModel: CreateOrderViewModel,
    items: List<CartItem>,
    onOrderCreated: (Order) -> Unit
) {
    val orderState by viewModel.orderState.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var orderType by remember { mutableStateOf(OrderType.DINE_IN) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(orderState) {
        if (orderState is OrderCreationState.Success) {
            val order = (orderState as OrderCreationState.Success).order
            onOrderCreated(order)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Create Order", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Order type selection
        Text("Order Type", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OrderType.values().forEach { type ->
                FilterChip(
                    selected = orderType == type,
                    onClick = { orderType = type },
                    label = { Text(type.name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (orderType == OrderType.DELIVERY) {
            OutlinedTextField(
                value = customerAddress,
                onValueChange = { customerAddress = it },
                label = { Text("Delivery Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Order summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Order Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.product.name} x${item.quantity}")
                        Text("$${item.product.price * item.quantity.toBigDecimal()}")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$${items.sumOf { it.product.price * it.quantity.toBigDecimal() }}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (orderState is OrderCreationState.Error) {
            Text(
                (orderState as OrderCreationState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.createOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    orderType = orderType,
                    items = items,
                    notes = notes.ifBlank { null }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = orderState !is OrderCreationState.Loading &&
                    customerName.isNotBlank() &&
                    customerPhone.isNotBlank() &&
                    (orderType != OrderType.DELIVERY || customerAddress.isNotBlank())
        ) {
            if (orderState is OrderCreationState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Create Order")
            }
        }
    }
}
```

#### Step 3: Real-time Order Tracking

**Flutter**:
```dart
class OrderTrackingScreen extends StatefulWidget {
  final Order order;

  OrderTrackingScreen({required this.order});

  @override
  _OrderTrackingScreenState createState() => _OrderTrackingScreenState();
}

class _OrderTrackingScreenState extends State<OrderTrackingScreen> {
  late Order _currentOrder;
  late WebSocketManager _wsManager;

  @override
  void initState() {
    super.initState();
    _currentOrder = widget.order;
    _setupWebSocket();
  }

  void _setupWebSocket() {
    _wsManager = WebSocketManager();
    _wsManager.connect();

    // Subscribe to order updates
    _wsManager.subscribeToOrders((message) {
      if (message.type == 'ORDER_STATUS_CHANGED') {
        final content = jsonDecode(message.content);
        if (content['orderId'] == _currentOrder.id) {
          setState(() {
            _currentOrder = _currentOrder.copyWith(
              status: OrderStatus.values.firstWhere(
                (e) => e.toString().split('.').last == content['newStatus']
              )
            );
          });

          // Show notification
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Order status updated to ${content['newStatus']}'),
              duration: Duration(seconds: 3),
            ),
          );
        }
      }
    });
  }

  @override
  void dispose() {
    _wsManager.disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Order #${_currentOrder.id}'),
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Status indicator
            OrderStatusTimeline(
              currentStatus: _currentOrder.status,
              orderType: _currentOrder.orderType,
            ),

            SizedBox(height: 24),

            // Customer details
            Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Customer Details',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    SizedBox(height: 8),
                    Text(_currentOrder.customerDetails),
                  ],
                ),
              ),
            ),

            SizedBox(height: 16),

            // Order items
            Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Order Items',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    SizedBox(height: 8),
                    ..._currentOrder.orderItems.map((item) => ListTile(
                      title: Text(item.product.name),
                      subtitle: Text('x${item.quantity}'),
                      trailing: Text('\$${item.totalPrice}'),
                    )),
                    Divider(),
                    ListTile(
                      title: Text(
                        'Total',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      trailing: Text(
                        '\$${_currentOrder.totalPrice}',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 18,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            SizedBox(height: 16),

            // Delivery info (if applicable)
            if (_currentOrder.delivery != null)
              Card(
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Delivery Information',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      SizedBox(height: 8),
                      Text('Driver: ${_currentOrder.delivery!.driver?.fullName ?? "Pending"}'),
                      Text('Status: ${_currentOrder.delivery!.status}'),
                      Text('Address: ${_currentOrder.delivery!.deliveryAddress}'),
                    ],
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class OrderStatusTimeline extends StatelessWidget {
  final OrderStatus currentStatus;
  final OrderType orderType;

  OrderStatusTimeline({
    required this.currentStatus,
    required this.orderType,
  });

  @override
  Widget build(BuildContext context) {
    final statuses = orderType == OrderType.DELIVERY
        ? [
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.COMPLETED,
          ]
        : [
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.COMPLETED,
          ];

    final currentIndex = statuses.indexOf(currentStatus);

    return Card(
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          children: statuses.asMap().entries.map((entry) {
            final index = entry.key;
            final status = entry.value;
            final isActive = index <= currentIndex;
            final isCurrent = index == currentIndex;

            return Row(
              children: [
                Column(
                  children: [
                    Icon(
                      isCurrent
                          ? Icons.radio_button_checked
                          : (isActive ? Icons.check_circle : Icons.radio_button_unchecked),
                      color: isActive ? Colors.green : Colors.grey,
                    ),
                    if (index < statuses.length - 1)
                      Container(
                        width: 2,
                        height: 40,
                        color: isActive ? Colors.green : Colors.grey,
                      ),
                  ],
                ),
                SizedBox(width: 16),
                Expanded(
                  child: Text(
                    status.toString().split('.').last.replaceAll('_', ' '),
                    style: TextStyle(
                      fontWeight: isCurrent ? FontWeight.bold : FontWeight.normal,
                      color: isActive ? Colors.black : Colors.grey,
                    ),
                  ),
                ),
              ],
            );
          }).toList(),
        ),
      ),
    );
  }
}
```

---

## Kitchen Display System

### Use Case Description

Kitchen staff sees incoming orders in real-time, updates order status as they prepare items.

### Requirements

- Display orders needing preparation
- Real-time updates when new orders arrive
- Update order status (Confirmed → Preparing → Ready)
- Color-coded priority display

---

### Implementation

**React Native**:
```typescript
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  RefreshControl,
} from 'react-native';
import apiService from '../services/apiService';
import WebSocketManager from '../services/webSocketManager';

interface KitchenOrder {
  id: number;
  customerDetails: string;
  status: string;
  orderType: string;
  createdAt: string;
  orderItems: OrderItem[];
  totalPrice: number;
}

export const KitchenDisplayScreen = () => {
  const [orders, setOrders] = useState<KitchenOrder[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [wsConnected, setWsConnected] = useState(false);

  useEffect(() => {
    loadOrders();
    setupWebSocket();

    return () => {
      WebSocketManager.disconnect();
    };
  }, []);

  const loadOrders = async () => {
    try {
      const response = await apiService.get<ApiResponse<KitchenOrder[]>>(
        '/orders/kitchen'
      );
      if (response.success) {
        setOrders(response.data || []);
      }
    } catch (error) {
      console.error('Error loading orders:', error);
    }
  };

  const setupWebSocket = () => {
    WebSocketManager.connect();

    WebSocketManager.client.onConnect = () => {
      setWsConnected(true);

      // Subscribe to kitchen orders
      WebSocketManager.client.subscribe('/topic/kitchen', (message) => {
        const wsMessage = JSON.parse(message.body);
        handleWebSocketMessage(wsMessage);
      });

      // Subscribe to order updates
      WebSocketManager.client.subscribe('/topic/orders', (message) => {
        const wsMessage = JSON.parse(message.body);
        handleWebSocketMessage(wsMessage);
      });
    };

    WebSocketManager.client.onDisconnect = () => {
      setWsConnected(false);
    };
  };

  const handleWebSocketMessage = (message: WebSocketMessage) => {
    switch (message.type) {
      case 'ORDER_CREATED':
        const newOrderData = JSON.parse(message.content);
        loadOrders(); // Reload orders
        // Show notification
        break;

      case 'ORDER_STATUS_CHANGED':
        const statusData = JSON.parse(message.content);
        updateOrderStatus(statusData.orderId, statusData.newStatus);
        break;
    }
  };

  const updateOrderStatus = (orderId: number, newStatus: string) => {
    setOrders((prevOrders) =>
      prevOrders.map((order) =>
        order.id === orderId ? { ...order, status: newStatus } : order
      )
    );
  };

  const handleStatusUpdate = async (orderId: number, newStatus: string) => {
    try {
      const response = await apiService.patch(`/orders/${orderId}/status`, {
        status: newStatus,
      });

      if (response.success) {
        updateOrderStatus(orderId, newStatus);
      }
    } catch (error) {
      console.error('Error updating status:', error);
      Alert.alert('Error', 'Failed to update order status');
    }
  };

  const getOrderColor = (status: string): string => {
    switch (status) {
      case 'CONFIRMED':
        return '#FFF3CD'; // Yellow
      case 'PREPARING':
        return '#D1ECF1'; // Blue
      case 'READY_FOR_PICKUP':
      case 'READY_FOR_DELIVERY':
        return '#D4EDDA'; // Green
      default:
        return '#F8F9FA'; // Gray
    }
  };

  const getElapsedTime = (createdAt: string): string => {
    const created = new Date(createdAt);
    const now = new Date();
    const elapsed = Math.floor((now.getTime() - created.getTime()) / 60000);
    return `${elapsed} min ago`;
  };

  const renderOrderCard = ({ item: order }: { item: KitchenOrder }) => (
    <View
      style={[
        styles.orderCard,
        { backgroundColor: getOrderColor(order.status) },
      ]}
    >
      <View style={styles.orderHeader}>
        <Text style={styles.orderId}>Order #{order.id}</Text>
        <Text style={styles.orderTime}>{getElapsedTime(order.createdAt)}</Text>
      </View>

      <Text style={styles.orderType}>
        {order.orderType} - {order.customerDetails.split(',')[0]}
      </Text>

      <View style={styles.itemsList}>
        {order.orderItems.map((item, index) => (
          <View key={index} style={styles.itemRow}>
            <Text style={styles.itemQuantity}>{item.quantity}x</Text>
            <Text style={styles.itemName}>{item.product.name}</Text>
          </View>
        ))}
      </View>

      <View style={styles.actionButtons}>
        {order.status === 'CONFIRMED' && (
          <TouchableOpacity
            style={[styles.button, styles.startButton]}
            onPress={() => handleStatusUpdate(order.id, 'PREPARING')}
          >
            <Text style={styles.buttonText}>Start Preparing</Text>
          </TouchableOpacity>
        )}

        {order.status === 'PREPARING' && (
          <TouchableOpacity
            style={[styles.button, styles.readyButton]}
            onPress={() =>
              handleStatusUpdate(
                order.id,
                order.orderType === 'DELIVERY'
                  ? 'READY_FOR_DELIVERY'
                  : 'READY_FOR_PICKUP'
              )
            }
          >
            <Text style={styles.buttonText}>Mark Ready</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Kitchen Display</Text>
        <View
          style={[
            styles.wsIndicator,
            { backgroundColor: wsConnected ? '#28a745' : '#dc3545' },
          ]}
        />
      </View>

      <FlatList
        data={orders}
        renderItem={renderOrderCard}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.listContainer}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={loadOrders} />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>No orders in kitchen queue</Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  wsIndicator: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  listContainer: {
    padding: 16,
  },
  orderCard: {
    padding: 16,
    borderRadius: 8,
    marginBottom: 16,
    elevation: 2,
  },
  orderHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  orderId: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  orderTime: {
    fontSize: 14,
    color: '#666',
  },
  orderType: {
    fontSize: 16,
    marginBottom: 12,
  },
  itemsList: {
    marginBottom: 12,
  },
  itemRow: {
    flexDirection: 'row',
    paddingVertical: 4,
  },
  itemQuantity: {
    width: 40,
    fontWeight: 'bold',
  },
  itemName: {
    flex: 1,
  },
  actionButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  button: {
    flex: 1,
    padding: 12,
    borderRadius: 6,
    alignItems: 'center',
  },
  startButton: {
    backgroundColor: '#007bff',
  },
  readyButton: {
    backgroundColor: '#28a745',
  },
  buttonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
  emptyContainer: {
    padding: 32,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
  },
});
```

---

## Delivery Driver Workflow

### Use Case Description

Delivery driver receives new delivery assignments, updates delivery status, and marks deliveries as completed.

### Workflow

```
1. Driver logs in
2. Views assigned deliveries
3. Receives new delivery notification via WebSocket
4. Reviews delivery details (address, notes, order items)
5. Starts delivery (status: OUT_FOR_DELIVERY)
6. Navigates to customer location
7. Marks delivery as DELIVERED upon completion
8. System records completion timestamp
```

---

### Implementation Summary

**Key Features**:
- List view of assigned deliveries
- WebSocket notifications for new assignments
- Status update buttons
- Delivery details with customer info
- Navigation integration (Google Maps/Apple Maps)

**Implementation Notes**:
- Filter deliveries by current driver ID
- Subscribe to `/topic/delivery-staff` for assignments
- Subscribe to `/user/{username}/queue/notifications` for personal messages
- Include map integration for navigation
- Show delivery notes prominently
- Provide quick status update buttons

---

## Manager Dashboard

### Use Case Description

Manager monitors overall restaurant operations including active orders, deliveries, and statistics.

### Features

1. **Today's Statistics**:
   - Total orders
   - Total revenue
   - Orders by status
   - Active deliveries

2. **Active Orders List**:
   - Sortable by status
   - Filterable by order type
   - Real-time updates

3. **Delivery Management**:
   - Assign deliveries to drivers
   - Monitor delivery status
   - View available drivers

4. **Real-time Updates**:
   - WebSocket notifications
   - Auto-refresh statistics
   - Status change alerts

---

## Real-time Notifications

### Use Case Description

Implement system-wide notifications for important events using WebSocket.

### Implementation

**Notification Types**:
1. Order created → Kitchen staff
2. Order ready → Delivery staff / Customer
3. Delivery assigned → Driver
4. Status changes → All relevant parties
5. System alerts → All users

**Best Practices**:
- Show in-app banners for non-critical notifications
- Use push notifications for critical updates
- Allow users to dismiss notifications
- Store notification history
- Provide sound/vibration options

---

**Last Updated**: 2025-11-13
**Version**: 1.0.1
