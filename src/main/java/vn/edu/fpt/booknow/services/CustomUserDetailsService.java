package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.model.map.CustomerDetails;
import vn.edu.fpt.booknow.model.map.StaffUserDetails;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;



    @Override
    public UserDetails loadUserByUsername(String username)  {

        
        // check customer
        Customer customer = customerRepository.findCustomerByEmail(username).orElse(null);
        if (customer != null) {
            return new CustomerDetails(customer);
        }
        
        throw new UsernameNotFoundException("Not found user with " + username);
    }
}
