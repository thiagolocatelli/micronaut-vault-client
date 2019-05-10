package com.github.thiagolocatelli.controller;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.HashMap;
import java.util.Map;

@Controller("/issues")
public class DemoController {

    @Value("${vault-backend-key-one:LOCAL}")
    protected String vaultBackendKey;

    @Value("${vault-backend-kv-version:LOCAL}")
    protected String vaultBackendKvVersion;

    @Get("/test")
    Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("vault-backend-key-one", vaultBackendKey);
        response.put("vault-backend-kv-version", vaultBackendKvVersion);
        return response;
    }

}
