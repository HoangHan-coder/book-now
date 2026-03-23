package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Amenity;
import vn.edu.fpt.booknow.repositories.AmenityRepository;

import java.util.List;

@Service
public class AmenityService {
    private AmenityRepository amenityRepository;

    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Transactional
    public List<Amenity> findAll() {
        return amenityRepository.findAll();
    }
}
