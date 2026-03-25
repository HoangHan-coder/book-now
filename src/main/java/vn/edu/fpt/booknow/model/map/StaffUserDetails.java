package vn.edu.fpt.booknow.model.map;


import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.fpt.booknow.model.entities.StaffAccount;

import java.util.Collection;
import java.util.List;


@Setter
@Getter
public class StaffUserDetails implements UserDetails {
    private final StaffAccount staffAccount;

    private String fullName;

    private String role;



    public StaffUserDetails(StaffAccount staffAccount) {
        this.staffAccount = staffAccount;
        this.fullName = staffAccount.getFullName();
    }

    @Override
    public String getPassword() { return staffAccount.getPasswordHash(); }
    @Override
    public String getUsername() { return staffAccount.getEmail(); }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + staffAccount.getRole()));
    }

    public String getRole() {
        return staffAccount.getRole();
    }

    public String getFullName() {
        return staffAccount.getFullName();
    }

    public void setRole(String role) {
        this.staffAccount.setRole(role);
    }

    @Override public boolean isEnabled() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
