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

package io.micronaut.discovery.vault.config.client.v1.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Internal;
import io.micronaut.discovery.vault.config.client.AbstractVaultResponse;

import java.util.List;
import java.util.Map;

/**
 *  Vault Response Envelope
 *
 *  @author thiagolocatelli
 *  @author graemerocher
 *  @since 1.1.1
 */
public class VaultResponseV1 extends AbstractVaultResponse<Map<String, Object>> {

    @JsonCreator
    @Internal
    public VaultResponseV1(
            @JsonProperty("data") Map<String, Object> data,
            @JsonProperty("lease_duration") Long leaseDuration,
            @JsonProperty("lease_id") String leaseId,
            @JsonProperty("request_id") String requestId,
            @JsonProperty("wrap_info") Map<String, String> wrapInfo,
            @JsonProperty("renewable") boolean renewable,
            @JsonProperty("warnings") List<String> warnings) {
        super(data, leaseDuration, leaseId, requestId, wrapInfo, renewable, warnings);
    }
}
