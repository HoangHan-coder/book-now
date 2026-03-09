package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.entities.*;
import vn.edu.fpt.booknow.repositories.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



@Service
public class ManageRoomServices {
    private RoomRepository roomRepository;
    private RoomAmenityRepository roomAmenityRepository;
    private RoomTypeRepository roomTypeRepository;
    private AmenityRepository amenityRepository;
    private ImageRepository imageRepository;
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );


    public ManageRoomServices(RoomRepository roomRepository, RoomAmenityRepository roomAmenityRepository, RoomTypeRepository roomTypeRepository, AmenityRepository amenityRepository, ImageRepository imageRepository, Cloudinary cloudinary) {
        this.roomRepository = roomRepository;
        this.roomAmenityRepository = roomAmenityRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.imageRepository = imageRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public Page<Room> filterRooms(
            String status,
            String type,
            String roomNumber,
            Pageable pageable
    ) {

        Specification<Room> spec = (root, query, cb) -> cb.conjunction();

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (type != null && !type.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("roomType").get("name"), type));
        }

        if (roomNumber != null && !roomNumber.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("roomNumber"), "%" + roomNumber + "%"));
        }

        return roomRepository.findAll(spec, pageable);
    }

    @Transactional
    public Room findRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    @Transactional
    public void editRoom(
            Long roomId,
            BigDecimal basePrice,
            BigDecimal overPrice,
            String status,
            Long roomTypeId,
            String roomTypeDescription,
            List<Long> amenityIds,
            List<String> newAmenityNames,
            MultipartFile[] images,
            String deletedImageIds
    ) {
        /* ===== 1. LẤY ROOM ===== */
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        /* ===== 2. UPDATE ROOM ===== */
        room.getRoomType().setBasePrice(basePrice);
        room.getRoomType().setOverPrice(overPrice);
        room.setStatus(status);

        /* ===== 3. UPDATE ROOM TYPE ===== */
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        roomType.setDescription(roomTypeDescription);
        room.setRoomType(roomType);

        /* ===== 4. UPDATE AMENITIES ===== */
        roomAmenityRepository.deleteByRoom(room);

        if (amenityIds != null && !amenityIds.isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

            for (Amenity amenity : amenities) {
                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);
                roomAmenityRepository.save(ra);
            }
        }

        if (newAmenityNames != null && !newAmenityNames.isEmpty()) {

            for (String name : newAmenityNames) {
                name = name.trim();
                // tìm amenity theo tên
                Amenity amenity = amenityRepository.findByNameIgnoreCase(name);

                // nếu chưa tồn tại thì tạo mới
                if (amenity == null) {
                    amenity = new Amenity();
                    amenity.setName(name);
                    amenityRepository.save(amenity);
                }

                // thêm vào room
                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }

        /* ===== 5. BỎ ẢNH KHỎI PHÒNG (DETACH, KHÔNG DELETE) ===== */
        if (deletedImageIds != null && !deletedImageIds.isBlank()) {

            List<Long> imageIds = Arrays.stream(deletedImageIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();

            for (Long imageId : imageIds) {
                Image image = imageRepository.findById(imageId).orElse(null);

                if (image != null
                        && image.getRoom() != null
                        && image.getRoom().getRoomId().equals(roomId)) {

                    image.setRoom(null);
                    imageRepository.save(image);
                }
            }
        }

        /* ===== 6. UPLOAD ẢNH MỚI ===== */
        if (images != null) {

            for (MultipartFile file : images) {

                if (file == null || file.isEmpty()) continue;

                try {

                    /* ===== VALIDATE ===== */

                    if (file.getSize() > MAX_FILE_SIZE) {
                        throw new IllegalArgumentException("Image must be <= 2MB");
                    }

                    if (!ALLOWED_TYPES.contains(file.getContentType())) {
                        throw new IllegalArgumentException("Only JPEG, PNG, WebP allowed");
                    }

                    /* ===== UPLOAD CLOUDINARY ===== */

                    @SuppressWarnings("unchecked")
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", "booknow/rooms"
                            )
                    );

                    String imageUrl = (String) uploadResult.get("secure_url");
                    String publicId = (String) uploadResult.get("public_id");

                    if (imageUrl == null || publicId == null) {
                        throw new RuntimeException("Failed to upload image");
                    }

                    /* ===== SAVE IMAGE ===== */

                    Image image = new Image();
                    image.setImageUrl(imageUrl);
                    image.setPublicId(publicId);
                    image.setIsCover(false);
                    image.setRoom(room);

                    imageRepository.save(image);

                } catch (Exception e) {
                    throw new RuntimeException("Upload image failed", e);
                }
            }
        }

        /* ===== 7. SAVE ROOM ===== */
        roomRepository.save(room);
    }

    @Transactional
    public void createRoom(
            String roomNumber,
            Long roomTypeId,
            Long basePrice,
            Long overPrice,
            String status,
            String description,
            List<Long> amenityIds,
            List<String> newAmenityNames
    ) {

        // CHECK TRÙNG
        if(roomRepository.existsByRoomNumber(roomNumber)){
            throw new RuntimeException("Room already exists");
        }

        /* ===== 1. LẤY ROOM TYPE ===== */
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        /* ===== 2. UPDATE ROOM TYPE (TẠM THỜI) ===== */
        roomType.setBasePrice(BigDecimal.valueOf(basePrice));
        roomType.setOverPrice(BigDecimal.valueOf(overPrice));
        roomType.setDescription(description);

        /* ===== 3. TẠO ROOM ===== */
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setStatus(status);
        room.setRoomType(roomType);
        room.setIsDeleted(false);

        roomRepository.save(room);

        /* ===== 4. ADD AMENITIES ===== */
        if (amenityIds != null && !amenityIds.isEmpty()) {

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

            for (Amenity amenity : amenities) {

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }

        if (newAmenityNames != null) {
            for (String name : newAmenityNames) {

                Amenity amenity = new Amenity();
                amenity.setName(name);

                amenityRepository.save(amenity);

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }
    }

}
