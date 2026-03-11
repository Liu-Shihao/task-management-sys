package com.finblock.tms.modules.auth;

import com.finblock.tms.common.util.Sha256;
import com.finblock.tms.modules.user.UserEntity;
import com.finblock.tms.modules.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiTokenService {

    private final UserRepository userRepository;

    public ApiTokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<AuthenticatedUser> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = Sha256.hex(rawToken.trim());
        return userRepository.findByPasswordHash(tokenHash)
                .map(this::toAuthenticatedUser);
    }

    private AuthenticatedUser toAuthenticatedUser(UserEntity user) {
        return new AuthenticatedUser(user.getId(), user.getName(), user.getEmail());
    }
}

