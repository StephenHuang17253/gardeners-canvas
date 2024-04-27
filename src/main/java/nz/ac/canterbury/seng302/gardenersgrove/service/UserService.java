package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for User, defined by the {@link Service} annotation.
 * This class links automatically with {@link UserRepository}
 */
@Service
public class UserService {

    /** passwordEncoder to use for encoding passwords before storage */
    private final PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * tells Spring to inject a PasswordEncoder bean when creating an instance of
     * UserService
     * 
     * @param passwordEncoder delegatingPasswordEncoder to encode passwords for
     *                        storage
     * @param userRepository  interface for user table in persistance
     */
    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Returns a user found by id
     * 
     * @param id
     * @return user if found, null otherwise
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Adds a user to persistence
     *
     * @param user        object to persist
     * @param rawPassword string to encode and add to user
     */
    public void addUser(User user, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * Returns a user found by email and password
     * 
     * @param email
     * @param password
     * @return user if found, null otherwise
     */
    public User getUserByEmailAndPassword(String email, String password) {
        User[] users = userRepository.findByEmailAddress(email);
        if (users.length == 0) {
            return null;
        }
        User user = users[0];
        if (passwordEncoder.matches(password, user.getEncodedPassword())) {
            return user;
        }
        return null;
    }

    /**
     * Returns a user found by email
     * 
     * @param email
     * @return user if found, null otherwise
     */
    public User getUserByEmail(String email) {
        User[] users = userRepository.findByEmailAddress(email);
        if (users.length == 0) {
            return null;
        }
        return users[0];
    }

    /**
     * Returns all users in persistence
     * 
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Takes a user instance and copies the fields to the user with the given id
     * 
     * @param newUser
     * @param id
     */
    public void updateUser(long id, String firstName, String lastName, String emailAddress, LocalDate dateOfBirth) {
        User user = getUserById(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailAddress(emailAddress);
        user.setDateOfBirth(dateOfBirth);
        userRepository.save(user);
    }

    /**
     * Checks if a user with the given email exists
     * 
     * @param email
     * @return true if a user with the given email exists, false otherwise
     */
    public boolean emailInUse(String email) {
        return userRepository.countDistinctByEmailAddress(email) > 0;
    }

    /**
     * Update users profile picture filename
     * 
     * @param filename filename of profile picture
     * @param id      id of user to update
     */
    public void updateProfilePictureFilename(String filename, long id) {
        User user = getUserById(id);
        user.setProfilePictureFilename(filename);
        userRepository.save(user);
    }

    /**
     * Finds user by the id input then encodes the NewPassword input using passwordEncoder
     * then sets it as the users password
     *
     * @param id id of user to update
     * @param NewPassword New password to set for user's account
     */
    public void updatePassword(long id, String NewPassword) {
        User user = getUserById(id);
        String encodedNewPassword = passwordEncoder.encode(NewPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }

    /**
     * Returns true if input password matches user's password in database
     *
     * @param id id of user to match
     * @param passwordToCheck Password to check if same in database
     */
    public boolean checkPassword(long id, String passwordToCheck) {
        User user = getUserById(id);
        String currentPassword = user.getEncodedPassword();
        return passwordEncoder.matches(passwordToCheck, currentPassword);
    }
}
