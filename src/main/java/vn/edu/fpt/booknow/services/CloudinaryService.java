package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Khởi tạo Cloudinary thông qua Constructor Injection
    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public Map uploadVideo(MultipartFile file) {
        try {
            // Cloudinary yêu cầu xác định resource_type là video cho các file mp4, mov...
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "customer_identity_videos" // Lưu vào thư mục riêng
                    )
            );
            // Trả về URL của video đã upload thành công
            return uploadResult;

        } catch (IOException e) {
            // Trong thực tế, bạn nên log lỗi và ném ra một Custom Exception
            throw new RuntimeException("Lỗi khi upload video lên Cloudinary: " + e.getMessage());
        }
    }
}
