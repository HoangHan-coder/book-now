# 🎯 BOOKNOW - QUICK REFERENCE & CODE EXAMPLES

## 📐 SYSTEM ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────┐
│                        WEB BROWSER (Client)                     │
├─────────────────────────────────────────────────────────────────┤
│  HTML (Thymeleaf) ← Tailwind CSS + FontAwesome                 │
│  - staff-manage-house-keeping.html (Task List)                 │
│  - staff-housekeeping-task-detail.html (Task Detail)           │
│  - customer-checked-out.html (Checkout)                        │
└──────────────────────────┬──────────────────────────────────────┘
                          ↓
        ┌──────────────── HTTP ─────────────────┐
        │ (GET/POST requests with form data)   │
        ↓                                       ↓
┌──────────────────────────────────────────────────────────┐
│              SPRING BOOT 3.5.4 (Java 21)                │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  LAYER 1: Controllers (MVC)                            │
│  ┌────────────────────────────────────────────┐        │
│  │ @Controller                                │        │
│  │ ├─ StaManageHouseKeepingController (⭐)   │        │
│  │ ├─ CusCheckOutController                  │        │
│  │ ├─ StaBookingDetailController             │        │
│  │ └─ ...                                     │        │
│  └────────────────────────────────────────────┘        │
│           ↓ inject                                      │
│                                                         │
│  LAYER 2: Services (Business Logic)                   │
│  ┌────────────────────────────────────────────┐        │
│  │ @Service                                   │        │
│  │ ├─ ManageHouseKeepingService (⭐)         │        │
│  │ │  - getAllHousekeepingTask()              │        │
│  │ │  - updateHousekeepingTaskDetail()        │        │
│  │ │  - getAllHousekeepingTaskWithPagination()│        │
│  │ │  - getAllHousekeepingTaskWithPaginationAndFilters() │
│  │ │                                          │        │
│  │ ├─ CheckOutService                        │        │
│  │ │  - checkOut(bookingCode)                │        │
│  │ │    [statusflow: CHECKED_IN → CHECKED_OUT]│       │
│  │ │                                          │        │
│  │ └─ ...                                     │        │
│  └────────────────────────────────────────────┘        │
│           ↓ inject                                      │
│                                                         │
│  LAYER 3: Repositories (Data Access)                  │
│  ┌────────────────────────────────────────────┐        │
│  │ @Repository extends JpaRepository          │        │
│  │ ├─ HouseKeepingRepository (⭐)            │        │
│  │ │  - findById(Long id)                     │        │
│  │ │  - findAll()                             │        │
│  │ │  - findAllWithDetails() [custom query]   │        │
│  │ │  - save(HousekeepingTask)                │        │
│  │ │                                          │        │
│  │ ├─ StaffAccountRepository                  │        │
│  │ ├─ BookingRepository                       │        │
│  │ └─ ...                                     │        │
│  └────────────────────────────────────────────┘        │
│           ↓ execute queries                             │
│                                                         │
│  LAYER 4: Entities (ORM/Domain)                       │
│  ┌────────────────────────────────────────────┐        │
│  │ @Entity JPA managed classes                │        │
│  │ ├─ HousekeepingTask (⭐)                   │        │
│  │ │  tasks: id, room, booking, assignedTo   │        │
│  │ │  ├─ status (PENDING/ASSIGNED/IN_PROGRESS/  │      │
│  │ │  │          COMPLETED)                  │        │
│  │ │  └─ priority (LOW/NORMAL/HIGH/URGENT)  │        │
│  │ │                                          │        │
│  │ ├─ TaskChecklist                           │        │
│  │ ├─ StaffAccount                            │        │
│  │ ├─ Booking                                 │        │
│  │ ├─ Room                                    │        │
│  │ ├─ Customer                                │        │
│  │ ├─ RoomStatusLog                           │        │
│  │ └─ ...                                     │        │
│  └────────────────────────────────────────────┘        │
│           ↓ Hibernate ORM                               │
└──────────────────────────────────────────────────────────┘
                          ↓
        ┌──────────────── SQL ──────────────────┐
        │   (T-SQL for SQL Server)              │
        ↓                                       ↓
┌──────────────────────────────────────────────────────────┐
│            SQL SERVER 2022 (Database)                  │
├──────────────────────────────────────────────────────────┤
│  Database: Test_Home_VersionNew3                       │
│                                                        │
│  Tables:                                              │
│  ├─ HousekeepingTask (⭐)                              │
│  ├─ TaskChecklist                                     │
│  ├─ StaffAccounts (Staff ← Staff)                     │
│  ├─ Booking                                           │
│  ├─ Customer                                          │
│  ├─ Room                                              │
│  ├─ RoomType                                          │
│  ├─ RoomStatusLog                                     │
│  ├─ Feedback                                          │
│  ├─ Invoice                                           │
│  ├─ Payment                                           │
│  ├─ Scheduler                                         │
│  ├─ Timetable                                         │
│  ├─ Amenity                                           │
│  └─ ...                                               │
└──────────────────────────────────────────────────────────┘
```

---

## 🔗 REQUEST/RESPONSE FLOW EXAMPLES

### Example 1: View Housekeeping Tasks List

```
┌─────────────────────────────────────────────────────────────────┐
│ Browser: Click "Dọn dẹp" in navbar                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         GET /admin/manage-housekeeping?page=1&taskStatus=PENDING&priority=HIGH
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ StaManageHouseKeepingController.showManageHouseKeepingPage()    │
│                                                                 │
│ 1. Parse request params:                                        │
│    - page = 1                                                   │
│    - taskStatus = "PENDING"                                     │
│    - priority = "HIGH"                                          │
│                                                                 │
│ 2. Try convert to enums:                                        │
│    - taskStatus = TaskStatus.PENDING                            │
│    - priority = PriorityStatus.HIGH                             │
│                                                                 │
│ 3. Call service:                                                │
│    ManageHouseKeepingService.getAllHousekeepingTaskWithPaginationAndFilters(
│      page=1, taskStatus=PENDING, priority=HIGH)                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ ManageHouseKeepingService                                       │
│                                                                 │
│ 1. getAllHousekeepingTask():                                    │
│    Query: SELECT from HousekeepingTask with FETCH              │
│    Result: List<HousekeepingTask> = [Task1, Task2, ..., Task25]│
│                                                                 │
│ 2. Filter in memory:                                            │
│    .filter(task → task.status == PENDING)                       │
│    .filter(task → task.priority == HIGH)                        │
│    Result: List<HousekeepingTask> = [Task1, Task5, Task12]     │
│                                                                 │
│ 3. Paginate:                                                    │
│    - totalItems = 3                                             │
│    - pageSize = 10                                              │
│    - totalPages = 1 (ceil(3/10))                                │
│    - startIndex = 0, endIndex = 3                               │
│    - pageData = [Task1, Task5, Task12]                          │
│                                                                 │
│ 4. Return PaginatedResponse:                                    │
│    {                                                            │
│      data: [Task1, Task5, Task12],                              │
│      currentPage: 1,                                            │
│      totalPages: 1,                                             │
│      totalItems: 3,                                             │
│      pageSize: 10,                                              │
│      hasNext: false,                                            │
│      hasPrevious: false                                         │
│    }                                                            │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Controller: Add to Model                                        │
│                                                                 │
│ model.addAttribute("housekeepingTaskLists", [Task1, Task5,...])│
│ model.addAttribute("currentPage", 1)                            │
│ model.addAttribute("totalPages", 1)                             │
│ model.addAttribute("totalItems", 3)                             │
│ model.addAttribute("hasNext", false)                            │
│ model.addAttribute("hasPrevious", false)                        │
│ model.addAttribute("selectedTaskStatus", TaskStatus.PENDING)    │
│ model.addAttribute("selectedPriority", PriorityStatus.HIGH)     │
│ model.addAttribute("taskStatuses", [PENDING, ASSIGNED, ...])    │
│ model.addAttribute("priorities", [LOW, NORMAL, HIGH, URGENT])   │
│                                                                 │
│ return "private/staff-manage-house-keeping";                    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Thymeleaf: Render HTML                                          │
│                                                                 │
│ <!-- Filter Dropdowns -->                                       │
│ <select name="taskStatus">                                      │
│   <option value="">-- Tất cả --</option>                        │
│   <option th:each="status : ${taskStatuses}"                    │
│           th:value="${status.name()}"                           │
│           th:text="${status.displayName}"                       │
│           th:selected="${selectedTaskStatus == status}">        │
│ </select>                                                       │
│ <!-- Rendered: <option selected value="PENDING">Chờ xử lý</...> │
│                                                                 │
│ <!-- Task Table -->                                             │
│ <tbody>                                                         │
│   <tr th:each="task : ${housekeepingTaskLists}">               │
│     <td th:text="${task.room.roomNumber}">101</td>              │
│     <td th:text="${task.priority}"                              │
│         th:classappend="${task.priority.getCssEnumClass()}">    │
│       HIGH                                                      │
│     </td>                                                       │
│     <td th:text="${task.status.getDisplayName()}">Chờ xử lý</td>│
│     <td th:text="${task.assignedTo?.fullName ?: 'Unassigned'}">│
│   </tr>                                                         │
│ </tbody>                                                        │
│                                                                 │
│ <!-- Pagination -->                                             │
│ <a th:if="${hasPrevious}"                                       │
│    th:href="@{/admin/manage-housekeeping(page=1,               │
│              taskStatus=${param.taskStatus})}">                 │
│   Previous                                                      │
│ </a>                                                            │
│ Page 1 of 1                                                     │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ HTML Response:                                                  │
│                                                                 │
│ <table>                                                         │
│   <tr>                                                          │
│     <td>101</td>                                                │
│     <td>Deluxe</td>                                             │
│     <td>2026-03-13 14:00</td>                                   │
│     <td class="bg-red-600 text-white">URGENT</td>              │
│     <td class="bg-red-100 text-red-700">Chờ xử lý</td>          │
│     <td>Staff Name</td>                                         │
│     <td><a href="/admin/manage-housekeeping/task-detail/1">... │
│   </tr>                                                         │
│   <tr>                                                          │
│     <td>102</td>                                                │
│     ...                                                         │
│   </tr>                                                         │
│ </table>                                                        │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         BROWSER RENDERS TABLE WITH FILTERS & PAGINATION
```

---

### Example 2: Update Housekeeping Task (Assign Staff)

```
┌─────────────────────────────────────────────────────────────────┐
│ Browser: Click "View" link on Task 1                           │
│ GET /admin/manage-housekeeping/task-detail/1                   │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ StaManageHouseKeepingController.showHouseKeepingTaskDetail()    │
│                                                                 │
│ 1. Get task: ManageHouseKeepingService.getHousekeepingTaskById(1)│
│    Result: HousekeepingTask (id=1, room=101, status=PENDING, ...) │
│                                                                 │
│ 2. Get staff list: StaffAccountRepository.findAll()             │
│    Result: List<StaffAccount> = [Staff1, Staff2, Staff3, ...]  │
│                                                                 │
│ 3. Add to model:                                                │
│    model.addAttribute("housekeepingTask", task1)                │
│    model.addAttribute("availableStaff", [Staff1, ..., Staff10]) │
│                                                                 │
│ return "private/staff-housekeeping-task-detail";               │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Thymeleaf Renders Form:                                         │
│                                                                 │
│ <form method="POST" action="/admin/manage-housekeeping/...">    │
│   Room: 101 (Deluxe) <readonly>                                │
│   Assigned To:                                                 │
│   <select name="assignedStaffId">                              │
│     <option th:each="staff : ${availableStaff}"                │
│             th:value="${staff.staffAccountId}"                 │
│             th:text="${staff.fullName}">                       │
│   </select>                                                    │
│   Priority:                                                    │
│   <select name="priority">                                     │
│     <option th:each="p : ${priorities}"                        │
│             th:value="${p.name()}"                             │
│             th:text="${p.displayName}">                        │
│   </select>                                                    │
│   Notes:                                                       │
│   <textarea name="notes" placeholder="..."></textarea>         │
│   <button type="submit">Update Task</button>                   │
│ </form>                                                        │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ User Action: Select Staff [5: "Staff Name"] + Priority [HIGH]   │
│ Click "Update Task"                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         POST /admin/manage-housekeeping/task-detail/1
         assignedStaffId=5
         priority=HIGH
         notes=Urgent cleaning needed
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ StaManageHouseKeepingController.updateHouseKeepingTaskDetail()  │
│                                                                 │
│ params: taskId=1, assignedStaffId=5,                            │
│         priority="HIGH", notes="Urgent cleaning needed"         │
│                                                                 │
│ Call: ManageHouseKeepingService.updateHousekeepingTaskDetail()  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ ManageHouseKeepingService.updateHousekeepingTaskDetail()        │
│                                                                 │
│ 1. Find task by ID:                                             │
│    task = HouseKeepingRepository.findById(1)                    │
│    Result: HousekeepingTask (id=1, status=PENDING, ...)         │
│                                                                 │
│ 2. Process assignedStaffId = 5:                                 │
│    - Find staff: StaffAccountRepository.findById(5)             │
│    - Set task.assignedTo = Staff5                               │
│    - CHECK: if task.status == PENDING                           │
│      - AUTO SET: task.status = TaskStatus.ASSIGNED              │
│                                                                 │
│ 3. Process priority = "HIGH":                                   │
│    - priority_enum = PriorityStatus.valueOf("HIGH")             │
│    - task.priority = PriorityStatus.HIGH                        │
│                                                                 │
│ 4. Process notes:                                               │
│    - task.notes = "Urgent cleaning needed"                      │
│                                                                 │
│ 5. Save:                                                        │
│    HouseKeepingRepository.save(task)                            │
│                                                                 │
│ 6. Return updated task:                                         │
│    HousekeepingTask(id=1, status=ASSIGNED, priority=HIGH, ...)  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Controller redirects:                                           │
│ return "redirect:/admin/manage-housekeeping";                   │
│                                                                 │
│ With flash attribute:                                          │
│ model.addAttribute("successMessage",                            │
│   "Task updated successfully!");                                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         redirect: /admin/manage-housekeeping?page=1
                            ↓
         Browser: Shows task list with success message
         Task 1 now shows:
         - Status: "Đã phân công" (yellow badge)
         - Assigned To: "Staff Name"
         - Priority: "Khẩn cấp" (red badge)
```

---

### Example 3: Customer Check-Out

```
┌─────────────────────────────────────────────────────────────────┐
│ Browser URL: /user/check-out/BK00123                           │
│ GET method                                                      │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ CusCheckOutController.showCheckOutPage()                        │
│                                                                 │
│ 1. Check auth: if principal == null → redirect:/auth/login      │
│                                                                 │
│ 2. Get booking:                                                 │
│    booking = BookingService.getBookingDetail("BK00123")         │
│                                                                 │
│ 3. Check ownership:                                             │
│    if (booking.customer.email != principal.name)                │
│      → redirect with error message                              │
│                                                                 │
│ 4. Add to model:                                                │
│    model.addAttribute("booking", booking)                       │
│    (booking has: customer, room, checkInTime, checkOutTime, ...) │
│                                                                 │
│ return "private/customer-checked-out";                          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         Thymeleaf renders form with booking details
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ User sees:                                                      │
│ - Room: 101 (Deluxe)                                            │
│ - Check-in: 2026-03-13 14:00                                    │
│ - Check-out: 2026-03-15 11:00                                   │
│ - Status: CHECKED_IN                                            │
│ - Amount: 1,500,000 VND                                         │
│ - [Confirm Check-Out] button                                    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ User clicks [Confirm Check-Out]                                 │
│ POST /user/check-out/BK00123/checkout                           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ CusCheckOutController.checkout()                                │
│                                                                 │
│ 1. Get booking:                                                 │
│    booking = BookingService.getBookingDetail("BK00123")         │
│                                                                 │
│ 2. Verify ownership:                                            │
│    if (booking.customer.email != principal.name)                │
│      → error                                                    │
│                                                                 │
│ 3. Call CheckOutService:                                        │
│    message = CheckOutService.checkOut("BK00123")                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ CheckOutService.checkOut()                                      │
│                                                                 │
│ @Transactional                                                  │
│ 1. Find booking:                                                │
│    booking = BookingRepository.findByBookingCode("BK00123")      │
│                                                                 │
│ 2. Check conditions:                                            │
│    if (booking.status != CHECKED_IN)                            │
│      return "Booking chưa check-in";                            │
│                                                                 │
│    if (booking.totalAmount == null || <= 0)                     │
│      return "Chưa thanh toán";                                  │
│                                                                 │
│ 3. Update booking:                                              │
│    booking.bookingStatus = BookingStatus.CHECKED_OUT            │
│    booking.updateAt = now()                                     │
│    booking.actualCheckOutTime = now()                           │
│    BookingRepository.save(booking)                              │
│                                                                 │
│ 4. Return:                                                      │
│    "Check-out thành công"                                       │
│                                                                 │
│ [SIDE EFFECT: Room status should be updated to CLEANING]       │
│ [SIDE EFFECT: HousekeepingTask should be created]              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Controller:                                                     │
│ if (message.equals("Check-out thành công"))                     │
│   redirectAttributes.addFlashAttribute("success", message)      │
│ else                                                            │
│   redirectAttributes.addFlashAttribute("error", message)        │
│                                                                 │
│ return "redirect:/user/booking-detail/BK00123";                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         Redirect: /user/booking-detail/BK00123
         Success Message: "Check-out thành công"
         Now booking status shows: CHECKED_OUT
```

---

## 💻 CODE SNIPPETS & EXAMPLES

### Service Method: Get Tasks with Pagination & Filters

```java
@Service
public class ManageHouseKeepingService {
    
    public PaginatedResponse<HousekeepingTask> 
        getAllHousekeepingTaskWithPaginationAndFilters(
            int page,
            TaskStatus taskStatus,      // null = no filter
            PriorityStatus priority) {   // null = no filter
        
        final int PAGE_SIZE = 10;

        // 1. Get all tasks (with eager loading)
        List<HousekeepingTask> allTasks = getAllHousekeepingTask();

        // 2. Apply filters (stream pipeline)
        List<HousekeepingTask> filteredTasks = allTasks.stream()
            .filter(task -> taskStatus == null || task.getStatus() == taskStatus)
            .filter(task -> priority == null || task.getPriority() == priority)
            .toList();

        // 3. Calculate pagination metrics
        long totalItems = filteredTasks.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        // 4. Validate page number (ensure 1 <= page <= totalPages)
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        // 5. Slice data for current page
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, (int) totalItems);
        List<HousekeepingTask> pageData = filteredTasks.subList(startIndex, endIndex);

        // 6. Return paginated response
        return new PaginatedResponse<>(
            pageData,
            page,
            totalPages,
            totalItems,
            PAGE_SIZE);
    }
}
```

---

### Service Method: Update Task (Auto-Status)

```java
public HousekeepingTask updateHousekeepingTaskDetail(
        Long taskId,
        Long assignedStaffId,
        String priority,
        String notes) {

    // 1. Find task
    HousekeepingTask housekeepingTask = 
        houseKeepingRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Task not found with ID: " + taskId));

    // 2. Assign staff and auto-update status
    if (assignedStaffId != null && assignedStaffId > 0) {
        Optional<StaffAccount> staff = 
            staffAccountRepository.findById(assignedStaffId);
        
        if (staff.isPresent()) {
            housekeepingTask.setAssignedTo(staff.get());
            
            // ⭐ AUTO STATUS: PENDING → ASSIGNED
            if (housekeepingTask.getStatus() == TaskStatus.PENDING) {
                housekeepingTask.setStatus(TaskStatus.ASSIGNED);
            }
        }
    } else {
        housekeepingTask.setAssignedTo(null);  // Unassign
    }

    // 3. Update priority (if provided)
    if (priority != null && !priority.isBlank()) {
        housekeepingTask.setPriority(
            PriorityStatus.valueOf(priority));
    }

    // 4. Update notes (if provided)
    if (notes != null && !notes.isBlank()) {
        housekeepingTask.setNotes(notes);
    }

    // 5. Save and return
    return houseKeepingRepository.save(housekeepingTask);
}
```

---

### Controller Method: Show Task List with Filters

```java
@GetMapping("/manage-housekeeping")
public String showManageHouseKeepingPage(
        @RequestParam(name = "taskStatus", required = false) String taskStatusStr,
        @RequestParam(name = "priority", required = false) String priorityStr,
        @RequestParam(name = "page", defaultValue = "1") int page,
        Model model) {

    // 1. Parse filter parameters (with error handling)
    TaskStatus taskStatus = null;
    PriorityStatus priority = null;

    if (taskStatusStr != null && !taskStatusStr.isBlank()) {
        try {
            taskStatus = TaskStatus.valueOf(taskStatusStr);
        } catch (IllegalArgumentException e) {
            // Invalid enum value, ignore filter
        }
    }

    if (priorityStr != null && !priorityStr.isBlank()) {
        try {
            priority = PriorityStatus.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            // Invalid enum value, ignore filter
        }
    }

    // 2. Get paginated and filtered tasks
    PaginatedResponse<HousekeepingTask> paginatedTasks = 
        manageHouseKeepingService
            .getAllHousekeepingTaskWithPaginationAndFilters(
                page, taskStatus, priority);
    
    // 3. Add data to model
    model.addAttribute("housekeepingTaskLists", paginatedTasks.getData());
    model.addAttribute("currentPage", paginatedTasks.getCurrentPage());
    model.addAttribute("totalPages", paginatedTasks.getTotalPages());
    model.addAttribute("totalItems", paginatedTasks.getTotalItems());
    model.addAttribute("hasNext", paginatedTasks.isHasNext());
    model.addAttribute("hasPrevious", paginatedTasks.isHasPrevious());
    
    // 4. Pass filter values back to re-select in dropdowns
    model.addAttribute("selectedTaskStatus", taskStatus);
    model.addAttribute("selectedPriority", priority);
    
    // 5. Add enums to template for dropdown rendering
    model.addAttribute("taskStatuses", TaskStatus.values());
    model.addAttribute("priorities", PriorityStatus.values());
    
    // 6. Render template
    return "private/staff-manage-house-keeping";
}
```

---

### Thymeleaf Template: Enum Dropdown

```html
<!-- Dropdown for Task Status Filter -->
<select id="taskStatus" name="taskStatus" 
        class="px-3 py-2 border border-gray-300 rounded-lg">
    
    <!-- Default option -->
    <option value="">-- Tất cả --</option>
    
    <!-- Iterate through all TaskStatus enum values -->
    <option th:each="status : ${taskStatuses}" 
            th:value="${status.name()}"                           <!-- "PENDING", "ASSIGNED", ... -->
            th:text="${status.displayName}"                       <!-- "Chờ xử lý", "Đã phân công", ... -->
            th:selected="${selectedTaskStatus == status}">        <!-- Show selected if matching -->
        Default Text
    </option>
</select>

<!-- Rendered HTML example: -->
<!--
<select id="taskStatus" name="taskStatus" class="px-3 py-2 border border-gray-300 rounded-lg">
    <option value="">-- Tất cả --</option>
    <option value="PENDING" selected>Chờ xử lý</option>
    <option value="ASSIGNED">Đã phân công</option>
    <option value="IN_PROGRESS">Đang dọn phòng</option>
    <option value="COMPLETED">Hoàn thành</option>
</select>
-->
```

---

### Thymeleaf Template: Task List Table with Badges

```html
<table class="w-full">
    <thead class="bg-gray-50">
        <tr class="text-left text-sm text-gray-600">
            <th class="px-6 py-4">Room Number</th>
            <th class="px-6 py-4">Room Type</th>
            <th class="px-6 py-4">Checkout Time</th>
            <th class="px-6 py-4">Priority</th>
            <th class="px-6 py-4">Status</th>
            <th class="px-6 py-4">Assigned To</th>
            <th class="px-6 py-4">Action</th>
        </tr>
    </thead>
    <tbody class="divide-y divide-gray-200">
        <!-- Loop through each task -->
        <tr th:each="task : ${housekeepingTaskLists}" class="hover:bg-gray-50">
            
            <!-- Room Number -->
            <td class="px-6 py-4">
                <span class="font-bold text-gray-800"
                      th:text="${task.room.roomNumber}">
                    101
                </span>
            </td>

            <!-- Room Type -->
            <td class="px-6 py-4 text-gray-600"
                th:text="${task.room.roomType.name}">
                Deluxe
            </td>

            <!-- Checkout Time (formatted) -->
            <td class="px-6 py-4 text-gray-600">
                <div th:text="${#temporals.format(
                            task.booking.checkOutTime,
                            'yyyy-MM-dd')}">
                    2026-03-13
                </div>
                <p class="text-xs text-gray-500 mt-1" 
                   th:text="${#temporals.format(
                            task.booking.checkOutTime,
                            'HH:mm')}">
                    10:00 AM
                </p>
            </td>

            <!-- Priority Badge (with CSS from enum) -->
            <td class="px-6 py-4">
                <span class="px-2 py-1 rounded text-xs font-medium"
                      th:text="${task.priority.displayName}"  <!-- "Khẩn cấp" -->
                      th:classappend="${task.priority.getCssEnumClass()}">
                    <!-- CSS: "bg-red-600 text-white ring-1 ring-red-600/20" -->
                </span>
            </td>

            <!-- Status Badge (with CSS from enum) -->
            <td class="px-6 py-4">
                <span class="px-2 py-1 rounded text-xs font-medium"
                      th:text="${task.status.displayName}"    <!-- "Chờ xử lý" -->
                      th:classappend="${task.status.getCssEnumClass()}">
                    <!-- CSS: "bg-gray-100 text-gray-700 ring-1 ring-gray-600/20" -->
                </span>
            </td>

            <!-- Assigned Staff (with safe navigation - Elvis operator) -->
            <td class="px-6 py-4 text-gray-600"
                th:text="${task.assignedTo != null ? 
                          task.assignedTo.fullName : 
                          'Unassigned'}">
                Staff Name or Unassigned
            </td>

            <!-- Action Link -->
            <td class="px-6 py-4">
                <a th:href="@{/admin/manage-housekeeping/task-detail/{id}(id=${task.id})}"
                   class="text-blue-600 hover:underline text-sm mr-3">
                    View
                </a>
            </td>

        </tr>
    </tbody>
</table>
```

---

## 🛠️ ENUM HELPER METHODS

### TaskStatus Enum:
```java
// Display in Vietnamese
status.getDisplayName() 
  → PENDING = "Chờ xử lý"
  → ASSIGNED = "Đã phân công"
  → IN_PROGRESS = "Đang dọn phòng"
  → COMPLETED = "Hoàn thành"

// CSS Classes for Tailwind
status.getCssEnumClass()
  → PENDING = "bg-gray-100 text-gray-700 ring-1 ring-gray-600/20"
  → ASSIGNED = "bg-yellow-100 text-yellow-700 ring-1 ring-yellow-600/20"
  → IN_PROGRESS = "bg-blue-100 text-blue-700 ring-1 ring-blue-600/20"
  → COMPLETED = "bg-green-100 text-green-700 ring-1 ring-green-600/20"
```

### PriorityStatus Enum:
```java
// Display in Vietnamese
priority.getDisplayName()
  → LOW = "Thấp"
  → NORMAL = "Bình thường"
  → HIGH = "Cao"
  → URGENT = "Khẩn cấp"

// CSS Classes for Tailwind
priority.getCssEnumClass()
  → LOW = "bg-gray-100 text-gray-600 ring-1 ring-gray-500/20"
  → NORMAL = "bg-yellow-100 text-yellow-700 ring-1 ring-yellow-600/20"
  → HIGH = "bg-red-100 text-red-700 ring-1 ring-red-600/20"
  → URGENT = "bg-red-600 text-white ring-1 ring-red-600/20"
```

---

## 📌 DEPENDENCY INJECTION IN ACTION

```java
@Controller
@RequestMapping("/admin")
public class StaManageHouseKeepingController {
    
    private final ManageHouseKeepingService manageHouseKeepingService;
    private final StaffAccountRepository staffAccountRepository;

    // Constructor Injection (Spring automatically injects)
    public StaManageHouseKeepingController(
            ManageHouseKeepingService manageHouseKeepingService,
            StaffAccountRepository staffAccountRepository) {
        this.manageHouseKeepingService = manageHouseKeepingService;
        this.staffAccountRepository = staffAccountRepository;
    }

    @GetMapping("/manage-housekeeping")
    public String showManageHouseKeepingPage(...) {
        // Use injected service
        paginatedTasks = manageHouseKeepingService
            .getAllHousekeepingTaskWithPaginationAndFilters(...);
        
        // Use injected repository
        staffList = staffAccountRepository.findAll();
    }
}
```

**How Spring injects**:
1. Scan classpath for @Service, @Repository classes
2. Find matching constructors in @Controller
3. Auto-wire beans at runtime
4. No need for `new` keyword!

---

## ⚡ TRANSACTION MANAGEMENT

```java
@Service
public class CheckOutService {
    
    @Autowired
    private BookingRepository bookingRepository;

    // @Transactional ensures ACID properties
    @Transactional  // Begins transaction, commits if success, rollback if exception
    public String checkOut(String bookingCode) {
        
        // Database operation 1
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
            .orElseThrow(() -> new RuntimeException("Not found"));
        
        // Validations
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN)
            return "Error message";
        
        if (booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
            return "Error message";
        
        // Database operation 2
        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        booking.setUpdateAt(LocalDateTime.now());
        booking.setActualCheckOutTime(LocalDateTime.now());
        
        // Database operation 3 (implicit flush at transaction end)
        bookingRepository.save(booking);
        
        // If no exception thrown → COMMIT
        // If exception thrown → ROLLBACK (all changes undone)
        
        return "Success message";
    }
}
```

---

## 🎨 CSS BADGE EXAMPLE

```html
<!-- High Priority Task -->
<span class="px-2 py-1 rounded text-xs font-medium
           bg-red-600 text-white ring-1 ring-red-600/20">
    Khẩn cấp
</span>

<!-- Output: Red badge with white text -->

<!-- Low Priority Task -->
<span class="px-2 py-1 rounded text-xs font-medium
           bg-gray-100 text-gray-600 ring-1 ring-gray-500/20">
    Thấp
</span>

<!-- Output: Gray badge with dark text -->

<!-- Completed Status -->
<span class="px-2 py-1 rounded text-xs font-medium
           bg-green-100 text-green-700 ring-1 ring-green-600/20">
    Hoàn thành
</span>

<!-- Output: Green badge -->
```

---

## 📝 PAGINATION URL STRUCTURE

```
/admin/manage-housekeeping
  ?page=1
  &taskStatus=PENDING
  &priority=HIGH

Query Parameters:
- page: 1-based page number (default: 1)
- taskStatus: Enum name filter (optional)
- priority: Enum name filter (optional)

Pagination links preserve filters:
  Previous: /admin/manage-housekeeping?page=0&taskStatus=PENDING&priority=HIGH
  Next: /admin/manage-housekeeping?page=2&taskStatus=PENDING&priority=HIGH
```

---

**Document Generated**: 22/03/2026
**Version**: 1.0 - Quick Reference
**Contains**: Architecture, Flow Examples, Code Snippets, CSS Examples
