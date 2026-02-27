package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.repositories.RoomRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManageRoomServices {
    private RoomRepository roomRepository;
    public ManageRoomServices(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Page<Room> filterRooms(
            String status,
            String type,
            String roomNumber,
            Pageable pageable
    ) {
        if (roomNumber != null && !roomNumber.isBlank()) {
            return roomRepository.findByRoomNumberContaining(
                    roomNumber.trim(), pageable
            );
        }

        if (status != null && !status.isBlank()
                && type != null && !type.isBlank()) {
            return roomRepository.findByStatusAndRoomType_Name(
                    status, type, pageable
            );
        }

        if (status != null && !status.isBlank()) {
            return roomRepository.findByStatus(status, pageable);
        }

        if (type != null && !type.isBlank()) {
            return roomRepository.findByRoomType_Name(type, pageable);
        }

        return roomRepository.findAll(pageable);
    }

    @Transactional
    public Room findRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }
}
