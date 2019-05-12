package com.github.thiagolocatelli.controller;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DemoController {

    @Value("${vault-backend-key-one:LOCAL}")
    protected String vaultBackendKey;

    @Value("${vault-backend-kv-version:LOCAL}")
    protected String vaultBackendKvVersion;

    @Value("${vault-backend-name:LOCAL}")
    protected String vaultBackendName;

    @Get("/test")
    Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("vault-backend-key-one", vaultBackendKey);
        response.put("vault-backend-kv-version", vaultBackendKvVersion);
        response.put("vault-backend-name", vaultBackendName);
        return response;
    }

}
