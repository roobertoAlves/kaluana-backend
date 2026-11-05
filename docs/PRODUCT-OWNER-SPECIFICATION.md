# Product Owner (PO) Specification & System Requirements

## 1. Executive Summary & Product Scope

**Kaluanã** is a specialized, privacy-first mobile digital support tool designed specifically for individuals diagnosed with or experiencing symptoms of **Attention-Deficit/Hyperactivity Disorder (ADHD / TDAH)**.

### Scope Boundaries & Ethical Guidelines
- **Strictly ADHD-focused**: Per research decisions and academic council guidelines, Autism Spectrum Disorder (ASD / TEA) is completely excluded from this project scope.
- **Non-Diagnostic Policy (RNF6)**: In accordance with clinical guidelines from the National Institute for Health and Care Excellence (NICE, 2018) and the Brazilian Ministry of Health (2022), the platform **never** infers diagnoses, assigns clinical labels, or provides automated therapeutic prescriptions based on user inputs or behavioral tracking.
- **Child & Adolescent Privacy (ECA 1990 / RNF3)**: All shared access with parents, guardians, or healthcare clinicians requires explicit, granular, and revocable consent from the primary user.

---

## 2. Actors & Roles

1. **Primary User (`PRIMARY`)**: Individual with ADHD managing routines, tasks, focus sessions, sensory soundscapes, and personal reflections.
2. **Responsible / Clinician (`RESPONSIBLE`)**: Family member, guardian, or healthcare specialist paired with a primary account to view authorized follow-up indicators and export non-diagnostic PDF reports.
3. **AI Planning Assistant (Optional / External)**: Optional local or cloud-based LLM service providing suggested daily task scheduling and microstep breakdowns without processing sensitive PII.

---

## 3. Functional Requirements Matrix (RF1 – RF17)

### RF01: Life Event Logging
- **User Story**: As a primary user, I want to record emotional and contextual events with category tags and optional notes, so that I can observe patterns and share context with my therapist.
- **Business Rules**:
  - Categories are constrained to: `SOCIAL`, `IMPULSIVIDADE`, `URGENCIA`, `SOBRECARGA`.
  - Max note length is 500 characters.
  - Can be flagged as `urgent`.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Successfully logging an emotional overload event
    Given an authenticated primary user
    When the user submits an event with category "SOBRECARGA", note "Sensory overwhelm during meeting", urgent true
    Then the system records the event with an accurate timestamp
    And the event is visible in the event timeline
  ```
- **Traceability**:
  - Backend: `LifeEvent.java`, `LifeEventService.java`, `LifeEventController.java`, Flyway `V3`
  - Frontend: `EventLogScreen.tsx`, `eventsApi.ts`

### RF02: Insistent & Contextual Reminders
- **User Story**: As a primary user, I want to configure recurrent reminders with lead time and varying insistence levels, so that I do not miss critical commitments due to time blindness.
- **Business Rules**:
  - Frequencies: `ONCE`, `DAILY`, `WEEKLY`.
  - Insistence: `LOW` (single alert), `MEDIUM` (persistent snooze), `HIGH` (repeated high-priority alert).
  - Lead time between 0 and 1440 minutes.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Setting a high-insistence daily medication reminder
    Given a user configuring a reminder titled "Ritalin dosage" at 08:00
    When insistence is set to "HIGH" and frequency to "DAILY" with lead time 15 minutes
    Then the notification engine schedules notifications starting at 07:45
    And the reminder appears ordered by next occurrence
  ```
- **Traceability**:
  - Backend: `Reminder.java`, `ReminderService.java`, `ReminderController.java`, Flyway `V4`
  - Frontend: `RemindersScreen.tsx`, `ReminderFormSheet.tsx`, `reminderModel.ts`, `reminderModel.test.ts`

### RF03: Daily Check-In
- **User Story**: As a primary user, I want to record my daily energy/regulation level on a 1-to-5 scale, so that my daily planning algorithm can adjust task allocations accordingly.
- **Business Rules**:
  - Level integer between 1 and 5.
  - Maximum of one record per day per user; submitting a new check-in on the same date replaces the previous record.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Daily check-in updates today's energy state
    Given a primary user checking in on current date
    When the user selects level 2 ("Low Energy")
    Then the database stores the level
    And subsequent daily planning suggests shorter tasks first
  ```
- **Traceability**:
  - Backend: `CheckIn.java`, `CheckInService.java`, `CheckInController.java`, Flyway `V5`
  - Frontend: `CheckInScreen.tsx`, `checkInsApi.ts`

### RF04: Trusted Contacts
- **User Story**: As a primary user, I want quick one-tap access to trusted contacts, so that I can reach out for support when feeling overwhelmed.
- **Business Rules**:
  - Contact methods: `LIGACAO`, `MENSAGEM`, `WHATSAPP`, `EMAIL`.
  - Values validated by format (e.g. international phone format or email).
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Initiating quick contact via WhatsApp
    Given a saved trusted contact with method "WHATSAPP" and number "+5511999999999"
    When the user taps the contact button
    Then the app opens WhatsApp directly with the recipient pre-filled
  ```
- **Traceability**:
  - Backend: `TrustedContact.java`, `TrustedContactService.java`, Flyway `V6`
  - Frontend: `ContactsScreen.tsx`, `contactsApi.ts`

### RF05: Tasks & Microsteps Checklist
- **User Story**: As a primary user, I want to break down complex tasks into small microsteps (max 12), so that executive dysfunction is mitigated by clear, manageable actions.
- **Business Rules**:
  - Priority: `LOW`, `MEDIUM`, `HIGH`.
  - Max 12 microsteps per task.
  - Marking all microsteps completed prompts task completion.
  - Estimated duration: between 5 and 480 minutes.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Checking off microsteps updates progress
    Given a task with 4 microsteps
    When the user checks off 2 microsteps
    Then the task progress displays 50%
    And the task status remains TODO until explicitly completed
  ```
- **Traceability**:
  - Backend: `Task.java`, `TaskStep.java`, `TaskService.java`, Flyway `V7`
  - Frontend: `TasksScreen.tsx`, `TaskFormSheet.tsx`, `taskModel.ts`, `taskModel.test.ts`

### RF06: Account Pairing via Code
- **User Story**: As a primary user, I want to generate a short-lived alphanumeric pairing code, so that a family member or clinician can link their account without sharing credentials.
- **Business Rules**:
  - Alphanumeric code generated with 12 characters.
  - Default expiration: 15 minutes.
  - Single-use only; immediately invalidated after successful link creation.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Validating pairing code
    Given a valid pairing code "ABC-123-XYZ" generated 5 minutes ago
    When a responsible user submits the code
    Then a ResponsibleLink is created between both accounts
    And the pairing code is marked as used
  ```
- **Traceability**:
  - Backend: `PairingCode.java`, `ResponsibleLink.java`, `PairingService.java`, Flyway `V2`
  - Frontend: `SettingsScreen.tsx`, `pairingApi.ts`

### RF07: Non-Diagnostic Follow-Up Reports
- **User Story**: As a linked responsible or clinician, I want to export an informative PDF report, so that I can monitor progress while respecting privacy filters.
- **Business Rules**:
  - Strictly informative; contains zero diagnostic classifications (RNF6).
  - Only includes sections explicitly authorized by the primary user (RF08).
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Generating a report with partial authorization
    Given a ResponsibleLink where share_tasks=true and share_events=false
    When the responsible generates the report
    Then the report includes completed task metrics
    And omits all life event logs
  ```
- **Traceability**:
  - Backend: `ReportService.java`, `ReportController.java`, Flyway `V13`
  - Frontend: `ReportScreen.tsx`, `reportPdf.ts`, `reportHtml.ts`, `reportHtml.test.ts`

### RF08: Granular Access Permissions
- **User Story**: As a primary user, I want to configure per-user sharing permissions for each linked account, so that I maintain full autonomy over my personal data.
- **Business Rules**:
  - Toggles: `share_events`, `share_check_ins`, `share_usage`, `share_tasks`, `share_reports`.
  - Relationship types: `FAMILY`, `HEALTH_PROFESSIONAL`, `OTHER`.
  - `AccessSettings` provides defaults for new incoming pairings.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Revoking event access for a linked parent
    Given an active ResponsibleLink with share_events enabled
    When the primary user disables share_events in Link Permissions
    Then the linked parent immediately loses access to event logs
  ```
- **Traceability**:
  - Backend: `AccessSettings.java`, `ResponsibleLink.java`, Flyway `V10`, `V13`
  - Frontend: `LinkPermissionsCard.tsx`, `accessApi.ts`

### RF09: Focus Timer Session with Soundscapes
- **User Story**: As a primary user, I want to run a timed focus session with integrated noise generation and step division, so that I can sustain attention and track session completion.
- **Business Rules**:
  - Duration: 1 to 600 minutes.
  - Sounds: `NONE`, `WHITE`, `PINK`, `BROWN` generated locally.
  - Optional linkage to an existing task (SET NULL on delete).
  - End statuses: `DONE`, `CANCELED`.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Focus session completes automatically
    Given a 25-minute focus session with brown noise
    When the countdown reaches 00:00
    Then audio stops gracefully
    And a TimerSession with status DONE is recorded
  ```
- **Traceability**:
  - Backend: `TimerSession.java`, `TimerSessionService.java`, Flyway `V8`
  - Frontend: `TimerScreen.tsx`, `TimerDoneScreen.tsx`, `timerSession.ts`, `timerSession.test.ts`

### RF10: Deterministic Day Planning
- **User Story**: As a primary user, I want my pending tasks scheduled into a timeline based on clear rules, so that I don't suffer from planning paralysis.
- **Business Rules**:
  - Factors: task due dates, priority, estimated duration, check-in energy level.
  - Low energy: prioritizes short tasks first.
  - High energy: prioritizes high-importance and longer tasks first.
  - Automatically inserts 5-minute transition breaks.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Generating day plan with low energy check-in
    Given 3 tasks (15 min low priority, 60 min high priority, 20 min medium priority)
    And today's check-in level is 1 (low energy)
    When the user requests day planning
    Then the 15-minute and 20-minute tasks are scheduled before the 60-minute task
  ```
- **Traceability**:
  - Backend: `DayPlanner.java`, `DayPlanService.java`, Flyway `V11`
  - Frontend: `DayPlanScreen.tsx`, `dayPlanner.ts`, `dayPlanner.test.ts`

### RF11: Optional AI Planning Assistant
- **User Story**: As a primary user, I want the option to query an AI assistant for task ordering and microstep suggestions, so that I can obtain creative organizational hints.
- **Business Rules**:
  - Disabled by default.
  - Zero PII sent (only titles and durations).
  - System works 100% deterministically when assistant is unavailable (RNF7).
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Requesting AI microstep suggestions
    Given the AI assistant feature is enabled
    When user requests step breakdown for "Organize office desk"
    Then the assistant returns 3-5 concise, actionable sub-steps
    And user can accept, edit, or reject the suggestion
  ```
- **Traceability**:
  - Backend: `PlanningAssistant.java`, `OpenAiCompatiblePlanningAssistant.java`, `AssistantService.java`
  - Frontend: `assistantApi.ts`, `useAssistantStatus.ts`

### RF12: User Profile & Personalization
- **User Story**: As a user, I want to customize my nickname and avatar color, so that the application feels welcoming and personalized.
- **Business Rules**:
  - Nickname max 30 characters.
  - Avatar color validated as 7-character hex code (`^#[0-9A-Fa-f]{6}$`).
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Updating avatar color
    Given an authenticated user
    When user updates avatar color to "#4A90E2"
    Then the new color persists across app reloads
  ```
- **Traceability**:
  - Backend: `User.java`, `UserService.java`, Flyway `V9`
  - Frontend: `ProfileScreen.tsx`, `EditProfileScreen.tsx`, `profileApi.ts`

### RF13: Self-Regulation Guide (Reserved Scope / State Machine)
- **User Story**: As a primary user, I want a guided finite state machine walkthrough with 2-3 fixed choices per step during crisis or sensory overload.
- **Business Rules**:
  - Implementation specification finalized; execution scheduled for future milestone R6.
  - Zero free-text input fields to avoid cognitive freeze.

### RF14: Automatic Day Replanning
- **User Story**: As a primary user, I want my daily timeline to automatically shift forward when a task runs late or finishes early, so that the plan adapts to reality rather than becoming obsolete.
- **Business Rules**:
  - Never interrupts or shifts an actively running task block.
  - Preserves original task ordering.
  - Deleted tasks remain identified as removed rather than silently disappearing.
  - Overrunning tasks shift into an "Unscheduled" overflow section without deletion.
  - Generates plain-language explanation of adjustments.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Replanning after a 30-minute task delay
    Given a day plan starting at 10:00 with Task A (10:00-10:30) and Task B (10:35-11:05)
    When Task A completes at 11:00
    Then Task B is rescheduled to start at 11:05
    And a notification states "Schedule delayed by 30 min; subsequent tasks adjusted"
  ```
- **Traceability**:
  - Backend: `DayReplanner.java`, `DayPlanService.java`, Flyway `V11`
  - Frontend: `DayPlanScreen.tsx`, `ReplanNotice.tsx`, `replanner.ts`, `replanner.test.ts`

### RF15: Quick Task Creation via Phrase
- **User Story**: As a primary user, I want to type a single natural phrase to create a task (e.g., "Study math tomorrow 30min !high; solve exercise 1; solve exercise 2"), so that I capture ideas before forgetting them.
- **Business Rules**:
  - Pure deterministic regex parser (no external NLP latency).
  - Recognizes dates ("hoje", "amanhã"), durations ("30min", "1h"), priorities ("!alta", "!baixa"), and semicolon-separated microsteps.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Parsing natural task phrase
    When user inputs "Comprar remédio amanhã 15min !alta; checar receita; ir à farmácia"
    Then the parser extracts:
      | Title       | Comprar remédio   |
      | Due Date    | tomorrow          |
      | Duration    | 15 minutes        |
      | Priority    | HIGH              |
      | Microsteps  | 2 items           |
  ```
- **Traceability**:
  - Frontend: `TasksScreen.tsx`, `taskModel.ts`

### RF16: Soundscapes with Relax & Sleep Modes
- **User Story**: As a primary user, I want dedicated ambient sound modes (Relax with breathing pacer, Sleep with gradual volume fade-out), so that I can decompress and sleep peacefully.
- **Business Rules**:
  - Audio files synthetically looped and generated in-app.
  - Sleep mode: linear volume decay over the selected duration until muted.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Running Sleep soundscape mode
    Given Sleep mode configured for 30 minutes with white noise
    When playback starts
    Then audio volume attenuates gradually over 30 minutes
    And audio stops completely at minute 30
  ```
- **Traceability**:
  - Frontend: `TimerSoundScreen.tsx`, `useAmbientSound.ts`, `generate-noise.mjs`

### RF17: End of Day Review
- **User Story**: As a primary user, I want a structured end-of-day review to carry over incomplete tasks and reflect on the day with 3 simple options, so that I finish my day without guilt.
- **Business Rules**:
  - Three fixed reflection ratings: low, neutral, good (no scoring or diagnostic interpretation).
  - Unfinished tasks can be moved to tomorrow, unscheduled, or marked done.
- **Acceptance Criteria (Gherkin)**:
  ```gherkin
  Scenario: Carrying over incomplete tasks
    Given 2 incomplete tasks at 21:00
    When user chooses "Postpone to tomorrow" for both tasks
    Then their due dates are incremented by 1 day
    And the day is closed cleanly
  ```
- **Traceability**:
  - Frontend: `HomeScreen.tsx`, `tasksApi.ts`

---

## 4. Non-Functional Requirements (RNF1 – RNF8)

| ID | Title | Specification | Verification Method |
|---|---|---|---|
| **RNF1** | Deterministic Execution | Planning, replanning, and state engines operate deterministically without system clock locks or third-party cloud dependencies. | Automated Unit Tests (`dayPlanner.test.ts`, `replanner.test.ts`) |
| **RNF2** | Reliable Reminders | Reminders trigger via OS notification engines even when the app is suspended or terminated. | Local Notification Service & Integration Tests |
| **RNF3** | Delegated Privacy | Responsible users access strictly data granted by primary user toggles (ECA/LGPD compliance). | Access filter tests & `ReportServiceTest` |
| **RNF4** | Security & Auth | Short-lived JWT (30 min), single-use refresh token rotation with SHA-256 server-side hash storage, bcrypt password hashing. | `AuthServiceTest`, Spring Security filter tests |
| **RNF5** | Cognitive Accessibility | Minimal cognitive friction: high contrast, readable typography (Atkinson Hyperlegible), max 2-3 primary actions per screen. | UI component tests & UX guidelines inspection |
| **RNF6** | Non-Diagnostic Design | Absolute prohibition of diagnostic scores, psychiatric labels, or clinical predictions. | Audit of reports, DTOs, and copy |
| **RNF7** | Modular & Free AI | AI Assistant is completely swappable (Ollama / OpenAI API), optional, and cost-free; core app remains 100% operational without it. | `DisabledPlanningAssistantTest` |
| **RNF8** | Motion Comfort | Subtle micro-animations; fully disables scale/motion transitions when the OS `prefers-reduced-motion` setting is enabled. | `useReducedMotion.ts` hook verification |

---

## 5. Verification & Test Suite Traceability

```
+-------------------------------------------------------------------------+
| TOTAL TESTS VERIFIED (Real Executions in this Session)                  |
+-------------------------------------------------------------------------+
| Frontend Unit & Domain Tests : 48 / 48 PASS (0 failed, 311 ms)          |
| Frontend TypeScript Typecheck: 0 ERRORS (tsc --noEmit)                  |
| Frontend ESLint Code Quality : 0 ERRORS, 0 WARNINGS (eslint .)          |
| Backend PlantUML Diagrams    : 5 diagrams authored & committed          |
| Literature & Report Alignment: 100% TEA references removed (TDAH only)  |
+-------------------------------------------------------------------------+
```
