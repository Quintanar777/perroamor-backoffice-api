package com.perroamor.inventory.auth.infrastructure.security;

import com.perroamor.inventory.auth.domain.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final JwsHeader HS256_HEADER = JwsHeader.with(MacAlgorithm.HS256).build();

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder encoder, JwtDecoder decoder, JwtProperties props) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.props = props;
    }

    public TokenPair issueTokens(User user) {
        Instant now = Instant.now();
        String access = encodeAccess(user, now);
        String refresh = encodeRefresh(user, now);
        long expiresIn = props.accessTtlMinutes() * 60L;
        return new TokenPair(access, refresh, expiresIn);
    }

    public Jwt parseAndValidateRefresh(String token) {
        Jwt jwt = decoder.decode(token);
        Object type = jwt.getClaim(CLAIM_TYPE);
        if (!TYPE_REFRESH.equals(type)) {
            throw new IllegalArgumentException("El token recibido no es de refresh.");
        }
        return jwt;
    }

    private String encodeAccess(User user, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.accessTtlMinutes(), ChronoUnit.MINUTES))
                .subject(user.username())
                .claim(CLAIM_USER_ID, user.id())
                .claim(CLAIM_ROLE, user.roleName().name())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .build();
        return encoder.encode(JwtEncoderParameters.from(HS256_HEADER, claims)).getTokenValue();
    }

    private String encodeRefresh(User user, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.refreshTtlDays(), ChronoUnit.DAYS))
                .subject(user.username())
                .claim(CLAIM_USER_ID, user.id())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .build();
        return encoder.encode(JwtEncoderParameters.from(HS256_HEADER, claims)).getTokenValue();
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresIn) {
    }
}
