# PlantUML System Diagrams

This directory contains the PlantUML (`.puml`) source files modeling the Kaluanã platform.

## Diagrams Included

1. `class-diagram.puml`
   - High-cohesion domain model structured into logical packages (`Identity & Access`, `Pairing & Delegation`, `Daily Planning & Tasks`, `Focus & Sensory Support`, `Self-Monitoring & Support`).
   - Reflects actual PostgreSQL foreign keys from Flyway migrations (`V1`..`V13`).
   - Weak dependencies explicitly identified (e.g., `PlannedBlock` referencing `Task` without database FK constraint).
   - Eliminates misplaced methods and empty classes from legacy models.

2. `use-case-diagram.puml`
   - Subsystem-partitioned use cases cleanly separating Primary User (ADHD individual), Responsible/Professional, and AI Assistant Service.
   - Standardized `<<extend>>`, `<<include>>`, and direct actor associations.

3. `architecture-diagram.puml`
   - Layered architecture: Mobile Client (React Native/Expo + Axios), Security/Filter Layer, Backend Core (Spring Boot), Relational DB (PostgreSQL 16), Cache/Rate Limiting (Redis 7), and External Services.

4. `focus-session-state-diagram.puml`
   - State transition diagram for the focus timer (`IDLE`, `RUNNING`, `PAUSED`, `COMPLETED`, `CANCELED`).

5. `replanning-activity-diagram.puml`
   - Activity workflow detailing deterministic schedule recalculation, block shifting, overflow detection, and user notification.

## How to Render

### Option 1: PlantUML CLI / Jar
```bash
java -jar plantuml.jar *.puml
```

### Option 2: VS Code / IDE Plugin
- Install the **PlantUML** extension (`jebbs.plantuml`).
- Open any `.puml` file and press `Alt + D` to preview.

### Option 3: Online PlantUML Server
- Copy and paste the contents into [PlantText](https://www.planttext.com/) or the official [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/).
