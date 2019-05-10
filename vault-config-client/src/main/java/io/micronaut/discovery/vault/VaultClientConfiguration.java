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

package io.micronaut.discovery.vault;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.discovery.config.ConfigDiscoveryConfiguration;
import io.micronaut.discovery.vault.condition.RequiresVaultClientConfig;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;

import javax.inject.Inject;

/**
 * A {@link HttpClientConfiguration} for Vault Client.
 *
 *  @author Thiago Locatelli
 *  @since 1.1.1
 */
@RequiresVaultClientConfig
@ConfigurationProperties(VaultClientConstants.PREFIX)
public class VaultClientConfiguration extends HttpClientConfiguration {

    public static final String VAULT_CLIENT_CONFIG_ENDPOINT = "${" + VaultClientConstants.PREFIX + ".uri}";

    public enum KV_VERSION { V1, V2 };

    private final VaultClientConnectionPoolConfiguration vaultClientConnectionPoolConfiguration;
    private final VaultClientDiscoveryConfiguration vaultClientDiscoveryConfiguration = new VaultClientDiscoveryConfiguration();

    @Inject
    public VaultClientConfiguration(VaultClientConnectionPoolConfiguration vaultClientConnectionPoolConfiguration, ApplicationConfiguration applicationConfiguration) {
        super(applicationConfiguration);
        this.vaultClientConnectionPoolConfiguration = vaultClientConnectionPoolConfiguration;
    }

    @Override
    public ConnectionPoolConfiguration getConnectionPoolConfiguration() {
        return vaultClientConnectionPoolConfiguration;
    }

    public VaultClientDiscoveryConfiguration getDiscoveryConfiguration() {
        return vaultClientDiscoveryConfiguration;
    }

    @ConfigurationProperties(HttpClientConfiguration.ConnectionPoolConfiguration.PREFIX)
    public static class VaultClientConnectionPoolConfiguration extends HttpClientConfiguration.ConnectionPoolConfiguration { }

    @ConfigurationProperties(ConfigDiscoveryConfiguration.PREFIX)
    public static class VaultClientDiscoveryConfiguration extends ConfigDiscoveryConfiguration { }

    private String uri = "http://locahost:8200";;
    private String token;
    private KV_VERSION kvVersion = KV_VERSION.V1;
    private String backend = "secret";

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public KV_VERSION getKvVersion() {
        return kvVersion;
    }

    public void setKvVersion(KV_VERSION kvVersion) {
        this.kvVersion = kvVersion;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }
}
