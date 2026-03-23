package vn.edu.fpt.booknow.model.entities;

public enum BookingStatus {
    APPROVED,
    PENDING_PAYMENT,
    PAID,
    COMPLETED,
    CHECKED_IN,
    CHECKED_OUT,
    REJECTED,
    REJECTED_CHECKIN, // thêm ở đây
    FAILED,
    CANCELED;

    public String getCssClass() {
        return switch (this) {
            case PENDING_PAYMENT -> "bg-yellow-50 text-yellow-700 ring-1 ring-yellow-600/20";
            case APPROVED -> "bg-gray-50 text-blue-700 ring-1 ring-gray-600/20";
            case PAID -> "bg-blue-50 text-blue-700 ring-1 ring-blue-600/20";
            case CHECKED_IN -> "bg-purple-50 text-purple-700 ring-1 ring-purple-600/20";
            case CHECKED_OUT -> "bg-indigo-50 text-indigo-700 ring-1 ring-indigo-600/20";
            case COMPLETED -> "bg-green-50 text-green-700 ring-1 ring-green-600/20";
            case REJECTED -> "bg-red-50 text-red-700 ring-1 ring-red-600/20";
            case REJECTED_CHECKIN -> "bg-orange-50 text-orange-700 ring-1 ring-orange-600/20"; // thêm
            case FAILED -> "bg-red-50 text-red-701 ring-1 ring-red-600/20";
            case CANCELED -> "bg-red-50 text-red-702 ring-1 ring-red-600/20";
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case APPROVED -> "Đã kiểm duyệt";
            case PENDING_PAYMENT -> "Chờ thanh toán";
            case PAID -> "Đã thanh toán";
            case CHECKED_IN -> "Đã nhận phòng";
            case CHECKED_OUT -> "Đang chờ FeedBack";
            case COMPLETED -> "Hoàn thành";
            case REJECTED -> "Từ chối";
            case REJECTED_CHECKIN -> "Từ chối nhận phòng"; // thêm
            case FAILED -> "Thất bại";
            case CANCELED -> "Đã hủy";
        };
    }
}