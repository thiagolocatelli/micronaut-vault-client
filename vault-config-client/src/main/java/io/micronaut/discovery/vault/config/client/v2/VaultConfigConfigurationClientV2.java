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

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.condition.RequiresVaultClientConfig;
import io.micronaut.discovery.vault.config.client.AbstractVaultConfigConfigurationClient;
import io.micronaut.discovery.vault.config.client.response.VaultResponse;
import io.micronaut.discovery.vault.config.client.v2.condition.RequiresVaultClientConfigV2;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 *  A {@link ConfigurationClient} for Vault Configuration.
 *
 *  @author Thiago Locatelli
 *  @author graemerocher
 *  @since 1.1.1
 */
@Singleton
@RequiresVaultClientConfig
@RequiresVaultClientConfigV2
@Requires(property = ConfigurationClient.ENABLED, value = "true", defaultValue = "false")
@BootstrapContextCompatible
public class VaultConfigConfigurationClientV2 extends AbstractVaultConfigConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultConfigConfigurationClientV2.class);

    private final VaultConfigHttpClientV2 vaultConfigClientV2;
    private final String vaultUri;

    /**
     *
     * @param vaultConfigClientV2       Vault Config Http Client
     * @param vaultClientConfiguration  Vault Client Configuration
     * @param applicationConfiguration  The application configuration
     * @param environment               The environment
     * @param vaultUri                  Vault endpoint uri
     */
    public VaultConfigConfigurationClientV2(VaultConfigHttpClientV2 vaultConfigClientV2,
                                            VaultClientConfiguration vaultClientConfiguration,
                                            ApplicationConfiguration applicationConfiguration,
                                            Environment environment,
                                            @Value(VaultClientConfiguration.VAULT_CLIENT_CONFIG_ENDPOINT) String vaultUri,
                                            @Named(TaskExecutors.IO) @Nullable ExecutorService executorService) {

        super(vaultClientConfiguration, applicationConfiguration, environment, executorService);
        this.vaultConfigClientV2 = vaultConfigClientV2;
        this.vaultUri = vaultUri;
    }

    @Override
    public List<Flowable<VaultResponse>> retrieveVaultProperties(VaultClientConfiguration vaultClientConfiguration,
                                                                 ApplicationConfiguration applicationConfiguration,
                                                                 Environment environment,
                                                                 Function<Throwable, Publisher<? extends VaultResponse>> errorHandler) {

        String applicationName = applicationConfiguration.getName().get();
        Set<String> activeNames = environment.getActiveNames();

        if (activeNames.isEmpty()) {
            activeNames.add(Environment.DEVELOPMENT);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Vault server endpoint: {}, secret engine version: {}", vaultClientConfiguration.getUri(), vaultClientConfiguration.getKvVersion());
            LOG.info("Application name: {}, application profiles: {}", applicationName, activeNames);
        }

        return Arrays.asList(Flowable.fromPublisher(
                vaultConfigClientV2.readConfiguratoinValues(vaultClientConfiguration.getBackend(), applicationName))
                .onErrorResumeNext(errorHandler));
    }

    @Override
    public String getDescription() {
        return VaultConfigHttpClientV2.CLIENT_DESCRIPTION;
    }
}