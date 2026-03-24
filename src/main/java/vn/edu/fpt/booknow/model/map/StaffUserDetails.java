package vn.edu.fpt.booknow.model.map;


import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.fpt.booknow.model.entities.StaffAccount;

import java.util.Collection;
import java.util.List;

public class StaffUserDetails implements UserDetails {
    private final StaffAccount staffAccount;
    @Setter
    @Getter
    private String fullName;
    @Setter
    @Getter
    private String role;
    public StaffUserDetails(StaffAccount staffAccount) {
        this.staffAccount = staffAccount;
        this.fullName = staffAccount.getFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String getPassword() { return staffAccount.getPasswordHash(); }
    @Override
    public String getUsername() { return staffAccount.getEmail(); }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + staffAccount.getRole()));
    }

    @Override public boolean isEnabled() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
