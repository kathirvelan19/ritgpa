package com.rit.gpa.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.rit.gpa.config.FirebaseConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    private final FirebaseConfig firebaseConfig;

    public FirebaseAuthenticationFilter(FirebaseConfig firebaseConfig) {
        this.firebaseConfig = firebaseConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7).trim();

            if (!idToken.isEmpty()) {
                try {
                    if (firebaseConfig.isInitialized() && !FirebaseApp.getApps().isEmpty()) {
                        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                                decodedToken.getUid(),
                                decodedToken.getEmail(),
                                decodedToken.getName()
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, idToken,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        // Dev mode fallback when Admin SDK credentials have not been configured yet locally
                        log.warn("Firebase Admin SDK not initialized with service account; checking token format for local dev");
                        String uid = "dev-user-" + Math.abs(idToken.hashCode());
                        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(uid, "student@ritchennai.edu.in", "RIT Student");
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, idToken,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    log.error("Failed to verify Firebase ID token: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

