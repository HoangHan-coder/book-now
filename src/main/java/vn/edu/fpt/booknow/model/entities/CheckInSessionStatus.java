package vn.edu.fpt.booknow.model.entities;

public enum CheckInSessionStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public String getCssClass() {
        return switch (this) {
            case PENDING -> "bg-yellow-50 text-yellow-700 ring-1 ring-yellow-600/20";
            case APPROVED -> "bg-green-50 text-green-700 ring-1 ring-green-600/20";
            case REJECTED -> "bg-red-50 text-red-700 ring-1 ring-red-600/20";
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Chờ duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Từ chối";
        };
    }
}
