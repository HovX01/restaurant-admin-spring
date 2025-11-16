# Map Integration Guide for Order Delivery

## Overview
The ResAdmin API now supports storing and managing delivery coordinates (latitude/longitude) for delivery orders. This allows frontend applications to integrate with map services like Google Maps or Leaflet to visualize and manage delivery locations.

## API Changes

### 1. Create Order Endpoint
**Endpoint:** `POST /api/orders`

#### Updated Request Body
```json
{
    "customerName": "John Doe",
    "customerPhone": "555-1234",
    "customerAddress": "123 Main St, Springfield",
    "latitude": 39.7817,        // NEW: Optional latitude
    "longitude": -89.6501,       // NEW: Optional longitude
    "notes": "Ring doorbell twice",
    "totalAmount": 45.99,
    "orderType": "DELIVERY",
    "items": [
        {
            "productId": 1,
            "quantity": 2
        }
    ]
}
```

### 2. Order Response
When fetching orders, delivery orders will now include coordinate information:

```json
{
    "id": 123,
    "customerName": "John Doe",
    "customerPhone": "555-1234",
    "customerAddress": "123 Main St, Springfield",
    "orderType": "DELIVERY",
    "status": "PENDING",
    "delivery": {
        "id": 45,
        "deliveryAddress": "123 Main St, Springfield",
        "latitude": 39.7817,        // Delivery latitude
        "longitude": -89.6501,       // Delivery longitude
        "status": "PENDING",
        "deliveryNotes": "Ring doorbell twice"
    }
}
```

## Frontend Integration Examples

### 1. Using Leaflet (Open Source)

```javascript
// Initialize map
const map = L.map('delivery-map').setView([0, 0], 13);

// Add tile layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

// When creating an order
let deliveryMarker = null;

// Allow user to click on map to set delivery location
map.on('click', function(e) {
    const { lat, lng } = e.latlng;
    
    // Update or create marker
    if (deliveryMarker) {
        deliveryMarker.setLatLng([lat, lng]);
    } else {
        deliveryMarker = L.marker([lat, lng]).addTo(map);
    }
    
    // Update form fields
    document.getElementById('latitude').value = lat;
    document.getElementById('longitude').value = lng;
    
    // Optionally reverse geocode to get address
    reverseGeocode(lat, lng);
});

// Function to reverse geocode coordinates to address
async function reverseGeocode(lat, lng) {
    const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`
    );
    const data = await response.json();
    document.getElementById('customerAddress').value = data.display_name;
}

// Submit order with coordinates
async function createOrder() {
    const orderData = {
        customerName: document.getElementById('customerName').value,
        customerPhone: document.getElementById('customerPhone').value,
        customerAddress: document.getElementById('customerAddress').value,
        latitude: parseFloat(document.getElementById('latitude').value),
        longitude: parseFloat(document.getElementById('longitude').value),
        notes: document.getElementById('notes').value,
        totalAmount: calculateTotal(),
        orderType: "DELIVERY",
        items: getSelectedItems()
    };
    
    const response = await fetch('/api/orders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(orderData)
    });
    
    const order = await response.json();
    console.log('Order created:', order);
}
```

### 2. Using Google Maps

```javascript
let map;
let deliveryMarker;

function initMap() {
    // Initialize map centered on your restaurant
    map = new google.maps.Map(document.getElementById('delivery-map'), {
        center: { lat: 39.7817, lng: -89.6501 },
        zoom: 13,
    });
    
    // Add click listener for delivery location selection
    map.addListener('click', (event) => {
        placeDeliveryMarker(event.latLng);
    });
    
    // Add search box for address input
    const input = document.getElementById('customerAddress');
    const searchBox = new google.maps.places.SearchBox(input);
    
    searchBox.addListener('places_changed', () => {
        const places = searchBox.getPlaces();
        if (places.length === 0) return;
        
        const place = places[0];
        if (!place.geometry || !place.geometry.location) return;
        
        // Set marker at selected place
        placeDeliveryMarker(place.geometry.location);
        
        // Center map on selected location
        map.setCenter(place.geometry.location);
        map.setZoom(15);
    });
}

function placeDeliveryMarker(location) {
    // Remove existing marker
    if (deliveryMarker) {
        deliveryMarker.setMap(null);
    }
    
    // Create new marker
    deliveryMarker = new google.maps.Marker({
        position: location,
        map: map,
        title: 'Delivery Location',
        draggable: true
    });
    
    // Update form with coordinates
    document.getElementById('latitude').value = location.lat();
    document.getElementById('longitude').value = location.lng();
    
    // Allow marker to be dragged for fine-tuning
    deliveryMarker.addListener('dragend', (event) => {
        document.getElementById('latitude').value = event.latLng.lat();
        document.getElementById('longitude').value = event.latLng.lng();
    });
}
```

## Displaying Existing Orders on Map

### Showing Multiple Delivery Orders

```javascript
// Fetch delivery orders
async function loadDeliveryOrders() {
    const response = await fetch('/api/orders?orderType=DELIVERY&status=PENDING');
    const orders = await response.json();
    
    // Clear existing markers
    clearMarkers();
    
    // Add marker for each delivery
    orders.content.forEach(order => {
        if (order.delivery && order.delivery.latitude && order.delivery.longitude) {
            const marker = L.marker([
                order.delivery.latitude, 
                order.delivery.longitude
            ]).addTo(map);
            
            // Add popup with order details
            marker.bindPopup(`
                <b>Order #${order.id}</b><br>
                Customer: ${order.customerName}<br>
                Phone: ${order.customerPhone}<br>
                Address: ${order.delivery.deliveryAddress}<br>
                Status: ${order.delivery.status}
            `);
            
            markers.push(marker);
        }
    });
    
    // Fit map to show all markers
    if (markers.length > 0) {
        const group = L.featureGroup(markers);
        map.fitBounds(group.getBounds());
    }
}
```

## Mobile App Integration

### React Native with react-native-maps

```jsx
import MapView, { Marker } from 'react-native-maps';

function DeliveryMapScreen() {
    const [deliveryLocation, setDeliveryLocation] = useState(null);
    
    const handleMapPress = (event) => {
        const { latitude, longitude } = event.nativeEvent.coordinate;
        setDeliveryLocation({ latitude, longitude });
    };
    
    const createOrder = async () => {
        const orderData = {
            customerName: customerInfo.name,
            customerPhone: customerInfo.phone,
            customerAddress: customerInfo.address,
            latitude: deliveryLocation?.latitude,
            longitude: deliveryLocation?.longitude,
            notes: orderNotes,
            totalAmount: calculateTotal(),
            orderType: "DELIVERY",
            items: cartItems
        };
        
        // Submit order to API
        const response = await api.createOrder(orderData);
    };
    
    return (
        <MapView
            style={{ flex: 1 }}
            onPress={handleMapPress}
            initialRegion={{
                latitude: 39.7817,
                longitude: -89.6501,
                latitudeDelta: 0.0922,
                longitudeDelta: 0.0421,
            }}
        >
            {deliveryLocation && (
                <Marker
                    coordinate={deliveryLocation}
                    title="Delivery Location"
                    draggable
                    onDragEnd={(e) => setDeliveryLocation(e.nativeEvent.coordinate)}
                />
            )}
        </MapView>
    );
}
```

## Best Practices

1. **Address Validation**: Always validate that coordinates fall within your delivery area before accepting the order.

2. **Fallback for Missing Coordinates**: Not all orders may have coordinates. Always handle cases where latitude/longitude are null.

3. **Geocoding Service**: Consider implementing server-side geocoding to convert addresses to coordinates automatically if not provided by the client.

4. **Delivery Zones**: Implement delivery zone validation to ensure orders are within your service area:
   ```javascript
   function isWithinDeliveryZone(lat, lng) {
       const restaurantLat = 39.7817;
       const restaurantLng = -89.6501;
       const maxDeliveryRadius = 10; // kilometers
       
       const distance = calculateDistance(
           restaurantLat, restaurantLng, lat, lng
       );
       
       return distance <= maxDeliveryRadius;
   }
   ```

5. **Privacy Considerations**: Be mindful of customer privacy when displaying delivery locations to drivers or on public-facing maps.

## Database Considerations

The coordinates are stored as `DOUBLE PRECISION` fields in PostgreSQL, which provides approximately 15 decimal digits of precision, sufficient for accurate location tracking.

## Error Handling

Always handle cases where geocoding or map services might fail:

```javascript
try {
    const coordinates = await geocodeAddress(address);
    orderData.latitude = coordinates.lat;
    orderData.longitude = coordinates.lng;
} catch (error) {
    console.warn('Geocoding failed, proceeding without coordinates:', error);
    // Order can still be created without coordinates
}
```

## Testing

When testing the map integration:
1. Test with valid coordinates within your delivery area
2. Test with coordinates outside your delivery area
3. Test without coordinates (address only)
4. Test with invalid/malformed coordinate values
5. Test marker dragging and position updates
6. Test on different screen sizes and devices

## Security Notes

- Never expose API keys in frontend code
- Implement rate limiting for geocoding requests
- Validate coordinates on the server side
- Consider using environment variables for map service configurations

## Conclusion

The ResAdmin API now fully supports map integration for delivery orders. Frontend applications can use any mapping library (Google Maps, Leaflet, Mapbox, etc.) to provide visual delivery location selection and tracking capabilities.
