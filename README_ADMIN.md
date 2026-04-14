# MO-Billing Administrative Utilities

This document outlines the administrative tools available for managing the MO-Billing system security and user accounts.

## Super User Creation (CLI)

If you need to create an initial administrator account or recover access, use the built-in CLI utility.

### Prerequisite
Ensure you have Maven installed and the application database is accessible.

### Usage
Run the following command in your terminal from the project root:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--create-superuser"
```

### Process
1. The utility will initialize the Spring context and connect to the database.
2. It will interactively prompt you for:
   - **Username**: The login ID for the new user.
   - **Password**: Securely hashed before storage.
   - **Role**: `ADMIN` (full access) or `EMPLOYEE` (operational access).
3. Upon success, the user will be immediately active and able to log in via the web interface.

## Database Segregation
All billing module tables are prefixed with `bill_` (e.g., `bill_users`, `bill_estimates`) to separate them from other system components.

---
*For further assistance, refer to the project documentation or contact the system administrator.*
