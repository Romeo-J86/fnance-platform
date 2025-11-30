package com.smatech.finance.util;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 08:46
 * projectName Finance Platform
 **/

@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + NUMBERS + SPECIAL_CHARS;
    private static final SecureRandom random = new SecureRandom();

    public String generateStrongPassword() {
        return generateStrongPassword(12); // Default to 12 characters
    }

    public String generateStrongPassword(int length) {
        if (length < 10) {
            throw new IllegalArgumentException("Password length must be at least 10 characters");
        }

        List<Character> passwordChars = new ArrayList<>();

        // Ensure at least one of each required character type
        passwordChars.add(getRandomChar(UPPERCASE));
        passwordChars.add(getRandomChar(LOWERCASE));
        passwordChars.add(getRandomChar(NUMBERS));
        passwordChars.add(getRandomChar(SPECIAL_CHARS));

        // Fill the rest with random characters from all sets
        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(getRandomChar(ALL_CHARS));
        }

        // Shuffle the characters to randomize positions
        for (int i = passwordChars.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Character temp = passwordChars.get(i);
            passwordChars.set(i, passwordChars.get(j));
            passwordChars.set(j, temp);
        }

        // Convert to String
        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    private char getRandomChar(String characterSet) {
        return characterSet.charAt(random.nextInt(characterSet.length()));
    }

    // Generate memorable but strong password
    public String generateMemorablePassword() {
        String[] words = {"Secure", "Finance", "Tech", "Bank", "Money", "Safe", "Wealth", "Growth", "Smart", "Future"};
        String[] separators = {"!", "@", "#", "$", "&", "*"};

        String word1 = words[random.nextInt(words.length)];
        String word2 = words[random.nextInt(words.length)];
        String separator = separators[random.nextInt(separators.length)];
        int number = random.nextInt(90) + 10; // 10-99

        return word1 + separator + word2 + number;
    }

    // Validate password strength
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 10) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (SPECIAL_CHARS.indexOf(c) >= 0) hasSpecial = true;
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
}
