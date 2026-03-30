package vn.edu.fpt.booknow.services;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.map.StaffUserDetails;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

@Service
public class CustomerService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JWTService jwtService;

    public boolean verify(Customer users, HttpServletResponse response) {
        System.out.println("Verify customer is running..");

        Customer customer = customerRepository.getCustomerByEmail(users.getEmail());

        if (customer == null) {
            return false;
        }

        if (!customer.getStatus().equals("ACTIVE")) {
            return false;
        }

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(users.getEmail(), users.getPasswordHash()));

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("customer is authenticated");
            if (userDetails instanceof StaffUserDetails) {
                System.out.println("This is Staff or admin");
                return false;
            }
            jwtService.createCookie(users,response);
            return true;
        }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    public Customer findCusByEmail(String email) {
        return customerRepository.findCustomerByEmail(email).orElse(null);
    }

    public void save(Customer customer) {
        customerRepository.save(customer);
    }
}
