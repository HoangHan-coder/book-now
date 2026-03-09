package vn.edu.fpt.booknow.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dvt1knnb9",
                "api_key", "719465729237954",
                "api_secret", "rccPvLR6H0YjcZCSy9eSqJGDWgw"
        ));
    }
}
