# 📋 DỰ ÁN SPRING BOOT - BOOKNOW HOMESTAY SYSTEM

## 🏗️ THÔNG TIN DỰ ÁN
- **Tên Dự Án**: BookNow (Hệ thống đặt phòng Homestay)
- **Framework**: Spring Boot 3.5.4
- **Ngôn Ngữ**: Java 21
- **Server**: Apache Tomcat (Port: 8080)
- **Database**: SQL Server (Test_Home_VersionNew3)
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven

---

## 📦 CẤU TRÚC THƯ MỤC

```
src/main/
├── java/vn/edu/fpt/booknow/
│   ├── App.java                           (Main Application)
│   ├── component/                         (Spring Components)
│   ├── config/                            (Cấu hình Spring)
│   │   ├── WebSocketConfig.java
│   │   ├── RedisConfig.java
│   │   ├── MomoConfig.java
│   │   └── CloudinaryConfig.java
│   ├── controllers/
│   │   ├── AuthController.java            (Xác thực & Đăng nhập)
│   │   ├── HomeController.java
│   │   ├── PaymentController.java
│   │   ├── admin/
│   │   │   └── AdminDashboardController.java
│   │   ├── customer/
│   │   │   ├── CusBookingDetailController.java
│   │   │   ├── CusCheckOutController.java
│   │   │   ├── CusProController.java      (Customer Profile)
│   │   │   ├── CusUpdateProController.java
│   │   │   └── CusChangePassController.java
│   │   └── staff/
│   │       ├── StaManageHouseKeepingController.java ⭐
│   │       ├── StaBookingDetailController.java
│   │       ├── StaBookingListController.java
│   │       └── StaBookingUpdateStatusController.java
│   ├── model/
│   │   ├── dto/                           (Data Transfer Objects)
│   │   │   ├── PaginatedResponse.java
│   │   │   ├── MomoRequestDTO.java
│   │   │   ├── MomoResponseDTO.java
│   │   │   ├── VerifyOtpRequest.java
│   │   │   └── ResetPasswordRequest.java
│   │   ├── entities/                      (JPA Entities)
│   │   │   ├── HousekeepingTask.java ⭐
│   │   │   ├── TaskChecklist.java ⭐
│   │   │   ├── StaffAccount.java ⭐
│   │   │   ├── Booking.java
│   │   │   ├── Customer.java
│   │   │   ├── Room.java
│   │   │   ├── RoomType.java
│   │   │   ├── RoomStatus.java
│   │   │   ├── RoomAmenity.java
│   │   │   ├── RoomStatusLog.java
│   │   │   ├── Invoice.java
│   │   │   ├── Payment.java
│   │   │   ├── Feedback.java
│   │   │   ├── Amenity.java
│   │   │   ├── Image.java
│   │   │   ├── Scheduler.java
│   │   │   ├── Timetable.java
│   │   │   ├── CheckInSession.java
│   │   │   ├── RefreshToken.java
│   │   │   ├── TaskStatus.java ⭐ (ENUM)
│   │   │   ├── PriorityStatus.java ⭐ (ENUM)
│   │   │   └── BookingStatus.java (ENUM)
│   │   └── map/
│   │       ├── StaffUserDetails.java
│   │       └── CustomerDetails.java
│   ├── repositories/
│   │   ├── HouseKeepingRepository.java ⭐
│   │   ├── StaffAccountRepository.java ⭐
│   │   ├── BookingRepository.java
│   │   ├── CustomerRepository.java
│   │   └── (...)
│   ├── services/
│   │   ├── JWTService.java
│   │   ├── OTPService.java
│   │   ├── MailService.java
│   │   ├── MomoPaymentService.java
│   │   ├── RedisService.java
│   │   ├── RecaptchaService.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── customer/
│   │   │   ├── BookingService.java
│   │   │   ├── CheckOutService.java
│   │   │   ├── CustomerService.java
│   │   │   ├── ProfileService.java
│   │   │   ├── UpdateProfileService.java
│   │   │   └── ChangePasswordService.java
│   │   └── staff/
│   │       ├── ManageHouseKeepingService.java ⭐
│   │       ├── BookingDetailService.java
│   │       ├── BookingListService.java
│   │       └── BookingUpdateService.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JWTFilter.java
│   │   ├── RecaptchaFilter.java
│   │   └── CloudinaryConfig.java
│   └── utils/
│       └── SignatureUtils.java
└── resources/
    ├── application.properties              (Cấu hình ứng dụng)
    ├── static/
    │   ├── css/
    │   └── js/
    └── templates/
        ├── index.html
        ├── payment.html
        ├── payment_method.html
        ├── result.html
        ├── error/
        │   └── 404.html
        ├── public/authentication/
        │   ├── login-customer.html
        │   ├── login-admin.html
        │   ├── forgot-password.html
        │   ├── reset-password.html
        │   └── verify-otp.html
        └── private/
            ├── admin-dashboard.html
            ├── customer-profile.html
            ├── customer-update-profile.html
            ├── customer-booking-detail.html
            ├── customer-checked-out.html
            ├── customer-change-password.html
            ├── staff-booking-list.html
            ├── staff-booking-detail.html
            ├── staff-booking-updated-status.html
            ├── staff-manage-house-keeping.html ⭐
            ├── staff-housekeeping-task-detail.html ⭐
            └── fragments/
```

---

## 🗄️ ENTITY CLASSES VÀ DATABASE SCHEMA

### 1️⃣ **HousekeepingTask** (Nhiệm vụ dọn dẹp)
**⭐ Entity Chính cho Housekeeping**

```java
@Entity
@Table(name = "HousekeepingTask")
public class HousekeepingTask {
    @Id
    private Long id;                                    // task_id
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;                                  // Phòng cần dọn
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;                            // Booking liên quan
    
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private StaffAccount assignedTo;                    // Nhân viên được giao việc
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private StaffAccount createdBy;                     // Nhân viên tạo task
    
    @Column(name = "task_type")
    private String taskType;                            // Loại nhiệm vụ (30 ký tự max)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus status;                          // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private PriorityStatus priority;                    // LOW, NORMAL, HIGH, URGENT
    
    @Column(name = "notes")
    private String notes;                               // Ghi chú (1000 ký tự max)
    
    @Column(name = "created_at")
    private Instant createdAt;                          // Thời gian tạo (mặc định: sysdatetime())
    
    @Column(name = "started_at")
    private Instant startedAt;                          // Thời gian bắt đầu
    
    @Column(name = "completed_at")
    private Instant completedAt;                        // Thời gian hoàn thành
}
```

### 2️⃣ **TaskStatus** (ENUM)
```java
public enum TaskStatus {
    PENDING,          // Chờ xử lý (màu xám)
    ASSIGNED,         // Đã phân công (màu vàng)
    IN_PROGRESS,      // Đang dọn phòng (màu xanh dương)
    COMPLETED;        // Hoàn thành (màu xanh lá)
    
    // Hỗ trợ CSS classes (Tailwind) cho template
    public String getCssEnumClass() { ... }
    
    // Hiển thị tiếng Việt
    public String getDisplayName() { ... }
}
```

### 3️⃣ **PriorityStatus** (ENUM)
```java
public enum PriorityStatus {
    LOW,              // Thấp (màu xám)
    NORMAL,           // Bình thường (màu vàng) - mặc định
    HIGH,             // Cao (màu đỏ)
    URGENT;           // Khẩn cấp (màu đỏ đậm)
    
    public String getCssEnumClass() { ... }
    public String getDisplayName() { ... }
}
```

### 4️⃣ **TaskChecklist** (Danh sách kiểm tra cho Task)
```java
@Entity
@Table(name = "TaskChecklist")
public class TaskChecklist {
    @Id
    private Long id;                                    // checklist_id
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private HousekeepingTask task;                      // Task liên quan
    
    @Column(name = "item_name")
    private String itemName;                            // Tên mục kiểm tra (200 ký tự max)
    
    @Column(name = "is_completed")
    private Boolean isCompleted;                        // 0 = không, 1 = hoàn thành
    
    @Column(name = "completed_at")
    private Instant completedAt;                        // Thời gian hoàn thành
}
```

### 5️⃣ **StaffAccount** (Tài khoản Nhân viên)
```java
@Entity
@Table(name = "StaffAccounts")
public class StaffAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffAccountId;
    
    @Column(name = "email", nullable = false)
    private String email;                               // Email đăng nhập
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;                        // Mật khẩu hash
    
    @Column(name = "full_name")
    private String fullName;                            // Tên đầy đủ
    
    @Column(name = "phone")
    private String phone;                               // Số điện thoại
    
    @Column(name = "avatar_url")
    private String avatarUrl;                           // URL avatar
    
    @Column(name = "role")
    private String role;                                // ROLE (Admin, Staff, ...)
    
    @Column(name = "status")
    private String status;                              // ACTIVE, INACTIVE (mặc định: ACTIVE)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;                    // Thời gian tạo tài khoản
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;                          // Soft delete
    
    @OneToMany(mappedBy = "admin")
    private List<Feedback> feedbacks;
    
    @OneToMany(mappedBy = "staffAccount")
    private List<RefreshToken> refreshTokens;
}
```

### 6️⃣ **Booking** (Đơn đặt phòng)
```java
@Entity
public class Booking {
    @Id
    private Long bookingId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;                          // Khách hàng đặt phòng
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;                                  // Phòng được đặt
    
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;                  // Thời gian nhận phòng
    
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;                 // Thời gian trả phòng
    
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;                // PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;                     // Tổng tiền
    
    @Column(name = "booking_code")
    private String bookingCode;                         // Mã booking (key tìm kiếm)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "booking")
    private List<HousekeepingTask> housekeepingTasks;   // ⭐ Liên kết tới Tasks
}
```

### 7️⃣ **Room** (Phòng)
```java
@Entity
@Table(name = "Room")
public class Room {
    @Id
    private Long roomId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;                          // Loại phòng
    
    @Column(name = "room_number")
    private String roomNumber;                          // Số phòng
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;                          // AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;
    
    @OneToMany(mappedBy = "room")
    private List<HousekeepingTask> housekeepingTasks;  // ⭐ Tasks cho phòng này
}
```

### 8️⃣ **Customer** (Khách hàng)
```java
@Entity
public class Customer {
    @Id
    private Long customerId;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "status")
    private String status;                              // ACTIVE, INACTIVE (mặc định: ACTIVE)
    
    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;
}
```

---

## 🎮 CONTROLLERS (MVC)

### 📌 **StaManageHouseKeepingController** ⭐
**File**: `controllers/staff/StaManageHouseKeepingController.java`

**Các Endpoint**:

| HTTP | Url | Mô tả | Logic |
|------|-----|-------|-------|
| GET | `/admin/manage-housekeeping` | Xem danh sách tasks | Lấy tasks với phân trang & lọc theo status/priority |
| GET | `/admin/manage-housekeeping/task-detail/{id}` | Xem chi tiết task | Lấy task theo ID, hiển thị danh sách staff |
| POST | `/admin/manage-housekeeping/task-detail/{id}` | Cập nhật task | Gán staff, cập nhật priority, notes |

**Các Tham số Lọc**:
- `taskStatus` (TaskStatus): PENDING, ASSIGNED, IN_PROGRESS, COMPLETED
- `priority` (PriorityStatus): LOW, NORMAL, HIGH, URGENT
- `page` (int): Số trang (mặc định: 1)

**Template**: `private/staff-manage-house-keeping.html`

---

### 📌 **StaBookingListController**
**File**: `controllers/staff/StaBookingListController.java`

Hiển thị danh sách booking cho nhân viên quản lý.

---

### 📌 **StaBookingDetailController**
**File**: `controllers/staff/StaBookingDetailController.java`

Xem chi tiết booking, thông tin khách hàng, phòng.

---

### 📌 **StaBookingUpdateStatusController**
**File**: `controllers/staff/StaBookingUpdateStatusController.java`

Cập nhật trạng thái booking (check-in, check-out, ...).

---

### 📌 **CusCheckOutController** (Customer)
**File**: `controllers/customer/CusCheckOutController.java`

| HTTP | Url | Mô tả |
|------|-----|-------|
| GET | `/user/check-out/{bookingCode}` | Xem trang check-out |
| POST | `/user/check-out/{bookingCode}/checkout` | Thực hiện check-out |

**Logic Check-Out**:
1. Tìm booking theo bookingCode
2. Kiểm tra status == CHECKED_IN
3. Kiểm tra đã thanh toán (totalAmount > 0)
4. Cập nhật status = CHECKED_OUT
5. Ghi lại thời gian check-out thực tế

---

### 📌 **AuthController**
**File**: `controllers/AuthController.java`

Xử lý:
- Đăng nhập (Admin/Customer)
- Đăng ký
- Quên mật khẩu / Reset mật khẩu
- Verify OTP

---

## 🔧 SERVICES (Business Logic)

### ⭐ **ManageHouseKeepingService**
**File**: `services/staff/ManageHouseKeepingService.java`

**Các Method Chính**:

```java
// 1. Lấy tất cả tasks với chi tiết (room, booking, staff)
public List<HousekeepingTask> getAllHousekeepingTask() 
    → Throws IllegalStateException nếu không có tasks

// 2. Lấy task theo ID
public HousekeepingTask getHousekeepingTaskById(Long id)

// 3. Cập nhật chi tiết task (gán staff, cập nhật priority/notes)
// Auto-status: PENDING → ASSIGNED khi gán staff
public HousekeepingTask updateHousekeepingTaskDetail(
    Long taskId, 
    Long assignedStaffId,    // null = bỏ gán
    String priority,
    String notes)

// 4. Lấy tasks với phân trang (mặc định 10 items/page)
public PaginatedResponse<HousekeepingTask> 
    getAllHousekeepingTaskWithPagination(int page)

// 5. Lấy tasks với phân trang + lọc theo status/priority
public PaginatedResponse<HousekeepingTask> 
    getAllHousekeepingTaskWithPaginationAndFilters(
        int page,
        TaskStatus taskStatus,
        PriorityStatus priority)
```

**Dependencies**:
- `HouseKeepingRepository` - Truy cập DB
- `StaffAccountRepository` - Tìm nhân viên để gán

---

### ⭐ **CheckOutService** (Customer)
**File**: `services/customer/CheckOutService.java`

```java
@Transactional
public String checkOut(String bookingCode)
    - Check status == CHECKED_IN
    - Check totalAmount > 0
    - Update status = CHECKED_OUT
    - Set actualCheckOutTime = now()
    - Return message (thành công / lỗi)
```

---

### 📌 **BookingService** (Customer)
**File**: `services/customer/BookingService.java`

Quản lý booking của khách hàng.

---

### 📌 **CustomerService**
**File**: `services/customer/CustomerService.java`

Quản lý thông tin khách hàng.

---

### 📌 **ProfileService** & **UpdateProfileService**
**File**: `services/customer/ProfileService.java`

Xem & cập nhật profile khách hàng.

---

### 📌 **BookingDetailService** (Staff)
**File**: `services/staff/BookingDetailService.java`

Lấy chi tiết booking để staff xử lý.

---

### 📌 **BookingListService** (Staff)
**File**: `services/staff/BookingListService.java`

Danh sách booking cho staff xem.

---

### 📌 **BookingUpdateService** (Staff)
**File**: `services/staff/BookingUpdateService.java`

Cập nhật trạng thái booking.

---

### 📌 **StaffAccountService** (Admin)
**File**: `services/admin/StaffAccountService.java`

Quản lý tài khoản nhân viên.

---

### 📌 **Các Service Khác**:
- **JWTService** - Quản lý JWT tokens
- **OTPService** - Xử lý OTP cho reset password
- **MailService** - Gửi email
- **MomoPaymentService** - Tích hợp thanh toán Momo
- **RedisService** - Cache với Redis
- **RecaptchaService** - Xác thực reCaptcha

---

## 📊 REPOSITORIES (Data Access Layer)

### ⭐ **HouseKeepingRepository**
```java
@Repository
public interface HouseKeepingRepository extends JpaRepository<HousekeepingTask, Long> {
    
    @Query("""
        SELECT DISTINCT t
        FROM HousekeepingTask t
        LEFT JOIN FETCH t.room r
        LEFT JOIN FETCH r.roomType
        LEFT JOIN FETCH t.booking
        LEFT JOIN FETCH t.assignedTo
    """)
    List<HousekeepingTask> findAllWithDetails();  // Fetch graph để tất cả quan hệ
}
```

### ⭐ **StaffAccountRepository**
```java
@Repository
public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {
    // Mặc định: findById(Long), findAll(), save(), delete(), ...
}
```

### 📌 **BookingRepository**
```java
Optional<Booking> findByBookingCode(String bookingCode);
```

### 📌 **CustomerRepository**
Tìm khách hàng theo email, ID.

---

## 🌐 HTML TEMPLATES (Thymeleaf)

### ⭐ **staff-manage-house-keeping.html**
**Đường dẫn**: `templates/private/staff-manage-house-keeping.html`

**Chức năng**:
- Hiển thị danh sách housekeeping tasks
- Phân trang (10 items/page)
- Lọc theo status & priority
- Nút "View Detail" để vào chi tiết task

**Model Attributes**:
```
housekeepingTaskLists   - List<HousekeepingTask>
currentPage            - Trang hiện tại
totalPages             - Tổng trang
totalItems             - Tổng items
hasNext / hasPrevious  - Kiểm tra trang trước/sau
selectedTaskStatus     - Filter được chọn
selectedPriority       - Filter được chọn
taskStatuses           - Tất cả TaskStatus enum values
priorities             - Tất cả PriorityStatus enum values
```

---

### ⭐ **staff-housekeeping-task-detail.html**
**Đường dẫn**: `templates/private/staff-housekeeping-task-detail.html`

**Chức năng**:
- Xem chi tiết task (room, booking, assigned staff, priority, notes)
- Form để gán nhân viên
- Cập nhật priority
- Cập nhật notes
- Nút "Back to Tasks"

**Model Attributes**:
```
housekeepingTask  - HousekeepingTask (entity)
availableStaff    - List<StaffAccount> (dropdown)
successMessage    - Thông báo thành công
```

**Form POST**: `/admin/manage-housekeeping/task-detail/{id}`

---

### 📌 **admin-dashboard.html**
Dashboard quản trị viên, tổng quan hệ thống.

---

### 📌 **staff-booking-list.html**
Danh sách booking cho nhân viên.

---

### 📌 **staff-booking-detail.html**
Chi tiết booking (customer info, room info, payment).

---

### 📌 **staff-booking-updated-status.html**
Cập nhật trạng thái booking (check-in, check-out).

---

### 📌 **customer-profile.html**
Profile khách hàng (xem thông tin cá nhân).

---

### 📌 **customer-update-profile.html**
Form cập nhật thông tin khách hàng.

---

### 📌 **customer-booking-detail.html**
Chi tiết booking của khách hàng (xem phòng, thời gian, ...).

---

### 📌 **customer-checked-out.html**
Trang check-out cho khách hàng.

---

### 📌 **customer-change-password.html**
Form đổi mật khẩu.

---

### 📌 **payment.html**
Thanh toán (Momo gateway).

---

### 📌 **Authentication Templates**:
- `login-customer.html` - Đăng nhập khách hàng
- `login-admin.html` - Đăng nhập admin/staff
- `forgot-password.html` - Quên mật khẩu
- `reset-password.html` - Reset mật khẩu
- `verify-otp.html` - Xác thực OTP

---

## ⚙️ CONFIGURATION

### **application.properties**

```properties
# Server
server.port=8080

# Database (SQL Server)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=Test_Home_VersionNew3;
spring.datasource.username=sa
spring.datasource.password=123
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Hibernate/JPA
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
spring.jpa.show-sql=true
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# Cloudinary (Image Upload)
cloudinary.cloud-name=dun6cug7p
cloudinary.api-key=234973538287588
cloudinary.api-secret=K30KWUVPXLB6zpB2cmOkGG15RLQ

# reCAPTCHA
google.recaptcha.secret=6LdnJ3YsAAAAAC7poECdcWrXZEged1oK1Nr-JFRo

# Momo Payment (Sandbox)
momo.partner-code=MOMO
momo.access-key=F8BBA842ECF85
momo.secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz
momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
momo.return-url=http://localhost:8080/book-now/pay/momo-return

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com

# Logging
logging.level.com.example.momo=DEBUG
```

---

## 📦 DEPENDENCIES (pom.xml)

| Dependency | Version | Mục đích |
|-----------|---------|---------|
| Spring Boot | 3.5.4 | Framework chính |
| Spring Data JPA | - | ORM, Database |
| Spring Web | - | MVC, REST |
| Spring Security | - | Xác thực & phân quyền |
| Thymeleaf | - | Template engine |
| Lombok | 1.18.42 | Giảm boilerplate code |
| JWT (JJWT) | 0.12.6 | Token authentication |
| SQL Server Driver | 13.2.1 | JDBC for SQL Server |
| Cloudinary | 1.39.0 | Image upload |
| Selenium | 4.18.1 | Web automation testing |
| Mail Starter | - | Email sending |
| Data Redis | - | Cache |
| Validation | - | Bean validation |
| Jackson | - | JSON processing |

---

## 🔐 ENUM CLASSES

### **TaskStatus** (Trạng thái Task)
```
PENDING          → "Chờ xử lý"    (CSS: gray)
ASSIGNED         → "Đã phân công" (CSS: yellow)
IN_PROGRESS      → "Đang dọn"     (CSS: blue)
COMPLETED        → "Hoàn thành"   (CSS: green)
```

### **PriorityStatus** (Mức độ ưu tiên)
```
LOW              → "Thấp"         (CSS: gray)
NORMAL           → "Bình thường"  (CSS: yellow, mặc định)
HIGH             → "Cao"          (CSS: red)
URGENT           → "Khẩn cấp"     (CSS: red-dark)
```

### **BookingStatus** (Trạng thái Booking)
```
PENDING          → Chờ xác nhận
CONFIRMED        → Đã xác nhận
CHECKED_IN       → Đã nhận phòng
CHECKED_OUT      → Đã trả phòng
CANCELLED        → Hủy đơn
```

### **RoomStatus**
```
AVAILABLE        → Phòng trống
OCCUPIED         → Phòng có khách
CLEANING         → Đang dọn (liên kết tới HousekeepingTask)
MAINTENANCE      → Bảo trì
```

---

## 🔄 USE CASES & BUSINESS LOGIC

### 📌 **USE CASE 1: Tạo Housekeeping Task** 
**Trigger**: Khách trả phòng (CHECKED_OUT) hoặc theo lịch dọn định kỳ

**Flow**:
```
1. Tạo HousekeepingTask
   - room_id ← Phòng cần dọn
   - booking_id ← Booking liên quan (nếu có)
   - task_type ← "ROUTINE", "URGENT_CLEANING", ...
   - status ← PENDING
   - priority ← NORMAL (mặc định)
   - created_by ← Admin/Manager
   - created_at ← NOW()
   
2. Tạo TaskChecklist items (nếu có template)
   - item_name ← "Lau bề mặt", "Hút bụi", ...
   - is_completed ← 0
```

---

### 📌 **USE CASE 2: Gán Nhân viên dọn phòng**
**Trigger**: Staff view task detail & gán nhân viên

**Flow** (ManageHouseKeepingService.updateHousekeepingTaskDetail):
```
1. Nhân viên chọn task từ danh sách
2. Click "View Detail" → GET /admin/manage-housekeeping/task-detail/{id}
   - Hiển thị form với dropdown availableStaff
   
3. Chọn nhân viên + cập nhật Priority & Notes
   - POST /admin/manage-housekeeping/task-detail/{id}
   
4. Backend:
   - assignedTo = staffAccount (từ assignedStaffId)
   - priority = PriorityStatus.valueOf(priority)
   - notes = notes
   - ⭐ AUTO-UPDATE: Nếu status == PENDING → status = ASSIGNED
   - save() → HousekeepingRepository
   
5. Redirect: /admin/manage-housekeeping với success message
```

---

### 📌 **USE CASE 3: Cập nhật Task Status**
**Flow** (Assumed từ structure):
```
PENDING  → [Gán staff]  → ASSIGNED
ASSIGNED → [Bắt đầu dọn] → IN_PROGRESS
IN_PROGRESS → [Hoàn thành] → COMPLETED (startedAt, completedAt được ghi)
```
*Note*: Không thấy endpoint POST để update status trực tiếp, có thể done qua API riêng.

---

### 📌 **USE CASE 4: Danh sách Task với Phân trang & Lọc**
**Trigger**: GET /admin/manage-housekeeping?page=1&taskStatus=PENDING&priority=HIGH

**Flow**:
```
1. Controller: StaManageHouseKeepingController.showManageHouseKeepingPage()
   - Parse params: page, taskStatus, priority
   
2. Service: ManageHouseKeepingService
   a) getAllHousekeepingTask() 
       → Query: SELECT from HousekeepingTask with FETCH graph
       → Load room, roomType, booking, assignedTo
   
   b) Filter in-memory:
       - taskStatus == null OR task.status == taskStatus
       - priority == null OR task.priority == priority
   
   c) Pagination (PAGE_SIZE = 10):
       - totalPages = ceil(totalItems / 10)
       - Validate page (1 <= page <= totalPages)
       - slice: [startIndex, endIndex]
   
   d) Return: PaginatedResponse<HousekeepingTask>
       {
           data: List<HousekeepingTask>,
           currentPage: int,
           totalPages: int,
           totalItems: long,
           pageSize: int,
           hasNext: boolean,
           hasPrevious: boolean
       }

3. Template: staff-manage-house-keeping.html
   - Display tasks in table
   - Show pagination controls
   - Show filter dropdowns
```

---

### 📌 **USE CASE 5: Customer Check-Out**
**Trigger**: Customer hoàn thành phòng

**Flow** (CusCheckOutController & CheckOutService):
```
1. GET /user/check-out/{bookingCode}
   - Find booking by code
   - Check permissions (customer đó sở hữu booking)
   - Render: customer-checked-out.html
   
2. POST /user/check-out/{bookingCode}/checkout
   - CheckOutService.checkOut(bookingCode):
     a) Find booking
     b) Check status == CHECKED_IN
     c) Check totalAmount > 0 (đã thanh toán)
     d) Update:
        - bookingStatus = CHECKED_OUT
        - actualCheckOutTime = NOW()
        - updateAt = NOW()
     e) Save & return message
   
3. Redirect: /user/booking-detail/{bookingCode}
   - Show success/error message
```

---

### 📌 **USE CASE 6: Xem Danh sách Booking (Staff)**
**Trigger**: Staff click "Đơn đặt phòng"

**Flow**:
```
1. GET /admin/booking-list?page=1&status=CONFIRMED
   
2. BookingListService:
   - Get all bookings with status = CONFIRMED
   - Apply filters (check-in date, room type, ...)
   - Paginate
   
3. Template: staff-booking-list.html
   - Show booking table (booking code, customer, room, check-in, status)
   - Link to detail view
```

---

### 📌 **USE CASE 7: Update Booking Status (Staff)**
**Trigger**: Staff xác nhận check-in, check-out

**Flow**:
```
1. GET /admin/booking-detail/{bookingCode}
   
2. POST /admin/booking-detail/{bookingCode}/update-status
   - BookingUpdateService:
     a) Check conditions based on current status
     b) Update status (CONFIRMED → CHECKED_IN, etc.)
     c) Create RoomStatusLog entry
     d) Update Room status (AVAILABLE → OCCUPIED → CLEANING)
     e) ⭐ Auto-create HousekeepingTask nếu cần dọn
   
3. Redirect with success message
```

---

## 📋 SUMMARY - QUAN HỆ ENTITY

```
┌─────────────┐
│  Customer   │ 1─────→ * Booking
├─────────────┤          │
│ customerId  │          │
│ email       │          │
│ password    │          │
│ fullName    │          │
└─────────────┘          │
                         │
                    ┌────┴──────────────┐
                    │    Booking        │
                    ├───────────────────┤
                    │ bookingId         │
                    │ bookingCode       │
                    │ customer_id (FK)  │
                    │ room_id (FK)      │
                    │ checkInTime       │
                    │ checkOutTime      │
                    │ bookingStatus     │ ─────→ PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT
                    │ totalAmount       │
                    └────┬──────────────┘
                         │
                    ┌────┴──────────────┐
                    │      Room         │
                    ├───────────────────┤
                    │ roomId            │
                    │ roomType_id (FK)  │
                    │ roomNumber        │
                    │ status            │ ─────→ AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE
                    └────┬──────────────┘
                         │
    1            *       │
┌──────────────────┐     │
│ House-           │     │
│ keepingTask ◄────┴─────┤
├──────────────────┤     │
│ task_id          │     │
│ room_id (FK) ◄───┴─────┘
│ booking_id (FK)──→ Booking (1)
│ assigned_to (FK) → StaffAccount
│ created_by (FK)  → StaffAccount
│ task_type        │
│ task_status      │
│ priority         │
│ notes            │
│ created_at       │
│ started_at       │
│ completed_at     │
└────┬─────────────┘
     │
     │ 1    *
     └─→ TaskChecklist
         ├──────────────
         │ checklist_id
         │ task_id (FK)
         │ item_name
         │ is_completed


┌──────────────────┐
│  StaffAccount    │
├──────────────────┤
│ staffAccountId   │
│ email            │
│ passwordHash     │
│ fullName         │
│ phone            │
│ role             │
│ status           │
│ createdAt        │
│ isDeleted        │
└──────────────────┘
      ▲
      │ ForeignKey
      │
   HousekeepingTask.assignedTo
   HousekeepingTask.createdBy
```

---

## 🎯 MAIN WORKFLOWS SUMMARY

| Workflow | Controller | Service | Entity | Status |
|----------|-----------|---------|--------|--------|
| **Housekeeping** | StaManageHouseKeepingController | ManageHouseKeepingService | HousekeepingTask ⭐ | ✅ DONE |
| **Task Assignment** | StaManageHouseKeepingController | ManageHouseKeepingService | HousekeepingTask | ✅ DONE |
| **Task Pagination** | StaManageHouseKeepingController | ManageHouseKeepingService | HousekeepingTask | ✅ DONE |
| **Check-Out** | CusCheckOutController | CheckOutService | Booking | ✅ DONE |
| **Booking List** | StaBookingListController | BookingListService | Booking | ✅ DONE |
| **Booking Detail** | StaBookingDetailController | BookingDetailService | Booking | ✅ DONE |
| **Update Booking** | StaBookingUpdateStatusController | BookingUpdateService | Booking | ✅ DONE |
| **Customer Profile** | CusProController | CustomerService | Customer | ✅ DONE |
| **Update Profile** | CusUpdateProController | UpdateProfileService | Customer | ✅ DONE |
| **Change Password** | CusChangePassController | ChangePasswordService | Customer | ✅ DONE |
| **Payment** | PaymentController | MomoPaymentService | Payment | ✅ DONE |

---

## 🚀 NEXT STEPS (Gợi ý)

1. **Kiểm tra** chi tiết các enum display names trong TaskStatus & PriorityStatus
2. **Xác nhận** các lifecycle của HousekeepingTask:
   - Khi nào task được tạo? (Auto sau CHECKED_OUT? Manual?)
   - Khi nào task được mark COMPLETED?
   - Có webhook hay event listener không?
3. **Kiểm tra** RoomStatusLog - log updates đến Room status
4. **Xác nhận** auto-creation tasks rule
5. **Test** pagination & filtering logic
6. **Review** template HTML để verify Thymeleaf bindings

---

**Report Generated**: 22/03/2026
**Language**: Vietnamese + Code
**Scope**: Complete SpringBoot Application Architecture
