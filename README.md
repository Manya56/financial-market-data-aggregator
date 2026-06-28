Financial Market Data Aggregator (FinAgg)
An enterprise-grade, fault-tolerant data ingestion pipeline and processing engine built with Spring Boot and Spring Batch. The system automates the ingestion of real-time stock histories and global currency exchange rates, sanitizes and maps the multi-source payloads, optimizes relational storage inside PostgreSQL, and exposes clean consumption layers monitored by industry-standard operational metrics.

🚀 Key Architectural Features
Chunk-Driven Batch Processing: Architected using Spring Batch to handle data streams efficiently using a 100-row transactional chunk execution strategy.

Resiliency & Fault Tolerance: Robust handling of unreliable external APIs via native step throttling, automated network Retry configurations (up to 3 times), and structural Skip mechanics to ignore isolated corrupted data rows.

Performance Optimization: Tuned JPA/Hibernate persistence tier with ordered batch inserts (batch_size=100), reducing network overhead during high-volume data dumps.

Clean Data Segregation: Used MapStruct for compile-time, type-safe mapping of complex JSON API DTOs into structured PostgreSQL entities.

Operational Monitoring: Exposes production-ready system health checks and metrics via Spring Boot Actuator to monitor application runtime status and live database connectivity.

🛠️ Tech Stack & Ecosystem
Backend Framework: Spring Boot v3.5.x, Spring Batch

Data Access Layer: Spring Data JPA, Hibernate ORM

Database: PostgreSQL 17

Compilation & Mapping: MapStruct, Lombok

Testing Automation: JUnit 5, Mockito

Build Automation Tool: Apache Maven

IDE: Eclipse / Spring Tool Suite (STS)

🗂️ System Layer Architecture
Code snippet
src/main/java/com/example/demo/
├── config/         # Spring Batch Job/Step configurations & resilience policies
├── controller/     # REST Endpoints with built-in pagination support
├── domain/         # PostgreSQL Relational JPA Entity schemas
├── dto/            # Third-party API Request/Response Data Transfer Objects
├── mapper/         # MapStruct compile-time data transformation interfaces
├── repository/     # JpaRepository extensions for customized querying
└── service/        # External API Clients & Business Logic layers
⚙️ Core Pipeline Phases Implemented
1. Consumption Layer (REST API Endpoints)
Exposes a clean, paginated web layer allowing client applications to fetch aggregated metrics seamlessly:

GET /api/v1/financial-data/{symbol}?page=0&size=10 -> Returns paginated history filtered by stock ticker or currency code.

2. Fault Tolerance & Throttling Policies
Configured steps inside BatchJobConfig to insulate the processing loop from environmental failures:

Retries: Intercepts RestClientException errors caused by network drops or rate-limiting, pausing and retrying up to 3 times.

Skips: Catches mapping errors like NullPointerException or IllegalArgumentException on corrupted payloads, skipping up to 10 rows per step without crashing the batch runtime.

3. Automated Validation Tier
Built independent, high-speed pure Java unit tests (FinancialDataMapperTest) bypassing the Spring Context lifecycle to validate mapping logic accuracy.

4. Enterprise Monitoring
Configured Spring Boot Actuator web hooks exposing underlying infrastructure performance:

GET /actuator/health -> Provides deep system status checks confirming real-time PostgreSQL connection health.

🛫 Getting Started Locally
Prerequisites
Java Development Kit (JDK) 17 or higher

Apache Maven

PostgreSQL running locally on port 5432

Environment Variables
Configure your OS or workspace running environment to expose your active third-party token keys:

Bash
export EXCHANGE_RATE_KEY="your_exchangerate_api_key"
export ALPHA_VANTAGE_KEY="your_alphavantage_api_key"
Database & Application Configuration
Ensure your src/main/resources/application.properties maps your active environment setup:

Properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD:yourpassword}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.batch.jdbc.initialize-schema=always

management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=always
Installation and Execution Steps
Clean and build your project to auto-generate the MapStruct implementation files:

Bash
mvn clean install
Run your test suite to verify the mapping validation logic:

Bash
mvn test
Boot up the embedded Tomcat container to start the server pipelines:

Bash
mvn spring-boot:run
Verify deployment health inside your browser:
http://localhost:8080/actuator/health
