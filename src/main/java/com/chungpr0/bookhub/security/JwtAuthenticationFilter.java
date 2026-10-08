package com.chungpr0.bookhub.security;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_ERROR_ATTR = "jwt_error";
    public static final String JWT_ERROR_DETAILS_ATTR = "jwt_error_details";

    private static final Set<String> UNVERIFIED_ALLOWED_PATHS = Set.of(
            "/api/v1/auth/password",
            "/api/v1/auth/me",
            "/api/v1/auth/logout"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                Long accountId = jwtTokenProvider.getAccountId(claims);
                Integer tokenVersion = jwtTokenProvider.getTokenVersion(claims);

                Optional<Account> accountOpt = accountRepository.findById(accountId);
                if (accountOpt.isEmpty()) {
                    request.setAttribute(JWT_ERROR_ATTR, ErrorCode.UNAUTHENTICATED);
                } else {
                    Account account = accountOpt.get();

                    if (account.getStatus() == AccountStatus.LOCKED) {
                        request.setAttribute(JWT_ERROR_ATTR, ErrorCode.ACCOUNT_LOCKED);
                        if (account.getLockedReason() != null) {
                            request.setAttribute(JWT_ERROR_DETAILS_ATTR, Map.of("reason", account.getLockedReason()));
                        }
                    } else if (tokenVersion == null || account.getTokenVersion() != tokenVersion) {
                        request.setAttribute(JWT_ERROR_ATTR, ErrorCode.TOKEN_REVOKED);
                    } else if (account.getStatus() == AccountStatus.UNVERIFIED && !isUnverifiedAllowedPath(request.getRequestURI())) {
                        request.setAttribute(JWT_ERROR_ATTR, ErrorCode.PASSWORD_CHANGE_REQUIRED);
                    } else {
                        UserPrincipal principal = UserPrincipal.create(
                                account.getId(),
                                account.getUsername(),
                                account.getRole(),
                                account.getStatus(),
                                account.getTokenVersion()
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (ExpiredJwtException e) {
                log.debug("Expired JWT token: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_ATTR, ErrorCode.TOKEN_EXPIRED);
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Invalid JWT token: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_ATTR, ErrorCode.TOKEN_INVALID);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }

    private boolean isUnverifiedAllowedPath(String uri) {
        if (uri == null) {
            return false;
        }
        return UNVERIFIED_ALLOWED_PATHS.contains(uri);
    }
}

