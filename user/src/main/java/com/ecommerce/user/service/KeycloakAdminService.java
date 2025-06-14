package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KeycloakAdminService {
    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.client-uid}")
    private String clientUid;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getKeycloakAdminToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("username", adminUsername);
        params.add("password", adminPassword);
        params.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                request,
                Map.class
        );
        return Objects.requireNonNull(response.getBody()).get("access_token").toString();
    }

    public String createUser(String token, UserRequest userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", userRequest.getUsername());
        userPayload.put("enabled", true);
        userPayload.put("firstName", userRequest.getFirstName());
        userPayload.put("lastName", userRequest.getLastName());
        userPayload.put("email", userRequest.getEmail());

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userRequest.getPassword());
        credentials.put("temporary", false);
        userPayload.put("credentials", List.of(credentials));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userPayload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                keycloakServerUrl + "/admin/realms/" + realm + "/users",
                request,
                String.class
        );

        if(!HttpStatus.CREATED.equals(response.getStatusCode())) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getBody());
        }

//      Extract Keycloak user ID from the Location header
        URI location = response.getHeaders().getLocation();
        if(location == null) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getBody());
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private Map<String,Object> getClientRoleRepresentation(String token, String roleName){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUid + "/roles/" + roleName,
                HttpMethod.GET,
                entity,
                Map.class
        );
        return response.getBody();
    }

    public void assignClientRoleToUser(String username, String roleName, String keycloakUserId) {
        String token = getKeycloakAdminToken();
        Map<String, Object> roleRepresentation = getClientRoleRepresentation(token, roleName);
        if (roleRepresentation == null) {
            throw new RuntimeException("Role not found: " + roleName);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(List.of(roleRepresentation), headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mappings/clients/" + clientUid,
                request,
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to assign role " + roleName
                    +  " to user " + username +
                    " :HTTP " + response.getStatusCode());
        }

    }
}
