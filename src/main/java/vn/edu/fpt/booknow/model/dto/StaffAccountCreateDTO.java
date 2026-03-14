package vn.edu.fpt.booknow.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StaffAccountCreateDTO {

    private String fullName;

    private String phone;

    private String email;

    private String password;

    private String confirmPassword;

    private String role;

    private MultipartFile avatar;
}