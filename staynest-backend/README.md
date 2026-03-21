# 🏠 StayNest - Vacation Rental Platform

A comprehensive REST API for managing vacation rental properties, bookings, payments, and reviews. Built with Spring Boot 3.5.10, Java 21, and PostgreSQL.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## ✨ Features

### Core Features (V1.0 MVP) ✅
- **User Authentication**: JWT-based authentication with role-based access (GUEST/HOST)
- **Property Management**: Full CRUD operations for properties and units
- **Booking System**: Date conflict prevention, status workflow, availability checking
- **Payment Processing**: Payment recording with overpayment validation
- **Review System**: Post-checkout reviews with category ratings
- **Wishlist**: Save favorite properties
- **Advanced Search**: Multi-criteria search with pagination and filters
- **Dashboard Analytics**: Comprehensive statistics for guests and hosts

### Security Features
- JWT token authentication
- Role-based authorization (GUEST vs HOST)
- Password encryption (BCrypt)
- Protected endpoints with ownership validation
- CORS configuration

### Advanced Features
- Pagination support (all listings)
- Advanced filtering (location, price, capacity, rating)
- Property image management
- Monthly trend analysis (bookings/earnings)
- Top performing properties tracking

## 🛠 Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.5.10**
- **Spring Security** (JWT authentication)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL** (Database)
- **Maven** (Build tool)
- **Lombok** (Boilerplate reduction)
- **JJWT 0.12.5** (JWT implementation)

### API Documentation
- **Swagger/OpenAPI 3.0** (Interactive API docs)

### Database
- **PostgreSQL 16+**

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 16+
- Git

### Installation

1. **Clone the repository**
```bash
   git clone https://github.com/yourusername/staynest.git
   cd staynest
```

2. **Create PostgreSQL database**
```sql
   CREATE DATABASE staynest_db;
```

3. **Configure database credentials**

   Edit `src/main/resources/application.properties`:
```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/staynest_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
```

4. **Build the project**
```bash
   mvn clean install
```

5. **Run the application**
```bash
   mvn spring-boot:run
```

6. **Access the application**
    - API Base URL: http://localhost:8080/api/v1
    - Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
    - Health Check: http://localhost:8080/api/v1/health

## 📚 API Documentation

### Interactive Documentation
Access Swagger UI at: http://localhost:8080/api/v1/swagger-ui.html

### Quick Start Guide

#### 1. Register a User
```bash
POST /api/v1/auth/register
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

#### 2. Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "Password123!"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "uuid-here",
  "email": "john@example.com",
  "role": "GUEST"
}
```

#### 3. Use the Token
```bash
GET /api/v1/properties
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### API Endpoints Summary

#### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `GET /api/v1/auth/test` - Test authentication (protected)

#### Properties
- `POST /api/v1/properties` - Create property (HOST only)
- `GET /api/v1/properties` - Get all properties (public)
- `GET /api/v1/properties/{id}` - Get property by ID (public)
- `PUT /api/v1/properties/{id}` - Update property (owner only)
- `DELETE /api/v1/properties/{id}` - Delete property (owner only)
- `GET /api/v1/properties/search?city={city}` - Search by city
- `POST /api/v1/properties/search/advanced` - Advanced search
- `GET /api/v1/properties/paginated` - Paginated properties

#### Units
- `POST /api/v1/units` - Create unit (property owner only)
- `GET /api/v1/units/{id}` - Get unit by ID
- `GET /api/v1/units/property/{propertyId}` - Get units by property
- `PUT /api/v1/units/{id}` - Update unit (owner only)
- `DELETE /api/v1/units/{id}` - Delete unit (owner only)

#### Bookings
- `POST /api/v1/bookings` - Create booking
- `GET /api/v1/bookings/{id}` - Get booking details
- `GET /api/v1/bookings/my-bookings` - Get my bookings
- `GET /api/v1/bookings/upcoming` - Get upcoming bookings
- `POST /api/v1/bookings/{id}/cancel` - Cancel booking
- `POST /api/v1/bookings/{id}/confirm` - Confirm booking (host)
- `GET /api/v1/bookings/availability/{unitId}` - Check availability

#### Payments
- `POST /api/v1/payments` - Create payment
- `GET /api/v1/payments/{id}` - Get payment details
- `GET /api/v1/payments/booking/{bookingId}` - Get payments by booking
- `GET /api/v1/payments/my-payments` - Get my payments

#### Reviews
- `POST /api/v1/reviews` - Create review
- `GET /api/v1/reviews/{id}` - Get review
- `GET /api/v1/reviews/property/{propertyId}` - Get property reviews
- `GET /api/v1/reviews/my-reviews` - Get my reviews
- `GET /api/v1/reviews/reviewable-bookings` - Get reviewable bookings

#### Wishlists
- `POST /api/v1/wishlists/{propertyId}` - Add to wishlist
- `DELETE /api/v1/wishlists/{propertyId}` - Remove from wishlist
- `GET /api/v1/wishlists/my-wishlists` - Get my wishlists
- `GET /api/v1/wishlists/is-saved/{propertyId}` - Check if saved

#### Users
- `GET /api/v1/users/me` - Get my profile
- `PUT /api/v1/users/me` - Update my profile
- `POST /api/v1/users/me/change-password` - Change password
- `GET /api/v1/users/me/stats` - Get my statistics

#### Dashboard
- `GET /api/v1/dashboard/stats` - Get dashboard statistics

#### Images
- `POST /api/v1/images/properties/{propertyId}` - Add property image
- `POST /api/v1/images/units/{unitId}` - Add unit image
- `DELETE /api/v1/images/{imageId}` - Delete image
- `GET /api/v1/images/properties/{propertyId}` - Get property images

#### Health
- `GET /api/v1/health` - Basic health check
- `GET /api/v1/health/detailed` - Detailed health check
- `GET /api/v1/health/ping` - Ping endpoint

**Total Endpoints:** 50+

## 📁 Project Structure
```
staynest/
├── src/main/java/com/staynest/
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── JpaConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── BookingController.java
│   │   ├── DashboardController.java
│   │   ├── HealthCheckController.java
│   │   ├── PaymentController.java
│   │   ├── PropertyController.java
│   │   ├── PropertyImageController.java
│   │   ├── ReviewController.java
│   │   ├── UnitController.java
│   │   ├── UserController.java
│   │   └── WishlistController.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entity/
│   │   ├── Booking.java
│   │   ├── Payment.java
│   │   ├── Property.java
│   │   ├── PropertyImage.java
│   │   ├── Review.java
│   │   ├── Unit.java
│   │   ├── User.java
│   │   └── Wishlist.java
│   ├── enums/
│   ├── exception/
│   ├── repository/
│   ├── security/
│   │   ├── CustomUserDetailsService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtTokenProvider.java
│   ├── service/
│   └── StayNestApplication.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-prod.properties
├── pom.xml
└── README.md
```

## 🔧 Development

### Running in Development Mode
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building for Production
```bash
mvn clean package -Pprod
java -jar target/staynest-1.0.0.jar --spring.profiles.active=prod
```

### Code Style

- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write meaningful commit messages
- Add JavaDoc comments for public methods

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Test Coverage
```bash
mvn clean test jacoco:report
```

### Postman Collection
Import `StayNest_Tests.postman_collection.json` for API testing.

## 🚢 Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

### Environment Variables (Production)
```bash
DB_URL=jdbc:postgresql://your-db-host:5432/staynest_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_secure_jwt_secret_key_here
ALLOWED_ORIGINS=https://yourdomain.com
```

## 🗺 Roadmap

### V1.0 (Current) ✅
- User authentication
- Property & unit management
- Booking system
- Payment recording
- Reviews & ratings
- Wishlist
- Advanced search
- Dashboard analytics

### V2.0 (Planned)
- [ ] Payment gateway integration (Stripe/Razorpay)
- [ ] File upload (AWS S3)
- [ ] Email notifications
- [ ] SMS notifications
- [ ] Real-time chat (host-guest)
- [ ] Booking calendar view
- [ ] Multi-currency support
- [ ] Multi-language support

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Sanchit Pawar**
- Email: sanchitp.dev@gmail.com
- GitHub: [@sanchitpawar](https://github.com/sanchitpawar)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- PostgreSQL community
- All contributors and testers

## 📞 Support

For support, email sanchitp.dev@gmail.com or open an issue in the GitHub repository.

---

**Built with ❤️ by Sanchit Pawar**