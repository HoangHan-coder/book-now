package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.controllers.model.entities.Room;
import vn.edu.fpt.booknow.repositories.RoomRepository;

@Service
public class UpdateSttService {
    private RoomRepository roomRepository;

    public UpdateSttService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public void updateRoomStatus(Long roomId, String status) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("không tìm thấy phòng"));
        if ("DELETED".equals(room.getStatus())) {
            throw new RuntimeException("Phòng đã bị xóa");
        }
        room.setStatus(status);
        roomRepository.save(room);
    }
}
