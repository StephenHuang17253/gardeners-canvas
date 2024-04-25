package nz.ac.canterbury.seng302.gardenersgrove.repository;

import org.springframework.stereotype.Repository;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

/**
 * Token repository accessor using Spring's {@link Repository}.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 */
@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {

    /**
     * Returns a token found by id
     * 
     * @param id the token id
     * @return Token or null if not found
     */
    Token findById(long id);

    /**
     * Returns a token found by token string
     * 
     * @param tokenString the token string
     * @return Token or null if not found
     */
    Token findByTokenString(String tokenString);

    /**
     * Returns a token found by user
     * 
     * @param user the user
     * @return Token or null if not found
     */
    Token findByUser(User user);

    /**
     * Returns all tokens
     * 
     * @return List of tokens
     */
    List<Token> findAll();

    /**
     * Deletes a token by id
     * 
     * @param id the token id
     */
    void deleteById(long id);

}
