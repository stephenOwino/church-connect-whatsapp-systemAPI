package com.stephenotieno.church_whatsapp_system.churchconnect.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.Base64;


@Service
public class MpesaService {

    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.passkey}")
    private String passkey;

    private final String MPESA_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";

    public void initiateSTKPush(String phoneNumber, Double amount, String accountReference) {
        RestTemplate restTemplate = new RestTemplate();

        // Get OAuth token
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        Map<String, Object> request = new HashMap<>();
        request.put("BusinessShortCode", shortcode);
        request.put("Password", generatePassword());
        request.put("Timestamp", getTimestamp());
        request.put("TransactionType", "CustomerPayBillOnline");
        request.put("Amount", amount);
        request.put("PartyA", phoneNumber);
        request.put("PartyB", shortcode);
        request.put("PhoneNumber", phoneNumber);
        request.put("CallBackURL", "https://yourdomain.com/api/offerings/mpesa-callback");
        request.put("AccountReference", accountReference);
        request.put("TransactionDesc", "Church Offering");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(MPESA_URL, entity, String.class);
        } catch (Exception e) {
            // Log error
        }
    }

    private String getAccessToken() {
        // Implementation to get M-Pesa OAuth token
        return "token";
    }

    private String generatePassword() {
        // Base64 encode: shortcode + passkey + timestamp
        return Base64.getEncoder().encodeToString(
                (shortcode + passkey + getTimestamp()).getBytes()
        );
    }

    private String getTimestamp() {
        return new java.text.SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
    }
}