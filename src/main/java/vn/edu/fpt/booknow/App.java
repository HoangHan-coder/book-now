package vn.edu.fpt.booknow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);}

}
//public class App {
//    public static void main(String[] args) {
//        ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);
//        RoomServices roomServices = ctx.getBean(RoomServices.class);
//
//       List<Room> list = roomServices.getAll();
//       for(Room r: list) {
//           System.out.println(r.getRoomNumber() + r.getRoomType() + r.getRoomType().getName());
//       }
//    }
//
//}