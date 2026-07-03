package com.calio.identity.service;

import com.calio.identity.dto.request.*;
import com.calio.identity.dto.response.*;
import java.util.List;

public interface IdentityService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void deleteAccount(Long userId);
    BiometricResponse addBiometricRecord(Long userId, BiometricRequest request);
    List<BiometricResponse> getBiometricHistory(Long userId);
    GoalResponse setGoal(Long userId, GoalRequest request);
    GoalResponse getActiveGoal(Long userId);
    SettingsResponse getSettings(Long userId);
    SettingsResponse updateSettings(Long userId, UpdateProfileRequest request);
}
