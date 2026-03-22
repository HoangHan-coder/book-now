package vn.edu.fpt.booknow.model.entities;

public enum PriorityStatus {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    public String getCssEnumClass() {
        return switch (this) {

            case LOW ->
                    "bg-gray-100 text-gray-600 ring-1 ring-gray-500/20";

            case NORMAL ->
                    "bg-yellow-100 text-yellow-700 ring-1 ring-yellow-600/20";

            case HIGH ->
                    "bg-red-100 text-red-700 ring-1 ring-red-600/20";

            case URGENT ->
                    "bg-red-600 text-white ring-1 ring-red-600/20";
        };
    }

    public String getDisplayName() {
        return switch (this) {

            case LOW -> "Thấp";
            case NORMAL -> "Bình thường";
            case HIGH -> "Cao";
            case URGENT -> "Khẩn cấp";
        };
    }
}
