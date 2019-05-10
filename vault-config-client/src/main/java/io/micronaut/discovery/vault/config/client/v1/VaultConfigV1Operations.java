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

import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.config.client.VaultConfigClient;
import io.micronaut.discovery.vault.config.client.response.VaultResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.retry.annotation.Retryable;
import org.reactivestreams.Publisher;

/**
 * API operations for Vault Configuration - KV Version 1.
 *
 * @author Thiago Locatelli
 * @since 1.1.1
 */
public interface VaultConfigV1Operations extends VaultConfigClient {

    @Get("/{backend}/{applicationName}")
    @Produces(single = true)
    @Retryable(
            attempts = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryCount:3}",
            delay = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryDelay:1s}"
    )
    Publisher<VaultResponse> readConfiguratoinValues(String applicationName);

    @Get("/{backend}/{applicationName}/{profile}")
    @Produces(single = true)
    @Retryable(
            attempts = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryCount:3}",
            delay = "${" + VaultClientConfiguration.VaultClientConnectionPoolConfiguration.PREFIX + ".retryDelay:1s}"
    )
    Publisher<VaultResponse> readConfiguratoinValues(String backend, String applicationName, String profile);
}
