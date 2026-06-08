# Sante Laboratory Information Management System

JavaFX and PostgreSQL Laboratory Information Management System for Sante Diagnostics Ltd.

The app supports three roles:

- Super Admin: user provisioning, custom test builder, request queue, and audit trail.
- Lab Attendant: payment confirmation, sample tracking, result upload, and result validation.
- Customer: self-registration, test catalog, request history, dashboard countdowns, result vault, and notifications.

## Requirements

Install these before running the project:

- Java JDK 17 or newer.
- Apache NetBeans with Maven support, or Maven on your system PATH.
- PostgreSQL.
- Git.

## Project Structure

```text
OOP/
|-- docs/
|   |-- database-schema.sql
|-- src/
|   |-- main/
|       |-- java/com/sante/lims/
|       |-- resources/
|           |-- application.properties
|           |-- sql/
|               |-- seed.sql
|               |-- fix-local-database.sql
|-- uploads/
|   |-- images/
|   |-- pdfs/
|-- pom.xml
|-- README.md
```

## Database Setup

1. Open pgAdmin or psql.
2. Create a PostgreSQL database named:

```sql
CREATE DATABASE sante_lims;
```

3. Connect to the `sante_lims` database.
4. Run the schema file:

```sql
\i docs/database-schema.sql
```

5. Run the local repair file. This is safe to run even if the columns already exist:

```sql
\i src/main/resources/sql/fix-local-database.sql
```

6. Run the seed file:

```sql
\i src/main/resources/sql/seed.sql
```

The seed file creates demo users and sample test catalog records.

## App Configuration

Open `src/main/resources/application.properties` and update the PostgreSQL settings for your own computer:

```properties
db.url=jdbc:postgresql://localhost:5432/sante_lims
db.username=postgres
db.password=postgres
```

If your PostgreSQL runs on another port, change `5432`. If your PostgreSQL password is different, change `db.password`.

SMTP email settings are also in `application.properties`:

```properties
smtp.host=smtp.gmail.com
smtp.port=587
smtp.username=your-email@example.com
smtp.password=your-app-password
smtp.from=your-email@example.com
smtp.auth=true
smtp.starttls=true
```

For Gmail, use an app password, not your normal Gmail password. If SMTP is left as the placeholder values, registration still works and the verification token is shown inside the app.

## Run In NetBeans

1. Open NetBeans.
2. Select `File > Open Project`.
3. Choose the project folder.
4. Let NetBeans download Maven dependencies.
5. Confirm `application.properties` matches your PostgreSQL setup.
6. Press Run.

## Run From Terminal

If Maven is on your PATH:

```bash
mvn clean javafx:run
```

If you are using the Maven bundled with NetBeans on Windows, run:

```powershell
& 'C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd' clean javafx:run
```

Adjust the NetBeans path if your version is installed somewhere else.

## Demo Logins

```text
Super Admin:   admin@sante.com / admin123
Lab Attendant: lab@sante.com / lab123
Customer:      customer@sante.com / customer123
```

## Important Workflow Notes

- Staff-created users must change their password on first login.
- Customers can self-register and verify their email token.
- Lab Attendants and Super Admins can mark requests as paid.
- Uploaded results only become visible after validation.
- Customers cannot open or download validated results until payment is marked as `PAID`.
- Audit logs are written for important user and result actions.

## Troubleshooting

If login says invalid details, rerun `src/main/resources/sql/seed.sql` while connected to `sante_lims`.

If registration or login mentions a missing database column, rerun:

```sql
\i src/main/resources/sql/fix-local-database.sql
```

If PostgreSQL says password authentication failed, update `db.username` and `db.password` in `src/main/resources/application.properties`.

If Maven is not recognized, run the project from NetBeans or use the bundled `mvn.cmd` path shown above.
