# OrbitERP Status

## Current status

This repository has completed the initial project bootstrap for the MVP implementation plan.

## Completed work

- Created the monorepo structure with separate backend and frontend folders.
- Added the backend Spring Boot project skeleton with Java 17, Spring Boot, JPA, security, and Flyway configuration.
- Added the initial tenant, security, and common-domain base classes.
- Created the initial Flyway migration for tenants, users, and roles.
- Added the frontend Angular + Tailwind starter app shell.
- Set up Docker Compose for local and production deployment.
- Added a GitHub Actions deployment workflow stub.
- Added a project-level .gitignore.

## Repository layout

```text
orbiterp/
├── backend/
│   ├── src/main/java/com/orbiterp/
│   │   ├── auth/
│   │   ├── common/
│   │   ├── security/
│   │   └── tenant/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   ├── gradlew
│   └── gradlew.bat
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── angular.json
│   ├── tsconfig.json
│   └── Dockerfile
├── docker-compose.yml
├── docker-compose.prod.yml
├── .github/workflows/deploy.yml
├── .gitignore
├── OrbitERP — MVP Implementation Plan.md
└── STATUS.md
```

## Notes

- This is a foundation scaffold for the MVP and not yet a complete ERP feature set.
- The next implementation phase will focus on the tenant-routing and base-domain foundation from the project plan.
