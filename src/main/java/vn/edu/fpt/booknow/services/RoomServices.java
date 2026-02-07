package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.repositories.RoomRepository;

import java.util.List;

@Service
public class RoomServices {
    private RoomRepository roomRepository;

    public RoomServices(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    @Transactional
    public List<Room> searchRoom(String status, String type, String roomNumber) {
        return roomRepository.search(
                status,
                type,
                roomNumber == null ? null : "%" + roomNumber + "%"
        );
    }
}
