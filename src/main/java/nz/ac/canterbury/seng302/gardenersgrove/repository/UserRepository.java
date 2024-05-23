package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User repository accessor using Spring's {@link Repository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    /**
     * Returns a user found by id
     * @param id
     * @return User or null if not found
     */
    User findById(long id);

    /**
     * Returns a user found by email address
     * @param emailAddress
     * @return User or null if not found
     */
    User[] findByEmailAddressIgnoreCase(String emailAddress);

    /**
     * Returns all users
     * @return List of all users
     */
    List<User> findAll();

    /**
     * Returns the number of users with the given email address
     * @param emailAddress
     * @return number of users with the given email address
     */
    int countDistinctByEmailAddressIgnoreCase(String emailAddress);


    /**
     * Deletes a user by id
     * @param id The id of the user to delete
     */
    void deleteById(long id);


    /**
     * Finds matching users. Based on examples found on chatgpt and on research on https://www.baeldung.com/spring-data-jpa-query
     * @param email Email address to match
     * @param firstName First name to match
     * @param lastName Last name to match
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "(u.firstName = :firstName AND u.lastName = :lastName) OR " +
            "u.emailAddress = :email")
    User[] findUsersByEmailAddressOrFirstNameAndLastName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email);
}
