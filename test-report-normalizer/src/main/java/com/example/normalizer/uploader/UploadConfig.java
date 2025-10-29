package com.example.normalizer.uploader;

import java.util.HashMap;
import java.util.Map;

public class UploadConfig {
    private String endpoint;
    private AuthType authType = AuthType.NONE;
    private String username;
    private String password;
    private String token;
    private Map<String, String> customHeaders = new HashMap<>();

    public enum AuthType { NONE, BASIC, BEARER, API_KEY, CUSTOM_HEADER }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Map<String, String> getCustomHeaders() { return customHeaders; }
    public void addHeader(String key, String value) { this.customHeaders.put(key, value); }
}
