package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TokenRepository;

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
    
}
