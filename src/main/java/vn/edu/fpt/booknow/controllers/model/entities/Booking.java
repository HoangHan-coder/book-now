package vn.edu.fpt.booknow.controllers.model.entities;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Booking")
public class Booking {

    // ===== PRIMARY KEY =====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    // ===== CUSTOMER =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // ===== ROOM =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // ===== CHECK IN TIME =====
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    // ===== CHECK OUT TIME =====
    @Column(name = "check_out_time", nullable = false)
    private LocalDateTime checkOutTime;

    // ===== ACTUAL CHECK IN =====
    @Column(name = "actual_check_in_time")
    private LocalDateTime actualCheckInTime;

    // ===== ACTUAL CHECK OUT =====
    @Column(name = "actual_check_out_time")
    private LocalDateTime actualCheckOutTime;

    // ===== ID CARD FRONT =====
    @Column(name = "id_card_front_url", length = 500, nullable = false)
    private String idCardFrontUrl;

    // ===== ID CARD BACK =====
    @Column(name = "id_card_back_url", length = 500, nullable = false)
    private String idCardBackUrl;

    // ===== BOOKING STATUS =====
    @Column(name = "booking_status", length = 20, nullable = false)
    private String bookingStatus;

    // ===== TOTAL AMOUNT =====
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    // ===== BOOKING CODE =====
    @Column(name = "booking_code", length = 500)
    private String bookingCode;

    // ===== CREATED AT =====
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ===== NOTE =====
    @Column(name = "note", length = 255)
    private String note;

    // ===== UPDATED AT =====
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // ===== CONSTRUCTOR =====
    public Booking() {
    }

    // ===== GETTERS AND SETTERS =====

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalDateTime getActualCheckInTime() {
        return actualCheckInTime;
    }

    public void setActualCheckInTime(LocalDateTime actualCheckInTime) {
        this.actualCheckInTime = actualCheckInTime;
    }

    public LocalDateTime getActualCheckOutTime() {
        return actualCheckOutTime;
    }

    public void setActualCheckOutTime(LocalDateTime actualCheckOutTime) {
        this.actualCheckOutTime = actualCheckOutTime;
    }

    public String getIdCardFrontUrl() {
        return idCardFrontUrl;
    }

    public void setIdCardFrontUrl(String idCardFrontUrl) {
        this.idCardFrontUrl = idCardFrontUrl;
    }

    public String getIdCardBackUrl() {
        return idCardBackUrl;
    }

    public void setIdCardBackUrl(String idCardBackUrl) {
        this.idCardBackUrl = idCardBackUrl;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
