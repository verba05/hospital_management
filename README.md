# Hospital Management System - Backend API

A RESTful API for managing hospital operations including patient appointments, doctor schedules, treatments, and administration. **Built entirely in pure Java 21 without any frameworks** - all HTTP handling, routing, authentication,
and business logic implemented from scratch using Java core libraries and external dependencies.

## Main Project Functionalities

- **Patient Management** - Patients can register, log in, manage their profiles, and book appointments with doctors
- **Doctor Management** - Doctors can handle appointments, maintain treatment records
- **Hospital Administration** - Hospital administrators can manage hospital information and manage doctors

## Installation

### 1. Clone Repository
```bash
git clone https://github.com/verba05/hospital_management.git
cd hospital_management
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Create `.env` File

Create a `.env` file in the project root:

```env
# Database Configuration
SUPABASE_URL=jdbc:postgresql://your_postgre_url.com:5432/hospital_management
SUPABASE_USERNAME=postgres
SUPABASE_PASSWORD=your_database_password

# JWT Configuration
JWT_SECRET_KEY=your-secret-key-at-least-64-characters
JWT_EXPIRATION_TIME=28800000

# Email Configuration (Optional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-gmail-app-password
FROM_EMAIL=noreply@hms.com
BASE_URL=http://localhost:8080
```

**Configuration Notes:**
- `SUPABASE_URL`: Format is `jdbc:postgresql://[HOST]:[PORT]/[DATABASE_NAME]`
- `JWT_SECRET_KEY`: Must be at least 64 characters
- `JWT_EXPIRATION_TIME`: In milliseconds (28800000 = 8 hours, 86400000 = 24 hours)
- `SMTP_PASSWORD`: For Gmail, use App Password from [here](https://myaccount.google.com/apppasswords) (enable two-step verification for it to work)

### 4. Setup Database
Run `generate_db.sql` inside your PostgreSQL database to create all required tables, enums, and initial seed data.

### 5. Run Application

```bash
mvn clean compile exec:java -Dexec.mainClass="hospital_managment.Main"
```

Server starts on **http://localhost:8080**

## API Usage

### Authentication
When logging in or registering, send the SHA-256 hash of your password. For example:
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"patient1", "password":"<sha256-hash-of-your-password>"}'

# Register a new patient
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"name": "John", "surname": "Doe", "email": "john.doe@example.com", "login": "johndoe", "password": "<sha256-hash-of-your-password>"}'
```

### Protected Endpoints
Use token in Authorization header. For example:
```bash
curl -X GET http://localhost:8080/api/patients/me \
  -H "Authorization: <your-jwt-token>"
```
## Predefined Accounts

For convenience, the database is initialized with **pre-created user accounts** that you can use to try the application without registering new users.

> When logging in, remember to send the **SHA-256 hash** of the password.

| Role    | Login           | Password    |
|:--------|:----------------|:------------|
| Admin   | admin.warsaw    | password123 |
| Doctor  | dr.lewandowski  | password123 |
| Patient | jblack          | password123 |

