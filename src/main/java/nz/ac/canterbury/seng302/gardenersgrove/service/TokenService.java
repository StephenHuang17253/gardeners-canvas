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

    public Token getTokenById(Long id) {
        return tokenRepository.findById(id).orElse(null);
    }

    public Token getTokenByUser(User user) {
        return tokenRepository.findByUser(user);
    }

    public void addToken(Token token) {
        User thisUser = token.getUser();
        if (this.getTokenByUser(thisUser) != null) {
            this.deleteToken(this.getTokenByUser(thisUser));
        }
        tokenRepository.save(token);
    }

    public Token getTokenByTokenString(String tokenString) {
        String capitalisedTokenString = tokenString.toUpperCase();
        logger.info(capitalisedTokenString);
        return tokenRepository.findByTokenString(capitalisedTokenString);
    }
    
    public List<Token> getAllTokens() {
        return tokenRepository.findAll();
    }

    public void deleteToken(Token token) {
        tokenRepository.deleteById(token.getId());
    }

}
