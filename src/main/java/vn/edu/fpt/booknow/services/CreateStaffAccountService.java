package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.model.dto.StaffAccountCreateDTO;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.services.MailService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class CreateStaffAccountService {

    private final StaffAccountRepository repository;
    private final Cloudinary cloudinary;
    private final MailService mailService;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public CreateStaffAccountService(StaffAccountRepository repository,
                                     Cloudinary cloudinary,
                                     MailService mailService) {
        this.repository = repository;
        this.cloudinary = cloudinary;
        this.mailService = mailService;
    }

    public void createStaffAccount(StaffAccountCreateDTO dto) {

        validateInput(dto);

        if(repository.findStaffAccountByEmail(dto.getEmail()).isPresent()){
            throw new RuntimeException("Email đã tồn tại");
        }

        StaffAccount account = buildEntity(dto);
        uploadAvatar(dto.getAvatar(), account);
        repository.save(account);
        // gửi mail
        mailService.sendAccountCreated(
                account.getEmail(),
                account.getFullName(),
                account.getPhone(),
                account.getEmail(),
                dto.getPassword() // dùng password gốc
        );
    }

    private void uploadAvatar(MultipartFile avatar, StaffAccount account) {

        try {

            if(avatar != null && !avatar.isEmpty()){

                // validate file size
                if(avatar.getSize() > MAX_FILE_SIZE){
                    throw new RuntimeException("Avatar tối đa 10MB");
                }

                Map uploadResult = cloudinary.uploader().upload(
                        avatar.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "booknow/avatar",
                                "transformation", "c_fill,w_300,h_300"
                        )
                );

                account.setAvatarUrl(uploadResult.get("secure_url").toString());
                account.setAvatarPublicId(uploadResult.get("public_id").toString());
            }

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar thất bại");
        }
    }

    private void validateInput(StaffAccountCreateDTO dto){

        if(dto.getFullName() == null || dto.getFullName().isBlank()){
            throw new IllegalArgumentException("Tên không được bỏ trống");
        }

        if(dto.getPhone() == null || !dto.getPhone().matches("^\\d{10}$")){
            throw new IllegalArgumentException("Số điện thoại phải đủ 10 số");
        }

        if(dto.getEmail() == null || !dto.getEmail().contains("@")){
            throw new IllegalArgumentException("Email không hợp lệ");
        }

        if(dto.getPassword() == null || dto.getPassword().isBlank()){
            throw new IllegalArgumentException("Mật khẩu không được bỏ trống");
        }

        if(!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        // ⭐ CHECK AVATAR
        if(dto.getAvatar() == null || dto.getAvatar().isEmpty()){
            throw new RuntimeException("Vui lòng tải lên ảnh đại diện");
        }
    }

    private StaffAccount buildEntity(StaffAccountCreateDTO dto){

        StaffAccount account = new StaffAccount();

        account.setFullName(dto.getFullName());
        account.setPhone(dto.getPhone());
        account.setEmail(dto.getEmail());
        account.setPasswordHash(hashPassword(dto.getPassword()));
        account.setRole(dto.getRole());
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        account.setIsDeleted(false);

        return account;
    }

    private String hashPassword(String password){
        return org.springframework.security.crypto.bcrypt.BCrypt
                .hashpw(password,
                        org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12));
    }
}