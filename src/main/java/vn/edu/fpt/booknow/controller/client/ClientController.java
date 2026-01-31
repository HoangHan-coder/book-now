package vn.edu.fpt.booknow.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/client")
public class ClientController {
    @GetMapping("/homepage")
    public String HomePage() {
        System.out.println("test");
        return "public/HomePage";
    }
}
