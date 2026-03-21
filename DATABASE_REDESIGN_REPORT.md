# BookNow — Database Redesign \& Housekeeping Module Report

> \*\*Prepared by\*\*: Senior Software Architect / Senior Database Architect  
> \*\*Date\*\*: 2026-03-12  
> \*\*System\*\*: BookNow — Homestay Booking System (SWP392)  
> \*\*Database\*\*: Microsoft SQL Server  
> \*\*Backend\*\*: Java Spring Boot (JPA/Hibernate)

\---

## Table of Contents

1. [System Analysis](#1-system-analysis)
2. [Database Anti-Pattern Detection](#2-database-anti-pattern-detection)
3. [Room Status Lifecycle Redesign](#3-room-status-lifecycle-redesign)
4. [Housekeeping System Design](#4-housekeeping-system-design)
5. [Database Refactor Strategy](#5-database-refactor-strategy)
6. [New Tables SQL Design](#6-new-tables-sql-design)
7. [Index Optimization Plan](#7-index-optimization-plan)
8. [Migration Strategy](#8-migration-strategy)
9. [Final Database Schema](#9-final-database-schema)

\---

## 1\. System Analysis

### 1.1 Current System Architecture

BookNow is a **monolithic Spring Boot 3-tier web application** for managing homestay room bookings. It uses:

|Layer|Technology|
|-|-|
|Frontend|Server-rendered views (Thymeleaf)|
|Backend|Java, Spring Boot, Spring MVC, Spring Security|
|ORM|JPA / Hibernate|
|Database|Microsoft SQL Server|
|Auth|JWT + Refresh Token, Google OAuth2, reCAPTCHA|
|Payment|MoMo Sandbox (HMAC-SHA256)|
|Media|Cloudinary (video/image CDN)|
|Real-time|WebSocket (check-in notifications)|

Architecture flow: `Browser → Controller → Service → Repository → SQL Server`

Three user roles exist: **Customer**, **Staff**, **Admin**. There is **no Housekeeping role** in the current system.

### 1.2 Current Room Lifecycle

The current Room entity tracks only **3 statuses**:

```
AVAILABLE ──► BOOKED ──► AVAILABLE (after checkout)
    │                        ▲
    ▼                        │
MAINTENANCE ─────────────────┘
```

**Critical gap**: There is no intermediate state between checkout and the room becoming available again. The system sets the room directly to `AVAILABLE` after checkout, which means:

* No tracking of whether the room was cleaned
* No tracking of whether the room is physically ready for the next guest
* No signal to housekeeping staff

### 1.3 Booking → Check-in → Check-out Flow

```
Customer creates booking
    → Status: PENDING
    → Staff approves
    → Status: WAITING\_PAYMENT
    → Customer pays via MoMo
    → Status: PAID
    → Customer uploads check-in video
    → Admin reviews and approves
    → Status: CHECKED\_IN
    → Customer confirms checkout
    → Status: CHECKED\_OUT
    → Room.status → AVAILABLE (immediately!)
    → Status: COMPLETED
```

**Problem**: Checkout triggers `Room.status = 'AVAILABLE'` directly. In a real hotel, checkout should trigger `Room.status = 'DIRTY'` or `'CLEANING'`, and only after housekeeping confirms should it become `AVAILABLE`.

### 1.4 Current Room Status Management

Room status is managed by a `VARCHAR(50)` column with a CHECK constraint:

```sql
CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE'))
```

Status transitions happen in service-layer code:

* **Booking created** → Room stays as-is (scheduler handles availability)
* **Checkout** → Room set to `AVAILABLE`
* **Staff manual action** → Room set to `MAINTENANCE` or back to `AVAILABLE`

There is **no event log, no audit trail, no status history** for room state changes.

### 1.5 Weak Points in the Current Design

|#|Issue|Impact|
|-|-|-|
|W1|Room goes directly to AVAILABLE after checkout|Dirty rooms may be assigned to new guests|
|W2|No CLEANING / DIRTY status exists|Housekeeping staff cannot see which rooms need attention|
|W3|No housekeeping task tracking|No record of who cleaned what, when|
|W4|No overdue checkout detection mechanism|Staff cannot see guests who overstay|
|W5|No room inspection after cleaning|No quality control for room readiness|
|W6|No maintenance request workflow|Maintenance is just a binary status toggle|
|W7|Pricing is on Room instead of RoomType|Data inconsistency across rooms of the same type|
|W8|CheckInSession table is documented but missing from SQL|Incomplete schema deployment|
|W9|Feedback.admin\_id is NOT NULL|Cannot create feedback without assigning an admin first|
|W10|No room status change history|Cannot audit or debug status issues|
|W11|No actual checkout timestamp|Only planned checkout time exists|
|W12|Customer who leaves without checkout → room stuck in BOOKED|No automated overdue handling|

### 1.6 Problems Caused by Current Database Structure

1. **Operational blind spot**: After checkout, the room is immediately `AVAILABLE` — housekeeping doesn't know which rooms need cleaning.
2. **Guest walks out without checkout**: Room stays `BOOKED` indefinitely. No one is alerted.
3. **No maintenance workflow**: Setting a room to `MAINTENANCE` has no description, no tracking, no resolution path.
4. **Price drift**: If `base\_price` differs across rooms of the same type, the system shows inconsistent pricing.
5. **Dead invoice table**: Invoice has no meaningful data (no amount, no tax, no line items).
6. **Payment timing error**: `paid\_at` defaults to `sysdatetime()` at record creation, not when payment actually succeeds.

\---

## 2\. Database Anti-Pattern Detection

### AP-01: Price Stored in Room Instead of RoomType ⚠️ CRITICAL

**Current state**: `Room` table contains `base\_price`, `over\_price`, `description`.  
**Documentation says**: `RoomType` should have `base\_price`, `over\_price`, `description`.  
**Actual RoomType schema**: Only has `room\_type\_id`, `name`, `image\_url`, `max\_guests`, `is\_deleted`.

**Impact**:

* If you want to change the price for all "Deluxe" rooms, you must update every individual Room row.
* Two rooms of the same type can show different prices (data inconsistency).
* Room filtering by price is correct per-room, but marketing/display "starting from" prices require aggregation.

**Verdict**: The design docs are correct — `base\_price`, `over\_price`, and `description` belong on `RoomType`. The implementation deviated.

\---

### AP-02: Room Status Enum Too Simplistic

**Current**: `CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE'))`

**Missing states**: `OCCUPIED`, `DIRTY`, `CLEANING`, `INSPECTING`, `OUT\_OF\_SERVICE`

**Impact**: Cannot model the real room lifecycle. Checkout → AVAILABLE skips the cleaning phase entirely.

\---

### AP-03: Feedback.admin\_id is NOT NULL

```sql
\[admin\_id] \[bigint] NOT NULL
```

Feedback is created by *customers*. At creation time, no admin has been assigned yet. The `admin\_id` should be **nullable** — it gets populated only when a staff member replies.

**Impact**: Application code must either:

* Assign a "dummy" admin\_id (bad)
* Force an admin assignment at feedback creation time (wrong business logic)

\---

### AP-04: No CheckInSession Table in Schema

The `DATA\_MODEL\_OVERVIEW.md` documents a `CheckInSession` table, but it does not exist in `database\_schema.txt`.

**Impact**: Check-in video workflow is either:

* Not yet deployed to the production schema
* Handled via a column on Booking (fragile)
* Managed outside the DB (data loss risk)

\---

### AP-05: Payment.paid\_at Defaults to sysdatetime() at Creation

```sql
ALTER TABLE \[dbo].\[Payment] ADD CONSTRAINT \[DF\_Payment\_PaidAt] DEFAULT (sysdatetime()) FOR \[paid\_at]
```

The payment record is created with status `PENDING` before the customer pays. `paid\_at` should only be set when `payment\_status` becomes `SUCCESS`. Setting it at creation means the timestamp is wrong.

**Impact**: Reporting shows incorrect payment timestamps. Financial reconciliation is unreliable.

\---

### AP-06: No Booking Status CHECK Constraint

```sql
\[booking\_status] \[nvarchar](20) NOT NULL
-- No CHECK constraint!
```

Status transitions are enforced only in application code. Any direct DB operation or bug can insert invalid statuses.

**Impact**: Data integrity relies entirely on application layer — risky for production.

\---

### AP-07: Missing updated\_at on Booking Table

The column is named `update\_at` (typo, missing 'd') and exists, but many other tables lack `updated\_at` entirely (Room, RoomType, Payment, etc.).

**Impact**: Cannot track when records were last modified. Audit trail is incomplete.

\---

### AP-08: No Room Status History / Audit Log

There is no table recording room status changes. When debugging "why is this room BOOKED?", staff have no history to examine.

**Impact**: Operational blind spot. Cannot trace issues.

\---

### AP-09: Invoice Table is Nearly Empty

```sql
CREATE TABLE \[dbo].\[Invoice](
    \[invoice\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id] \[bigint] NOT NULL,
    \[invoice\_number] \[nvarchar](50) NULL,
    \[issued\_at] \[datetime2](7) NOT NULL
)
```

An invoice with no `amount`, no `tax`, no `customer\_name`, no `line\_items` is not a real invoice. It's a placeholder.

**Impact**: Cannot generate actual invoices. Low priority to fix now since it's not related to housekeeping.

\---

### AP-10: RoomAmenity Missing Unique Constraint

```sql
CREATE TABLE \[dbo].\[RoomAmenity](
    \[room\_amenity\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id] \[bigint] NOT NULL,
    \[amenity\_id] \[bigint] NOT NULL
)
```

No `UNIQUE(room\_id, amenity\_id)` constraint. The same amenity can be linked to the same room multiple times.

**Impact**: Duplicate data, incorrect counts in UI display.

\---

### AP-11: No Soft Delete Consistency

`Customer` and `StaffAccounts` have `is\_deleted`, but `Booking`, `Payment`, `Invoice`, `Scheduler`, `Timetable` do not. Inconsistent deletion strategy.

\---

### AP-12: Booking Lacks Actual Check-in/Check-out Timestamps

`check\_in\_time` and `check\_out\_time` are **planned** times set at booking creation. There is no `actual\_check\_in\_time` or `actual\_check\_out\_time`. (Note: The docs mention that `check\_in\_time` is set during check-in approval, but `check\_out\_time` is always the planned time.)

**Impact**: Cannot calculate actual stay duration. Cannot detect overdue checkouts by comparing `check\_out\_time` with `NOW()`.

\---

## 3\. Room Status Lifecycle Redesign

### 3.1 New Room Statuses

|Status|Description|Who Sets It|Trigger|
|-|-|-|-|
|`AVAILABLE`|Room is clean, inspected, and ready for booking|Housekeeping / System|After cleaning completed or maintenance resolved|
|`BOOKED`|Room has an active reservation (future)|System|Booking confirmed/paid|
|`OCCUPIED`|Guest has physically checked in|System|Admin approves check-in|
|`CHECKOUT\_PENDING`|Check-out time has passed but guest has not checked out|System (scheduler)|`NOW() > check\_out\_time` while status is `OCCUPIED`|
|`DIRTY`|Guest checked out; room needs cleaning|System|Checkout completed|
|`CLEANING`|Housekeeping staff has started cleaning|Housekeeping staff|Staff starts cleaning task|
|`MAINTENANCE`|Room requires repair / maintenance work|Housekeeping staff / Staff|Reported during cleaning or by staff|
|`OUT\_OF\_SERVICE`|Room is temporarily removed from inventory|Admin|Administrative decision|

### 3.2 Status Transition Diagram

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                                                         │
                    ▼                                                         │
              ┌───────────┐  Booking paid   ┌────────┐  Check-in    ┌──────────┐
              │ AVAILABLE  │ ──────────────► │ BOOKED │ ──────────► │ OCCUPIED  │
              └───────────┘                  └────────┘  approved    └──────────┘
                    ▲                                                    │
                    │                                          ┌────────┤
                    │                                          │        │
                    │                              Checkout    │  Time   │
                    │                              confirmed   │  passes │
                    │                                          ▼        ▼
                    │                                    ┌───────┐  ┌──────────────────┐
                    │                                    │ DIRTY  │  │ CHECKOUT\_PENDING  │
                    │                                    └───────┘  └──────────────────┘
                    │                                        │              │
                    │                           Staff starts │    Customer  │
                    │                            cleaning    │  checks out  │
                    │                                        ▼              │
                    │                                   ┌──────────┐       │
                    │                                   │ CLEANING  │◄──────┘
                    │                                   └──────────┘
                    │                                        │
                    │                            ┌───────────┤
                    │               Cleaning     │           │  Issue found
                    │               completed    │           │
                    │                            ▼           ▼
                    │                      ┌───────────┐  ┌─────────────┐
                    └──────────────────────│ AVAILABLE  │  │ MAINTENANCE  │
                                           └───────────┘  └─────────────┘
                                                 ▲              │
                                                 │  Resolved    │
                                                 └──────────────┘

              ┌────────────────┐
              │ OUT\_OF\_SERVICE  │  ← Admin can set from any status
              └────────────────┘    → Admin restores to AVAILABLE
```

### 3.3 Transition Rules

|#|From|To|Trigger|Actor|
|-|-|-|-|-|
|T1|`AVAILABLE`|`BOOKED`|Booking reaches PAID status|System|
|T2|`BOOKED`|`OCCUPIED`|Admin approves check-in|Admin|
|T3|`OCCUPIED`|`DIRTY`|Customer confirms checkout|Customer / System|
|T4|`OCCUPIED`|`CHECKOUT\_PENDING`|`NOW() > booking.check\_out\_time` (scheduled job)|System (cron)|
|T5|`CHECKOUT\_PENDING`|`DIRTY`|Staff forces checkout / Customer late-checks-out|Staff / Customer|
|T6|`DIRTY`|`CLEANING`|Housekeeping starts cleaning task|Housekeeping|
|T7|`CLEANING`|`AVAILABLE`|Housekeeping completes all checklist items|Housekeeping|
|T8|`CLEANING`|`MAINTENANCE`|Housekeeping reports issue during cleaning|Housekeeping|
|T9|`MAINTENANCE`|`AVAILABLE`|Maintenance resolved by staff/housekeeping|Staff / Housekeeping|
|T10|Any|`OUT\_OF\_SERVICE`|Admin takes room offline|Admin|
|T11|`OUT\_OF\_SERVICE`|`AVAILABLE`|Admin restores room|Admin|

### 3.4 Overdue Checkout Detection

A **scheduled job** (e.g., Spring `@Scheduled` cron every 5–15 minutes) should:

```
SELECT b.booking\_id, b.room\_id, r.room\_number
FROM Booking b
JOIN Room r ON b.room\_id = r.room\_id
WHERE b.booking\_status = 'CHECKED\_IN'
  AND r.status = 'OCCUPIED'
  AND b.check\_out\_time < GETDATE()
```

For each result:

1. Update `Room.status = 'CHECKOUT\_PENDING'`
2. Create a `HousekeepingTask` with `task\_type = 'OVERDUE\_CHECKOUT'`
3. Optionally notify staff via WebSocket

\---

## 4\. Housekeeping System Design

### 4.1 Overview

The Housekeeping module introduces:

* A new role: **HOUSEKEEPING** (added to `StaffAccounts.role`)
* New tables for task management and room cleaning tracking
* Dashboard views for housekeeping staff

### 4.2 New Role: HOUSEKEEPING

Added to `StaffAccounts.role` CHECK constraint:

```sql
CHECK (role IN ('ADMIN', 'STAFF', 'HOUSEKEEPING'))
```

Housekeeping staff can:

|Permission|Description|
|-|-|
|View task list|See all rooms needing cleaning/maintenance|
|View task detail|See room info, checklist, notes|
|Start cleaning|Mark a DIRTY room as CLEANING|
|Complete cleaning|Mark a CLEANING room as AVAILABLE|
|Report maintenance|Flag room for maintenance|
|Add notes|Record observations during inspection|

Housekeeping staff **cannot**:

* Manage bookings, customers, or payments
* Create/delete rooms
* Access revenue reports
* Manage user accounts

### 4.3 Required Tables

|Table|Purpose|
|-|-|
|`HousekeepingTask`|Central task tracking — one task per room cleaning/maintenance event|
|`TaskChecklist`|Individual checklist items within a task (bed, bathroom, floor, etc.)|
|`RoomStatusLog`|Audit trail: every room status change, by whom, when|

### 4.4 HousekeepingTask Design

Each time a room needs housekeeping attention, a task is created:

```
Fields:
  task\_id (PK)
  room\_id (FK → Room)
  booking\_id (FK → Booking, nullable — maintenance may not be booking-related)
  assigned\_to (FK → StaffAccounts, nullable — unassigned tasks exist)
  task\_type: CLEANING | MAINTENANCE | INSPECTION | OVERDUE\_CHECKOUT
  task\_status: PENDING | IN\_PROGRESS | COMPLETED | CANCELLED
  priority: LOW | NORMAL | HIGH | URGENT
  notes (staff notes)
  created\_at
  started\_at
  completed\_at
  created\_by (FK → StaffAccounts, nullable — system-generated tasks)
```

### 4.5 TaskChecklist Design

Pre-defined checklist items for cleaning tasks:

```
Fields:
  checklist\_id (PK)
  task\_id (FK → HousekeepingTask)
  item\_name (e.g., "Change bedsheets", "Clean bathroom")
  is\_completed (BIT)
  completed\_at
```

### 4.6 RoomStatusLog Design

Every room status transition is logged:

```
Fields:
  log\_id (PK)
  room\_id (FK → Room)
  previous\_status
  new\_status
  changed\_by (FK → StaffAccounts, nullable — system changes have NULL)
  change\_reason (description)
  booking\_id (FK → Booking, nullable)
  created\_at
```

### 4.7 Housekeeping Dashboard Queries

**1. Rooms needing cleaning** (DIRTY status):

```sql
SELECT r.room\_id, r.room\_number, rt.name AS room\_type
FROM Room r
JOIN RoomType rt ON r.room\_type\_id = rt.room\_type\_id
WHERE r.status = 'DIRTY' AND r.is\_deleted = 0
```

**2. Rooms needing maintenance**:

```sql
SELECT r.room\_id, r.room\_number, ht.notes, ht.priority
FROM Room r
JOIN HousekeepingTask ht ON r.room\_id = ht.room\_id
WHERE r.status = 'MAINTENANCE'
  AND ht.task\_status IN ('PENDING', 'IN\_PROGRESS')
  AND r.is\_deleted = 0
```

**3. Rooms currently being cleaned**:

```sql
SELECT r.room\_id, r.room\_number, ht.assigned\_to, sa.full\_name, ht.started\_at
FROM Room r
JOIN HousekeepingTask ht ON r.room\_id = ht.room\_id
JOIN StaffAccounts sa ON ht.assigned\_to = sa.staff\_account\_id
WHERE r.status = 'CLEANING'
  AND ht.task\_status = 'IN\_PROGRESS'
```

**4. Overdue checkout rooms**:

```sql
SELECT r.room\_id, r.room\_number, b.booking\_id, b.check\_out\_time,
       c.full\_name AS guest\_name,
       DATEDIFF(MINUTE, b.check\_out\_time, GETDATE()) AS minutes\_overdue
FROM Room r
JOIN Booking b ON r.room\_id = b.room\_id
JOIN Customer c ON b.customer\_id = c.customer\_id
WHERE r.status IN ('OCCUPIED', 'CHECKOUT\_PENDING')
  AND b.booking\_status = 'CHECKED\_IN'
  AND b.check\_out\_time < GETDATE()
ORDER BY b.check\_out\_time ASC
```

**5. Rooms reported for problems**:

```sql
SELECT r.room\_id, r.room\_number, ht.task\_id, ht.notes, ht.priority, ht.created\_at
FROM Room r
JOIN HousekeepingTask ht ON r.room\_id = ht.room\_id
WHERE ht.task\_type = 'MAINTENANCE'
  AND ht.task\_status IN ('PENDING', 'IN\_PROGRESS')
ORDER BY
  CASE ht.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END,
  ht.created\_at ASC
```

\---

## 5\. Database Refactor Strategy

### 5.1 Table-by-Table Decision

|#|Table|Decision|Action|Reason|
|-|-|-|-|-|
|01|`Customer`|**KEEP**|No changes|Working correctly|
|02|`StaffAccounts`|**MODIFY**|Update role CHECK constraint|Add `'HOUSEKEEPING'` role|
|03|`RefreshTokens`|**KEEP**|No changes|Working correctly|
|04|`RoomType`|**EXTEND**|Add `base\_price`, `over\_price`, `description` columns|Fix AP-01: pricing belongs on type|
|05|`Room`|**MODIFY**|Update status CHECK constraint; keep `base\_price`/`over\_price` temporarily for backward compat|Add new statuses; mark price columns for deprecation|
|06|`Amenity`|**KEEP**|No changes|Working correctly|
|07|`RoomAmenity`|**EXTEND**|Add UNIQUE constraint `(room\_id, amenity\_id)`|Fix AP-10: prevent duplicates|
|08|`Image`|**KEEP**|No changes|Working correctly|
|09|`Booking`|**EXTEND**|Add `actual\_check\_in\_time`, `actual\_check\_out\_time`; add status CHECK|Fix AP-06, AP-12|
|10|`Payment`|**MODIFY**|Make `paid\_at` nullable, remove default|Fix AP-05: timestamp only on success|
|11|`Invoice`|**KEEP**|No changes now|Low priority; not related to housekeeping|
|12|`Feedback`|**MODIFY**|Make `admin\_id` nullable|Fix AP-03: feedback exists before reply|
|13|`Timetable`|**KEEP**|No changes|Working correctly|
|14|`Scheduler`|**KEEP**|No changes|Working correctly|
|15|`CheckInSession`|**ADD**|Create table (missing from schema)|Fix AP-04|
|16|`HousekeepingTask`|**ADD**|New table|Housekeeping module|
|17|`TaskChecklist`|**ADD**|New table|Housekeeping module|
|18|`RoomStatusLog`|**ADD**|New table|Room audit trail|

### 5.2 Columns to Deprecate (Not Remove)

To maintain backward compatibility, these columns will remain but should be deprecated in code:

|Table|Column|Reason|
|-|-|-|
|`Room`|`base\_price`|Will be migrated to `RoomType.base\_price`; keep for existing code|
|`Room`|`over\_price`|Will be migrated to `RoomType.over\_price`; keep for existing code|
|`Room`|`description`|Will be migrated to `RoomType.description`; keep for existing code|

**Code migration path**: Update service layer to read price from `RoomType` first, fall back to `Room` if null. Eventually remove Room-level price columns after all code is updated.

\---

## 6\. New Tables SQL Design

### 6.1 CheckInSession (Missing — Must Create)

```sql
CREATE TABLE \[dbo].\[CheckInSession](
    \[check\_in\_session\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id]          \[bigint] NOT NULL,
    \[video\_url]           \[nvarchar](500) NOT NULL,
    \[video\_public\_id]     \[nvarchar](255) NULL,
    \[status]              \[varchar](20) NOT NULL DEFAULT 'PENDING',
    \[reviewed\_by]         \[bigint] NULL,
    \[created\_at]          \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[reviewed\_at]         \[datetime2](7) NULL,

    CONSTRAINT \[PK\_CheckInSession] PRIMARY KEY CLUSTERED (\[check\_in\_session\_id]),

    CONSTRAINT \[FK\_CheckInSession\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id]),

    CONSTRAINT \[FK\_CheckInSession\_Reviewer] FOREIGN KEY (\[reviewed\_by])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),

    CONSTRAINT \[CK\_CheckInSession\_Status] CHECK (\[status] IN ('PENDING', 'APPROVED', 'REJECTED'))
);
```

### 6.2 HousekeepingTask

```sql
CREATE TABLE \[dbo].\[HousekeepingTask](
    \[task\_id]       \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id]       \[bigint] NOT NULL,
    \[booking\_id]    \[bigint] NULL,
    \[assigned\_to]   \[bigint] NULL,
    \[created\_by]    \[bigint] NULL,
    \[task\_type]     \[varchar](30) NOT NULL,
    \[task\_status]   \[varchar](20) NOT NULL DEFAULT 'PENDING',
    \[priority]      \[varchar](10) NOT NULL DEFAULT 'NORMAL',
    \[notes]         \[nvarchar](1000) NULL,
    \[created\_at]    \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[started\_at]    \[datetime2](7) NULL,
    \[completed\_at]  \[datetime2](7) NULL,

    CONSTRAINT \[PK\_HousekeepingTask] PRIMARY KEY CLUSTERED (\[task\_id]),

    CONSTRAINT \[FK\_HousekeepingTask\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id]),

    CONSTRAINT \[FK\_HousekeepingTask\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id]),

    CONSTRAINT \[FK\_HousekeepingTask\_AssignedTo] FOREIGN KEY (\[assigned\_to])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),

    CONSTRAINT \[FK\_HousekeepingTask\_CreatedBy] FOREIGN KEY (\[created\_by])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),

    CONSTRAINT \[CK\_HousekeepingTask\_Type] CHECK (\[task\_type] IN (
        'CLEANING', 'MAINTENANCE', 'INSPECTION', 'OVERDUE\_CHECKOUT'
    )),

    CONSTRAINT \[CK\_HousekeepingTask\_Status] CHECK (\[task\_status] IN (
        'PENDING', 'IN\_PROGRESS', 'COMPLETED', 'CANCELLED'
    )),

    CONSTRAINT \[CK\_HousekeepingTask\_Priority] CHECK (\[priority] IN (
        'LOW', 'NORMAL', 'HIGH', 'URGENT'
    ))
);
```

### 6.3 TaskChecklist

```sql
CREATE TABLE \[dbo].\[TaskChecklist](
    \[checklist\_id]   \[bigint] IDENTITY(1,1) NOT NULL,
    \[task\_id]        \[bigint] NOT NULL,
    \[item\_name]      \[nvarchar](200) NOT NULL,
    \[is\_completed]   \[bit] NOT NULL DEFAULT (0),
    \[completed\_at]   \[datetime2](7) NULL,

    CONSTRAINT \[PK\_TaskChecklist] PRIMARY KEY CLUSTERED (\[checklist\_id]),

    CONSTRAINT \[FK\_TaskChecklist\_Task] FOREIGN KEY (\[task\_id])
        REFERENCES \[dbo].\[HousekeepingTask](\[task\_id])
        ON DELETE CASCADE
);
```

### 6.4 RoomStatusLog

```sql
CREATE TABLE \[dbo].\[RoomStatusLog](
    \[log\_id]           \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id]          \[bigint] NOT NULL,
    \[previous\_status]  \[varchar](30) NULL,
    \[new\_status]       \[varchar](30) NOT NULL,
    \[changed\_by]       \[bigint] NULL,
    \[change\_reason]    \[nvarchar](500) NULL,
    \[booking\_id]       \[bigint] NULL,
    \[created\_at]       \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),

    CONSTRAINT \[PK\_RoomStatusLog] PRIMARY KEY CLUSTERED (\[log\_id]),

    CONSTRAINT \[FK\_RoomStatusLog\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id]),

    CONSTRAINT \[FK\_RoomStatusLog\_ChangedBy] FOREIGN KEY (\[changed\_by])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),

    CONSTRAINT \[FK\_RoomStatusLog\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id])
);
```

\---

## 7\. Index Optimization Plan

### 7.1 Room Status Lookup

```sql
-- Housekeeping dashboard: filter rooms by status
CREATE NONCLUSTERED INDEX \[IX\_Room\_Status]
ON \[dbo].\[Room] (\[status])
INCLUDE (\[room\_number], \[room\_type\_id])
WHERE \[is\_deleted] = 0;
```

**Reason**: The housekeeping dashboard will frequently query rooms by `status IN ('DIRTY', 'CLEANING', 'MAINTENANCE', 'CHECKOUT\_PENDING')`. This filtered index makes those lookups fast while excluding deleted rooms.

### 7.2 Housekeeping Task Lists

```sql
-- Active tasks for dashboard
CREATE NONCLUSTERED INDEX \[IX\_HousekeepingTask\_Status\_Type]
ON \[dbo].\[HousekeepingTask] (\[task\_status], \[task\_type])
INCLUDE (\[room\_id], \[assigned\_to], \[priority], \[created\_at]);
```

**Reason**: Housekeeping staff filter tasks by status (PENDING, IN\_PROGRESS) and type (CLEANING, MAINTENANCE). This composite index covers the primary query pattern.

```sql
-- Tasks assigned to a specific staff member
CREATE NONCLUSTERED INDEX \[IX\_HousekeepingTask\_AssignedTo]
ON \[dbo].\[HousekeepingTask] (\[assigned\_to], \[task\_status])
INCLUDE (\[room\_id], \[task\_type], \[priority]);
```

**Reason**: "My tasks" view for each housekeeping staff member.

### 7.3 Booking → Room Queries

```sql
-- Active bookings per room (availability check + overdue detection)
CREATE NONCLUSTERED INDEX \[IX\_Booking\_Room\_Status]
ON \[dbo].\[Booking] (\[room\_id], \[booking\_status])
INCLUDE (\[customer\_id], \[check\_in\_time], \[check\_out\_time]);
```

**Reason**: Room availability checks and overdue detection both query Booking by `room\_id` + `booking\_status`.

```sql
-- Customer booking history
CREATE NONCLUSTERED INDEX \[IX\_Booking\_Customer\_CreatedAt]
ON \[dbo].\[Booking] (\[customer\_id], \[created\_at] DESC)
INCLUDE (\[room\_id], \[booking\_status], \[total\_amount]);
```

**Reason**: Booking history page lists bookings by customer, sorted by newest first.

### 7.4 Overdue Checkout Detection

```sql
-- Scheduled job: find rooms past checkout time
CREATE NONCLUSTERED INDEX \[IX\_Booking\_CheckedIn\_CheckoutTime]
ON \[dbo].\[Booking] (\[booking\_status], \[check\_out\_time])
INCLUDE (\[room\_id], \[customer\_id])
WHERE \[booking\_status] = 'CHECKED\_IN';
```

**Reason**: The overdue detection cron job queries `WHERE booking\_status = 'CHECKED\_IN' AND check\_out\_time < GETDATE()`. This filtered index is optimal for that specific query pattern.

### 7.5 Room Status Log

```sql
-- Room history lookup
CREATE NONCLUSTERED INDEX \[IX\_RoomStatusLog\_Room\_CreatedAt]
ON \[dbo].\[RoomStatusLog] (\[room\_id], \[created\_at] DESC)
INCLUDE (\[previous\_status], \[new\_status], \[changed\_by]);
```

**Reason**: Viewing the status change history for a specific room.

### 7.6 Task Checklist

```sql
-- Checklist items per task
CREATE NONCLUSTERED INDEX \[IX\_TaskChecklist\_TaskId]
ON \[dbo].\[TaskChecklist] (\[task\_id])
INCLUDE (\[item\_name], \[is\_completed]);
```

**Reason**: Loading checklist items for a task detail view.

\---

## 8\. Migration Strategy

### 8.1 Migration Order

The migration must be executed in a specific order to avoid FK constraint violations and minimize downtime.

#### Phase 1: Non-Breaking Additions (Safe — Zero Downtime)

These changes add new columns/tables without affecting existing code:

```
Step 1.1: Add columns to RoomType (base\_price, over\_price, description)
Step 1.2: Add columns to Booking (actual\_check\_in\_time, actual\_check\_out\_time)
Step 1.3: Create CheckInSession table
Step 1.4: Create HousekeepingTask table
Step 1.5: Create TaskChecklist table
Step 1.6: Create RoomStatusLog table
Step 1.7: Create all new indexes
```

#### Phase 2: Data Migration (Safe — Idempotent)

```
Step 2.1: Copy pricing from Room to RoomType (group by room\_type\_id)
Step 2.2: Seed initial RoomStatusLog entries for current room states
```

#### Phase 3: Constraint Modifications (Requires Brief Testing Window)

```
Step 3.1: Update Room.status CHECK constraint (add new statuses)
Step 3.2: Update StaffAccounts.role CHECK constraint (add HOUSEKEEPING)
Step 3.3: Add Booking.booking\_status CHECK constraint
Step 3.4: Make Feedback.admin\_id nullable
Step 3.5: Make Payment.paid\_at nullable, remove default
Step 3.6: Add RoomAmenity UNIQUE constraint
```

### 8.2 Detailed Migration Scripts

#### Phase 1: Non-Breaking Additions

```sql
-- ============================================================
-- PHASE 1: NON-BREAKING ADDITIONS
-- ============================================================

-- 1.1: Extend RoomType with pricing columns
ALTER TABLE \[dbo].\[RoomType] ADD \[base\_price] \[decimal](12, 2) NULL;
ALTER TABLE \[dbo].\[RoomType] ADD \[over\_price] \[decimal](12, 2) NULL;
ALTER TABLE \[dbo].\[RoomType] ADD \[description] \[nvarchar](500) NULL;
GO

-- 1.2: Add actual timestamps to Booking
ALTER TABLE \[dbo].\[Booking] ADD \[actual\_check\_in\_time]  \[datetime2](7) NULL;
ALTER TABLE \[dbo].\[Booking] ADD \[actual\_check\_out\_time] \[datetime2](7) NULL;
GO

-- 1.3: Create CheckInSession
-- (See Section 6.1 for full CREATE TABLE script)

-- 1.4: Create HousekeepingTask
-- (See Section 6.2 for full CREATE TABLE script)

-- 1.5: Create TaskChecklist
-- (See Section 6.3 for full CREATE TABLE script)

-- 1.6: Create RoomStatusLog
-- (See Section 6.4 for full CREATE TABLE script)

-- 1.7: Create indexes
-- (See Section 7 for all CREATE INDEX scripts)
```

#### Phase 2: Data Migration

```sql
-- ============================================================
-- PHASE 2: DATA MIGRATION
-- ============================================================

-- 2.1: Copy pricing from Room to RoomType
-- Strategy: For each RoomType, take the price from the first non-deleted Room of that type
UPDATE rt
SET rt.base\_price = sub.base\_price,
    rt.over\_price = sub.over\_price,
    rt.description = sub.description
FROM \[dbo].\[RoomType] rt
INNER JOIN (
    SELECT room\_type\_id,
           base\_price,
           over\_price,
           description,
           ROW\_NUMBER() OVER (PARTITION BY room\_type\_id ORDER BY room\_id) AS rn
    FROM \[dbo].\[Room]
    WHERE is\_deleted = 0 AND base\_price IS NOT NULL
) sub ON rt.room\_type\_id = sub.room\_type\_id AND sub.rn = 1;
GO

-- 2.2: Seed RoomStatusLog with current state
INSERT INTO \[dbo].\[RoomStatusLog] (room\_id, previous\_status, new\_status, change\_reason, created\_at)
SELECT room\_id, NULL, status, 'Initial state captured during migration', sysdatetime()
FROM \[dbo].\[Room]
WHERE is\_deleted = 0;
GO

-- 2.3: Backfill actual\_check\_in\_time from existing CHECKED\_IN bookings
-- For bookings already in CHECKED\_IN or later state, set actual\_check\_in\_time = check\_in\_time
UPDATE \[dbo].\[Booking]
SET actual\_check\_in\_time = check\_in\_time
WHERE booking\_status IN ('CHECKED\_IN', 'CHECKED\_OUT', 'COMPLETED')
  AND actual\_check\_in\_time IS NULL;
GO

-- 2.4: Backfill actual\_check\_out\_time from existing CHECKED\_OUT bookings
UPDATE \[dbo].\[Booking]
SET actual\_check\_out\_time = COALESCE(update\_at, check\_out\_time)
WHERE booking\_status IN ('CHECKED\_OUT', 'COMPLETED')
  AND actual\_check\_out\_time IS NULL;
GO
```

#### Phase 3: Constraint Modifications

```sql
-- ============================================================
-- PHASE 3: CONSTRAINT MODIFICATIONS
-- ============================================================

-- 3.1: Update Room.status CHECK constraint
ALTER TABLE \[dbo].\[Room] DROP CONSTRAINT \[CK\_Room\_Status];
GO
ALTER TABLE \[dbo].\[Room] ADD CONSTRAINT \[CK\_Room\_Status]
CHECK (\[status] IN (
    'AVAILABLE', 'BOOKED', 'OCCUPIED', 'CHECKOUT\_PENDING',
    'DIRTY', 'CLEANING', 'MAINTENANCE', 'OUT\_OF\_SERVICE'
));
GO

-- 3.2: Update StaffAccounts.role CHECK constraint
ALTER TABLE \[dbo].\[StaffAccounts] DROP CONSTRAINT \[CK\_Admin\_Role];
GO
ALTER TABLE \[dbo].\[StaffAccounts] ADD CONSTRAINT \[CK\_Admin\_Role]
CHECK (\[role] IN ('ADMIN', 'STAFF', 'HOUSEKEEPING'));
GO

-- 3.3: Add Booking.booking\_status CHECK constraint
ALTER TABLE \[dbo].\[Booking] ADD CONSTRAINT \[CK\_Booking\_Status]
CHECK (\[booking\_status] IN (
    'PENDING', 'WAITING\_PAYMENT', 'PAID',
    'CHECKED\_IN', 'CHECKED\_OUT', 'COMPLETED', 'CANCELLED'
));
GO

-- 3.4: Make Feedback.admin\_id nullable
-- Step A: Drop the existing FK constraint
ALTER TABLE \[dbo].\[Feedback] DROP CONSTRAINT \[FK\_Feedback\_StaffAccounts];
GO
-- Step B: Alter the column to allow NULL
ALTER TABLE \[dbo].\[Feedback] ALTER COLUMN \[admin\_id] \[bigint] NULL;
GO
-- Step C: Re-add the FK constraint
ALTER TABLE \[dbo].\[Feedback] ADD CONSTRAINT \[FK\_Feedback\_StaffAccounts]
FOREIGN KEY (\[admin\_id]) REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]);
GO

-- 3.5: Make Payment.paid\_at nullable, remove default
ALTER TABLE \[dbo].\[Payment] DROP CONSTRAINT \[DF\_Payment\_PaidAt];
GO
ALTER TABLE \[dbo].\[Payment] ALTER COLUMN \[paid\_at] \[datetime2](7) NULL;
GO

-- 3.6: Add unique constraint on RoomAmenity
ALTER TABLE \[dbo].\[RoomAmenity] ADD CONSTRAINT \[UQ\_RoomAmenity\_Room\_Amenity]
UNIQUE (\[room\_id], \[amenity\_id]);
GO

-- Clean up duplicate RoomAmenity records first (if any exist)
-- Run this BEFORE the UNIQUE constraint above if duplicates exist:
/\*
WITH cte AS (
    SELECT room\_amenity\_id,
           ROW\_NUMBER() OVER (PARTITION BY room\_id, amenity\_id ORDER BY room\_amenity\_id) AS rn
    FROM \[dbo].\[RoomAmenity]
)
DELETE FROM cte WHERE rn > 1;
\*/
```

### 8.3 How to Avoid Breaking Current Code

|Change|Backward Compatibility Strategy|
|-|-|
|New Room statuses|Existing code checks for `'AVAILABLE'`, `'BOOKED'`, `'MAINTENANCE'` — all still valid. New statuses are additive. Update service layer to handle new values gradually.|
|Price on RoomType|Keep `Room.base\_price` / `over\_price` intact. Update read logic to prefer `RoomType` price, fall back to `Room` price.|
|`Feedback.admin\_id` nullable|Existing records already have admin\_id populated. New feedback can be created without it. Existing read code still works.|
|`Payment.paid\_at` nullable|Existing records retain their timestamps. Update the payment success handler to `SET paid\_at = sysdatetime()` explicitly.|
|New tables|Completely additive — no existing code interacts with them until new features are built.|
|New Booking columns|Nullable, so existing INSERT statements work without modification.|
|HOUSEKEEPING role|Additive — existing ADMIN/STAFF auth checks unaffected. New role requires new controller endpoints.|

### 8.4 Rollback Plan

Every migration step is independently reversible:

```sql
-- Rollback Phase 3.1:
ALTER TABLE \[dbo].\[Room] DROP CONSTRAINT \[CK\_Room\_Status];
ALTER TABLE \[dbo].\[Room] ADD CONSTRAINT \[CK\_Room\_Status]
CHECK (\[status] IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE'));

-- Rollback Phase 1.1:
ALTER TABLE \[dbo].\[RoomType] DROP COLUMN \[base\_price];
ALTER TABLE \[dbo].\[RoomType] DROP COLUMN \[over\_price];
ALTER TABLE \[dbo].\[RoomType] DROP COLUMN \[description];

-- Rollback new tables:
DROP TABLE \[dbo].\[TaskChecklist];
DROP TABLE \[dbo].\[HousekeepingTask];
DROP TABLE \[dbo].\[RoomStatusLog];
DROP TABLE \[dbo].\[CheckInSession];
```

\---

## 9\. Final Database Schema

### 9.1 Complete Table List

|#|Table|Status|Description|
|-|-|-|-|
|01|`Customer`|Unchanged|Guest user accounts|
|02|`StaffAccounts`|Modified constraint|Staff/Admin/Housekeeping accounts|
|03|`RefreshTokens`|Unchanged|Session tokens|
|04|`RoomType`|Extended|Room categories + pricing (NEW: base\_price, over\_price, description)|
|05|`Room`|Modified constraint|Individual rooms (NEW status values)|
|06|`Amenity`|Unchanged|Room features|
|07|`RoomAmenity`|New constraint|Room-Amenity junction (NEW: unique constraint)|
|08|`Image`|Unchanged|Room photos|
|09|`Booking`|Extended|Reservations (NEW: actual timestamps, CHECK constraint)|
|10|`Payment`|Modified|Payments (FIX: paid\_at nullable)|
|11|`Invoice`|Unchanged|Invoices (to be improved later)|
|12|`Feedback`|Modified|Reviews (FIX: admin\_id nullable)|
|13|`Timetable`|Unchanged|Time slots|
|14|`Scheduler`|Unchanged|Booking-timetable links|
|15|`CheckInSession`|**NEW**|Check-in video tracking|
|16|`HousekeepingTask`|**NEW**|Cleaning/maintenance tasks|
|17|`TaskChecklist`|**NEW**|Cleaning checklist items|
|18|`RoomStatusLog`|**NEW**|Room status audit trail|

### 9.2 Entity Relationship Diagram

```
Customer ──────────< Booking >──────────── Room ──────── RoomType
    │                   │                    │
    │                   ├──< Payment         ├──<< RoomAmenity >>── Amenity
    │                   │                    │
    │                   ├──< Invoice         ├──< Image
    │                   │                    │
    │                   ├──< Feedback ──┐    ├──< RoomStatusLog
    │                   │               │    │
    │                   ├──< Scheduler  │    └──< HousekeepingTask ──< TaskChecklist
    │                   │    │          │              │
    │                   ├──< CheckIn    │              │
    │                   │    Session    │              │
    │                   │               │              │
    └──< RefreshToken >── StaffAccounts ◄──────────────┘
                              │
                              ├── assigned\_to (HousekeepingTask)
                              ├── created\_by (HousekeepingTask)
                              ├── changed\_by (RoomStatusLog)
                              └── reviewed\_by (CheckInSession)
```

### 9.3 Final SQL Schema Script

Below is the **complete** final schema, including all existing tables (with modifications applied) and all new tables.

```sql
-- ============================================================
-- BookNow — Final Optimized Database Schema
-- Version: 2.0 (with Housekeeping Module)
-- Database: Microsoft SQL Server
-- ============================================================

USE \[database\_name]
GO

-- ============================================================
-- TABLE: Customer (UNCHANGED)
-- ============================================================
CREATE TABLE \[dbo].\[Customer](
    \[customer\_id]      \[bigint] IDENTITY(1,1) NOT NULL,
    \[email]            \[nvarchar](255) NOT NULL,
    \[password\_hash]    \[nvarchar](255) NULL,
    \[full\_name]        \[nvarchar](50) NULL,
    \[avatar\_url]       \[nvarchar](255) NULL,
    \[phone]            \[nvarchar](20) NULL,
    \[status]           \[varchar](50) NOT NULL DEFAULT ('ACTIVE'),
    \[created\_at]       \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[updated\_at]       \[datetime2](7) NULL,
    \[is\_deleted]       \[bit] NOT NULL DEFAULT (0),
    \[avatar\_public\_id] \[nvarchar](255) NULL,

    CONSTRAINT \[PK\_Customer] PRIMARY KEY CLUSTERED (\[customer\_id]),
    CONSTRAINT \[UQ\_Customer\_Email] UNIQUE (\[email]),
    CONSTRAINT \[CK\_Customer\_Status] CHECK (\[status] IN ('ACTIVE', 'INACTIVE'))
);
GO

-- ============================================================
-- TABLE: StaffAccounts (MODIFIED: role constraint)
-- ============================================================
CREATE TABLE \[dbo].\[StaffAccounts](
    \[staff\_account\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[email]            \[nvarchar](255) NOT NULL,
    \[phone]            \[nvarchar](20) NULL,
    \[password\_hash]    \[nvarchar](255) NOT NULL,
    \[full\_name]        \[nvarchar](50) NULL,
    \[avatar\_url]       \[nvarchar](255) NULL,
    \[role]             \[nvarchar](20) NOT NULL,
    \[status]           \[varchar](50) NOT NULL DEFAULT ('ACTIVE'),
    \[created\_at]       \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[is\_deleted]       \[bit] NOT NULL DEFAULT (0),
    \[avatar\_public\_id] \[nvarchar](255) NULL,

    CONSTRAINT \[PK\_Admin] PRIMARY KEY CLUSTERED (\[staff\_account\_id]),
    CONSTRAINT \[UQ\_Admin\_Email] UNIQUE (\[email]),
    CONSTRAINT \[CK\_Admin\_Role] CHECK (\[role] IN ('ADMIN', 'STAFF', 'HOUSEKEEPING')),
    CONSTRAINT \[CK\_Admin\_Status] CHECK (\[status] IN ('ACTIVE', 'INACTIVE'))
);
GO

-- ============================================================
-- TABLE: RefreshTokens (UNCHANGED)
-- ============================================================
CREATE TABLE \[dbo].\[RefreshTokens](
    \[id]               \[bigint] IDENTITY(1,1) NOT NULL,
    \[token\_hash]       \[varchar](255) NOT NULL,
    \[expires\_at]       \[datetime2](7) NOT NULL,
    \[is\_revoked]       \[bit] NOT NULL DEFAULT (0),
    \[revoked\_at]       \[datetime2](7) NULL,
    \[created\_at]       \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[account\_type]     \[varchar](20) NOT NULL,
    \[customer\_id]      \[bigint] NULL,
    \[staff\_account\_id] \[bigint] NULL,

    PRIMARY KEY CLUSTERED (\[id]),

    CONSTRAINT \[FK\_RefreshTokens\_Customers] FOREIGN KEY (\[customer\_id])
        REFERENCES \[dbo].\[Customer](\[customer\_id]) ON DELETE CASCADE,
    CONSTRAINT \[FK\_RefreshTokens\_StaffAccounts] FOREIGN KEY (\[staff\_account\_id])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]) ON DELETE CASCADE,
    CONSTRAINT \[CK\_RefreshTokens\_AccountType] CHECK (
        (\[account\_type] = 'CUSTOMER' AND \[customer\_id] IS NOT NULL AND \[staff\_account\_id] IS NULL)
        OR
        (\[account\_type] = 'STAFF' AND \[staff\_account\_id] IS NOT NULL AND \[customer\_id] IS NULL)
    )
);
GO

-- ============================================================
-- TABLE: RoomType (EXTENDED: added pricing + description)
-- ============================================================
CREATE TABLE \[dbo].\[RoomType](
    \[room\_type\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[name]         \[nvarchar](100) NOT NULL,
    \[description]  \[nvarchar](500) NULL,                  -- NEW
    \[base\_price]   \[decimal](12, 2) NULL,                  -- NEW
    \[over\_price]   \[decimal](12, 2) NULL,                  -- NEW
    \[image\_url]    \[nvarchar](500) NULL,
    \[max\_guests]   \[int] NOT NULL,
    \[is\_deleted]   \[bit] NOT NULL DEFAULT (0),

    CONSTRAINT \[PK\_RoomType] PRIMARY KEY CLUSTERED (\[room\_type\_id]),
    CONSTRAINT \[UQ\_RoomType\_Name] UNIQUE (\[name])
);
GO

-- ============================================================
-- TABLE: Room (MODIFIED: expanded status constraint)
-- Note: base\_price, over\_price, description kept for backward
-- compatibility but should be migrated to RoomType over time.
-- ============================================================
CREATE TABLE \[dbo].\[Room](
    \[room\_id]      \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_type\_id] \[bigint] NOT NULL,
    \[room\_number]  \[nvarchar](50) NOT NULL,
    \[status]       \[varchar](50) NOT NULL DEFAULT ('AVAILABLE'),
    \[is\_deleted]   \[bit] NOT NULL DEFAULT (0),
    \[area\_m2]      \[decimal](10, 2) NULL,
    \[base\_price]   \[decimal](12, 2) NULL,                  -- DEPRECATED: use RoomType.base\_price
    \[over\_price]   \[decimal](12, 2) NULL,                  -- DEPRECATED: use RoomType.over\_price
    \[description]  \[nvarchar](500) NULL,                   -- DEPRECATED: use RoomType.description

    CONSTRAINT \[PK\_Room] PRIMARY KEY CLUSTERED (\[room\_id]),
    CONSTRAINT \[UQ\_Room\_RoomNumber] UNIQUE (\[room\_number]),
    CONSTRAINT \[FK\_Room\_RoomType] FOREIGN KEY (\[room\_type\_id])
        REFERENCES \[dbo].\[RoomType](\[room\_type\_id]),
    CONSTRAINT \[CK\_Room\_Status] CHECK (\[status] IN (
        'AVAILABLE', 'BOOKED', 'OCCUPIED', 'CHECKOUT\_PENDING',
        'DIRTY', 'CLEANING', 'MAINTENANCE', 'OUT\_OF\_SERVICE'
    ))
);
GO

-- ============================================================
-- TABLE: Amenity (UNCHANGED)
-- ============================================================
CREATE TABLE \[dbo].\[Amenity](
    \[amenity\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[name]       \[nvarchar](100) NOT NULL,
    \[icon\_url]   \[nvarchar](255) NULL,
    \[is\_deleted] \[bit] NOT NULL DEFAULT (0),

    CONSTRAINT \[PK\_Amenity] PRIMARY KEY CLUSTERED (\[amenity\_id]),
    CONSTRAINT \[UQ\_Amenity\_Name] UNIQUE (\[name])
);
GO

-- ============================================================
-- TABLE: RoomAmenity (EXTENDED: added unique constraint)
-- ============================================================
CREATE TABLE \[dbo].\[RoomAmenity](
    \[room\_amenity\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id]         \[bigint] NOT NULL,
    \[amenity\_id]      \[bigint] NOT NULL,

    CONSTRAINT \[PK\_RoomAmenity] PRIMARY KEY CLUSTERED (\[room\_amenity\_id]),
    CONSTRAINT \[FK\_RoomAmenity\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id]),
    CONSTRAINT \[FK\_RoomAmenity\_Amenity] FOREIGN KEY (\[amenity\_id])
        REFERENCES \[dbo].\[Amenity](\[amenity\_id]),
    CONSTRAINT \[UQ\_RoomAmenity\_Room\_Amenity] UNIQUE (\[room\_id], \[amenity\_id])  -- NEW
);
GO

-- ============================================================
-- TABLE: Image (UNCHANGED)
-- ============================================================
CREATE TABLE \[dbo].\[Image](
    \[image\_id]  \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id]   \[bigint] NOT NULL,
    \[image\_url] \[nvarchar](500) NOT NULL,
    \[is\_cover]  \[bit] NOT NULL DEFAULT (0),
    \[public\_id] \[nvarchar](255) NULL,

    CONSTRAINT \[PK\_Image] PRIMARY KEY CLUSTERED (\[image\_id]),
    CONSTRAINT \[FK\_Image\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id])
);
GO

-- ============================================================
-- TABLE: Booking (EXTENDED: actual timestamps + CHECK)
-- ============================================================
CREATE TABLE \[dbo].\[Booking](
    \[booking\_id]            \[bigint] IDENTITY(1,1) NOT NULL,
    \[customer\_id]           \[bigint] NOT NULL,
    \[room\_id]               \[bigint] NOT NULL,
    \[check\_in\_time]         \[datetime2](7) NOT NULL,
    \[check\_out\_time]        \[datetime2](7) NOT NULL,
    \[actual\_check\_in\_time]  \[datetime2](7) NULL,               -- NEW
    \[actual\_check\_out\_time] \[datetime2](7) NULL,               -- NEW
    \[id\_card\_front\_url]     \[nvarchar](500) NOT NULL,
    \[id\_card\_back\_url]      \[nvarchar](500) NOT NULL,
    \[booking\_status]        \[nvarchar](20) NOT NULL,
    \[total\_amount]          \[decimal](12, 2) NOT NULL,
    \[booking\_code]          \[nvarchar](500) NULL,
    \[created\_at]            \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[note]                  \[nvarchar](255) NULL,
    \[update\_at]             \[datetime2](7) NULL,

    CONSTRAINT \[PK\_Booking] PRIMARY KEY CLUSTERED (\[booking\_id]),
    CONSTRAINT \[FK\_Booking\_Customer] FOREIGN KEY (\[customer\_id])
        REFERENCES \[dbo].\[Customer](\[customer\_id]),
    CONSTRAINT \[FK\_Booking\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id]),
    CONSTRAINT \[CK\_Booking\_Status] CHECK (\[booking\_status] IN (           -- NEW
        'PENDING', 'PENDING\_PAYMENT', 'PAID',
        'CHECKED\_IN', 'CHECKED\_OUT', 'COMPLETED','REJECTED' ,'FAILED'
    ))
);
GO

-- ============================================================
-- TABLE: Payment (MODIFIED: paid\_at nullable)
-- ============================================================
CREATE TABLE \[dbo].\[Payment](
    \[payment\_id]     \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id]     \[bigint] NOT NULL,
    \[amount]         \[decimal](12, 2) NOT NULL,
    \[method]         \[nvarchar](50) NOT NULL,
    \[payment\_status] \[varchar](20) NOT NULL,
    \[paid\_at]        \[datetime2](7) NULL,                     -- MODIFIED: was NOT NULL with default

    CONSTRAINT \[PK\_Payment] PRIMARY KEY CLUSTERED (\[payment\_id]),
    CONSTRAINT \[FK\_Payment\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id])
);
GO

-- ============================================================
-- TABLE: Invoice (UNCHANGED — to be improved in future)
-- ============================================================
CREATE TABLE \[dbo].\[Invoice](
    \[invoice\_id]     \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id]     \[bigint] NOT NULL,
    \[invoice\_number] \[nvarchar](50) NULL,
    \[issued\_at]      \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),

    CONSTRAINT \[PK\_Invoice] PRIMARY KEY CLUSTERED (\[invoice\_id]),
    CONSTRAINT \[UQ\_Invoice\_Number] UNIQUE (\[invoice\_number]),
    CONSTRAINT \[FK\_Invoice\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id])
);
GO

-- ============================================================
-- TABLE: Feedback (MODIFIED: admin\_id nullable)
-- ============================================================
CREATE TABLE \[dbo].\[Feedback](
    \[feedback\_id]   \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id]    \[bigint] NOT NULL,
    \[admin\_id]      \[bigint] NULL,                            -- MODIFIED: was NOT NULL
    \[rating]        \[int] NOT NULL,
    \[content]       \[nvarchar](1000) NOT NULL,
    \[content\_reply] \[nvarchar](1000) NULL,
    \[is\_hidden]     \[bit] NOT NULL DEFAULT (0),
    \[created\_at]    \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[reply\_at]      \[datetime2](7) NULL,

    CONSTRAINT \[PK\_Feedback] PRIMARY KEY CLUSTERED (\[feedback\_id]),
    CONSTRAINT \[FK\_Feedback\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id]),
    CONSTRAINT \[FK\_Feedback\_StaffAccounts] FOREIGN KEY (\[admin\_id])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),
    CONSTRAINT \[CK\_Feedback\_Rating] CHECK (\[rating] >= 1 AND \[rating] <= 5)
);
GO

-- ============================================================
-- TABLE: Timetable (UNCHANGED)
-- ============================================================
CREATE TABLE \[dbo].\[Timetable](
    \[timetable\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[slot\_name]    \[nvarchar](50) NOT NULL,
    \[start\_time]   \[time](7) NOT NULL,
    \[end\_time]     \[time](7) NOT NULL,

    CONSTRAINT \[PK\_Timetable] PRIMARY KEY CLUSTERED (\[timetable\_id])
);
GO

-- ============================================================
-- TABLE: Scheduler (UNCHANGED)
-- ============================================================
CREATE TABLE \[dbo].\[Scheduler](
    \[scheduler\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id]   \[bigint] NOT NULL,
    \[timetable\_id] \[bigint] NOT NULL,
    \[date]         \[date] NOT NULL,

    CONSTRAINT \[PK\_Scheduler] PRIMARY KEY CLUSTERED (\[scheduler\_id]),
    CONSTRAINT \[FK\_Scheduler\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id]),
    CONSTRAINT \[FK\_Scheduler\_Timetable] FOREIGN KEY (\[timetable\_id])
        REFERENCES \[dbo].\[Timetable](\[timetable\_id])
);
GO

-- ============================================================
-- TABLE: CheckInSession (NEW — was documented but missing)
-- ============================================================
CREATE TABLE \[dbo].\[CheckInSession](
    \[check\_in\_session\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[booking\_id]          \[bigint] NOT NULL,
    \[video\_url]           \[nvarchar](500) NOT NULL,
    \[video\_public\_id]     \[nvarchar](255) NULL,
    \[status]              \[varchar](20) NOT NULL DEFAULT ('PENDING'),
    \[reviewed\_by]         \[bigint] NULL,
    \[created\_at]          \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[reviewed\_at]         \[datetime2](7) NULL,

    CONSTRAINT \[PK\_CheckInSession] PRIMARY KEY CLUSTERED (\[check\_in\_session\_id]),
    CONSTRAINT \[FK\_CheckInSession\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id]),
    CONSTRAINT \[FK\_CheckInSession\_Reviewer] FOREIGN KEY (\[reviewed\_by])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),
    CONSTRAINT \[CK\_CheckInSession\_Status] CHECK (\[status] IN ('PENDING', 'APPROVED', 'REJECTED'))
);
GO

-- ============================================================
-- TABLE: HousekeepingTask (NEW)
-- ============================================================
CREATE TABLE \[dbo].\[HousekeepingTask](
    \[task\_id]      \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id]      \[bigint] NOT NULL,
    \[booking\_id]   \[bigint] NULL,
    \[assigned\_to]  \[bigint] NULL,
    \[created\_by]   \[bigint] NULL,
    \[task\_type]    \[varchar](30) NOT NULL,
    \[task\_status]  \[varchar](20) NOT NULL DEFAULT ('PENDING'),
    \[priority]     \[varchar](10) NOT NULL DEFAULT ('NORMAL'),
    \[notes]        \[nvarchar](1000) NULL,
    \[created\_at]   \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),
    \[started\_at]   \[datetime2](7) NULL,
    \[completed\_at] \[datetime2](7) NULL,

    CONSTRAINT \[PK\_HousekeepingTask] PRIMARY KEY CLUSTERED (\[task\_id]),
    CONSTRAINT \[FK\_HousekeepingTask\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id]),
    CONSTRAINT \[FK\_HousekeepingTask\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id]),
    CONSTRAINT \[FK\_HousekeepingTask\_AssignedTo] FOREIGN KEY (\[assigned\_to])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),
    CONSTRAINT \[FK\_HousekeepingTask\_CreatedBy] FOREIGN KEY (\[created\_by])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),
    CONSTRAINT \[CK\_HousekeepingTask\_Type] CHECK (\[task\_type] IN (
        'CLEANING', 'MAINTENANCE', 'INSPECTION', 'OVERDUE\_CHECKOUT'
    )),
    CONSTRAINT \[CK\_HousekeepingTask\_Status] CHECK (\[task\_status] IN (
        'PENDING', 'IN\_PROGRESS', 'COMPLETED', 'CANCELLED'
    )),
    CONSTRAINT \[CK\_HousekeepingTask\_Priority] CHECK (\[priority] IN (
        'LOW', 'NORMAL', 'HIGH', 'URGENT'
    ))
);
GO

-- ============================================================
-- TABLE: TaskChecklist (NEW)
-- ============================================================
CREATE TABLE \[dbo].\[TaskChecklist](
    \[checklist\_id] \[bigint] IDENTITY(1,1) NOT NULL,
    \[task\_id]      \[bigint] NOT NULL,
    \[item\_name]    \[nvarchar](200) NOT NULL,
    \[is\_completed] \[bit] NOT NULL DEFAULT (0),
    \[completed\_at] \[datetime2](7) NULL,

    CONSTRAINT \[PK\_TaskChecklist] PRIMARY KEY CLUSTERED (\[checklist\_id]),
    CONSTRAINT \[FK\_TaskChecklist\_Task] FOREIGN KEY (\[task\_id])
        REFERENCES \[dbo].\[HousekeepingTask](\[task\_id])
        ON DELETE CASCADE
);
GO

-- ============================================================
-- TABLE: RoomStatusLog (NEW)
-- ============================================================
CREATE TABLE \[dbo].\[RoomStatusLog](
    \[log\_id]          \[bigint] IDENTITY(1,1) NOT NULL,
    \[room\_id]         \[bigint] NOT NULL,
    \[previous\_status] \[varchar](30) NULL,
    \[new\_status]      \[varchar](30) NOT NULL,
    \[changed\_by]      \[bigint] NULL,
    \[change\_reason]   \[nvarchar](500) NULL,
    \[booking\_id]      \[bigint] NULL,
    \[created\_at]      \[datetime2](7) NOT NULL DEFAULT (sysdatetime()),

    CONSTRAINT \[PK\_RoomStatusLog] PRIMARY KEY CLUSTERED (\[log\_id]),
    CONSTRAINT \[FK\_RoomStatusLog\_Room] FOREIGN KEY (\[room\_id])
        REFERENCES \[dbo].\[Room](\[room\_id]),
    CONSTRAINT \[FK\_RoomStatusLog\_ChangedBy] FOREIGN KEY (\[changed\_by])
        REFERENCES \[dbo].\[StaffAccounts](\[staff\_account\_id]),
    CONSTRAINT \[FK\_RoomStatusLog\_Booking] FOREIGN KEY (\[booking\_id])
        REFERENCES \[dbo].\[Booking](\[booking\_id])
);
GO

-- ============================================================
-- INDEXES
-- ============================================================

-- Room status lookup (housekeeping dashboard)
CREATE NONCLUSTERED INDEX \[IX\_Room\_Status]
ON \[dbo].\[Room] (\[status])
INCLUDE (\[room\_number], \[room\_type\_id])
WHERE \[is\_deleted] = 0;
GO

-- Active housekeeping tasks
CREATE NONCLUSTERED INDEX \[IX\_HousekeepingTask\_Status\_Type]
ON \[dbo].\[HousekeepingTask] (\[task\_status], \[task\_type])
INCLUDE (\[room\_id], \[assigned\_to], \[priority], \[created\_at]);
GO

-- Tasks by assignee
CREATE NONCLUSTERED INDEX \[IX\_HousekeepingTask\_AssignedTo]
ON \[dbo].\[HousekeepingTask] (\[assigned\_to], \[task\_status])
INCLUDE (\[room\_id], \[task\_type], \[priority]);
GO

-- Booking by room and status
CREATE NONCLUSTERED INDEX \[IX\_Booking\_Room\_Status]
ON \[dbo].\[Booking] (\[room\_id], \[booking\_status])
INCLUDE (\[customer\_id], \[check\_in\_time], \[check\_out\_time]);
GO

-- Customer booking history
CREATE NONCLUSTERED INDEX \[IX\_Booking\_Customer\_CreatedAt]
ON \[dbo].\[Booking] (\[customer\_id], \[created\_at] DESC)
INCLUDE (\[room\_id], \[booking\_status], \[total\_amount]);
GO

-- Overdue checkout detection (filtered index)
CREATE NONCLUSTERED INDEX \[IX\_Booking\_CheckedIn\_CheckoutTime]
ON \[dbo].\[Booking] (\[booking\_status], \[check\_out\_time])
INCLUDE (\[room\_id], \[customer\_id])
WHERE \[booking\_status] = 'CHECKED\_IN';
GO

-- Room status history
CREATE NONCLUSTERED INDEX \[IX\_RoomStatusLog\_Room\_CreatedAt]
ON \[dbo].\[RoomStatusLog] (\[room\_id], \[created\_at] DESC)
INCLUDE (\[previous\_status], \[new\_status], \[changed\_by]);
GO

-- Task checklist items
CREATE NONCLUSTERED INDEX \[IX\_TaskChecklist\_TaskId]
ON \[dbo].\[TaskChecklist] (\[task\_id])
INCLUDE (\[item\_name], \[is\_completed]);
GO
```

\---

## Appendix A: Summary of Changes

### New Tables (4)

|Table|Rows Expected|Purpose|
|-|-|-|
|`CheckInSession`|1 per check-in attempt|Video review workflow|
|`HousekeepingTask`|1 per cleaning/maintenance event|Task management|
|`TaskChecklist`|\~5 per cleaning task|Cleaning quality control|
|`RoomStatusLog`|Grows with every status change|Audit trail|

### Modified Tables (5)

|Table|Change|Risk|
|-|-|-|
|`RoomType`|+3 nullable columns|Zero risk — additive|
|`Room`|Updated CHECK constraint|Low risk — existing values still valid|
|`StaffAccounts`|Updated role CHECK|Low risk — additive|
|`Booking`|+2 nullable columns, +CHECK|Low risk — nullable + additive|
|`Payment`|`paid\_at` made nullable|Low risk — existing data preserved|

### Modified Constraints (2)

|Table|Old|New|Risk|
|-|-|-|-|
|`Feedback`|`admin\_id NOT NULL`|`admin\_id NULL`|Low — relaxing constraint|
|`RoomAmenity`|No unique|`UNIQUE(room\_id, amenity\_id)`|Medium — must deduplicate first|

### New Indexes (8)

All non-clustered, non-unique indexes — zero risk, purely additive performance optimization.

\---

## Appendix B: Service Layer Changes Required

The following code changes are needed after the database migration:

### 1\. Checkout Service

```
OLD: Room.status = 'AVAILABLE'
NEW: Room.status = 'DIRTY'
     + Create HousekeepingTask(type=CLEANING, status=PENDING)
     + Log to RoomStatusLog
```

### 2\. Check-in Approval Service

```
OLD: Room.status (unchanged or set to BOOKED)
NEW: Room.status = 'OCCUPIED'
     + Set Booking.actual\_check\_in\_time = NOW()
     + Log to RoomStatusLog
```

### 3\. New Scheduled Job: Overdue Checkout Detector

```
NEW: Run every 5-15 minutes
     Find CHECKED\_IN bookings where check\_out\_time < NOW()
     Set Room.status = 'CHECKOUT\_PENDING'
     Create HousekeepingTask(type=OVERDUE\_CHECKOUT, priority=HIGH)
     Log to RoomStatusLog
```

### 4\. New Housekeeping Controller/Service

```
NEW: HousekeepingController
     - GET /housekeeping/tasks (list active tasks)
     - GET /housekeeping/tasks/{id} (task detail + checklist)
     - POST /housekeeping/tasks/{id}/start (begin cleaning)
     - POST /housekeeping/tasks/{id}/complete (finish cleaning)
     - POST /housekeeping/tasks/{id}/maintenance (report issue)
     - PUT /housekeeping/tasks/{id}/checklist/{checklistId} (toggle item)
```

### 5\. Room Pricing Read Logic

```
OLD: price = room.getBasePrice()
NEW: price = roomType.getBasePrice() != null ? roomType.getBasePrice() : room.getBasePrice()
```

### 6\. Payment Success Handler

```
OLD: paid\_at set automatically by DEFAULT
NEW: Explicitly SET paid\_at = sysdatetime() only when payment\_status → SUCCESS
```

\---

*End of Report*

