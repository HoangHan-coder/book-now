package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.repositories.RoomRepository;

import java.util.List;
import java.util.Map;

@Service
public class UpdateSttService {

    private final RoomRepository roomRepository;

    public UpdateSttService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // TRANSITION RULES (CONSTANT - KHÔNG TẠO LẠI NHIỀU LẦN)
    private static final Map<RoomStatus, List<RoomStatus>> TRANSITIONS = Map.of(

            RoomStatus.AVAILABLE, List.of(
                    RoomStatus.OUT_OF_SERVICE,
                    RoomStatus.MAINTENANCE
            ),

            RoomStatus.DIRTY, List.of(
                    RoomStatus.CLEANING
            ),

            RoomStatus.CLEANING, List.of(
                    RoomStatus.AVAILABLE
            ),

            RoomStatus.OUT_OF_SERVICE, List.of(
                    RoomStatus.MAINTENANCE,
                    RoomStatus.CLEANING
            ),

            RoomStatus.MAINTENANCE, List.of(
                    RoomStatus.DIRTY
            )
    );

    public void updateRoomStatus(Long roomId, RoomStatus newStatus) {

        // 1. Validate input
        if (newStatus == null) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        // 2. Lấy phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        RoomStatus currentStatus = room.getStatus();

        // 3. BLOCK trạng thái không được phép chỉnh tay
        if (currentStatus == RoomStatus.INACTIVE) {
            throw new RuntimeException("Phòng đã bị xóa, không thể cập nhật");
        }

        if (currentStatus == RoomStatus.BOOKED || currentStatus == RoomStatus.OCCUPIED) {
            throw new RuntimeException("Phòng đang có khách hoặc đã được đặt, không thể cập nhật thủ công");
        }

        // 4. Validate transition
        validateTransition(currentStatus, newStatus);

        // 5. Update
        room.setStatus(newStatus);
        roomRepository.save(room);
    }

    private void validateTransition(RoomStatus current, RoomStatus next) {

        List<RoomStatus> allowed = TRANSITIONS.get(current);

        if (allowed == null || !allowed.contains(next)) {
            throw new RuntimeException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next
            );
        }
    }
}
