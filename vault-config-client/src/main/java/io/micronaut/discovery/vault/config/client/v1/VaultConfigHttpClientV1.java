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

package io.micronaut.discovery.vault.config.client.v1;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.config.client.AbstractVaultResponse;
import io.micronaut.discovery.vault.config.client.v1.condition.RequiresVaultClientConfigV1;
import io.micronaut.discovery.vault.config.client.v1.response.VaultResponseV1;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;

/**
 *  A non-blocking HTTP client for Vault - KV v2.
 *
 *  @author Thiago Locatelli
 *  @author graemerocher
 *  @since 1.1.1
 */
@Client(value = VaultClientConfiguration.VAULT_CLIENT_CONFIG_ENDPOINT, configuration = VaultClientConfiguration.class)
@Requires(beans = VaultClientConfiguration.class)
@RequiresVaultClientConfigV1
@BootstrapContextCompatible
public interface VaultConfigHttpClientV1 {

    String CLIENT_DESCRIPTION = "vault-config-client-v1";

    /**
     * Reads an application configuration from Spring Config Server.
     *
     * @param backend           The name of the secret engine in Vault
     * @param applicationName   The application name
     * @return A {@link Publisher} that emits a list of {@link AbstractVaultResponse}
     */
    @Get("/v1/{backend}/{applicationName}")
    @Produces(single = true)
    @Retryable(
            attempts = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryCount:3}",
            delay = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryDelay:1s}"
    )
    @Nonnull
    Publisher<VaultResponseV1> readConfigurationValues(
            @Nonnull String backend,
            @Nonnull String applicationName);

    /**
     * Reads an application configuration from Spring Config Server.
     *
     * @param backend           The name of the secret engine in Vault
     * @param applicationName   The application name
     * @param profile           The active profiles
     * @return A {@link Publisher} that emits a list of {@link AbstractVaultResponse}
     */
    @Get("/v1/{backend}/{applicationName}/{profile}")
    @Produces(single = true)
    @Retryable(
            attempts = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryCount:3}",
            delay = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryDelay:1s}"
    )
    @Nonnull Publisher<VaultResponseV1> readConfigurationValues(
            @Nonnull String backend,
            @Nonnull String applicationName,
            @Nonnull String profile);
}
