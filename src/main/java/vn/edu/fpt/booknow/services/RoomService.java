package vn.edu.fpt.booknow.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.dto.DetailRoomDTO;
import vn.edu.fpt.booknow.model.dto.SearchDTO;
import vn.edu.fpt.booknow.model.dto.TimeTableDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.repositories.*;

import java.time.LocalDateTime;
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
    private ImageRepository imageRepository;
    public RoomService(RoomRepository roomRepository,
                       AmenityRepository amenityRepo,
                       BookingRepository bookingRepository,
                       RoomTypeRepository roomTypeRepository,
                       TimeTableRepository timeTableRepository,
                       ScheduleRepository scheduleRepository,
                       ImageRepository imageRepository) {
        this.amenityRepo = amenityRepo;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.timeTableRepository = timeTableRepository;
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.imageRepository = imageRepository;
    }

    public Page<DetailRoomDTO> getAllRoomService() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<DetailRoomDTO> listRoom = roomRepository.findRoom(pageable);
        return listRoom;
    }

    public Page<DetailRoomDTO> getSearchService(SearchDTO searchDTO, int page) {
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


    public List<DetailRoomDTO> detailRoomService(Long id) {
        List<DetailRoomDTO> roomAmenityFlatDTO = roomRepository.findRoomDetail(id);
        Map<String, DetailRoomDTO> map = new LinkedHashMap<>();
        for (DetailRoomDTO x : roomAmenityFlatDTO) {
            DetailRoomDTO detailRoomDTO = map.computeIfAbsent(
                    x.getRoomId() + "",
                    idd -> new DetailRoomDTO(
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
            detailRoomDTO.getAmenityList().add(
                    new Amenity(x.getUtilities(), x.getIconUrl())
            );
        }
        List<DetailRoomDTO> detailRoomDTO = new ArrayList<>(map.values());
        return detailRoomDTO;
    }
    public boolean isBetween(LocalDateTime currentData, Long currentSlotId, TimeTableDTO start, TimeTableDTO end) {
        // Chuyển đổi thành một con số duy nhất để so sánh (Năm + Tháng + Ngày + Slot)
        // Ví dụ: Ngày 12/03 Slot 2 -> 2026031202
        long currentVal = currentData.getYear() * 1000000L + currentData.getMonthValue() * 10000L + currentData.getDayOfMonth() * 100L + currentSlotId;

        long startVal = start.getDate().getYear() * 1000000L + start.getDate().getMonthValue() * 10000L + start.getDate().getDayOfMonth() * 100L + start.getTimetableId();

        long endVal = end.getDate().getYear() * 1000000L + end.getDate().getMonthValue() * 10000L + end.getDate().getDayOfMonth() * 100L + end.getTimetableId();

        return currentVal >= startVal && currentVal <= endVal;
    }
    public List<Timetable> getAllTimeTable() {
        List<Timetable> list = timeTableRepository.findAll();
        return list;
    }

    public List<TimeTableDTO> getSlot(Long id) {
        List<TimeTableDTO> list = timeTableRepository.getBookingDetailsByRoomId(id);
        return list;
    }
    public Room findRoom(Long id) {
        Room rooms = roomRepository.getByRoomId(id);
        return rooms;
    }
    public List<Image> getImgRoom(Room room) {
        List<Image> list = imageRepository.getByRoom(room);
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

    public List<DetailRoomDTO> roomAll() {
        List<DetailRoomDTO> list = roomRepository.findAllRoom();
        return list;
    }
    public List<Booking> getAllBookingStatus() {
        List<Booking> list = bookingRepository.getByBookingStatus(BookingStatus.PENDING);
        return list;
    }
}
