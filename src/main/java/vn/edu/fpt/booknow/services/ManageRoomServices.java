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
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    @Transactional
    public Page<Room> getAllWithPagination(Pageable pageable){
        return roomRepository.findAll(pageable);
    }
}
