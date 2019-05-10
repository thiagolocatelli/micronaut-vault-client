/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.discovery.vault.config.client.v2;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.condition.RequiresVaultClientConfig;
import io.micronaut.discovery.vault.config.client.v2.condition.RequiresVaultClientConfigV2;
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
@RequiresVaultClientConfigV2
@Requires(property = ConfigurationClient.ENABLED, value = "true", defaultValue = "false")
public class VaultConfigConfigurationClientV2 implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultConfigConfigurationClientV2.class);

    private final VaultConfigClientV2 vaultConfigClientV2;
    private final VaultClientConfiguration vaultClientConfiguration;
    private final ApplicationConfiguration applicationConfiguration;
    private final Environment environment;
    private final String vaultUri;

    private ExecutorService executionService;

    /**
     *
     * @param vaultConfigClientV2       Vault Config Http Client
     * @param vaultClientConfiguration  Vault Client Configuration
     * @param applicationConfiguration  The application configuration
     * @param environment               The environment
     * @param vaultUri                  Vault endpoint uri
     */
    public VaultConfigConfigurationClientV2(VaultConfigClientV2 vaultConfigClientV2, VaultClientConfiguration vaultClientConfiguration,
                                            ApplicationConfiguration applicationConfiguration, Environment environment,
                                            @Value(VaultClientConfiguration.VAULT_CLIENT_CONFIG_ENDPOINT) String vaultUri) {

        this.vaultConfigClientV2 = vaultConfigClientV2;
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
        return VaultConfigClientV2.CLIENT_DESCRIPTION;
    }
}
