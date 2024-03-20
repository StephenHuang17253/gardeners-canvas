package nz.ac.canterbury.seng302.gardenersgrove.authorisation;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 2 * Custom Authentication Provider class, to allow for handling
 * authentication in any way we see fit.
 * 3 * In this case using our existing {@link User}
 * 4
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    /**
     *
     * Autowired user service for custom authentication using our own user objects
     */
    @Autowired
    private UserService userService;

    public CustomAuthenticationProvider() {
        super();
    }

    /**
     * Custom authentication implementation
     * 
     * @param authentication An implementation object that must have non-empty email
     *                       (name) and password (credentials)
     * @return A new {@link UsernamePasswordAuthenticationToken} if email and
     *         password are valid with users authorities
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = String.valueOf(authentication.getName());
        String password = String.valueOf(authentication.getCredentials());

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new BadCredentialsException("Bad Credentials");
        }
        User u = userService.getUserByEmail(email);
        if (u == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(u.getEmailAddress(), null, null);

    }

    /**
     * Supports the UsernamePasswordAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
