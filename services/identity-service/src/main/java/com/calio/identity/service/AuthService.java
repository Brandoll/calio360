package com.calio.identity.service;

import com.calio.identity.dto.request.*;
import com.calio.identity.dto.response.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(Long userId);
}
