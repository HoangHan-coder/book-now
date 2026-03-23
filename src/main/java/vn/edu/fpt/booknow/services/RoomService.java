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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        if ("price_asc".equals(sortType)) {
            sort = Sort.by("t.basePrice").ascending();
        } else if ("price_desc".equals(sortType)) {
            sort = Sort.by("t.basePrice").descending();
        } else {
            sort = Sort.by("r.roomId").descending();
        }
        Pageable pageable = PageRequest.of(page, 4, sort);
        List<String> amenities = searchDTO.getAmenity();
        Long amenityCount = (amenities != null && !amenities.isEmpty()) ? (long) amenities.size() : 0L;
        return roomRepository.searchRooms(
                searchDTO.getKeyword(),
                searchDTO.getMaxGuest(),
                searchDTO.getPrice(),
                amenities,
                amenityCount,
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
        long currentVal = currentData.getYear() * 1000000L + currentData.getMonthValue() * 10000L + currentData.getDayOfMonth() * 100L + currentSlotId;
        long startVal = start.getDate().getYear() * 1000000L + start.getDate().getMonthValue() * 10000L + start.getDate().getDayOfMonth() * 100L + start.getTimetableId();
        long endVal = end.getDate().getYear() * 1000000L + end.getDate().getMonthValue() * 10000L + end.getDate().getDayOfMonth() * 100L + end.getTimetableId();
        return currentVal >= startVal && currentVal <= endVal;
    }
    public Set<String> getBookedKeys(List<TimeTableDTO> getSlot, List<LocalDateTime> weekDates, List<Timetable> timetables) {
        Set<String> bookedKeys = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMM");

        if (getSlot == null || getSlot.isEmpty()) {
            return bookedKeys;
        }

        Map<Long, List<TimeTableDTO>> bookingsGrouped = getSlot.stream()
                .filter(s -> s.getBookingId() != null)
                .collect(Collectors.groupingBy(TimeTableDTO::getBookingId));

        for (List<TimeTableDTO> slotsInBooking : bookingsGrouped.values()) {
            TimeTableDTO representative = slotsInBooking.get(0);
            BookingStatus status = representative.getBookingStatus();
            if (status != BookingStatus.CANCELED && status != BookingStatus.FAILED) {

                TimeTableDTO first = slotsInBooking.stream()
                        .min(Comparator.comparing(TimeTableDTO::getDate)
                                .thenComparing(TimeTableDTO::getTimetableId))
                        .orElse(null);

                TimeTableDTO last = slotsInBooking.stream()
                        .max(Comparator.comparing(TimeTableDTO::getDate)
                                .thenComparing(TimeTableDTO::getTimetableId))
                        .orElse(null);

                if (first != null && last != null) {
                    for (LocalDateTime d : weekDates) {
                        for (Timetable t : timetables) {
                            // Gọi hàm isBetween sẵn có trong service
                            if (this.isBetween(d, t.getTimetableId(), first, last)) {
                                bookedKeys.add(d.format(formatter) + "-" + t.getTimetableId());
                            }
                        }
                    }
                }
            }
        }
        return bookedKeys;
    }
    public List<Scheduler> extractSchedulersFromBookings(List<Booking> bookings) {
        if (bookings == null) return new ArrayList<>();
        return bookings.stream()
                .flatMap(b -> b.getSchedulers().stream())
                .collect(Collectors.toList());
    }
    public List<String> getNext365Days() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return IntStream.range(0, 365)
                .mapToObj(i -> LocalDate.now().plusDays(i + 1).format(formatter))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSimpleTimetables(List<Timetable> timetables) {
        if (timetables == null) return new ArrayList<>();

        return timetables.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("timetableId", t.getTimetableId());
            map.put("slotName", t.getSlotName());
            return map;
        }).collect(Collectors.toList());
    }
    public List<LocalDateTime> getWeekDates(int daysCount) {
        List<LocalDateTime> weekDates = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();
        for (int i = 0; i < daysCount; i++) {
            weekDates.add(today.plusDays(i + 1));
        }
        return weekDates;
    }
    public List<Integer> getPageNumbers(Page<?> pages, int displayRange) {
        int totalPages = pages.getTotalPages();
        int current = pages.getNumber();

        int start = Math.max(0, current - displayRange / 2);
        int end = Math.min(totalPages - 1, start + displayRange - 1);

        if (end - start + 1 < displayRange) {
            start = Math.max(0, end - displayRange + 1);
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            pageNumbers.add(i);
        }
        return pageNumbers;
    }
    public Map<String, String> getBookedStatusMap(List<Scheduler> schedulers) {
        Map<String, String> bookedKeys = new HashMap<>();

        if (schedulers == null || schedulers.isEmpty()) {
            return bookedKeys;
        }

        for (Scheduler s : schedulers) {
            if (s.getBooking() != null && s.getBooking().getBookingStatus() != null) {
                String status = s.getBooking().getBookingStatus().toString();

                String key = s.getBooking().getRoom().getRoomId() + "_" +
                        s.getDate().toLocalDate().toString() + "_" +
                        s.getTimetable().getTimetableId();

                bookedKeys.put(key, status);
            }
        }
        return bookedKeys;
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

    public List<DetailRoomDTO> roomAll() {
        List<DetailRoomDTO> list = roomRepository.findAllRoom();
        return list;
    }
    public List<Booking> getAllBookingStatus() {
        List<Booking> list = bookingRepository.getByBookingStatus(BookingStatus.APPROVED);
        return list;
    }
}
