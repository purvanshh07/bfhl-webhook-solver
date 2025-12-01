package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        // ⭐ Step 1: Generate Webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        String reqBody = """
        {
          "name": "Purvansh Vashistha",
          "regNo": "22BKT0014",
          "email": "purvanshh07@gmail.com"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(reqBody, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        System.out.println("Webhook Response: " + response.getBody());

        JsonNode json = new ObjectMapper().readTree(response.getBody());

        String webhook = json.get("webhook").asText();
        String token = json.get("accessToken").asText();

        // ⭐ Step 2: Final SQL Query
        String finalSQL = """
        SELECT 
            d.DEPARTMENT_NAME,
            AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) AS AVERAGE_AGE,
            GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) ORDER BY e.EMP_ID LIMIT 10 SEPARATOR ', ') AS EMPLOYEE_LIST
        FROM DEPARTMENT d
        JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT
        JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID
        WHERE p.AMOUNT > 70000
        GROUP BY d.DEPARTMENT_NAME, d.DEPARTMENT_ID
        ORDER BY d.DEPARTMENT_ID DESC;
        """;

        // ⭐ Step 3: Submit SQL Answer
        HttpHeaders submitHeaders = new HttpHeaders();
        submitHeaders.setContentType(MediaType.APPLICATION_JSON);
        submitHeaders.setBearerAuth(token);

        String finalBody = "{ \"finalQuery\": \"" + finalSQL.replace("\n", " ") + "\" }";

        HttpEntity<String> finalRequest = new HttpEntity<>(finalBody, submitHeaders);

        ResponseEntity<String> submitResp =
                restTemplate.postForEntity(webhook, finalRequest, String.class);

        System.out.println("Submission Response: " + submitResp.getBody());
    }
}
