package vn.edu.fpt.booknow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

import java.util.List;

@Component
public class CustomerAuthencationProvider implements AuthenticationProvider {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        Customer customer = customerRepo.findCustomerByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        if (!encoder.matches(password, customer.getPasswordHash())) {
            throw new BadCredentialsException("Username or password incorrect");
        }

        List<GrantedAuthority> roles =
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        return new UsernamePasswordAuthenticationToken(
                customer,
                null,
                roles
        );

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
