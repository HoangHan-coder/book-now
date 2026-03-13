package vn.edu.fpt.booknow.controllers.model.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private int bookingCount;

    private long revenue;

    private int totalRooms;

    private int activeRooms;

    private int totalCustomers;

    private int totalStaff;

    private int thisWeekBookings;

    private int lastWeekBookings;

    private double bookingPercent;

    private double revenuePercent;

    private String currentMonth;

    private String compareLabel;

    private List<Integer> statusData;

    private List<Integer> quarterBookings;

    private List<String> quarterLabels;

    private String chartTitle;

    private List<String> revenueLabels;

    private List<Long> revenueData;

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

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(int activeRooms) {
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
}

