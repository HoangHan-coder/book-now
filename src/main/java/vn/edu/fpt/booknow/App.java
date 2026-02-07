package vn.edu.fpt.booknow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.services.RoomServices;

import java.util.List;

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
//       List<Room> rooms = roomServices.getALl();
//
//       roomServices.testPrintRooms();
//    }
//
//}