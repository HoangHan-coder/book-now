package vn.edu.fpt.booknow.model.entities;

public enum TaskStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED;

    public String getCssEnumClass() {
        return switch (this) {
            case PENDING ->
                    "bg-gray-100 text-gray-700 ring-1 ring-gray-600/20";

            case ASSIGNED ->
                    "bg-yellow-100 text-yellow-700 ring-1 ring-yellow-600/20";

            case IN_PROGRESS ->
                    "bg-blue-100 text-blue-700 ring-1 ring-blue-600/20";

            case COMPLETED ->
                    "bg-green-100 text-green-700 ring-1 ring-green-600/20";
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Chờ xử lý";
            case ASSIGNED -> "Đã phân công";
            case IN_PROGRESS -> "Đang dọn phòng";
            case COMPLETED -> "Hoàn thành";
        };
    }
}
