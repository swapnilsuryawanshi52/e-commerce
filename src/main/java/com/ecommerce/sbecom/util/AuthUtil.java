package com.ecommerce.sbecom.util;

import com.ecommerce.sbecom.entity.User;
import com.ecommerce.sbecom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    @Autowired
    private UserRepository userRepository;

    public User authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public String loggedInEmail() {
        return authenticatedUser().getEmail();
    }

    public Long loggedInUserId() {
        return authenticatedUser().getUserId();
    }

    public User loggedInUser() {
        return authenticatedUser();
    }
}
