package com.furkancavdar.qrmenu.auth.application.port.in;

import com.furkancavdar.qrmenu.auth.application.port.in.dto.JwtTokenPair;
import com.furkancavdar.qrmenu.auth.domain.User;

public interface JwtTokenUseCase {

    /**
     * Add the JTI to blacklist.
     *
     * @param jti            JTI of the access or refresh JWT
     * @param reason         Optional reason
     * @param expirationTime long expiration time
     * @author Furkan Çavdar
     */
    void blacklistJti(String jti, String reason, long expirationTime);

    /**
     * Check if the JTI is blacklisted.
     *
     * @param jti JTI of the access or refresh JWT
     * @return {@code true} if JTI is blacklisted,
     * <p>
     * otherwise {@code false}
     * @author Furkan Çavdar
     */
    boolean isJtiBlacklisted(String jti);

    /**
     * Generate {@link JwtTokenPair} which holds {@code accessToken} and {@code refreshToken}.
     *
     * @param user      {@link User}
     * @param sessionId ID representing a session
     * @return {@link JwtTokenPair}
     * @author Furkan Çavdar
     */
    JwtTokenPair generateTokenPair(User user, String sessionId);
}
