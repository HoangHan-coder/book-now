package vn.edu.fpt.booknow.model.entities;

public enum RoomStatus {
    AVAILABLE,
    DIRTY,
    CLEANING,
    BOOKED,
    OCCUPIED,
    OUT_OF_SERVICE;



    public String getCssEnumClass() {
        return switch (this) {
            case AVAILABLE -> "bg-yellow-50 text-yellow-700 ring-1 ring-yellow-600/20";
            case CLEANING -> "bg-gray-50 text-gray-700 ring-1 ring-gray-600/20";
            case BOOKED -> "bg-blue-50 text-blue-700 ring-1 ring-blue-600/20";
            case OCCUPIED -> "bg-purple-50 text-purple-700 ring-1 ring-purple-600/20";
            case OUT_OF_SERVICE -> "bg-indigo-50 text-indigo-700 ring-1 ring-indigo-600/20";
            case DIRTY -> "bg-purple-50 text-purple-700 ring-1 ring-purple-600/20";
        };
    }

    public String getDisplayRoomName() {
        return switch (this) {
            case AVAILABLE -> "Phòng đang trống";
            case CLEANING -> "Đang dọn dẹp";
            case BOOKED -> "Phòng đã được đặt";
            case OCCUPIED -> "Đang ở";
            case OUT_OF_SERVICE -> "Quá giờ check out";
            case DIRTY-> "Phòng dơ";
        };
    }
}
