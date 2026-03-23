package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.CheckInSession;
import vn.edu.fpt.booknow.model.entities.CheckInSessionStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.repositories.CheckInSessionRepository;

import java.time.LocalDateTime;

@Service
public class CheckInSessionService {
    @Autowired
    private CheckInSessionRepository checkInSessionRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public CheckInSession getAll(){
        return checkInSessionRepository.findAll().iterator().next();
    }

    public CheckInSession getById(Long id){
        return checkInSessionRepository.findById(id).get();
    }

    public CheckInSession createCheckInSession(Long bookingId, String videoUrl, CheckInSessionStatus status, String videoPublicId){
        CheckInSession checkInSession = new CheckInSession();
        Booking booking = bookingRepository.findById(bookingId).get();
        LocalDateTime now = LocalDateTime.now();
        checkInSession.setBooking(booking);
        checkInSession.setStatus(status);
        checkInSession.setVideoUrl(videoUrl);
        checkInSession.setVideoPublicId(videoPublicId);
        checkInSession.setCreatedAt(now);
        return checkInSessionRepository.save(checkInSession);
    }

    public CheckInSession updateCheckInSession(Long checkInSessionId, CheckInSessionStatus status, String videoUrl){
        CheckInSession checkInSession = checkInSessionRepository.findById(checkInSessionId).get();
        checkInSession.setStatus(status);
        checkInSession.setVideoUrl(videoUrl);
        return checkInSessionRepository.save(checkInSession);
    }

    public CheckInSession getCheckInSessionId(Long bookingId) throws Exception{
        CheckInSession checkInSession = checkInSessionRepository.findCheckInSessionByBooking_BookingId(bookingId).orElse(null);
        if(checkInSession == null){
            throw new RuntimeException("CheckInSession not found");
        } else {
            return checkInSession;
        }

    }

    public void updateCheckInSessionWhenApprove(Long checkInSessionId, CheckInSessionStatus status){
    CheckInSession checkInSession = checkInSessionRepository.findById(checkInSessionId).get();
    checkInSession.setStatus(status);
    checkInSession.setReviewedAt(LocalDateTime.now());
    checkInSessionRepository.save(checkInSession);
    }

    public void updateCheckInSessionWhenReject(Long checkInSessionId, CheckInSessionStatus status){
        CheckInSession checkInSession = checkInSessionRepository.findById(checkInSessionId).get();
        checkInSession.setStatus(status);
        checkInSession.setReviewedAt(LocalDateTime.now());
        checkInSessionRepository.save(checkInSession);
    }
}
