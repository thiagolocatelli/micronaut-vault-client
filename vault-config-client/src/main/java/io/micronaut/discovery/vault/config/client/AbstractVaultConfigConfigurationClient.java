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

package io.micronaut.discovery.vault.config.client;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 *  A {@link ConfigurationClient} for Vault Configuration.
 *
 *  @author thiagolocatelli
 *  @author graemerocher
 *  @since 1.1.1
 */
public abstract class AbstractVaultConfigConfigurationClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractVaultConfigConfigurationClient.class);

    private VaultClientConfiguration vaultClientConfiguration;
    private ApplicationConfiguration applicationConfiguration;
    private Environment environment;
    private ExecutorService executorService;

    public AbstractVaultConfigConfigurationClient(VaultClientConfiguration vaultClientConfiguration,
                                                  ApplicationConfiguration applicationConfiguration,
                                                  Environment environment,
                                                  ExecutorService executorService) {

        this.vaultClientConfiguration = vaultClientConfiguration;
        this.applicationConfiguration = applicationConfiguration;
        this.environment = environment;
        this.executorService = executorService;
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        if(!vaultClientConfiguration.getDiscoveryConfiguration().isEnabled()) {
            return Flowable.empty();
        }

        final String applicationName = applicationConfiguration.getName().get();
        final Set<String> activeNames = environment.getActiveNames();

        if (LOG.isInfoEnabled()) {
            LOG.info("Vault server endpoint: {}, secret engine version: {}", vaultClientConfiguration.getUri(),
                    vaultClientConfiguration.getKvVersion());
            LOG.info("Application name: {}, application profiles: {}", applicationName, activeNames);
        }

        activeNames.add(applicationName);
        Flowable<PropertySource> propertySourceFlowable = getProperySources(activeNames);

        if (executorService != null) {
            return propertySourceFlowable.subscribeOn(io.reactivex.schedulers.Schedulers.from(
                    executorService
            ));
        } else {
            return propertySourceFlowable;
        }
    }

    protected String getVaultSourceName(Set<String> activeNames, String currentActiveName) {
        for(String activeName : activeNames) {
            if(activeName.equals(currentActiveName) && !activeName.equals(getApplicationConfiguration().getName().get())) {
                return "/" + getApplicationConfiguration().getName().get() + "/" + activeName;
            }
        }
        return "/" + getApplicationConfiguration().getName().get();
    }

    protected abstract Flowable<PropertySource> getProperySources(Set<String> activeNames);

    public ApplicationConfiguration getApplicationConfiguration() {
        return this.applicationConfiguration;
    }

    public VaultClientConfiguration getVaultClientConfiguration() {
        return this.vaultClientConfiguration;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public class Pair<L, R> {

        private L left;
        private R right;

        protected Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() {
            return left;
        }

        public void setLeft(L left) {
            this.left = left;
        }

        public R getRight() {
            return right;
        }

        public void setRight(R right) {
            this.right = right;
        }
    }
}
