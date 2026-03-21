# 📖 StayNest API Documentation

Comprehensive guide for using the StayNest REST API.

## 🌐 Base URL

- **Development**: `http://localhost:8080/api/v1`
- **Production**: `https://api.staynest.com/api/v1`

## 🔐 Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Getting a Token

1. **Register** or **Login** to get a JWT token
2. Include the token in all authenticated requests
3. Tokens expire after 24 hours (development) or 1 hour (production)

## 📋 API Endpoints

### Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "Password123!",
  "phoneNumber": "1234567890",
  "role": "GUEST"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "GUEST",
  "expiresIn": 86400000
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "Password123!"
}
```

**Response (200 OK):** Same as register response

---

### Properties

#### Create Property (HOST only)
```http
POST /properties
Authorization: Bearer <token>
Content-Type: application/json

{
  "propertyName": "Beach House Paradise",
  "description": "Beautiful beach house with ocean views",
  "propertyType": "HOUSE",
  "address": "123 Beach Road",
  "city": "Miami",
  "state": "Florida",
  "country": "USA",
  "postalCode": "33140",
  "latitude": 25.7617,
  "longitude": -80.1918,
  "amenities": {
    "wifi": true,
    "parking": true,
    "pool": true
  }
}
```

**Response (201 Created):**
```json
{
  "propertyId": "property-uuid",
  "propertyName": "Beach House Paradise",
  "description": "Beautiful beach house with ocean views",
  "propertyType": "HOUSE",
  "address": "123 Beach Road",
  "city": "Miami",
  "state": "Florida",
  "country": "USA",
  "postalCode": "33140",
  "latitude": 25.7617,
  "longitude": -80.1918,
  "amenities": {
    "wifi": true,
    "parking": true,
    "pool": true
  },
  "hostId": "host-uuid",
  "createdAt": "2026-03-21T10:30:00"
}
```

#### Advanced Search
```http
POST /properties/search/advanced
Content-Type: application/json

{
  "city": "Miami",
  "propertyType": "HOUSE",
  "minPrice": 100.00,
  "maxPrice": 500.00,
  "minBedrooms": 2,
  "minGuests": 4,
  "minRating": 4.0,
  "sortBy": "price",
  "sortDirection": "asc",
  "page": 0,
  "size": 10
}
```

**Response (200 OK):**
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5,
  "first": true,
  "last": false,
  "empty": false
}
```

---

### Bookings

#### Create Booking
```http
POST /bookings
Authorization: Bearer <token>
Content-Type: application/json

{
  "unitId": "unit-uuid",
  "checkInDate": "2026-04-01",
  "checkOutDate": "2026-04-05",
  "numGuests": 2,
  "specialRequests": "Early check-in if possible"
}
```

**Response (201 Created):**
```json
{
  "bookingId": "booking-uuid",
  "unitId": "unit-uuid",
  "unitName": "Ocean View Suite",
  "propertyId": "property-uuid",
  "propertyName": "Beach House Paradise",
  "guestId": "guest-uuid",
  "guestName": "John Doe",
  "checkInDate": "2026-04-01",
  "checkOutDate": "2026-04-05",
  "numberOfNights": 4,
  "numGuests": 2,
  "specialRequests": "Early check-in if possible",
  "totalPrice": 850.00,
  "bookingStatus": "PENDING",
  "createdAt": "2026-03-21T10:30:00"
}
```

#### Check Availability
```http
GET /bookings/availability/{unitId}?checkIn=2026-04-01&checkOut=2026-04-05
```

**Response (200 OK):**
```json
{
  "available": true
}
```

---

### Payments

#### Create Payment
```http
POST /payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "bookingId": "booking-uuid",
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "TXN_123456"
}
```

**Response (201 Created):**
```json
{
  "paymentId": "payment-uuid",
  "bookingId": "booking-uuid",
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "paymentStatus": "COMPLETED",
  "transactionId": "TXN_123456",
  "paymentDate": "2026-03-21T10:30:00",
  "createdAt": "2026-03-21T10:30:00"
}
```

---

### Reviews

#### Create Review
```http
POST /reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "bookingId": "booking-uuid",
  "rating": 5,
  "comment": "Amazing stay! Highly recommended.",
  "cleanlinessRating": 5,
  "accuracyRating": 5,
  "communicationRating": 5,
  "locationRating": 4,
  "valueRating": 5
}
```

**Response (201 Created):**
```json
{
  "reviewId": "review-uuid",
  "bookingId": "booking-uuid",
  "propertyId": "property-uuid",
  "propertyName": "Beach House Paradise",
  "reviewerId": "user-uuid",
  "reviewerName": "John Doe",
  "rating": 5,
  "comment": "Amazing stay! Highly recommended.",
  "cleanlinessRating": 5,
  "accuracyRating": 5,
  "communicationRating": 5,
  "locationRating": 4,
  "valueRating": 5,
  "createdAt": "2026-03-21T10:30:00"
}
```

---

## 📊 Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Request successful, no content to return |
| 400 | Bad Request - Invalid request data |
| 401 | Unauthorized - Missing or invalid authentication |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error - Server error |

## 🔄 Pagination

All list endpoints support pagination:

**Query Parameters:**
- `page` - Page number (0-based, default: 0)
- `size` - Page size (default: 10, max: 100)
- `sortBy` - Field to sort by (default: createdAt)
- `sortDirection` - Sort direction: asc/desc (default: desc)

**Example:**
```
GET /properties/paginated?page=0&size=20&sortBy=propertyName&sortDirection=asc
```

## 🔍 Filtering

### Property Search Filters

- `city` - Filter by city
- `state` - Filter by state
- `country` - Filter by country
- `propertyType` - HOUSE, APARTMENT, VILLA, CONDO
- `minPrice` - Minimum price per night
- `maxPrice` - Maximum price per night
- `minBedrooms` - Minimum bedrooms
- `maxBedrooms` - Maximum bedrooms
- `minGuests` - Minimum guest capacity
- `minRating` - Minimum average rating (1-5)

## 🛡️ Security

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@#$%^&+=)

### JWT Token

- Tokens expire after 24 hours (dev) or 1 hour (prod)
- Include in Authorization header: `Bearer <token>`
- Tokens are stateless and validated on each request

## 🚨 Error Responses

All errors follow this format:
```json
{
  "timestamp": "2026-03-21T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Payment amount exceeds remaining balance",
  "path": "/api/v1/payments"
}
```

---

For more details, see the interactive Swagger documentation at `/swagger-ui.html`