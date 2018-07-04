/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.data.cosmosdb;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.spring.data.cosmosdb.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.data.cosmosdb.telemetry.TelemetryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.spring.data.cosmosdb.telemetry.TelemetryProperties.PROPERTY_SERVICE_NAME;

public class DocumentDbFactory {

    private DocumentClient documentClient;
    private static final String USER_AGENT_SUFFIX = Constants.USER_AGENT_SUFFIX +
            DocumentDbFactory.class.getPackage().getImplementationVersion();

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    private String getUserAgentSuffix() {
        String suffix = ";" + USER_AGENT_SUFFIX;

        if (telemetryTracker != null) {
            suffix += ";" + TelemetryUtils.getHashMac();
        }

        return suffix;
    }

    public DocumentDbFactory(String host, String key) {
        Assert.hasText(host, "host must not be empty!");
        Assert.hasText(key, "key must not be empty!");

        final ConnectionPolicy policy = ConnectionPolicy.GetDefault();

        policy.setUserAgentSuffix(getUserAgentSuffix());

        this.documentClient = new DocumentClient(host, key, policy, ConsistencyLevel.Session);

        this.trackCustomEvent();
    }

    public DocumentDbFactory(DocumentClient client) {
        if (client != null && client.getConnectionPolicy() != null) {
            client.getConnectionPolicy().setUserAgentSuffix(this.getUserAgentSuffix());
        }

        this.documentClient = client;
        this.trackCustomEvent();
    }

    public DocumentClient getDocumentClient() {
        return documentClient;
    }

    private void trackCustomEvent() {
        final Map<String, String> customProperties = new HashMap<>();

        customProperties.put(PROPERTY_SERVICE_NAME, "cosmosdb");

        TelemetryUtils.telemetryTriggerEvent(telemetryTracker, this.getClass().getSimpleName(), customProperties);
    }
}
