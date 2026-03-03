# Micro-Audit System for College Committees

A **process accountability & transparency platform** that tracks committee actions, detects delays, and generates audit trails — built as a full-stack Java web application covering all FSD practicals.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | HTML5, CSS3 (Custom Properties, Grid, Flexbox), Vanilla JS (ES6 Classes) |
| **Servlets** | Jakarta Servlet API 5.0 (Login, Action, Dashboard, Profile, AuditLog) |
| **Filters** | Jakarta Servlet Filter (`AuditFilter` — logs every HTTP request) |
| **JSP + JSTL** | JSP 3.0 + JSTL 2.0 (admin-dashboard, audit-logs, analytics views) |
| **JDBC** | `PreparedStatement`, `ResultSet`, manual connection management via `DBUtil` |
| **Hibernate** | Hibernate ORM 6.2.6, `@Entity` annotations, `SessionFactory`, HQL |
| **Spring MVC** | Spring 6.0.11, `@Controller`, `@GetMapping`, `ViewResolver`, DI via `@Bean` |
| **Streams API** | `groupingBy`, `partitioningBy`, `averagingLong`, `summingLong`, `max`, `sorted` |
| **Database** | MySQL 8.x (`micro_audit` schema with 4 tables) |
| **Build** | Maven (WAR packaging), Java 17 |

---

## Project Structure

```
micro-audit-system/
├── pom.xml
├── sql/
│   └── schema.sql                    # MySQL schema + seed data
├── src/main/java/com/microaudit/
│   ├── model/                        # Hibernate Entities
│   │   ├── User.java                 #   @Entity — staff member
│   │   ├── Committee.java            #   @Entity — college committee
│   │   ├── Action.java               #   @Entity — committee action (with delay methods)
│   │   └── AuditLog.java             #   @Entity — audit trail entry
│   ├── dao/                          # Data Access (JDBC + Hibernate)
│   │   ├── UserDAO.java              #   authenticate, findAll, updateProfile
│   │   ├── ActionDAO.java            #   CRUD, countByStatus, ActionRow DTO
│   │   ├── AuditLogDAO.java          #   insertLog, findAll, findByAction/User
│   │   └── CommitteeDAO.java         #   findAll, findById, findByName
│   ├── servlet/                      # Jakarta Servlets
│   │   ├── LoginServlet.java         #   POST /api/login
│   │   ├── ActionServlet.java        #   GET/POST/PUT /api/actions
│   │   ├── DashboardServlet.java     #   GET /api/dashboard
│   │   ├── ProfileServlet.java       #   GET/POST /api/profile
│   │   └── AuditLogServlet.java      #   GET /api/audit-logs
│   ├── util/
│   │   ├── AuditFilter.java          #   Servlet Filter — logs HTTP requests
│   │   ├── HibernateUtil.java        #   SessionFactory singleton
│   │   └── DBUtil.java               #   JDBC connection provider
│   ├── analytics/                    # Streams API Engines
│   │   ├── StreamsAnalytics.java     #   10 analytics methods using Streams
│   │   └── DelayDetectionEngine.java #   Delay scores, patterns, health score
│   └── spring/
│       ├── config/
│       │   ├── WebConfig.java        #   @Configuration, ViewResolver, @Bean DAOs
│       │   └── WebAppInitializer.java#   DispatcherServlet on /app/*
│       └── controller/
│           └── AdminController.java  #   /app/admin/dashboard|audit-logs|analytics
├── src/main/resources/
│   └── hibernate.cfg.xml             # Hibernate config (MySQL, show_sql, hbm2ddl)
├── src/main/webapp/
│   ├── WEB-INF/
│   │   ├── web.xml                   # Servlet mappings, filter, session config
│   │   └── jsp/                      # JSP Views
│   │       ├── admin-dashboard.jsp
│   │       ├── audit-logs.jsp
│   │       └── analytics.jsp
│   ├── index.html                    # Login page
│   ├── css/styles.css                # Global styles
│   ├── js/                           # Frontend JS classes
│   │   ├── User.js
│   │   ├── Action.js
│   │   └── ActionRequest.js
│   └── pages/                        # SPA-style pages
│       ├── dashboard.html
│       ├── action-form.html
│       ├── profile.html
│       ├── audit-logs.html
│       └── analytics.html
└── pages/                            # Source pages (mirrored to webapp)
```

---

## Practical Coverage

| # | Practical | Implementation |
|---|-----------|---------------|
| 1 | **Servlet — Login** | `LoginServlet.java` — POST authentication with session creation |
| 2 | **Servlet — CRUD** | `ActionServlet.java` — create, read, update actions |
| 3 | **Servlet — Dashboard** | `DashboardServlet.java` — KPI aggregation via JSON |
| 4 | **Servlet Filter** | `AuditFilter.java` — logs method, URI, timing, user, IP for every request |
| 5 | **JDBC + PreparedStatement** | Every DAO has `*JDBC` methods using `PreparedStatement` and `ResultSet` |
| 6 | **Hibernate ORM** | 4 entities with `@Entity`, `@ManyToOne`, `@Enumerated`; `SessionFactory` via `HibernateUtil` |
| 7 | **Hibernate CRUD** | Every DAO has `*Hibernate` methods (save, find, update via HQL) |
| 8 | **Spring MVC** | `WebConfig`, `WebAppInitializer`, `AdminController` with 3 view endpoints |
| 9 | **Spring Core (DI)** | `@Bean` definitions for DAOs + analytics engines in `WebConfig` |
| 10 | **JSP + JSTL** | 3 JSP pages using `<c:forEach>`, `<c:if>`, `<c:choose>`, `<fmt:formatDate>` |
| 11 | **Streams API** | `StreamsAnalytics.java` — 10 methods: groupingBy, partitioningBy, averagingLong, summingLong, max, sorted, counting |
| 12 | **Advanced Streams** | `DelayDetectionEngine.java` — delay patterns, accountability scores, system health |

---

## Setup & Run

### Prerequisites
- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.x**
- **Tomcat 10+** (Jakarta EE 9+ compatible)

### Database Setup

```sql
-- Create the database and run the schema
mysql -u root -p < sql/schema.sql
```

This creates the `micro_audit` database with 4 tables and inserts seed data (6 committees, 6 users, 5 actions, 9 audit log entries).

**Default credentials** (all use password `password123`):

| Staff ID | Name | Role |
|----------|------|------|
| STF001 | Dr. Sharma | Head |
| STF002 | Prof. Patel | Member |
| STF003 | Dr. Kumar | Member |
| STF004 | Prof. Singh | Auditor |
| STF005 | Dr. Mehta | Head |
| STF006 | Prof. Gupta | Member |

### Configure Database Connection

Edit connection details in two places if your MySQL password differs from `root`:

1. `src/main/resources/hibernate.cfg.xml` — Hibernate connection
2. `src/main/java/com/microaudit/util/DBUtil.java` — JDBC connection

### Build & Deploy

```bash
# Build WAR file
mvn clean package

# Deploy to Tomcat
cp target/micro-audit-system-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/

# Start Tomcat
$TOMCAT_HOME/bin/startup.sh    # Linux/Mac
%TOMCAT_HOME%\bin\startup.bat  # Windows
```

### Access

| URL | Description |
|-----|-------------|
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/` | Login page |
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/pages/dashboard.html` | Dashboard |
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/pages/audit-logs.html` | Audit Logs |
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/pages/analytics.html` | Analytics |
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/app/admin/dashboard` | Admin Dashboard (JSP) |
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/app/admin/audit-logs` | Admin Audit Logs (JSP) |
| `http://localhost:8080/micro-audit-system-1.0-SNAPSHOT/app/admin/analytics` | Admin Analytics (JSP) |

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/login` | Authenticate with staffId + password |
| GET | `/api/dashboard` | Dashboard KPIs and action list |
| GET | `/api/actions` | List actions (optional `?filter=committee&value=...`) |
| POST | `/api/actions` | Create a new action |
| PUT | `/api/actions` | Update action status |
| GET | `/api/profile` | Get current user profile |
| POST | `/api/profile` | Update profile |
| GET | `/api/audit-logs` | List audit logs (optional `?actionId=...` or `?userId=...`) |

---

## Architecture

```
Browser (HTML/CSS/JS)
    │
    ├── Servlet Layer (LoginServlet, ActionServlet, etc.)
    │       │
    │       ├── AuditFilter (intercepts all requests)
    │       │
    │       ├── DAO Layer (JDBC methods)
    │       │       └── DBUtil → MySQL (PreparedStatement)
    │       │
    │       └── DAO Layer (Hibernate methods)
    │               └── HibernateUtil → SessionFactory → MySQL
    │
    └── Spring MVC Layer (/app/*)
            ├── AdminController
            │       ├── StreamsAnalytics (Streams API)
            │       └── DelayDetectionEngine (Streams API)
            └── JSP Views (JSTL rendering)
```

---

## Key Design Decisions

- **Dual DAO approach**: Every DAO has both JDBC (`PreparedStatement`) and Hibernate methods side-by-side — demonstrates both technologies using the same data model.
- **Servlet + Spring MVC coexistence**: Servlets handle the main SPA API; Spring MVC handles admin/analytics views — shows both approaches in one project.
- **Client-side + Server-side rendering**: HTML pages use `fetch()` to call servlet APIs; JSP pages render server-side with JSTL — covers both paradigms.
- **Streams API integration**: Analytics engines use functional-style operations (`groupingBy`, `partitioningBy`, `averagingLong`, etc.) for real business logic (delay detection, accountability scoring).

---

## License

This project is for educational purposes (FSD course practicals).