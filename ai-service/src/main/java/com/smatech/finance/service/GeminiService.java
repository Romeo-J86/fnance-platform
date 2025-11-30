package com.smatech.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:02
 * projectName Finance Platform
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${app.ai.gemini.api-key:AIzaSyAjPti91EV6kVqd2wBDkIdf83SSfHoPlT0}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // Use the latest available models - Gemini 2.5 Flash is the best option
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    // Alternative models in case the primary fails
    private static final List<String> FALLBACK_MODELS = Arrays.asList(
            "gemini-2.5-flash",      // Primary - latest and most capable
            "gemini-2.0-flash-001",  // Stable version
            "gemini-2.0-flash",      // Latest 2.0 flash
            "gemini-1.5-flash-latest" // Fallback to 1.5 if needed
    );

    private String currentModel = "gemini-2.5-flash";

    public String categorizeTransaction(String description, String merchant) {
        String prompt = String.format(
                "Categorize this financial transaction into one of these specific categories: " +
                        "FOOD_DINING, TRANSPORTATION, ENTERTAINMENT, SHOPPING_RETAIL, UTILITIES_BILLS, " +
                        "HEALTHCARE, TRAVEL, EDUCATION, GROCERIES, PERSONAL_CARE, INVESTMENTS, INCOME, OTHER. " +
                        "Transaction Description: '%s'. Merchant: '%s'. " +
                        "Return ONLY the category name in uppercase with no additional text or explanation.",
                description, merchant
        );

        try {
            String response = callGeminiAPI(prompt);
            log.info("Gemini categorization - Description: {}, Merchant: {}, Category: {}",
                    description, merchant, response);
            return response.trim().toUpperCase();
        } catch (Exception e) {
            log.error("Error calling Gemini API for transaction categorization", e);
            return "OTHER";
        }
    }

    public String generateFinancialInsights(Double totalSpent, Double monthlyBudget, String spendingByCategory) {
        String prompt = String.format(
                "Analyze this personal financial data and provide concise, actionable insights: " +
                        "Total spent this month: $%.2f, Monthly budget: $%.2f, Spending by category: %s. " +
                        "Provide a brief analysis of spending patterns, identify any concerning trends, " +
                        "and give 2-3 specific, practical recommendations for better financial management. " +
                        "Keep the response under 200 words and focus on actionable advice.",
                totalSpent, monthlyBudget, spendingByCategory
        );

        try {
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Error calling Gemini API for financial insights", e);
            return getMockFinancialInsights(totalSpent, monthlyBudget, spendingByCategory);
        }
    }

    public String generateBudgetRecommendations(String spendingPattern, String currentBudgets, Double monthlyIncome) {
        String prompt = String.format(
                "Based on this financial situation, provide specific budget adjustment recommendations: " +
                        "Monthly Income: $%.2f, Current Spending Pattern: %s, Current Budgets: %s. " +
                        "Provide 2-3 specific budget adjustment recommendations considering the 50/30/20 rule " +
                        "(50%% needs, 30%% wants, 20%% savings). Make the recommendations practical and personalized. " +
                        "Keep the response under 150 words.",
                monthlyIncome, spendingPattern, currentBudgets
        );

        try {
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Error calling Gemini API for budget recommendations", e);
            return getMockBudgetRecommendations(monthlyIncome, spendingPattern);
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            return makeGeminiAPICall(currentModel, prompt);
        } catch (Exception e) {
            log.warn("Primary model {} failed, trying fallback models", currentModel);
            // Try fallback models
            for (String model : FALLBACK_MODELS) {
                if (model.equals(currentModel)) continue; // Skip the one we already tried

                try {
                    String result = makeGeminiAPICall(model, prompt);
                    currentModel = model; // Switch to this working model
                    log.info("Switched to working model: {}", model);
                    return result;
                } catch (Exception ex) {
                    log.debug("Fallback model {} also failed: {}", model, ex.getMessage());
                    continue;
                }
            }
            throw new RuntimeException("All Gemini models failed: " + e.getMessage());
        }
    }

    private String makeGeminiAPICall(String model, String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + apiKey;

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();

        // Contents array
        List<Map<String, Object>> contentsList = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();

        // Parts array
        List<Map<String, Object>> partsList = new ArrayList<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        partsList.add(textPart);

        content.put("parts", partsList);
        contentsList.add(content);

        requestBody.put("contents", contentsList);

        // Add generation config optimized for financial tasks
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1); // Low temperature for consistent categorization
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.debug("Calling Gemini API with model: {}", model);

        // Make API call
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return extractTextFromResponse(response.getBody());
        } else {
            throw new RuntimeException("Gemini API returned status: " + response.getStatusCode());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    String text = (String) parts.get(0).get("text");
                    log.debug("Extracted text from response: {}", text);
                    return text;
                }
            }
            throw new RuntimeException("No text found in Gemini response");
        } catch (Exception e) {
            log.error("Error parsing Gemini API response: {}", response, e);
            throw new RuntimeException("Failed to parse Gemini API response");
        }
    }

    // Mock responses for fallback
    private String getMockFinancialInsights(Double totalSpent, Double monthlyBudget, String spendingByCategory) {
        double percentage = (totalSpent / monthlyBudget) * 100;
        return String.format(
                "Financial Insights for Your Spending:\n\n" +
                        "• You've spent $%.2f which is %.1f%% of your $%.2f budget\n" +
                        "• Spending breakdown: %s\n" +
                        "• Recommendations:\n" +
                        "  1. Review your highest spending categories\n" +
                        "  2. Consider setting category-specific limits\n" +
                        "  3. Build an emergency fund if you haven't already",
                totalSpent, percentage, monthlyBudget, spendingByCategory
        );
    }

    private String getMockBudgetRecommendations(Double monthlyIncome, String spendingPattern) {
        return String.format(
                "Budget Recommendations:\n\n" +
                        "Based on your $%.2f monthly income:\n" +
                        "• Essentials (50%%): $%.2f for housing, utilities, groceries\n" +
                        "• Discretionary (30%%): $%.2f for dining, entertainment, shopping\n" +
                        "• Savings (20%%): $%.2f for emergency fund and investments\n\n" +
                        "Your current pattern: %s - Consider aligning with these ratios.",
                monthlyIncome, monthlyIncome * 0.5, monthlyIncome * 0.3, monthlyIncome * 0.2, spendingPattern
        );
    }

    // Test method to verify API connectivity
    public boolean testConnection() {
        try {
            String testPrompt = "Respond with just: OK";
            String response = callGeminiAPI(testPrompt);
            return "OK".equalsIgnoreCase(response.trim());
        } catch (Exception e) {
            log.error("Gemini API connection test failed: {}", e.getMessage());
            return false;
        }
    }
}