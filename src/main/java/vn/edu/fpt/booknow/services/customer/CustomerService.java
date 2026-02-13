package vn.edu.fpt.booknow.services.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.map.CustomerDetails;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

@Service
public class CustomerService implements UserDetailsService {
    @Autowired
    private CustomerRepository customerRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepo.findCustomerByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomerDetails(customer);
    }

    public Customer findOrCreateGoogleUser(String email) {

    }

}
