# Order API Response Changes

## Summary
The Order API response has been enhanced to include individual customer fields in addition to the existing `customerDetails` field.

## Changes Made

### OrderDTO Response Structure

Previously, the order response only included:
```json
{
  "id": 1,
  "customerDetails": "Name: John Doe | Phone: 555-1234 | Address: 123 Main St | Notes: Ring doorbell",
  "status": "PENDING",
  "totalPrice": 45.97,
  "orderType": "DELIVERY",
  "createdAt": "2024-01-15T10:30:00",
  "orderItems": [...],
  "delivery": {...}
}
```

Now includes additional parsed fields:
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerPhone": "555-1234",
  "customerAddress": "123 Main St",
  "notes": "Ring doorbell",
  "customerDetails": "Name: John Doe | Phone: 555-1234 | Address: 123 Main St | Notes: Ring doorbell",
  "status": "PENDING",
  "totalPrice": 45.97,
  "orderType": "DELIVERY",
  "createdAt": "2024-01-15T10:30:00",
  "orderItems": [...],
  "delivery": {...}
}
```

## Benefits

1. **Easier Frontend Integration**: Frontend applications can now directly access individual customer fields without parsing the `customerDetails` string.

2. **Backward Compatibility**: The `customerDetails` field is still included for backward compatibility.

3. **Legacy Data Support**: The implementation supports both the new format (pipe-separated with labels) and legacy format (comma-separated) for existing data.

## Affected Endpoints

All endpoints that return order data now include these additional fields:
- `GET /api/orders` - Get all orders (paginated)
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/status/{status}` - Get orders by status
- `GET /api/orders/kitchen` - Get kitchen orders
- `GET /api/orders/delivery` - Get delivery orders
- `GET /api/orders/ready-for-delivery` - Get orders ready for delivery
- `GET /api/orders/today` - Get today's orders
- `GET /api/orders/date-range` - Get orders by date range
- `POST /api/orders` - Create new order (response)
- `PUT /api/orders/{id}` - Update order (response)
- `PATCH /api/orders/{id}/status` - Update order status (response)

## Field Descriptions

| Field | Type | Description | Required in Response |
|-------|------|-------------|---------------------|
| `customerName` | string | Customer's full name | Yes |
| `customerPhone` | string | Customer's phone number | Yes |
| `customerAddress` | string | Customer's delivery address | Optional (null for pickup orders) |
| `notes` | string | Special instructions or notes | Optional (null if not provided) |
| `customerDetails` | string | Combined customer information string | Yes (legacy field) |

## Data Format

### New Format (Current)
Customer details are stored in the format:
```
Name: [name] | Phone: [phone] | Address: [address] | Notes: [notes]
```

Example:
```
Name: John Doe | Phone: 555-1234 | Address: 123 Main St | Notes: Ring doorbell
```

### Legacy Format (Supported)
Old data may be in comma-separated format:
```
[name], [phone], [email]
```

Example:
```
John Doe, 555-1234, john@email.com
```

The parser automatically detects and handles both formats.

## Migration Notes

No database migration is required. The changes are implemented at the DTO/mapping layer and work with the existing database schema.
