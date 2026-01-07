package com.restaurant.backend.Service;

public interface EmailService {
    
    /**
     * Send password reset email to user
     * @param toEmail recipient email
     * @param resetToken password reset token
     * @param username username of the user
     */
    void sendPasswordResetEmail(String toEmail, String resetToken, String username);
    
    /**
     * Send profile update confirmation email
     * @param toEmail recipient email
     * @param username username of the user
     */
    void sendProfileUpdateConfirmation(String toEmail, String username);
    
    /**
     * Send password change confirmation email
     * @param toEmail recipient email
     * @param username username of the user
     */
    void sendPasswordChangeConfirmation(String toEmail, String username);
}





