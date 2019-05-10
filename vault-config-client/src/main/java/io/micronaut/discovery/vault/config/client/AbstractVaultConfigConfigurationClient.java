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

package io.micronaut.discovery.vault.config.client;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.discovery.vault.VaultClientConfiguration;
import io.micronaut.discovery.vault.config.client.response.VaultResponse;
import io.micronaut.discovery.vault.config.client.response.VaultResponseData;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 *  A {@link ConfigurationClient} for Vault Configuration.
 *
 *  @author Thiago Locatelli
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

        Function<Throwable, Publisher<? extends VaultResponse>> errorHandler = throwable -> {
            if (throwable instanceof HttpClientResponseException) {
                HttpClientResponseException httpClientResponseException = (HttpClientResponseException) throwable;
                if (httpClientResponseException.getStatus() == HttpStatus.NOT_FOUND) {
                    if (vaultClientConfiguration.isFailFast()) {
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

        List<Flowable<VaultResponse>> configurationValuesList = retrieveVaultProperties(vaultClientConfiguration,
                applicationConfiguration, environment, errorHandler);


        Flowable<PropertySource> propertySourceFlowable = configurationValuesList.get(0).flatMap(vaultResponse -> Flowable.create(emitter -> {
            VaultResponseData vaultResponseData = vaultResponse.getData();

            if (CollectionUtils.isEmpty(vaultResponseData.getData())) {
                emitter.onComplete();
            } else {
                int priority = Integer.MAX_VALUE;
                //for (ConfigServerPropertySource propertySource : propertySources) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Obtained property source [{}] from Spring Cloud Configuration Server", "vault");
                }

                emitter.onNext(PropertySource.of("Vault", vaultResponseData.getData(), priority));
                priority -= 10;
                //}
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR));

        if (executorService != null) {
            return propertySourceFlowable.subscribeOn(io.reactivex.schedulers.Schedulers.from(
                    executorService
            ));
        } else {
            return propertySourceFlowable;
        }
    }

    /**
     *
     * @param vaultClientConfiguration
     * @param applicationConfiguration
     * @param environment
     * @param errorHandler
     * @return
     */
    public abstract List<Flowable<VaultResponse>> retrieveVaultProperties(
            VaultClientConfiguration vaultClientConfiguration,
            ApplicationConfiguration applicationConfiguration,
            Environment environment,
            Function<Throwable, Publisher<? extends VaultResponse>> errorHandler);
}
