package com.businessos.auth.authentication;

import com.businessos.auth.user.UserResponse;
import com.businessos.auth.password.ForgotPasswordRequest;
import com.businessos.auth.password.ResetPasswordRequest;
import com.businessos.modules.company.RegisterRequest;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    JwtResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerification(ResendVerificationRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
