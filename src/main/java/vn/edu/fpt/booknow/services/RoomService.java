package vn.edu.fpt.booknow.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.dto.SearchDTO;
import vn.edu.fpt.booknow.entities.Amenity;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.repositories.AmenityRepo;
import vn.edu.fpt.booknow.repositories.BookingRepo;
import vn.edu.fpt.booknow.repositories.RoomRepo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {
    private RoomRepo roomRepo;
    private AmenityRepo amenityRepo;
    private BookingRepo bookingRepo;
    public RoomService(RoomRepo roomRepo, AmenityRepo amenityRepo, BookingRepo bookingRepo) {
        this.amenityRepo = amenityRepo;
        this.roomRepo = roomRepo;
        this.bookingRepo = bookingRepo;
    }
    public Page<RoomDTO> getAllRoomService(){
        Pageable pageable =  PageRequest.of(0,3);
        Page<RoomDTO> listRoom = roomRepo.findRoom(pageable);
        return listRoom;
    }
    public List<Amenity> getAllAmenity() {
        return amenityRepo.findAll();
    }
    public Page<RoomDTO> getSearchService(SearchDTO searchDTO){
        Pageable pageable = PageRequest.of(0,3);
        Page<RoomDTO> list = roomRepo.searchRooms(searchDTO.getKeyword(),searchDTO.getArea(),searchDTO.getMaxGuest(),searchDTO.getPrice(),searchDTO.getAmenity(),pageable);
        return list;
    }


    public List<RoomDTO> detailRoomService(Long id) {
        List<RoomDTO> roomAmenityFlatDTO = roomRepo.findRoomDetail(id);
        Map<String, RoomDTO> map = new LinkedHashMap<>();
        for (RoomDTO x : roomAmenityFlatDTO) {
            RoomDTO roomDTO = map.computeIfAbsent(
                    x.getRoomId() +"",
                    idd -> new RoomDTO(
                            x.getRoomId(),
                            x.getBasePrice(),
                            x.getMaxGuest(),
                            x.getName(),
                            x.getDescription(),
                            x.getImageUrl(),
                            null,
                            null,
                            x.getOverPrice(),
                            new ArrayList<>()
                    )
            );
            roomDTO.getAmenityList().add(
                new Amenity(null,x.getName(),x.getIconUrl(),null,null)
            );
        }
        List<RoomDTO> roomDTO = new ArrayList<>(map.values());
        return roomDTO;
    }
    public void saveBooking(Booking booking) {
        bookingRepo.save(booking);
    }
 }
