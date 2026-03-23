package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.RoomType;
import vn.edu.fpt.booknow.repositories.RoomTypeRepository;

import java.util.List;

@Service
public class RoomTypeService {
  private RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

}
