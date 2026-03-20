
package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UpdateProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProfileService.class);

    private final CustomerRepository customerRepository;
    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public UpdateProfileService(CustomerRepository customerRepository,
                                Cloudinary cloudinary) {
        this.customerRepository = customerRepository;
        this.cloudinary = cloudinary;
    }
    public Customer checkCustomerExistByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found with email: " + email));
    }
    @Transactional
    public void updateProfile(String email,
                              String fullName,
                              String phoneNumber,
                              MultipartFile avatar) throws Exception {

        Customer customer = checkCustomerExistByEmail(email);

        // ===== VALIDATE NAME =====
        if (fullName == null) {
            throw new IllegalArgumentException("Full name is required");
        }

        String cleanedName = fullName.trim().replaceAll("\\s+", " ");

        if (!cleanedName.matches("^[\\p{L}]+(\\s+[\\p{L}]+)+$")) {
            throw new IllegalArgumentException("Full name must contain at least 2 valid words");
        }

// ===== VALIDATE PHONE =====
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number is required");
        }

        String phoneNormalized = phoneNumber.replaceAll("\\s+", "");
        String phoneRegex = "^(84|0)[35789][0-9]{8}$";

        if (!Pattern.matches(phoneRegex, phoneNormalized)) {
            throw new IllegalArgumentException("Invalid phone number format (VN: 0xxxxxxxxx)");
        }

        customer.setFullName(cleanedName);
        customer.setPhone(phoneNormalized);
        // ===== HANDLE AVATAR =====
        if (avatar != null && !avatar.isEmpty()) {

            // Validate file size
            if (avatar.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Avatar must be <= 2MB");
            }

            // Validate file type
            if (!ALLOWED_TYPES.contains(avatar.getContentType())) {
                throw new IllegalArgumentException("Only JPEG, PNG, WebP allowed");
            }

            // Delete old avatar from Cloudinary if exists
            if (customer.getAvatarPublicId() != null && !customer.getAvatarPublicId().isEmpty()) {
                try {
                    cloudinary.uploader().destroy(customer.getAvatarPublicId(), ObjectUtils.emptyMap());
                } catch (Exception e) {
                    logger.error("Failed to delete old avatar with public ID: {}", customer.getAvatarPublicId(), e);
                }
            }

            // Upload new avatar to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    avatar.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "booknow/avatar",
                            "transformation", "c_fill,w_300,h_300"
                    )
            );

            String avatarUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (avatarUrl == null || publicId == null) {
                throw new RuntimeException("Failed to upload avatar to Cloudinary");
            }

            customer.setAvatarUrl(avatarUrl);
            customer.setAvatarPublicId(publicId);
        }

        customer.setFullName(fullName.trim());
        customer.setPhone(phoneNormalized);

        // Ensure status is not null
        if (customer.getStatus() == null) {
            customer.setStatus("active");
        }

        customerRepository.save(customer);
    }
}