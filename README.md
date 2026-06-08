# Sante Laboratory Information Management System

JavaFX and PostgreSQL Laboratory Information Management System for Sante Diagnostics Ltd.

The merged root application includes:

- Super Admin governance: user provisioning, custom test builder, request queue, and audit trail.
- Lab Attendant operations: payment confirmation, sample lifecycle tracking, result upload, and result validation.
- Customer transparency: self-registration, test catalog, request history, dashboard countdowns, result vault, and notifications.

## Project Structure

```text
OOP/
|-- docs/
|-- src/
|   |-- main/
|       |-- java/
|       |   |-- com/sante/lims/
|       |-- resources/
|-- uploads/
|   |-- images/
|   |-- pdfs/
|-- pom.xml
|-- README.md
```

## Setup In Apache NetBeans

1. Open NetBeans.
2. Select `File > Open Project`.
3. Choose this repository folder.
4. Let NetBeans load the Maven dependencies.
5. Configure PostgreSQL in `src/main/resources/application.properties`.
6. Run the app with Maven or the NetBeans Run button.

## Database

Create a PostgreSQL database, then run:

```sql
\i docs/database-schema.sql
\i src/main/resources/sql/seed.sql
```

Default connection settings are stored in `src/main/resources/application.properties`. Lab-operation database access can also be overridden using environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Demo Logins

- Super Admin: `admin@sante.com` / `admin123`
- Lab Attendant: `lab@sante.com` / `lab123`
- Customer: `customer@sante.com` / `customer123`

## Build And Run

```bash
mvn clean javafx:run
```
