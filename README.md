# RentFlow – Equipment Hire & Rental Management System

**Group 25 | Object-Oriented Analysis & Design (OOAD) and Database Management Systems (DBMS) Project**  
**Client / Case Study:** COMPULIN

RentFlow is a web-based system designed to support the equipment rental lifecycle, from company and equipment management to renter registration, rental issuing, payments, returns, damage assessment, and settlement. The project was developed collaboratively as a university group assignment.

> **Deployment status:** **Partially deployed on AWS.** The React login page is publicly accessible through CloudFront, and the Spring Boot backend and MySQL database have been deployed and tested separately. However, the public website's API integration is **not yet functional**: `/api/auth/csrf` has returned **HTTP 504 Gateway Timeout** through CloudFront. Consequently, login and authenticated workflows cannot currently be completed through the public URL.

## Live Deployment

**Frontend (CloudFront):** https://d2xo1fit86o5d4.cloudfront.net

The URL currently demonstrates the hosted React frontend. 
## Project Objectives

- Digitize renter records and equipment rental transactions.
- Track inventory availability, issued quantities, and rental history.
- Record rental periods, rates, refundable security deposits, and payments.
- Support equipment returns, inspections, losses, damage charges, and settlements.
- Maintain company-specific access to operational records through role-based access controls.

## Main Functional Modules

### Module 1 — Company, Staff & Equipment Management

- Company information and staff/user management.
- Access roles and company-specific dashboards.
- Equipment category management, inventory records, and availability.

### Module 2 — Renter & Rental Management

- Renter registration and maintenance, including identification documents and secondary contacts.
- Renter search and rental history.
- Equipment selection, quantity and availability validation.
- Rental creation, dates, agreed rates, periods, and refundable deposit calculations.
- Rental confirmation, equipment issuing, and rental extensions.
- Rental Reports

### Module 3 — Payments, Deposits & Financial Records

- Rental-related invoices and payment recording.
- Security deposit collection and refund processing.
- Financial and rental-related reporting features.

### Module 4 — Returns, Damage & Settlement Management

- Full and partial returns and returned-item condition recording.
- Return history and return details.
- Damage assessments and handling of lost or unreturned items.
- Settlement processing and deposit adjustments.

**Module 4 contribution / branch-history note:** For the explanation of an unintended Git pull/merge, the subsequent integration branches, and the distinction between individual Module 4 work and shared group contributions, read **[BRANCH_NOTES.md](BRANCH_NOTES.md)**. The original individual development branch was `feature/member4-returns-rental`; additional integration/recovery branches are identified in that document. For individual assessment, consult the member's authored commits, relevant pull requests, and Module 4 implementation alongside that note.

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React, Vite, JavaScript, CSS / Tailwind CSS |
| Backend | Java 21, Spring Boot, Spring MVC / REST APIs, Spring Data JPA / Hibernate |
| Database | MySQL |
| Build tools | Maven (backend), npm / Vite (frontend) |
| Cloud hosting | Amazon S3, Amazon CloudFront, Amazon EC2, Amazon RDS MySQL |
| Server management | Linux `systemd` for the Spring Boot process |
| Development tools | IntelliJ IDEA, MySQL Workbench, Git, GitHub |

## Application Architecture

```text
                           User
                             |
                        HTTPS website
                             |
                       Amazon CloudFront
                        /            \
                 Default (*)        /api/*
                    |                  |
              Amazon S3             EC2 origin
             React build         Spring Boot API
                                       |
                                 Amazon RDS MySQL
                                    rentflow_db
```

**Important:** This diagram describes the **intended** application routing. The CloudFront-to-EC2 API path is not yet confirmed to work. React static assets are served successfully, but authenticated public API requests are blocked by the unresolved origin connectivity error.

## Local Development Setup

### Prerequisites

- Java 21 and a compatible Maven installation (or the included Maven Wrapper).
- Node.js and npm compatible with the project's Vite version.
- MySQL Server and MySQL Workbench (optional GUI).
- A local MySQL schema matching the project's existing table definitions.

### 1. Clone the repository

```bash
git clone https://github.com/IT25104032/RentFlow-Group25.git
cd RentFlow-Group25
```

### 2. Set up the database

Create a local MySQL database called `rentflow_db`, then execute the **project's existing approved SQL schema** and any required development seed scripts in the correct dependency order. The backend is configured to validate the schema rather than create tables automatically.

```sql
CREATE DATABASE IF NOT EXISTS rentflow_db;
```

> **Note:** SQL schema/seed-script paths are not listed here because their exact paths have not been verified. Use the schema maintained in the repository or supplied by the group, rather than inventing table definitions. Use synthetic data for demonstrations, especially for ID documents.

### 3. Configure the Spring Boot backend

```properties
spring.application.name=rentflow

# Local Database Configuration
#spring.datasource.url=jdbc:mysql://localhost:3306/rentflow_db
#spring.datasource.username=${DB_USERNAME}
#spring.datasource.password=${DB_PASSWORD}

# AWS RDS Configuration
spring.datasource.url=jdbc:mysql://rentflow-db.c92mmq6msml8.ap-southeast-2.rds.amazonaws.com:3306/rentflow_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

server.port=8081

spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB

server.tomcat.max-http-form-post-size=20MB
server.tomcat.max-swallow-size=20MB
```

From the backend project directory (containing `pom.xml`), start the application using the Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, package and run the Spring Boot JAR:

```powershell
.\mvnw.cmd clean package
```

Use the actual executable JAR filename generated in `target/` when running `java -jar`.

### 4. Run the React frontend

From the `frontend` directory:

```bash
npm install
npm run dev
```

Open the local address shown by Vite (commonly `http://localhost:5173`). In the deployment-ready frontend, requests use relative `/api/...` paths. For local development, the Vite proxy must forward `/api` to Spring Boot at `http://localhost:8081`.

### 5. Build for deployment

```bash
# From frontend/
npm run build
```

The production frontend build is generated in `frontend/dist/`. The backend is packaged as a runnable JAR using Maven. Avoid uploading the source folder or `node_modules` to S3.

## AWS Deployment Summary

The following describes the deployment steps completed and their verified outcomes, rather than claiming that the full public application works.

| Service | Configuration and verification |
|---|---|
| **Amazon RDS** | RDS MySQL database `rentflow_db` created; EC2-to-RDS connection and database access previously verified. |
| **Amazon EC2** | Java 21 installed; Spring Boot executable JAR uploaded and run as an enabled `systemd` service. |
| **Spring Boot API** | `GET http://localhost:8081/api/auth/csrf` returned **HTTP 200** from within EC2 during testing. |
| **Amazon S3** | React `dist` contents uploaded to private `rentflow-frontend` bucket. |
| **Amazon CloudFront** | Distribution configured with the S3 frontend origin, default root object `index.html`, and a separate `/api/*` API behavior. The public login page loads. |
| **VPC origin** | A CloudFront VPC origin was created and reported as **Deployed**, but successful end-to-end routing through it has **not been established**. |

### Known Deployment Limitation — CloudFront API Gateway Timeout

The publicly hosted login page currently displays **“Unable to obtain CSRF token”** when it attempts to authenticate. The message results from a failed request to the backend endpoint:

```text
GET https://d2xo1fit86o5d4.cloudfront.net/api/auth/csrf
```

The endpoint has returned **HTTP 504 Gateway Timeout** through CloudFront, even though the Spring Boot CSRF endpoint returned HTTP 200 when called locally on EC2.

This evidence indicates an **unresolved connection or origin-configuration issue between CloudFront and EC2**. It does **not** establish that CSRF token generation itself is defective. In the recorded configuration, the backend uses HTTP on port `8081`; origin protocol, port, network reachability, security-group access, and the active `/api/*` behavior require further validation.

**Impact:** Public login, and all workflows that rely on authenticated API requests, are **not yet demonstrated as working** via CloudFront. The React login page being visible does not confirm full system integration.

**Remaining work:** Verify the API behavior targets the correct backend origin; validate origin protocol and port; check network/security-group permissions and the relevant CloudFront configuration; resolve the 504 response; then test login, CSRF, renter, rental, return, and company-isolation workflows end to end.


## Screenshots and Deployment Evidence

| Evidence | Suggested filename                                                                                                                                                 | What to capture |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| Login page | [View screenshot](docs/rentflow-login.png)                                                                                                                         | React login screen at the CloudFront URL |
| EC2 instance | [View screenshot](docs/aws-ec2.png)                                                                                                                                | Instance overview and running state |
| Backend service | [View screenshot](docs/springboot-service.png)                                                                                                                     | `systemctl status rentflow` or sanitized backend health evidence |
| RDS database | [View screenshot](docs/aws-rds.png)                                                                                                                                | Database available and engine information |
| S3 frontend bucket | [View screenshot](docs/aws-s3-frontend.png)                                                                                                                        | `index.html` and `assets/` at the bucket root |
| S3 buckets | [View screenshot](docs/aws-s3-buckets.png)                                                                                                                         | Frontend bucket and separate JAR deployment bucket |
| CloudFront distribution | [View screenshot](docs/aws-cloudfront.png)                                                                                                                         | Distribution, origins, and default root object |
| CloudFront API behavior | [View screenshot](docs/aws-api-behaviour.png)                                                                                                                      | `/api/*` behavior and associated origin |
| Deployment limitation | [View screenshot](docs/cloudfront-api-504.png)                                                                                                                     | 504 response for `/api/auth/csrf`, with sensitive details hidden |
| Module 1–4 functionality | Screenshots of the main functionalities across Modules 1–4 are available in the docs/ directory, captured from the Rental Officer's perspective where applicable.  | Selected locally working screens for each project module |


## Collaboration and Individual Contributions

This repository contains work integrated from group members across the four modules. A branch may include merged changes authored by other members; **branch contents alone should not be interpreted as evidence of individual authorship**. Individual contributions should be assessed using authored commits, pull requests, and the relevant code history.

**Module 4-specific clarification:** See **[BRANCH_NOTES.md](BRANCH_NOTES.md)** for the original individual branch `feature/member4-returns-rental`, the integration/recovery branches (`member4-schema-integration`, `member4-integration`, and `feature/member4-returns-new`), and the explanation of the accidental merge from shared `main` into the individual working branch.

## Project Scope

RentFlow was created for academic assessment and demonstration. The published CloudFront URL currently demonstrates **frontend hosting only**, not a production-ready or fully functioning public rental service.
