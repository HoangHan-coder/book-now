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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

import java.util.List;

@Component
public class AdminAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private StaffAccountRepository staffAccountRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        StaffAccount staffAccount = staffAccountRepo.findStaffAccountByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        if (!encoder.matches(password, staffAccount.getPasswordHash())) {
            throw new BadCredentialsException("Username or password incorrect");
        }

        List<GrantedAuthority> roles =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_STAFF"));

        return new UsernamePasswordAuthenticationToken(
                staffAccount,
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
