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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;

/**
 *  Vault Response Envelope
 *
 *  @author Thiago Locatelli
 *  @author graemerocher
 *  @since 1.1.1
 */
@Immutable
public class AbstractVaultResponse<T> {

    private T data;

    @JsonProperty("lease_duration")
    private Long leaseDuration;

    @JsonProperty("lease_id")
    private String leaseId;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("wrap_info")
    private Map<String, String> wrapInfo;

    private boolean renewable;

    private List<String> warnings;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getLeaseDuration() {
        return leaseDuration;
    }

    public void setLeaseDuration(Long leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, String> getWrapInfo() {
        return wrapInfo;
    }

    public void setWrapInfo(Map<String, String> wrapInfo) {
        this.wrapInfo = wrapInfo;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
