# Patient System Documentation

## Overview

The **Patient System** is a Spring Boot web application with Thymeleaf templates, Spring Security, and a relational database. It enables patients to register, log in, manage appointments, track medications, and view profiles. This documentation covers setup, configuration, running the application, and API endpoint details.

## Prerequisites

- **Java**: JDK 17+
- **Maven**: 3.6.0+
- **Database**: MySQL, PostgreSQL, or H2
- **Browser**: Chrome, Firefox, etc.
- **Git**: For cloning (optional)

## Setup Instructions

### 1. Clone or Extract the Project

```bash
git clone <repository-url>
cd patient-system
```

### 2. Configure the Database

#### MySQL/PostgreSQL

- Create a database:

  ```sql
  CREATE DATABASE patient_system;
  ```

- Update `src/main/resources/application.properties`:

  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/patient_system
  spring.datasource.username=root
  spring.datasource.password=your_password
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
  ```

#### H2 (In-Memory)

- Add to `pom.xml`:

  ```xml
  <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```

- Configure in `application.properties`:

  ```properties
  spring.datasource.url=jdbc:h2:mem:patient_system
  spring.datasource.driverClassName=org.h2.Driver
  spring.datasource.username=sa
  spring.datasource.password=
  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
  spring.jpa.hibernate.ddl-auto=update
  spring.h2.console.enabled=true
  ```

### 3. Install Dependencies

```bash
mvn clean install
```

### 4. Configure Application Properties

```properties
server.port=8085
spring.thymeleaf.cache=false
logging.level.org.springframework=INFO
logging.level.com.example.patient_system=DEBUG
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Access at `http://localhost:8085`.

## Configuration Details

- **Spring Security**: Form-based authentication with BCrypt-hashed passwords.
- **Database**: JPA/Hibernate, `patients` table with columns: `id`, `name`, `email` (unique), `password`, `phone`, `medical_history`.
- **Logging**: SLF4J with Logback, debug-enabled for troubleshooting.

## API Endpoints

### 1. Home (`http://localhost:8085/`)

- **Method**: GET

- **Access**: Public

- **Description**: Displays homepage with dashboard (authenticated) or login/register links (unauthenticated).

- **Response**:

    - Success: Renders `index.html`.
    - Error: Renders `index.html` with `errorMessage`.

- **Example**:

    - Request: `GET http://localhost:8085/`

    - Response (authenticated, HTML snippet):

      ```html
      <h1>Welcome, John Doe!</h1>
      <p class="card-text">Total: 2</p>
      ```

### 2. Registration (`http://localhost:8085/register`)

- **Method**: GET, POST

- **Access**: Public

- **GET**: Displays registration form (`register.html`).

- **POST**:

    - **Request Body** (Form Data):

        - `name`: String, required
        - `email`: String, required, valid email, unique
        - `password`: String, required
        - `phone`: String, optional
        - `medicalHistory`: String, optional

    - **Validation**:

        - `name`: Not empty, max 255 chars
        - `email`: Valid email, unique, max 255 chars
        - `password`: Not empty

    - **Response**:

        - Success: Redirect to `/login` with `success` flash.
        - Error: Redirect to `/register` with `error` flash.

    - **Example**:

      ```http
      POST /register
      Content-Type: application/x-www-form-urlencoded
      name=John+Doe&email=john.doe@example.com&password=Pass123!
      ```

### 3. Login (`http://localhost:8085/login`)

- **Method**: GET, POST

- **Access**: Public

- **GET**: Displays login form (`login.html`).

- **POST**:

    - **Request Body** (Form Data):

        - `username`: Email, required
        - `password`: Password, required
        - `_csrf`: CSRF token, required

    - **Validation**:

        - `username`: Valid email, exists in database
        - `password`: Matches BCrypt hash

    - **Response**:

        - Success: Redirect to `/`
        - Error: Redirect to `/login?error=true`

    - **Example**:

      ```http
      POST /login
      Content-Type: application/x-www-form-urlencoded
      username=john.doe@example.com&password=Pass123!&_csrf=abc123
      ```

### 4. Appointments (`http://localhost:8085/appointments`)

- **Method**: GET
- **Access**: Authenticated
- **Description**: Displays appointments (`appointments.html`).
- **Response**: Renders `appointments.html` with appointments and booking form.

### 5. Book Appointment (`http://localhost:8085/appointments/book`)

- **Method**: POST

- **Access**: Authenticated

- **Request Body** (Form Data):

    - `patientId`: Long, required
    - `doctorId`: Long, required
    - `appointmentTime`: String, ISO format, required
    - `notes`: String, optional
    - `_csrf`: CSRF token, required

- **Validation**:

    - `patientId`: Matches authenticated user
    - `doctorId`: Exists in database
    - `appointmentTime`: Valid ISO format

- **Response**:

    - Success: Redirect to `/appointments` with `success` flash
    - Error: Redirect with `error` flash

- **Example**:

  ```http
  POST /appointments/book
  Content-Type: application/x-www-form-urlencoded
  patientId=1&doctorId=2&appointmentTime=2025-05-01T10:00:00¬es=Follow-up&_csrf=abc123
  ```

### 6. Medications (`http://localhost:8085/medications`)

- **Method**: GET
- **Access**: Authenticated
- **Description**: Displays medications (`medications.html`).
- **Response**: Renders `medications.html` with medications and add form.

### 7. Add Medication (`http://localhost:8085/medications/add`)

- **Method**: POST

- **Access**: Authenticated

- **Request Body** (Form Data):

    - `patientId`: Long, required
    - `name`: String, required
    - `dosage`: String, optional
    - `frequency`: String, optional
    - `_csrf`: CSRF token, required

- **Validation**:

    - `patientId`: Matches authenticated user
    - `name`: Not empty

- **Response**:

    - Success: Redirect to `/medications?patientId={id}` with `success` flash
    - Error: Redirect with `error` flash

- **Example**:

  ```http
  POST /medications/add
  Content-Type: application/x-www-form-urlencoded
  patientId=1&name=Aspirin&dosage=100mg&frequency=Once+daily&_csrf=abc123
  ```

### 8. Delete Medication (`http://localhost:8085/medications/delete/{id}`)

- **Method**: POST

- **Access**: Authenticated

- **Request Body** (Form Data):

    - `patientId`: Long, required
    - `_csrf`: CSRF token, required

- **Path Parameters**:

    - `id`: Medication ID, required

- **Validation**:

    - `patientId`: Matches authenticated user
    - `id`: Exists and owned by patient

- **Response**:

    - Success: Redirect to `/medications?patientId={id}` with `success` flash
    - Error: Redirect with `error` flash

- **Example**:

  ```http
  POST /medications/delete/1
  Content-Type: application/x-www-form-urlencoded
  patientId=1&_csrf=abc123
  ```

### 9. Logout (`http://localhost:8085/logout`)

- **Method**: GET, POST

- **Access**: Public

- **Request Body** (POST):

    - `_csrf`: CSRF token, required

- **Response**: Redirect to `/login?logout=true`

- **Example**:

  ```http
  POST /logout
  Content-Type: application/x-www-form-urlencoded
  _csrf=abc123
  ```

## Troubleshooting

- **Login Failures**:

    - Check `patients` table for valid BCrypt hashes:

      ```sql
      SELECT email, password FROM patients WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%';
      ```

    - Reset passwords using the endpoint above.

- **Database Errors**: Verify `spring.datasource` settings.

- **404 Errors**: Ensure templates are in `src/main/resources/templates`.