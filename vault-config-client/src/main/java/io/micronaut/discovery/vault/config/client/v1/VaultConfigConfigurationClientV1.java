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

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.condition.RequiresVaultClientConfig;
import io.micronaut.discovery.vault.config.client.AbstractVaultConfigConfigurationClient;
import io.micronaut.discovery.vault.config.client.v1.condition.RequiresVaultClientConfigV1;
import io.micronaut.discovery.vault.config.client.v1.response.VaultResponseV1;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *  A {@link ConfigurationClient} for Vault Configuration.
 *
 *  @author Thiago Locatelli
 *  @author graemerocher
 *  @since 1.1.1
 */
@Singleton
@RequiresVaultClientConfig
@RequiresVaultClientConfigV1
@Requires(property = ConfigurationClient.ENABLED, value = "true", defaultValue = "false")
@BootstrapContextCompatible
public class VaultConfigConfigurationClientV1 extends AbstractVaultConfigConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultConfigConfigurationClientV1.class);

    private final VaultConfigHttpClientV1 vaultConfigClientV1;
    private final String vaultUri;

    /**
     *
     * @param vaultConfigClientV1       Vault Config Http Client
     * @param vaultClientConfiguration  Vault Client Configuration
     * @param applicationConfiguration  The application configuration
     * @param environment               The environment
     * @param vaultUri                  Vault endpoint uri
     */
        public VaultConfigConfigurationClientV1(VaultConfigHttpClientV1 vaultConfigClientV1,
                                            VaultClientConfiguration vaultClientConfiguration,
                                            ApplicationConfiguration applicationConfiguration,
                                            Environment environment,
                                            @Value(VaultClientConfiguration.VAULT_CLIENT_CONFIG_ENDPOINT) String vaultUri,
                                            @Named(TaskExecutors.IO) @Nullable ExecutorService executorService) {

        super(vaultClientConfiguration, applicationConfiguration, environment, executorService);
        this.vaultConfigClientV1 = vaultConfigClientV1;
        this.vaultUri = vaultUri;
    }

    @Override
    protected Flowable<PropertySource> getProperySources(Set<String> activeNames) {

        Function<Throwable, Publisher<? extends VaultResponseV1>> errorHandler = getErrorHandler();
        List<Flowable<VaultResponseV1>> configurationValuesList = retrieveVaultProperties(activeNames, errorHandler);
        final AtomicInteger priority = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicInteger source = new AtomicInteger(0);

        return Flowable.fromIterable(configurationValuesList).concatMapEager(vaultResponseFlowable -> {
            return vaultResponseFlowable.flatMap(vaultResponse -> Flowable.create(emitter -> {
                Map<String, Object> vaultResponseData = vaultResponse.getData();
                if (!CollectionUtils.isEmpty(vaultResponseData)) {
                    int innerPriority = 0;
                    synchronized (source) {
                        source.getAndIncrement();
                        innerPriority = priority.getAndDecrement();
                    }

                    if (LOG.isInfoEnabled()) {
                        LOG.info("Obtained property source from Vault, {}", vaultResponseData);
                    }
                    emitter.onNext(PropertySource.of("vault" + innerPriority, vaultResponseData, innerPriority));
                }

                //if all items have been processed, emit onComplete
                if (source.get() == configurationValuesList.size()) {
                    emitter.onComplete();
                }
            }, BackpressureStrategy.ERROR));
        });
    }

    private List<Flowable<VaultResponseV1>> retrieveVaultProperties(Set<String> activeNames, Function<Throwable,
            Publisher<? extends VaultResponseV1>> errorHandler) {

        String applicationName = getApplicationConfiguration().getName().get();
        return activeNames.stream().map(activeName -> activeName.equals(applicationName) ?
                        Flowable.fromPublisher(
                                vaultConfigClientV1.readConfigurationValues(
                                        getVaultClientConfiguration().getBackend(),
                                        applicationName))
                                .onErrorResumeNext(errorHandler) :
                        Flowable.fromPublisher(
                                vaultConfigClientV1.readConfigurationValues(
                                        getVaultClientConfiguration().getBackend(),
                                        applicationName, activeName))
                                .onErrorResumeNext(errorHandler)).collect(Collectors.toList());
    }

    private Function<Throwable, Publisher<? extends VaultResponseV1>>  getErrorHandler() {

        return throwable -> {
            if (throwable instanceof HttpClientResponseException) {
                HttpClientResponseException httpClientResponseException = (HttpClientResponseException) throwable;
                if (httpClientResponseException.getStatus() == HttpStatus.NOT_FOUND) {
                    if (getVaultClientConfiguration().isFailFast()) {
                        return Flowable.error(new IllegalStateException(
                                "Could not locate PropertySource and the fail fast property is set",
                                throwable));
                    }
                    LOG.warn("Could not locate PropertySource: ", throwable);
                    return Flowable.empty();
                }
            }
            return Flowable.error(throwable);
        };
    }

    @Override
    public String getDescription() {
        return VaultConfigHttpClientV1.CLIENT_DESCRIPTION;
    }
}