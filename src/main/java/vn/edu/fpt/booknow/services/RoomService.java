package vn.edu.fpt.booknow.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.dto.RoomDTO;
import vn.edu.fpt.booknow.model.dto.SearchDTO;
import vn.edu.fpt.booknow.model.dto.TimeTableDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.repositories.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {
    private RoomRepository roomRepository;
    private AmenityRepository amenityRepo;
    private RoomTypeRepository roomTypeRepository;
    private TimeTableRepository timeTableRepository;
    private BookingRepository bookingRepository;
    private ScheduleRepository scheduleRepository;

    public RoomService(RoomRepository roomRepository,
                       AmenityRepository amenityRepo,
                       BookingRepository bookingRepository,
                       RoomTypeRepository roomTypeRepository,
                       TimeTableRepository timeTableRepository,
                       ScheduleRepository scheduleRepository) {
        this.amenityRepo = amenityRepo;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.timeTableRepository = timeTableRepository;
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Page<RoomDTO> getAllRoomService() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<RoomDTO> listRoom = roomRepository.findRoom(pageable);
        return listRoom;
    }

    public Page<RoomDTO> getSearchService(SearchDTO searchDTO, int page) {
        Sort sort;
        String sortType = searchDTO.getSortType();

        // 1. Logic xác định Sort (Giữ nguyên của bạn)
        if ("price_asc".equals(sortType)) {
            sort = Sort.by("t.basePrice").ascending();
        } else if ("price_desc".equals(sortType)) {
            sort = Sort.by("t.basePrice").descending();
        } else {
            sort = Sort.by("r.roomId").descending();
        }

        // 2. Khởi tạo Pageable
        Pageable pageable = PageRequest.of(page, 2, sort);

        // 3. Tính toán số lượng tiện ích để pass vào Query
        List<String> amenities = searchDTO.getAmenity();
        Long amenityCount = (amenities != null && !amenities.isEmpty()) ? (long) amenities.size() : 0L;

        // 4. Gọi Repository với tham số amenityCount mới
        return roomRepository.searchRooms(
                searchDTO.getKeyword(),
                searchDTO.getMaxGuest(),
                searchDTO.getPrice(),
                amenities,
                amenityCount, // Truyền biến đã tính vào đây
                pageable
        );
    }


    public List<RoomDTO> detailRoomService(Long id) {
        List<RoomDTO> roomAmenityFlatDTO = roomRepository.findRoomDetail(id);
        Map<String, RoomDTO> map = new LinkedHashMap<>();
        for (RoomDTO x : roomAmenityFlatDTO) {
            RoomDTO roomDTO = map.computeIfAbsent(
                    x.getRoomId() + "",
                    idd -> new RoomDTO(
                            x.getRoomId(),
                            x.getBasePrice(),
                            x.getMaxGuest(),
                            x.getRoomNumber(),
                            x.getRoomType(),
                            x.getDescription(),
                            x.getImageUrl(),
                            null,
                            null,
                            x.getOverPrice(),
                            new ArrayList<>()
                    )
            );
            roomDTO.getAmenityList().add(
                    new Amenity(null, x.getUtilities(), x.getIconUrl(), null, null)
            );
        }
        List<RoomDTO> roomDTO = new ArrayList<>(map.values());
        return roomDTO;
    }

    public List<Timetable> getAllTimeTable() {
        List<Timetable> list = timeTableRepository.findAll();
        return list;
    }

    public List<TimeTableDTO> getSlot(Long id) {
        List<TimeTableDTO> list = timeTableRepository.getBookingDetailsByRoomId(id);
        return list;
    }

    public List<TimeTableDTO> getSlotBooking() {
        List<TimeTableDTO> list = timeTableRepository.getBookingDetails();
        return list;
    }

    public List<RoomDTO> getAllRoomService(Sort sort) {
        List<RoomDTO> list = roomRepository.findAllRoomsSorted(sort);
        return list;
    }

    public List<RoomType> getAllRoomType() {
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        return roomTypes;
    }

    public List<Booking> getAllBooking() {
        List<Booking> booking = bookingRepository.findAll();
        return booking;
    }

    public List<Amenity> getAllAmenity() {
        List<Amenity> list = amenityRepo.findAll();
        return list;
    }

    public List<Scheduler> schedulers() {
        List<Scheduler> list = scheduleRepository.findAll();
        return list;
    }

    public List<RoomDTO> roomAll() {
        List<RoomDTO> list = roomRepository.findAllRoom();
        return list;
    }
}
