package vn.edu.fpt.booknow.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.repositories.RoomRepo;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {
    private RoomRepo roomRepo;
    public RoomService(RoomRepo roomRepo ){
        this.roomRepo = roomRepo;
    }
    public Page<RoomDTO> getAllRoomService( ){
        Pageable pageable =  PageRequest.of(0,3);
        Page<RoomDTO> listRoom = roomRepo.findRoom(pageable);
        return listRoom;
    }
}
