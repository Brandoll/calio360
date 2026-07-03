package com.calio.identity.controller;

import com.calio.identity.dto.request.*;
import com.calio.identity.dto.response.*;
import com.calio.identity.service.IdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final IdentityService identityService;

    private Long uid(UserDetails ud) {
        return Long.parseLong(ud.getUsername());
    }

    /** RF-09: Ver perfil */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok("Perfil obtenido", identityService.getProfile(uid(ud))));
    }

    /** RF-09: Actualizar perfil */
    @PatchMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Perfil actualizado", identityService.updateProfile(uid(ud), req)));
    }

    /** RF-10: Eliminar cuenta */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal UserDetails ud) {
        identityService.deleteAccount(uid(ud));
        return ResponseEntity.ok(ApiResponse.ok("Cuenta eliminada", null));
    }

    /** RF-04: Registrar datos biométricos */
    @PostMapping("/biometrics")
    public ResponseEntity<ApiResponse<BiometricResponse>> addBiometric(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody BiometricRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Registro biométrico guardado (macros recalculados automáticamente)",
            identityService.addBiometricRecord(uid(ud), req)));
    }

    /** Historial biométrico */
    @GetMapping("/biometrics")
    public ResponseEntity<ApiResponse<List<BiometricResponse>>> getBiometrics(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok("Historial biométrico",
            identityService.getBiometricHistory(uid(ud))));
    }

    /** RF-05/07: Definir objetivo (calcula calorías automáticamente) */
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<GoalResponse>> setGoal(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody GoalRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Objetivo definido (calorías calculadas con Harris-Benedict)",
            identityService.setGoal(uid(ud), req)));
    }

    /** Objetivo activo */
    @GetMapping("/goals/active")
    public ResponseEntity<ApiResponse<GoalResponse>> getActiveGoal(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok("Objetivo activo",
            identityService.getActiveGoal(uid(ud))));
    }

    /** RF-06: Ver configuración (idioma, unidades, notificaciones) */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración obtenida",
            identityService.getSettings(uid(ud))));
    }

    /** RF-06: Actualizar configuración */
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración actualizada",
            identityService.updateSettings(uid(ud), req)));
    }
}
