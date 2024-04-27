package nz.ac.canterbury.seng302.gardenersgrove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Class to handle security configuration
 */
@Configuration
public class PasswordSecurityConfiguration {

    /**
     * Password encoder bean for storage of passwords in database
     * Created based on <a href=
     * "https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html">documentation</a>
     *
     * @return Delegating Password Encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder delegatingPasswordEncoder;
        delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return delegatingPasswordEncoder;
    }
}