# Payment Management Documentation

## Overview

The Restaurant Administration System now includes comprehensive payment tracking functionality. This feature allows administrators and managers to track whether orders have been paid and what payment method was used.

## Payment Features

### Payment Status
- **isPaid**: Boolean field indicating whether an order has been paid or not
- Default value: `false` (unpaid)
- Can be updated via API endpoints

### Payment Methods

The system supports three payment methods:

1. **CASH_ON_DELIVERY** (COD)
   - Payment is collected when the order is delivered
   - Common for delivery orders
   - Driver collects payment

2. **BANK**
   - Bank transfer or online banking payment
   - Typically pre-paid before order processing
   - Requires confirmation

3. **CARD**
   - Credit or debit card payment
   - Can be processed online or at point of sale
   - Immediate payment confirmation

## Database Schema Changes

### Orders Table

New fields added to the `orders` table:

```sql
is_paid BOOLEAN DEFAULT false NOT NULL
payment_method VARCHAR(20) CHECK (payment_method IN ('CASH_ON_DELIVERY', 'BANK', 'CARD'))
```

Indexes added for performance:
```sql
CREATE INDEX idx_orders_is_paid ON orders(is_paid);
CREATE INDEX idx_orders_payment_method ON orders(payment_method);
```

## API Endpoints

### 1. Update Payment Status

**Endpoint**: `PATCH /api/orders/{id}/payment`

**Required Role**: ADMIN, MANAGER

**Request Body**:
```json
{
  "isPaid": true,
  "paymentMethod": "CARD"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Payment status updated successfully",
  "data": {
    "id": 1,
    "customerName": "John Doe",
    "status": "CONFIRMED",
    "totalPrice": 25.99,
    "isPaid": true,
    "paymentMethod": "CARD",
    ...
  }
}
```

### 2. Mark Order as Paid

**Endpoint**: `POST /api/orders/{id}/mark-paid`

**Query Parameter**: `paymentMethod` (optional)

**Required Role**: ADMIN, MANAGER

**Example**:
```bash
POST /api/orders/1/mark-paid?paymentMethod=CASH_ON_DELIVERY
```

**Response**: Returns updated order with `isPaid: true`

### 3. Mark Order as Unpaid

**Endpoint**: `POST /api/orders/{id}/mark-unpaid`

**Required Role**: ADMIN, MANAGER

**Response**: Returns updated order with `isPaid: false` and `paymentMethod: null`

### 4. Get Paid Orders

**Endpoint**: `GET /api/orders/paid`

**Required Role**: ADMIN, MANAGER

**Response**: List of all paid orders

### 5. Get Unpaid Orders

**Endpoint**: `GET /api/orders/unpaid`

**Required Role**: ADMIN, MANAGER

**Response**: List of all unpaid orders

### 6. Get Orders by Payment Method

**Endpoint**: `GET /api/orders/payment-method/{paymentMethod}`

**Required Role**: ADMIN, MANAGER

**Path Variable**: `paymentMethod` - One of: CASH_ON_DELIVERY, BANK, CARD

**Response**: List of orders with the specified payment method

## Usage Examples

### Creating an Order with Payment Method

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "customerPhone": "555-1234",
    "customerAddress": "123 Main St",
    "paymentMethod": "CARD",
    "totalAmount": 25.99,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 12.99
      }
    ]
  }'
```

### Marking an Order as Paid

```bash
# Simple approach
curl -X POST http://localhost:8080/api/orders/1/mark-paid?paymentMethod=BANK \
  -H "Authorization: Bearer <token>"

# Detailed approach
curl -X PATCH http://localhost:8080/api/orders/1/payment \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "isPaid": true,
    "paymentMethod": "CARD"
  }'
```

### Retrieving Orders by Payment Status

```bash
# Get all paid orders
curl -X GET http://localhost:8080/api/orders/paid \
  -H "Authorization: Bearer <token>"

# Get all unpaid orders
curl -X GET http://localhost:8080/api/orders/unpaid \
  -H "Authorization: Bearer <token>"
```

### Filtering by Payment Method

```bash
# Get all cash on delivery orders
curl -X GET http://localhost:8080/api/orders/payment-method/CASH_ON_DELIVERY \
  -H "Authorization: Bearer <token>"

# Get all card payment orders
curl -X GET http://localhost:8080/api/orders/payment-method/CARD \
  -H "Authorization: Bearer <token>"
```

## Order Response Structure

All order responses now include payment information:

```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerPhone": "555-1234",
  "customerAddress": "123 Main St",
  "notes": "Ring doorbell",
  "customerDetails": "Name: John Doe | Phone: 555-1234 | Address: 123 Main St | Notes: Ring doorbell",
  "status": "CONFIRMED",
  "totalPrice": 25.99,
  "orderType": "DELIVERY",
  "isPaid": true,
  "paymentMethod": "CARD",
  "createdAt": "2024-01-15T10:30:00Z",
  "orderItems": [...],
  "delivery": {...}
}
```

## Business Logic

### Order Creation
- When an order is created, `isPaid` is automatically set to `false`
- `paymentMethod` can be specified during order creation but is optional
- If payment method is provided, it's stored even if order is not yet paid

### Payment Status Updates
- Only ADMIN and MANAGER roles can update payment status
- Payment status changes trigger WebSocket notifications for real-time updates
- When marking an order as unpaid, the payment method is cleared

### Payment Method Tracking
- Payment method can be set independently of payment status
- Useful for pre-selecting payment method before payment is completed
- Required when marking an order as paid (except when using mark-unpaid endpoint)

## Integration with Order Workflow

### Typical Order Flow with Payment

1. **Order Creation**
   - Customer places order
   - Payment method selected: CARD
   - Order created with `isPaid: false`

2. **Order Confirmation**
   - Order status: PENDING → CONFIRMED
   - Payment processed
   - Update: `isPaid: true`

3. **Order Processing**
   - Kitchen prepares order
   - Payment already confirmed
   - Status: CONFIRMED → PREPARING → READY

4. **Delivery/Pickup**
   - Order delivered or picked up
   - Status: COMPLETED

### Cash on Delivery Flow

1. **Order Creation**
   - Payment method: CASH_ON_DELIVERY
   - `isPaid: false`

2. **Order Processing**
   - Order processed normally
   - Payment collected by driver

3. **Delivery Completion**
   - Driver collects payment
   - Update: `isPaid: true`
   - Status: COMPLETED

## WebSocket Notifications

Payment status changes trigger real-time notifications:

```json
{
  "type": "ORDER_UPDATED",
  "message": "Order #123 payment status updated",
  "data": {
    "orderId": 123,
    "isPaid": true,
    "paymentMethod": "CARD"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Security Considerations

- Only ADMIN and MANAGER roles can modify payment status
- All payment operations are logged via WebSocket notifications
- Payment status changes require authentication token
- Payment method is optional to support various business scenarios

## Best Practices

1. **Always specify payment method when marking as paid**
   - Helps with financial tracking and reporting
   - Required for proper record keeping

2. **Verify payment before marking as paid**
   - Ensure payment is confirmed before updating status
   - Prevents order fulfillment issues

3. **Use unpaid orders list for follow-up**
   - Regularly check unpaid orders
   - Follow up with customers for pending payments

4. **Track payment methods for analytics**
   - Use payment method filtering for financial reports
   - Analyze customer payment preferences

## Future Enhancements

Potential future improvements:
- Payment amount tracking (partial payments)
- Payment transaction IDs
- Payment gateway integration
- Refund tracking
- Payment history/audit log
- Payment reminders for unpaid orders

## Migration Notes

### Existing Orders
- Existing orders in the database will automatically have `isPaid: false`
- Payment method will be `null` for existing orders
- No data migration script required
- Database schema update is handled automatically by JPA

### API Compatibility
- All existing endpoints remain functional
- New fields are added to responses (backward compatible)
- Frontend applications should handle null payment method gracefully
