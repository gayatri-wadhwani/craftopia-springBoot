# Craftopia Backend API

A comprehensive Spring Boot backend application for the Craftopia e-commerce platform, providing REST APIs for handmade art and craft products marketplace.

## Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (BUYER, SELLER, ADMIN)
- Secure user registration and login

### Product Management
- Complete CRUD operations for products
- Image upload with Cloudinary integration
- Bulk product creation via JSON and CSV
- AI-powered product auto-fill with multilingual support
- Category and tag-based filtering
- Search functionality

### Shopping Cart
- Add/remove items from cart
- Quantity management
- Cart persistence per user
- Clear cart functionality

### Order Management
- Order placement and tracking
- Order status management (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- Order history for buyers
- Admin order management

### Payment Integration
- Razorpay payment gateway integration
- Payment verification and recording
- Transaction management

### AI Integration
- Gemini AI model for product auto-fill
- Multilingual support for product descriptions
- Automatic category and tag suggestions

## Technical Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: MySQL with JPA/Hibernate
- **Security**: Spring Security with JWT
- **AI Integration**: Google Gemini API
- **Payment**: Razorpay
- **Image Storage**: Cloudinary
- **Build Tool**: Gradle

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Gradle 7.0 or higher

## Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    runtimeOnly 'com.mysql:mysql-connector-j'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'com.opencsv:opencsv:5.11'
    implementation "com.razorpay:razorpay-java:1.4.3"
    implementation 'com.cloudinary:cloudinary-http44:1.34.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
}
```

## Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd backend
   ```

2. **Configure MySQL Database**:
   ```sql
   CREATE DATABASE craftopia;
   CREATE USER 'craftopia_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON craftopia.* TO 'craftopia_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure application.properties**:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/craftopia
   spring.datasource.username=craftopia_user
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   
   # JPA Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
   
   # JWT Configuration
   jwt.secret=your_jwt_secret_key
   jwt.expiration=86400000
   
   # Gemini API Configuration
   gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
   gemini.api.key=your_gemini_api_key
   
   # Cloudinary Configuration
   cloudinary.cloud_name=your_cloud_name
   cloudinary.api_key=your_api_key
   cloudinary.api_secret=your_api_secret
   
   # Razorpay Configuration
   razorpay.key_id=your_razorpay_key_id
   razorpay.key_secret=your_razorpay_key_secret
   
   # CORS Configuration
   cors.allowed.origins=http://localhost:4200
   ```

4. **Build and run the application**:
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```

5. **Access the API**:
   - Base URL: `http://localhost:8080`
   - API Documentation: `http://localhost:8080/swagger-ui.html` (if Swagger is configured)

## Project Structure

```
src/main/java/com/example/craftopia/
├── Controller/              # REST Controllers
│   ├── AuthController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   └── ProductController.java
├── DTO/                     # Data Transfer Objects
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   └── ...
├── Entity/                  # JPA Entities
│   ├── User.java
│   ├── Product.java
│   ├── Cart.java
│   ├── Order.java
│   └── ...
├── Repository/              # JPA Repositories
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   └── ...
├── Service/                 # Business Logic Services
│   ├── AuthService.java
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   ├── AIOrchestrationService.java
│   └── CloudinaryService.java
├── Config/                  # Configuration Classes
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── ...
└── Util/                    # Utility Classes
    ├── JwtUtil.java
    ├── SecurityUtil.java
    └── ...
```

## API Endpoints

### Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User login

### Products
- `GET /products` - Get all products (with optional category and search filters)
- `GET /products/{id}` - Get product by ID
- `GET /products/my-products` - Get seller's products (SELLER role)
- `POST /products` - Create product with image upload (SELLER role)
- `POST /products/bulk-json` - Bulk create from JSON (SELLER role)
- `POST /products/bulk-csv` - Bulk create from CSV (SELLER role)
- `POST /products/ai/auto-fill` - AI-powered product auto-fill (SELLER role)
- `PATCH /products/{id}` - Update product (SELLER role)
- `DELETE /products/{id}` - Delete product (SELLER role)

### Cart
- `GET /cart` - Get cart items (BUYER role)
- `GET /cart/count` - Get cart item count (BUYER role)
- `POST /cart/add` - Add item to cart (BUYER role)
- `DELETE /cart/remove/{productId}` - Remove item from cart (BUYER role)
- `DELETE /cart/clear` - Clear cart (BUYER role)

### Orders
- `POST /orders/place` - Place order (BUYER role)
- `GET /orders/my` - Get buyer's orders (BUYER role)
- `GET /orders/all` - Get all orders (ADMIN role)
- `PATCH /orders/{id}/status` - Update order status (ADMIN role)

### Payment
- `GET /payment/{amount}` - Create Razorpay transaction (BUYER role)
- `POST /payment/verify` - Verify payment signature (BUYER role)

## Key Features

### AI Integration
The application integrates with Google Gemini AI for intelligent product auto-fill:

```java
@PostMapping("/ai/auto-fill")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<?> autoFillProduct(
    @RequestParam("image") MultipartFile image,
    @RequestParam(value = "text", required = false) String text,
    @RequestParam("price") Double price) {
    // AI processing logic
}
```

Features include:
- Automatic product categorization
- Tag generation
- Multilingual description support
- Price suggestions
- Material and color detection

### Security Configuration
- JWT-based authentication
- Role-based authorization using `@PreAuthorize`
- CORS configuration for frontend integration
- Password encryption using BCrypt

### Payment Integration
Razorpay integration for secure payments:
- Order creation
- Payment verification
- Signature validation
- Transaction recording

### Image Management
Cloudinary integration for image storage:
- Automatic image optimization
- CDN delivery
- Multiple format support
- Secure upload handling

## Database Schema

### Key Entities
- **User**: Authentication and user management
- **Product**: Product catalog with seller relationship
- **Cart**: Shopping cart items per user
- **Order**: Order management with items
- **Payment**: Payment transaction records

### Relationships
- User (1) → (N) Product (seller relationship)
- User (1) → (N) Cart (buyer relationship)
- User (1) → (N) Order (buyer relationship)
- Order (1) → (N) OrderItem
- Product (1) → (N) OrderItem

## Environment Variables

Set the following environment variables for production:

```bash
export DB_URL=jdbc:mysql://your-db-host:3306/craftopia
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret
export GEMINI_API_KEY=your_gemini_key
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
export RAZORPAY_KEY_ID=your_razorpay_key
export RAZORPAY_KEY_SECRET=your_razorpay_secret
```

## Testing

Run tests using:
```bash
./gradlew test
```

## Deployment

1. **Build the application**:
   ```bash
   ./gradlew build
   ```

2. **Create Docker image** (optional):
   ```bash
   docker build -t craftopia-backend .
   ```

3. **Deploy to your preferred platform**:
   - AWS EC2/ECS
   - Google Cloud Platform
   - Heroku
   - DigitalOcean

## Monitoring and Logging

- Spring Boot Actuator for health checks
- Structured logging with Logback
- Error handling with global exception handlers

## Performance Considerations

- Database indexing on frequently queried fields
- Connection pooling for database connections
- Caching for frequently accessed data
- Image optimization through Cloudinary

## Security Best Practices

- Input validation and sanitization
- SQL injection prevention through JPA
- XSS protection
- CSRF protection
- Rate limiting (recommended for production)
