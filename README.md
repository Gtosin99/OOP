# Sante Laboratory Information Management System

This repository contains the Maven JavaFX foundation for the Sante LIMS OOP project.

## Team Workflow

1. Pull the latest `main` branch before starting work.
2. Create or switch to your assigned feature branch.
3. Build your module using the shared packages and utilities in `src/main/java/com/sante/lims`.
4. Do not change the database schema without discussing it with the team lead.
5. Open a pull request back into `main` when your feature is ready.

## Branch Allocation

- `feature/auth-admin`: authentication, users, roles, email verification, audit logs
- `feature/lab-operations`: test requests, samples, status history, result upload and validation
- `feature/customer-module`: customer request flow, available tests, request tracking

## Project Structure

```text
OOP/
├── docs/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/sante/lims/
│       └── resources/
├── uploads/
│   ├── images/
│   └── pdfs/
├── pom.xml
└── README.md
```

## Setup In Apache NetBeans

1. Open NetBeans.
2. Select `File > Open Project`.
3. Choose this repository folder.
4. Let NetBeans load the Maven dependencies.
5. Configure PostgreSQL using `src/main/resources/database.properties`.
6. Run the app with Maven or the NetBeans Run button.

## Database

Create a PostgreSQL database, then run:

```sql
\i docs/database-schema.sql
```

Default connection settings are stored in `src/main/resources/database.properties`. They can also be overridden using environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Build And Run

```bash
mvn clean javafx:run
```
