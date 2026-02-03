package vn.edu.fpt.booknow.services.staffadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.model.map.StaffUserDetails;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

@Service
public class StaffAccountService implements UserDetailsService {

    @Autowired
    private StaffAccountRepository staffAccountRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffAccount sa = staffAccountRepo.findStaffAccountByEmail(username)
                .orElseThrow( () -> new UsernameNotFoundException("User not found"));
        return new StaffUserDetails(sa);
    }


}
