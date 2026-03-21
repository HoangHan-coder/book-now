package vn.edu.fpt.booknow.model.entities;

public enum RoomStatus {
    AVAILABLE,
    DIRTY,
    CLEANING,
    BOOKED,
    OCCUPIED,
    OUT_OF_SERVICE,
    MAINTENANCE,
    INACTIVE;



    public String getCssEnumClass() {
        return switch (this) {
            case AVAILABLE -> "bg-yellow-50 text-yellow-700 ring-1 ring-yellow-600/20";
            case CLEANING -> "bg-gray-50 text-gray-700 ring-1 ring-gray-600/20";
            case BOOKED -> "bg-blue-50 text-blue-700 ring-1 ring-blue-600/20";
            case OCCUPIED -> "bg-purple-50 text-purple-700 ring-1 ring-purple-600/20";
            case OUT_OF_SERVICE -> "bg-indigo-50 text-indigo-700 ring-1 ring-indigo-600/20";
            case DIRTY -> "bg-orange-50 text-orange-700 ring-1 ring-orange-600/20";
            case MAINTENANCE -> "bg-amber-50 text-amber-700 ring-1 ring-amber-600/20";
            case INACTIVE -> "bg-red-50 text-red-700 ring-1 ring-red-600/20";
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
            case MAINTENANCE ->  "Phòng đang Bảo trì";
            case INACTIVE -> "Phòng không hoạt động";
        };
    }
}
