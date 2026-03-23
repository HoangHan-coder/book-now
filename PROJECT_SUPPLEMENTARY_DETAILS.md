# 📊 BOOKNOW - SUPPLEMENTARY TECHNICAL DETAILS

## 🔍 DETAIL: BookingStatus Enum & Lifecycle

```java
public enum BookingStatus {
    PENDING,              // CSS: gray       | "Đã kiểm duyệt"
    PENDING_PAYMENT,      // CSS: yellow     | "Chờ thanh toán"
    PAID,                 // CSS: blue       | "Đã thanh toán"
    CHECKED_IN,           // CSS: purple     | "Đã nhận phòng"
    CHECKED_OUT,          // CSS: indigo     | "Đang chờ FeedBack"
    COMPLETED,            // CSS: green      | "Hoàn thành"
    REJECTED,             // CSS: red        | "Từ chối"
    FAILED;               // CSS: red        | "Thất bại"
}
```

### Booking Lifecycle Flow:
```
PENDING 
  ↓
PENDING_PAYMENT (Chờ thanh toán)
  ↓ [Payment success]
PAID (Đã thanh toán)
  ↓ [Check-in]
CHECKED_IN (Đã nhận phòng)
  ↓ [Check-out ngay → Trigger HousekeepingTask]
CHECKED_OUT (Chờ feedback)
  ↓ [Customer feedback → Update room status]
COMPLETED (Hoàn thành)

OR

FAILED / REJECTED (Nếu lỗi)
```

---

## 🗂️ DETAIL: RoomStatusLog Entity

**Purpose**: Audit trail cho tất cả room status changes

```java
@Entity
@Table(name = "RoomStatusLog")
public class RoomStatusLog {
    @Id
    private Long id;                           // log_id
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;                         // Phòng bị thay đổi
    
    @Column(name = "previous_status")
    private String previousStatus;              // AVAILABLE → OCCUPIED
    
    @Column(name = "new_status", nullable = false)
    private String newStatus;                   // AVAILABLE → OCCUPIED, OCCUPIED → CLEANING, ...
    
    @ManyToOne
    @JoinColumn(name = "changed_by")
    private StaffAccount changedBy;             // Nhân viên thực hiện thay đổi
    
    @Column(name = "change_reason", length = 500)
    private String changeReason;                // Lý do (e.g., "Khách check-out", "Dọn phòng xong")
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;                    // Booking liên quan (nếu có)
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;                  // Thời gian log (mặc định: sysdatetime())
}
```

### Examples:
| Time | Room | Previous | New | ChangedBy | Reason | Booking |
|------|------|----------|-----|-----------|--------|---------|
| 2026-03-20 10:00 | 101 | AVAILABLE | OCCUPIED | - | Customer check-in | BK001 |
| 2026-03-22 11:00 | 101 | OCCUPIED | CLEANING | Staff123 | Customer check-out | BK001 |
| 2026-03-22 12:30 | 101 | CLEANING | AVAILABLE | Staff456 | Housekeeping complete | - |
| 2026-03-22 13:00 | 101 | AVAILABLE | MAINTENANCE | Admin1 | AC repair needed | - |

---

## 🔗 DETAIL: Scheduler Entity

```java
@Entity
public class Scheduler {
    @Id
    private Long schedulerId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;                    // Booking liên quan
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;                // Lịch làm việc
    
    @Column(name = "date")
    private LocalDateTime date;                 // Ngày được schedule
}
```

**Usage**: Lập lịch cho nhân viên làm việc, liên kết tới booking.

---

## 📋 DETAIL: Feedback Entity

```java
@Entity
public class Feedback {
    @Id
    private Long feedbackId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;                    // Customer review cho booking này
    
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private StaffAccount admin;                 // Admin reply to feedback
    
    @Column(name = "rating")
    private Integer rating;                     // 1-5 sao
    
    @Column(name = "content", length = 1000)
    private String content;                     // Nội dung review
    
    @Column(name = "content_reply", length = 1000)
    private String contentReply;                // Reply từ admin
    
    @Column(name = "is_hidden")
    private Boolean isHidden = false;           // Ẩn review không phù hợp
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

**Relationship**:
- Booking → Feedback (1:N)
- StaffAccount (admin) ← Feedback (Feedback.admin_id)
- Customer feedback được phép sau CHECKED_OUT

---

## 🎨 DETAIL: HTML Template - staff-manage-house-keeping.html

### Layout Structure:
```html
<body>
  ├── Logo (Fixed top-left)
  │   └── BookNow icon + text
  │
  ├── Sidebar (Fixed left, responsive)
  │   ├── Dashboard link
  │   ├── Booking list link
  │   ├── Room link
  │   ├── Housekeeping link ← ACTIVE
  │   ├── Customer link
  │   └── Logout
  │
  └── Main Content (lg:ml-64)
      ├── Filter Section
      │   ├── Task Status Dropdown
      │   │   └── th:each="status : ${taskStatuses}"
      │   │       └── th:text="${status.displayName}"
      │   │           PENDING, ASSIGNED, IN_PROGRESS, COMPLETED
      │   │
      │   ├── Priority Dropdown
      │   │   └── th:each="prio : ${priorities}"
      │   │       └── th:text="${prio.displayName}"
      │   │           LOW, NORMAL, HIGH, URGENT
      │   │
      │   └── Filter button + Clear button
      │
      ├── Tasks Table
      │   ├── Headers:
      │   │   - Room Number (task.room.roomNumber)
      │   │   - Room Type (task.room.roomType.name)
      │   │   - Time Checkout (task.booking.checkOutTime)
      │   │   - Priority (task.priority.cssEnumClass + displayName)
      │   │   - Status (task.status.cssEnumClass + displayName)
      │   │   - Assigned To (task.assignedTo.fullName or "Unassigned")
      │   │   - Action (View link)
      │   │
      │   └── Rows: th:each="task : ${housekeepingTaskLists}"
      │       └── Each row has:
      │           - Room #101 (bold)
      │           - Room Type "Deluxe"
      │           - Checkout date + time (formatted)
      │           - Priority badge (CSS color)
      │           - Status badge (CSS color)
      │           - Staff name or "Unassigned"
      │           - "View" link → /admin/manage-housekeeping/task-detail/{id}
      │
      └── Pagination Controls
          ├── Previous button (if currentPage > 1)
          ├── Page numbers (show current page)
          ├── Next button (if currentPage < totalPages)
          └── Preserve filters in pagination links
              (taskStatus, priority params retained)
```

### Key Thymeleaf Attributes:
```html
<!-- Enums to template -->
model.addAttribute("taskStatuses", TaskStatus.values());
model.addAttribute("priorities", PriorityStatus.values());

<!-- Task data -->
model.addAttribute("housekeepingTaskLists", paginatedTasks.getData());

<!-- Pagination -->
model.addAttribute("currentPage", paginatedTasks.getCurrentPage());
model.addAttribute("totalPages", paginatedTasks.getTotalPages());
model.addAttribute("totalItems", paginatedTasks.getTotalItems());
model.addAttribute("hasNext", paginatedTasks.isHasNext());
model.addAttribute("hasPrevious", paginatedTasks.isHasPrevious());

<!-- Selected filters (for re-selection) -->
model.addAttribute("selectedTaskStatus", taskStatus);
model.addAttribute("selectedPriority", priority);
```

### CSS Styling:
- Tailwind CSS 3 via CDN
- FontAwesome 6.4 for icons
- Custom gradient for "BookNow" text
- Responsive sidebar (hidden on mobile, visible on lg)
- Hover effects on rows (bg-gray-50)
- Badge colors via enum.getCssEnumClass()

---

## 🎨 DETAIL: HTML Template - staff-housekeeping-task-detail.html

### Layout Structure:
```html
<body>
  <header>
    - Title: "Housekeeping Dashboard"
    - Welcome message with username
    - Logout button
  </header>
  
  <main>
    ├── Back Link
    │   └── ← Back to Tasks (href="/admin/manage-housekeeping")
    │
    ├── Page Header Section
    │   └── (Content expected but may be incomplete)
    │
    └── Task Detail Form (POST)
        ├── Task ID (read-only)
        ├── Room Info (read-only)
        │   ├── Room number
        │   ├── Room type
        │   ├── Room status
        │   └── Last checkout time
        │
        ├── Staff Assignment
        │   └── Dropdown: availableStaff
        │       └── th:each="staff : ${availableStaff}"
        │       └── th:value="${staff.staffAccountId}"
        │       └── th:text="${staff.fullName}"
        │
        ├── Priority Update
        │   └── Selector (LOW, NORMAL, HIGH, URGENT)
        │
        ├── Notes Update
        │   └── Textarea
        │
        └── Submit Button (Update)
            └── POST → /admin/manage-housekeeping/task-detail/{id}
                ├── assignedStaffId (optional)
                ├── priority (optional)
                └── notes (optional)
                
            Redirect: /admin/manage-housekeeping
            Success Message: "Task updated successfully!"
```

### Form Fields:
```
assignedStaffId  - Long (optional)   | null = remove assignment
priority         - String (optional) | "LOW", "NORMAL", "HIGH", "URGENT"
notes            - String (optional) | Up to 1000 characters
```

### Auto-Update Logic:
```
IF assignedStaffId != null AND assignedStaffId > 0:
    - Set task.assignedTo = StaffAccount
    - AUTO: If task.status == PENDING → task.status = ASSIGNED
ELSE:
    - Set task.assignedTo = null (unassign)
```

---

## 📋 DETAIL: PaginatedResponse DTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;                    // Actual page data
    private int currentPage;                 // Current page (1-indexed)
    private int totalPages;                  // Total pages
    private long totalItems;                 // Total records
    private int pageSize;                    // Items per page (10)
    private boolean hasNext;                 // currentPage < totalPages
    private boolean hasPrevious;             // currentPage > 1
}
```

### Example Response:
```json
{
  "data": [
    { "id": 1, "room": {...}, "status": "PENDING", ... },
    { "id": 2, "room": {...}, "status": "ASSIGNED", ... },
    ...
  ],
  "currentPage": 1,
  "totalPages": 3,
  "totalItems": 25,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## 🔄 USE CASE DETAILS

### 📌 USE CASE 2A: Task Assignment with Auto-Status Update

**Scenario**: Staff assigns a housekeeping task to a specific worker

**HTTP Request**:
```
POST /admin/manage-housekeeping/task-detail/{id}
Content-Type: application/x-www-form-urlencoded

assignedStaffId=5
priority=HIGH
notes=Clean window AC urgently
```

**Processing Flow** (ManageHouseKeepingService.updateHousekeepingTaskDetail):
```java
1. Get task by ID (taskId)
   - If not found → throw IllegalArgumentException

2. If assignedStaffId != null && > 0:
   a) Find StaffAccount by ID
   b) If found:
       - task.assignedTo = staff
       - CHECK: if task.status == PENDING
           - AUTO SET: task.status = ASSIGNED
   c) Else: skip (staff not found)

3. If priority != null && !isBlank:
   - priority_enum = PriorityStatus.valueOf(priority)
   - task.priority = priority_enum
   - Example: valueOf("HIGH") → PriorityStatus.HIGH

4. If notes != null && !isBlank:
   - task.notes = notes

5. SAVE: houseKeepingRepository.save(task)

6. RETURN: Updated HousekeepingTask
```

**Template Response** (form-target):
```html
<form method="POST" action="/admin/manage-housekeeping/task-detail/{id}">
  <input name="assignedStaffId" type="hidden" value="5" />
  <input name="priority" type="hidden" value="HIGH" />
  <textarea name="notes">Clean window AC urgently</textarea>
  <button type="submit">Update Task</button>
</form>

<!-- Result: redirect:/admin/manage-housekeeping -->
<!-- Success message: "Task updated successfully!" -->
```

---

### 📌 USE CASE 3: Task Status Transition (Assumed Workflow)

**Note**: No explicit endpoint found in provided code. Expected logic:

```
PENDING (Initial)
  ↓
  [Staff views task, confirms starting work]
  ↓
ASSIGNED (Auto-set when staff assigned)
  ↓
  [Update via unknown endpoint or manual status change]
  ↓
IN_PROGRESS (Staff started work)
  - startedAt = NOW()
  ↓
  [Staff completes work, checks off checklist items]
  ↓
COMPLETED (Task finished)
  - completedAt = NOW()
  
→ Room status can change: CLEANING → AVAILABLE (if all tasks done)
```

**Missing**: POST endpoint to update task status directly?

---

### 📌 USE CASE 4: Room Checkout Trigger → Housekeeping Workflow (Inferred)

```
1. Customer Checkout
   POST /user/check-out/{bookingCode}/checkout
   ↓
   
   CheckOutService.checkOut(bookingCode)
   - Set booking.status = CHECKED_OUT
   - Set booking.actualCheckOutTime = NOW()
   - Update booking.updateAt = NOW()
   - Save booking
   
2. Room Status Update (implied)
   - Room status changes: OCCUPIED → CLEANING
   
3. Create HousekeepingTask (assumed auto-creation)
   - NEW HousekeepingTask
     {
       room_id = booking.room_id,
       booking_id = booking.booking_id,
       task_type = "POST_CHECKOUT_CLEANING",
       status = PENDING,
       priority = NORMAL,
       created_by = (system or current user),
       created_at = NOW()
     }
   
4. Task appears in staff manage-housekeeping list
   - Staff sees new PENDING task for Room 101
   - Priority: NORMAL
   - Assigned To: (Unassigned)
   
5. Staff Assignment
   - Staff clicks "View" → task-detail page
   - Selects a housekeeping worker
   - Sets priority = HIGH if urgent
   - Submits form
   
6. Backend Update
   - task.assignedTo = selected staff
   - task.status = ASSIGNED (auto)
   - task.priority = HIGH
   - Save & redirect
   
7. Housekeeping Work
   - Assigned staff marks task IN_PROGRESS
   - Completes checklist items
   - Marks task COMPLETED
   
8. Room Status Finalization
   - Room status: CLEANING → AVAILABLE
   - Create RoomStatusLog entry
      {
        room_id, 
        previous_status = "CLEANING",
        new_status = "AVAILABLE",
        changed_by = staff,
        change_reason = "Post-checkout cleaning completed",
        created_at = NOW()
      }
```

---

## 🔌 API ENDPOINTS SUMMARY

### Staff Housekeeping APIs:

| Method | URL | Handler | Logic |
|--------|-----|---------|-------|
| GET | `/admin/manage-housekeeping` | StaManageHouseKeepingController.showManageHouseKeepingPage | List tasks with pagination & filters |
| GET | `/admin/manage-housekeeping/task-detail/{id}` | StaManageHouseKeepingController.showHouseKeepingTaskDetail | Get single task, load available staff |
| POST | `/admin/manage-housekeeping/task-detail/{id}` | StaManageHouseKeepingController.updateHouseKeepingTaskDetail | Update task (assign staff, priority, notes) |

### Query Parameters:
```
GET /admin/manage-housekeeping?page=1&taskStatus=PENDING&priority=HIGH

Parsed in controller:
- page: int (default: 1)
- taskStatus: String (enum: PENDING, ASSIGNED, IN_PROGRESS, COMPLETED)
- priority: String (enum: LOW, NORMAL, HIGH, URGENT)

Validation:
- If invalid enum value → catch Exception, skip filter
```

---

## 🗂️ DATABASE RELATIONSHIPS (Detailed)

```sql
-- HousekeepingTask Relations
HousekeepingTask
├── room_id (FK) → Room.room_id
├── booking_id (FK) → Booking.booking_id (nullable)
├── assigned_to (FK) → StaffAccount.staff_account_id (nullable)
├── created_by (FK) → StaffAccount.staff_account_id (nullable)
└── (Reverse) TaskChecklist.task_id → TaskChecklist (1:N)

Room
├── room_type_id (FK) → RoomType.room_type_id
├── (Reverse) Booking.room_id → Booking (1:N)
├── (Reverse) Image.room_id → Image (1:N)
├── (Reverse) RoomStatusLog.room_id → RoomStatusLog (1:N)
└── (Reverse) HousekeepingTask.room_id → HousekeepingTask (1:N)

Booking
├── customer_id (FK) → Customer.customer_id
├── room_id (FK) → Room.room_id
├── (Reverse) HousekeepingTask.booking_id → HousekeepingTask (1:N)
├── (Reverse) Feedback.booking_id → Feedback (1:N)
├── (Reverse) Invoice.booking_id → Invoice (1:N)
├── (Reverse) Payment.booking_id → Payment (1:N)
├── (Reverse) Scheduler.booking_id → Scheduler (1:N)
└── (Reverse) RoomStatusLog.booking_id → RoomStatusLog (1:N)

TaskChecklist
└── task_id (FK) → HousekeepingTask.task_id [CASCADE DELETE]

RoomStatusLog
├── room_id (FK) → Room.room_id
├── changed_by (FK) → StaffAccount.staff_account_id (nullable)
└── booking_id (FK) → Booking.booking_id (nullable)

Scheduler
├── booking_id (FK) → Booking.booking_id
└── timetable_id (FK) → Timetable.timetable_id

Feedback
├── booking_id (FK) → Booking.booking_id
└── admin_id (FK) → StaffAccount.staff_account_id (nullable)

Invoice
└── booking_id (FK) → Booking.booking_id

Payment
└── booking_id (FK) → Booking.booking_id

Customer
└── (Reverse) Booking.customer_id → Booking (1:N)
└── (Reverse) RefreshToken.customer_id → RefreshToken (1:N)

StaffAccount
├── (Reverse) HousekeepingTask.assigned_to → HousekeepingTask (1:N)
├── (Reverse) HousekeepingTask.created_by → HousekeepingTask (1:N)
├── (Reverse) RoomStatusLog.changed_by → RoomStatusLog (1:N)
├── (Reverse) Feedback.admin_id → Feedback (1:N)
└── (Reverse) RefreshToken.staff_account_id → RefreshToken (1:N)
```

---

## ✅ CHECKLIST: Key Implementation Details

- [x] HousekeepingTask entity with all relationships
- [x] TaskStatus enum (PENDING, ASSIGNED, IN_PROGRESS, COMPLETED)
- [x] PriorityStatus enum (LOW, NORMAL, HIGH, URGENT) 
- [x] Auto-status update (PENDING → ASSIGNED when staff assigned)
- [x] TaskChecklist for tracking sub-tasks with CASCADE DELETE
- [x] RoomStatusLog for audit trail
- [x] Pagination with 10 items/page
- [x] Filtering by TaskStatus and Priority
- [x] Controller endpoints for list/detail/update
- [x] Template with Thymeleaf bindings
- [x] Enum dropdown rendering with displayName
- [x] CSS styling via enum.getCssEnumClass()
- [x] Staff assignment with available staff dropdown
- [x] Priority and notes update
- [x] Responsive sidebar navigation

---

## ⚠️ NOTES & OBSERVATIONS

1. **Missing Endpoint**: No explicit endpoint found to update task.status (ASSIGNED → IN_PROGRESS → COMPLETED). This might:
   - Be handled by a different controller (API endpoint?)
   - Require JavaScript AJAX call
   - Need to be implemented

2. **Auto-Creation**: HousekeepingTask creation on CHECKED_OUT booking is **inferred** but not explicitly coded in provided snippets. Check:
   - Event listeners/observers?
   - Background schedulers?
   - Direct service call somewhere?

3. **Permissions**: Controller doesn't show @PreAuthorize or role checks. Verify:
   - Spring Security config filters these endpoints
   - SecurityConfig.java has authorization rules

4. **Database Triggers**: Check SQL Server stored procedures or triggers:
   - Auto room status update when task completed?
   - Auto task creation on booking status change?

5. **Fetch Strategy**: HouseKeepingRepository uses LEFT JOIN FETCH to avoid N+1 query problems. Good practice!

6. **Pagination Logic**: Done in-memory (filter then paginate). For large datasets:
   - Consider pushing filters to SQL (WHERE clause)
   - Consider database-level pagination (OFFSET/FETCH)

---

## 🚀 RECOMMENDED NEXT STEPS

1. **Verify** task status update mechanism:
   - Is there a hidden AJAX endpoint?
   - Check StaffManageHouseKeepingController for additional unposted methods
   
2. **Check** HousekeepingTask auto-creation:
   - Search codebase for "HousekeepingTask" constructor calls
   - Check service methods for task creation
   - Look for event listeners

3. **Test** pagination:
   - Verify URL params are preserved during pagination
   - Test filter combinations

4. **Review** security:
   - Ensure only STAFF/ADMIN access `/admin/manage-housekeeping`
   - Verify permissions in SecurityConfig

5. **Database**:
   - Run script to check table schemas
   - Verify foreign keys and constraints
   - Check for indexes on frequently queried columns

---

**Last Updated**: 22/03/2026
**Document Version**: 1.0
**Scope**: Detailed Analysis - HousekeepingTask & Related Workflows
