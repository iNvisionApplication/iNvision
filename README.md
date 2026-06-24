
---

```markdown
# iNVision - Enterprise Asset & Loan Management System 🚀

**iNVision** is a full-stack web application developed for **Mecer Inter-Ed** to streamline the tracking, management, and loaning of enterprise assets. This system provides a robust platform for handling shared corporate resources such as IT equipment and A/V gear, complete with role-based access control and real-time reporting.

---

## 🌟 Key Features

### 🔐 User & Role Management
* **Role-Based Access Control (RBAC):** Distinct permissions for `ADMIN`, `MANAGER`, and `BORROWER` roles, enforced via Spring Security.
* **Secure Authentication:** Password hashing using BCrypt and secure session management.

### 📦 Asset Management
* **Full CRUD Operations:** Seamlessly create, read, update, and retire hardware/software assets.
* **Detailed Asset Profiles:** Track title, category, serial number, acquisition date, cost, location, condition, and status.
* **Photo Handling:** Upload and associate photos with specific assets.
* **Smart Search & Filters:** Efficiently search assets by name/category and filter by location/condition.

### 🔄 Loan Processing Engine
* **End-to-End Workflow:** Borrowers request assets, Managers approve/reject, and automated check-out/check-in tracking.
* **Overdue Tracking:** Automated status updates for loans exceeding their due date.

### 📊 Dynamic Reporting & Dashboards
* **Key Metrics:** Dashboard displaying total assets, pending requests, and overdue loans.
* **Detailed Reports:** Generate Asset Inventory, Loan History (by user/asset), and Overdue Loan reports.
* **Audit Trails:** Automated logging of critical system actions for security and compliance.

---

## 🛠️ Technology Stack

* **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA, Hibernate.
* **Database:** PostgreSQL (hosted via Supabase).
* **Frontend:** HTML5, CSS3, Vanilla JavaScript, Thymeleaf.
* **API Documentation:** Swagger UI / OpenAPI 3.

---

## 🚀 Getting Started

### Prerequisites
* Java 17+
* Maven
* PostgreSQL Database

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/YourOrganization/iNvision.git](https://github.com/YourOrganization/iNvision.git)
   cd iNvision

```

2. **Configure the Database:**
Update `src/main/resources/application.properties` with your PostgreSQL credentials:
```properties
spring.datasource.url=jdbc:postgresql://<your-db-host>:6543/postgres?prepareThreshold=0
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>
spring.jpa.hibernate.ddl-auto=update

```


3. **Run the Application:**
```bash
./mvnw spring-boot:run

```


4. **Access the Application & API:**
* **Frontend:** `http://localhost:8080/`
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`



---

## 👥 Meet the Team (Group 1)

* **Aviwe** - Database Setup, User Authentication, & Security Validations
* **Nceba** - Asset Module (CRUD, Photo Handling, Search/Filter)
* **Dave** - Loan Module (Business Logic, Approvals, Check-in/out workflows)
* **Motheo** - UI/UX, Frontend Dashboards, Reporting, & Documentation

---

*Developed as part of the Mecer Inter-Ed Java System Project - 2026*

```

```
