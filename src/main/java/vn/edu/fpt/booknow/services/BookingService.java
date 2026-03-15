package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookingService {

    @Autowired
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> getBookingByEmail(String email) {
        return bookingRepository.getBookingByCustomer_Email(email).orElse(null);
    }

    public Booking getBookingDetail(String code) {
        return bookingRepository.findByBookingCode(code).orElse(null);
    }

    @Transactional
    public void updateStatus(BookingStatus bookingStatus, String bookingCode) {
        Booking booking = getBookingDetail(bookingCode);
        booking.setBookingStatus(bookingStatus);
    }

    @Transactional
    public void updateIdCard(MultipartFile idCardFront, MultipartFile idCardBack, Long bookingId)
            throws Exception{

        if (idCardFront.getSize() > MAX_FILE_SIZE || idCardBack.getSize() > MAX_FILE_SIZE) {
            throw new Exception("Ảnh phải nhỏ hơn hoặc bằng 2mb");
        }

        if (!ALLOWED_TYPES.contains(idCardFront.getContentType()) || !ALLOWED_TYPES.contains(idCardBack.getContentType())) {
            throw new Exception("Hệ thống chỉ hỗ trợ ảnh có đuôi .png hoặc .jpg");
        }
        Booking booking = bookingRepository.getReferenceById(bookingId);

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadidCardFrontResult = cloudinary.uploader().upload(
                    idCardFront.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "book-now/card-id"
                    ));

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadIdCardBackResult = cloudinary.uploader().upload(
                    idCardBack.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "book-now/card-id"
                    ));

            String idCardFrontUrl = (String) uploadidCardFrontResult.get("secure_url");
            String publicIdCardFrontUrl = (String) uploadidCardFrontResult.get("public_id");

            String idCardBackUrl = (String) uploadIdCardBackResult.get("secure_url");
            String publicIdCardBackUrl = (String) uploadIdCardBackResult.get("public_id");

            booking.setIdCardFrontUrl(idCardFrontUrl);
            booking.setIdCardFontPublicId(publicIdCardFrontUrl);

            booking.setIdCardBackUrl(idCardBackUrl);
            booking.setIdCardBackPublicId(publicIdCardBackUrl);

            updateStatus(BookingStatus.PENDING, booking.getBookingCode());

        } catch (Exception e) {
            throw new Exception("Lỗi upload ảnh lên cloud");
        }
    }


    @SuppressWarnings("null")
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }
}
