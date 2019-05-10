/*
 * Copyright (c) 2015 Transamerica Corporation. ("Transamerica" or "us"). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Transamerica ("Confidential Information").
 *
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Transamerica.
 */
package io.micronaut.discovery.vault.config.client.v1;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.condition.RequiresVaultClientConfig;
import io.micronaut.discovery.vault.config.client.v1.condition.RequiresVaultClientConfigV1;
import io.micronaut.discovery.vault.config.client.response.VaultResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * A {@link ConfigurationClient} for Vault Configuration.
 *
 *  @author Thiago Locatelli
 *  @since 1.1.1
 */
@Singleton
@RequiresVaultClientConfig
@RequiresVaultClientConfigV1
@Requires(property = ConfigurationClient.ENABLED, value = "true", defaultValue = "false")
public class VaultConfigConfirationClientV1 implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultConfigConfirationClientV1.class);

    private final VaultConfigClientV1 vaultConfigClientV1;
    private final VaultClientConfiguration vaultClientConfiguration;
    private final ApplicationConfiguration applicationConfiguration;
    private final Environment environment;
    private final String vaultUri;

    private ExecutorService executionService;

    /**
     *
     * @param vaultConfigClientV1       Vault Config Http Client
     * @param vaultClientConfiguration  Vault Client Configuration
     * @param applicationConfiguration  The application configuration
     * @param environment               The environment
     * @param vaultUri                  Vault endpoint uri
     */
    public VaultConfigConfirationClientV1(VaultConfigClientV1 vaultConfigClientV1, VaultClientConfiguration vaultClientConfiguration,
                                            ApplicationConfiguration applicationConfiguration, Environment environment,
                                            @Value(VaultClientConfiguration.VAULT_CLIENT_CONFIG_ENDPOINT) String vaultUri) {

        this.vaultConfigClientV1 = vaultConfigClientV1;
        this.vaultClientConfiguration = vaultClientConfiguration;
        this.applicationConfiguration = applicationConfiguration;
        this.environment = environment;
        this.vaultUri = vaultUri;
    }

    @Inject
    void setExecutionService(@Named(TaskExecutors.IO) @Nullable ExecutorService executionService) {
        if (executionService != null) {
            this.executionService = executionService;
        }
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        if(!vaultClientConfiguration.getDiscoveryConfiguration().isEnabled()) {
            return Flowable.empty();
        }

        String applicationName = applicationConfiguration.getName().get();
        Set<String> activeNames = environment.getActiveNames();

        LOG.info("Vault server endpoint: {}", vaultUri);
        LOG.info("Application name: {}, application profiles: {}", applicationName, activeNames);

        Function<Throwable, Publisher<? extends VaultResponse>> errorHandler = throwable -> {
            if (throwable instanceof HttpClientResponseException) {
                HttpClientResponseException httpClientResponseException = (HttpClientResponseException) throwable;
                if (httpClientResponseException.getStatus() == HttpStatus.NOT_FOUND) {
                    return Flowable.empty();
                }
            }
            return Flowable.error(throwable);
        };

        return Flowable.empty();
    }

    @Override
    public String getDescription() {
        return VaultConfigClientV1.CLIENT_DESCRIPTION;
    }
}