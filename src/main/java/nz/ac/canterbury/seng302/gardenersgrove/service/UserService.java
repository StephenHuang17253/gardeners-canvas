package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.HomePageLayout;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
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
    private final UserRepository userRepository;
    private final HomePageLayoutRepository homePageLayoutRepository;

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
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, HomePageLayoutRepository homePageLayoutRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.homePageLayoutRepository = homePageLayoutRepository;
    }

    /**
     * Returns a user found by id
     * 
     * @param id unique user id
     * @return user if found, null otherwise
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Adds a user to persistence with a default home page layout
     *
     * @param user        object to persist
     * @param rawPassword string to encode and add to user
     */
    public void addUser(User user, String rawPassword) {
        HomePageLayout newLayout = new HomePageLayout();
        homePageLayoutRepository.save(newLayout);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        user.setHomePageLayout(newLayout);
        userRepository.save(user);
    }

    /**
     * Returns a user found by email and password
     * 
     * @param email    user email
     * @param password user raw password
     * @return user if found, null otherwise
     */
    public User getUserByEmailAndPassword(String email, String password) {
        User[] users = userRepository.findByEmailAddressIgnoreCase(email);
        if (users.length == 0) {
            return null;
        }
        User user = users[0];
        if (passwordEncoder.matches(password, user.getEncodedPassword())
                || Objects.equals(password, user.getEncodedPassword())) {
            return user;
        }
        return null;
    }

    /**
     * Returns a user found by email
     * 
     * @param email user email to search by
     * @return user if found, null otherwise
     */
    public User getUserByEmail(String email) {
        User[] users = userRepository.findByEmailAddressIgnoreCase(email);
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
     * @param id unique id of user
     * @return updated user
     */
    public User updateUser(long id, String firstName, String lastName, String emailAddress, LocalDate dateOfBirth) {
        User user = getUserById(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailAddress(emailAddress);
        user.setDateOfBirth(dateOfBirth);
        return userRepository.save(user);
    }

    /**
     * Checks if a user with the given email exists
     * 
     * @param email user email
     * @return true if a user with the given email exists, false otherwise
     */
    public boolean emailInUse(String email) {
        return userRepository.countDistinctByEmailAddressIgnoreCase(email) > 0;
    }

    /**
     * Update users profile picture filename
     * 
     * @param filename filename of profile picture
     * @param id       id of user to update
     */
    public void updateProfilePictureFilename(String filename, long id) {
        User user = getUserById(id);
        user.setProfilePictureFilename(filename);
        userRepository.save(user);
    }

    /**
     * Finds user by the id input then encodes the NewPassword input using
     * passwordEncoder
     * then sets it as the users password
     *
     * @param id          id of user to update
     * @param newPassword New password to set for user's account
     */
    public void updatePassword(long id, String newPassword) {
        User user = getUserById(id);
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }

    /**
     * Returns true if input password matches user's password in database
     *
     * @param id              id of user to match
     * @param passwordToCheck Password to check if same in database
     */
    public boolean checkPassword(long id, String passwordToCheck) {
        User user = getUserById(id);
        String currentPassword = user.getEncodedPassword();
        return passwordEncoder.matches(passwordToCheck, currentPassword);
    }

    /**
     * Verify a user, for when they enter the correct token
     *
     * @param user user to verify
     */
    public void verifyUser(User user) {
        user.setVerified(true);
        userRepository.save(user);
    }

    /**
     * Deletes a user
     *
     * @param user User object
     */
    public void deleteUser(User user) {
        userRepository.deleteById(user.getId());
    }

    /**
     * Add garden to the user's garden list
     *
     * @param garden Garden entity to add to the list
     * @param id     id of the user to add garden to
     */
    public void addGardenToGardenList(Garden garden, Long id) {
        User user = getUserById(id);
        user.getGardens().add(garden);
        userRepository.save(user);
    }

    /**
     * ban a given user for a given amount of days, and reset their strikes to 0
     * 
     * @param user the user to ban
     * @param days length of ban
     */
    public void banUser(User user, int days) {
        user.setLastBanDate(LocalDateTime.now());
        user.setBanDuration(Duration.ofDays(days));
        user.setStrikes(0);
        userRepository.save(user);
    }

    /**
     * give a strike to the user
     * 
     * @param user user to strike
     */
    public void strikeUser(User user) {
        int strikes = user.getStrikes();
        user.setStrikes(strikes + 1);
        userRepository.save(user);
    }

    /**
     * Seperates input into firstname, lastname or identifies it as email
     * 
     * @param input           search box input to match users with
     * @param emailValidation ValidationResult regarding email check on input
     * @return users with emails or first and last names that match the input.
     */
    public User[] getMatchingUsers(String input, ValidationResult emailValidation) {
        String fName = "";
        String lName = "";
        String email = "";
        if (emailValidation.valid()) {
            email = input;
        } else {
            String[] seperated = input.strip().split(" ");
            fName = seperated[0];
            if (seperated.length > 1) {
                lName = seperated[1];
            }
        }
        return userRepository.findUsersByEmailAddressOrFirstNameAndLastName(fName, lName, email);
    }

    /**
     * Turns a list of user interactions into a list of users
     * 
     * @param userInteractions a list of recent interaction
     * @return a list of users associated with each recent interaction
     */
    public List<User> getUsersByInteraction(List<UserInteraction> userInteractions) {
        return userInteractions.stream()
                .map(UserInteraction::getItemId)
                .map(this::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Updates the home page layout of a user
     * 
     * @param id     id of the user to update layout for
     * @param layout new layout to set
     * @return updated user
     */
    public User updateHomePageLayout(Long id, HomePageLayout layout) {
        User user = getUserById(id);
        homePageLayoutRepository.save(layout);
        user.setHomePageLayout(layout);
        return userRepository.save(user);
    }

}
