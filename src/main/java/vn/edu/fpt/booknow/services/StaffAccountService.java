package vn.edu.fpt.booknow.services;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.model.map.CustomerDetails;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

@Service
public class StaffAccountService {

    @Autowired
    private AuthenticationManager authManagerProvider;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private JWTService jwtService;

    public boolean verify(StaffAccount users, HttpServletResponse response) {
        System.out.println("Verify is running..");
        try {
            StaffAccount accountTemp = staffAccountRepository.findStaffAccountByEmail(users.getEmail()).orElse(null);

            if (accountTemp == null) {
                return false;
            }

            if (!accountTemp.getStatus().equals("ACTIVE")) {
                return false;
            }

            Authentication authentication = authManagerProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(users.getEmail(), users.getPasswordHash()));
            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                if (userDetails instanceof CustomerDetails) {
                    System.out.println("This is customer");
                    return false;
                }
                jwtService.createCookie(users, response);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Verify failed: " + e.getMessage());
        }
        return false;
    }

    @Transactional
    public StaffAccount getAccount(String id) {
        return staffAccountRepository.findStaffAccountByEmail(id).orElse(null);
    }

}
