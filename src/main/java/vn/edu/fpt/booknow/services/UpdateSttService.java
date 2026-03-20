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

    public void updateRoomStatus(Long roomId, RoomStatus status) {

        // 1. Check null
        if (status == null) {
            throw new RuntimeException("Status không hợp lệ");
        }

        // 2. Lấy room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        // 3. Không cho update nếu đã DELETED
        if (room.getStatus() == RoomStatus.DELETED) {
            throw new RuntimeException("Phòng đã bị xóa");
        }

        // 4. Rule transition (giống JS)
        Map<RoomStatus, List<RoomStatus>> transitions = Map.of(
                RoomStatus.AVAILABLE, List.of(RoomStatus.BOOKED, RoomStatus.OUT_OF_SERVICE, RoomStatus.MAINTENANCE),
                RoomStatus.DIRTY, List.of(RoomStatus.CLEANING, RoomStatus.OUT_OF_SERVICE, RoomStatus.MAINTENANCE),
                RoomStatus.CLEANING, List.of(RoomStatus.AVAILABLE, RoomStatus.OUT_OF_SERVICE, RoomStatus.MAINTENANCE),
                RoomStatus.BOOKED, List.of(RoomStatus.OCCUPIED, RoomStatus.OUT_OF_SERVICE, RoomStatus.MAINTENANCE),
                RoomStatus.OCCUPIED, List.of(RoomStatus.DIRTY, RoomStatus.OUT_OF_SERVICE, RoomStatus.MAINTENANCE),
                RoomStatus.OUT_OF_SERVICE, List.of(RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE),
                RoomStatus.MAINTENANCE, List.of(RoomStatus.AVAILABLE)
        );

        RoomStatus current = room.getStatus();
        List<RoomStatus> allowed = transitions.get(current);

        if (allowed == null || !allowed.contains(status)) {
            throw new RuntimeException("Không thể chuyển trạng thái này");
        }

        // 5. Update
        room.setStatus(status);
        roomRepository.save(room);
    }
}
