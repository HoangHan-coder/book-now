package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
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
    public List<Room> getAllRoomService( ){
        List<Room> listRoom = roomRepo.findAll();
        return listRoom;
    }
}
