# Faculty Marks Approval System

A web-based application for managing academic marks, designed to simplify marks entry, assessment management, and student access to academic records.

## 1. Overview

The **Faculty Marks Approval System** is an academic project developed to streamline the process of managing student assessment marks.

The application provides three role-based interfaces:

- **Admin:** Manages students, faculty, subjects, and academic programs.
- **Faculty:** Views assigned subjects, manages student marks, and submits assessment records.
- **Student:** Accesses and views their academic marks.

The system also provides Excel-based marks import, draft saving, and individual assessment locking to make marks management more efficient and reliable.

## 2. Features

### Admin Module
- Secure administrator login.
- Add, view, update, and manage student records.
- Manage faculty records.
- Manage academic subjects.
- Manage academic programs.

### Faculty Module
- View assigned subjects.
- View student rosters.
- Filter student records using available academic filters.
- Enter marks for individual assessments.
- Import marks using Excel spreadsheets.
- Save marks as drafts without locking them.
- Submit marks and lock individual assessments.

### Student Module
- Secure student login.
- Access a dedicated student dashboard.
- View academic marks.

### Marks Management
- Supports NCP1, NCP2, and NCP3 assessments.
- Supports decimal marks.
- Individual locking of assessments.
- Validation of marks before submission.
- Excel import using student enrollment numbers.
- Automatically ignores Excel records belonging to students outside the selected roster.

## 3. Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java |
| Backend Framework | Spring Boot |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Frontend | HTML, CSS, JavaScript |
| UI Framework | Bootstrap 5 |
| Template Engine | Thymeleaf |
| Excel Processing | Apache POI |
| Build Tool | Maven |

## 4. Application Architecture

The application follows a layered architecture:

- **Controller Layer:** Handles HTTP requests and application navigation.
- **Service Layer:** Implements business logic and application operations.
- **Repository Layer:** Performs database operations using Spring Data JPA.
- **Entity Layer:** Represents the application's database entities.
- **View Layer:** Provides the user interface using Thymeleaf, HTML, CSS, JavaScript, and Bootstrap.

## 5. Prerequisites

The following software is required to run the application:

- Java Development Kit (JDK)
- MySQL Server
- Git
- An IDE or code editor such as Visual Studio Code or IntelliJ IDEA

## 6. Installation and Setup

### Step 1: Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

Navigate to the project directory:

```bash
cd marksapproval
```

### Step 2: Create the Database

Create the MySQL database:

```sql
CREATE DATABASE marks_approval;
```

### Step 3: Configure Database Connection

Open the application's `application.properties` file and configure the MySQL connection details according to your local environment.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/marks_approval
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Ensure that the database configuration and schema-generation settings are compatible with the project.

**Important:** Do not upload actual database passwords or other sensitive credentials to GitHub.

### Step 4: Run the Application

On Windows, execute the following command from the project directory:

```bash
.\mvnw.cmd spring-boot:run
```

Alternatively, open the project in an IDE and run the Spring Boot application.

### Step 5: Access the Application

Once the application starts successfully, open a browser and navigate to:

```text
http://localhost:8080
```

## 7. Excel Marks Import

The application provides an Excel import facility for faculty members to simplify marks entry.

The Excel spreadsheet uses the following columns:

| Column | Description |
|---|---|
| Enrolno | Student enrollment number |
| Marks | Assessment marks |

The importer:

1. Reads enrollment numbers and corresponding marks from the spreadsheet.
2. Matches enrollment numbers with students in the currently selected roster.
3. Validates the relevant marks.
4. Ignores records belonging to students outside the selected roster.
5. Populates the corresponding marks fields for faculty review.

Imported marks are not automatically saved or locked. Faculty members can review the values and then use the existing **Save Draft** or **Submit All & Lock** functionality.

## 8. Project Structure

The application follows the standard Spring Boot project structure.

```text
marksapproval/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/SP/marksapproval/
│       │       ├── controller/
│       │       ├── entity/
│       │       ├── repository/
│       │       ├── service/
│       │       └── ...
│       │
│       └── resources/
│           ├── static/
│           ├── templates/
│           └── application.properties
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

*This is a simplified representation of the project directory.*

## 9. Application Screenshots

The following screenshots demonstrate the application's interface and major functionalities.

### Login Page

![Login Page](docs/images/login.png)

### Admin Dashboard

![Admin Dashboard](docs/images/admin-dashboard.png)

### Faculty Marks Entry

![Faculty Marks Entry](docs/images/faculty-marks-entry.png)

### Excel Marks Import

![Excel Marks Import](docs/images/excel-import.png)

### Student Dashboard

![Student Dashboard](docs/images/student-dashboard.png)

## 10. Repository

**GitHub Repository:** <https://github.com/AnirudhGhatty/Faculty-marks-approval-system.git>

---

**Faculty Marks Approval System**

Academic Project | B.Tech Computer Science and Engineering