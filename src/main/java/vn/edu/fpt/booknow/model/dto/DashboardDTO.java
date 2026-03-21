package vn.edu.fpt.booknow.model.dto;

import vn.edu.fpt.booknow.model.entities.BookingStatus;

import java.util.List;

public class DashboardDTO {
    private int bookingCount;

    private long revenue;

    private long revenueReceived;

    private long totalRooms;

    private long activeRooms;

    private int totalCustomers;

    private int totalStaff;

    private int thisWeekBookings;

    private int lastWeekBookings;

    private double bookingPercent;

    private double revenuePercent;

    private String currentMonth;

    private String compareLabel;

    private List<Integer> statusData;

    private int totalBookings;

    private List<Integer> quarterBookings;

    private List<String> quarterLabels;

    private String chartTitle;

    private List<String> revenueLabels;

    private List<Long> revenueData;

    private String dateLabel;

    private List<BookingStatus> statusLabels;


    // getter setter

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int bookingCount) {
        this.bookingCount = bookingCount;
    }

    public long getRevenue() {
        return revenue;
    }

    public void setRevenue(long revenue) {
        this.revenue = revenue;
    }

    public long getRevenueReceived() {
        return revenueReceived;
    }

    public void setRevenueReceived(long revenueReceived) {
        this.revenueReceived = revenueReceived;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(long activeRooms) {
        this.activeRooms = activeRooms;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getTotalStaff() {
        return totalStaff;
    }

    public void setTotalStaff(int totalStaff) {
        this.totalStaff = totalStaff;
    }

    public int getThisWeekBookings() {
        return thisWeekBookings;
    }

    public void setThisWeekBookings(int thisWeekBookings) {
        this.thisWeekBookings = thisWeekBookings;
    }

    public int getLastWeekBookings() {
        return lastWeekBookings;
    }

    public void setLastWeekBookings(int lastWeekBookings) {
        this.lastWeekBookings = lastWeekBookings;
    }

    public double getBookingPercent() {
        return bookingPercent;
    }

    public void setBookingPercent(double bookingPercent) {
        this.bookingPercent = bookingPercent;
    }

    public double getRevenuePercent() {
        return revenuePercent;
    }

    public void setRevenuePercent(double revenuePercent) {
        this.revenuePercent = revenuePercent;
    }

    public String getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(String currentMonth) {
        this.currentMonth = currentMonth;
    }

    public String getCompareLabel() {
        return compareLabel;
    }

    public void setCompareLabel(String compareLabel) {
        this.compareLabel = compareLabel;
    }

    public List<Integer> getStatusData() {
        return statusData;
    }

    public void setStatusData(List<Integer> statusData) {
        this.statusData = statusData;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public List<Integer> getQuarterBookings() {
        return quarterBookings;
    }

    public void setQuarterBookings(List<Integer> quarterBookings) {
        this.quarterBookings = quarterBookings;
    }

    public List<String> getQuarterLabels() {
        return quarterLabels;
    }

    public void setQuarterLabels(List<String> quarterLabels) {
        this.quarterLabels = quarterLabels;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    public List<String> getRevenueLabels() {
        return revenueLabels;
    }

    public void setRevenueLabels(List<String> revenueLabels) {
        this.revenueLabels = revenueLabels;
    }

    public List<Long> getRevenueData() {
        return revenueData;
    }

    public void setRevenueData(List<Long> revenueData) {
        this.revenueData = revenueData;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public List<BookingStatus> getStatusLabels() {
        return statusLabels;
    }

    public void setStatusLabels(List<BookingStatus> statusLabels) {
        this.statusLabels = statusLabels;
    }


}

