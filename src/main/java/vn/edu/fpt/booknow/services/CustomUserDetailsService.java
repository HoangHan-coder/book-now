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
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffAccountRepository staffAccountRepository;


    @Override
    public UserDetails loadUserByUsername(String username)  {

        // check staff account
        StaffAccount staffAccount = staffAccountRepository.findStaffAccountByEmail(username).orElse(null);
        if (staffAccount != null) {
            System.out.println("Load staff or admin with " + username);
            return new StaffUserDetails(staffAccount);
        }
        
        // check customer
        Customer customer = customerRepository.findCustomerByEmail(username).orElse(null);
        if (customer != null) {
            System.out.println("load customer with " + username);
            return new CustomerDetails(customer);
        }
        System.out.println("Not found user with " + username);
        throw new UsernameNotFoundException("Not found user with " + username);
    }
}
