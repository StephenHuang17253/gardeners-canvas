package nz.ac.canterbury.seng302.gardenersgrove.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TokenRepository;

/**
 * Service class for Token, defined by the {@link Service} annotation.
 * This class links automatically with {@link TokenRepository}
 */
@Service
public class TokenService {

    Logger logger = LoggerFactory.getLogger(TokenService.class);
    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * get token by id
     * @param id of token
     * @return token
     */
    public Token getTokenById(Long id) {
        return tokenRepository.findById(id).orElse(null);
    }

    /**
     * Get user's token. Should be unique due to regulations on database
     * @param user user whose token has to be found
     * @return unique token belonging to user
     */
    public Token getTokenByUser(User user) {
        return tokenRepository.findByUser(user);
    }

    /**
     * Add token to repository and delete any tokens the new token's user already has
     * @param token verification token
     */
    public void addToken(Token token) {
        User thisUser = token.getUser();
        if (this.getTokenByUser(thisUser) != null) {
            this.deleteToken(this.getTokenByUser(thisUser));
        }
        tokenRepository.save(token);
    }

    /**
     * Get token from persistence by token string
     * @param tokenString unique string identifying token
     * @return token Unique token with user details
     */
    public Token getTokenByTokenString(String tokenString) {
        String capitalisedTokenString = tokenString.toUpperCase();
        return tokenRepository.findByTokenString(capitalisedTokenString);
    }

    /**
     * Get all tokens in persistence
     * @return list of all tokens
     */
    public List<Token> getAllTokens() {
        return tokenRepository.findAll();
    }

    /**
     * Delete token from persistence
     * @param token unique token
     */
    public void deleteToken(Token token) {
        tokenRepository.deleteById(token.getId());
    }

}
