/*
 * Copyright (c) Lightstreamer Srl
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mqttextender.auth_demo.hooks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles authorization requests issued by the user.  
 */
class AuthorizationRequest {

    /** User-token map, shared with the demo client. */
    private static final ConcurrentHashMap<String, String> TOKENS = new ConcurrentHashMap<>();

    // Initialize the user-token map
    static {
        TOKENS.put("user1", "ikgdfigdfhihdsih");
        TOKENS.put("user2", "slaoejkauekalkew");
        TOKENS.put("patient0", "lookihaveanewtokenhere");
        TOKENS.put("leto", "powerfultoken");
        TOKENS.put("gollum", "toobadforyou");
        TOKENS.put("lucky", "srsly");
    }

    /** URI of the allowed MQTT broker to connect to. You might want to change it. */
    private static final String ALLOWED_BROKER = "tcp://localhost:1883";

    /**
     * List of user-authorization pairs, shared with the demo client (the client simply shows these
     * infos in the interface, does not directly use them).
     */
    private static final ConcurrentHashMap<String, IPermissionInfo> AUTHORIZATIONS =
        new ConcurrentHashMap<>();

    // Initialize authorizations for each user.
    static {
        // Authorizations for user "user1":
        IPermissionInfo user1Auth = new DefaultPermissionInfo.AuthorizationBuilder()
            .withBroker(ALLOWED_BROKER)
            .withSubscribeTo("topics/topic_1")
            .withSubscribeTo("topics/topic_2")
            .withSubscribeTo("topics/topic_3")
            .withPublishingTo("topics/topic_4")
            .withPublishingTo("topics/topic_5")
            .withPublishingTo("topics/topic_6")
            .build();
        AUTHORIZATIONS.put("user1", user1Auth);

        /*
         * Authorizations for user "user2", which will be able to open a session but not to
         * connect to the MQTT broker.
         */
        IPermissionInfo user2Auth = new DefaultPermissionInfo.AuthorizationBuilder().build();
        AUTHORIZATIONS.put("user2", user2Auth);

        // Authorizations for user "patient0", which will never be able to open a new session.

        // Authorizations for user "leto", which will be able to authorized to do everything.
        IPermissionInfo letoAuth = DefaultPermissionInfo.AuthorizationBuilder.ALL;
        AUTHORIZATIONS.put("leto", letoAuth);

        // Authorizations for user "gollum", which will only be able to connect to the MQTT broker.
        IPermissionInfo gollumAuth = new DefaultPermissionInfo.AuthorizationBuilder()
            .withBroker(ALLOWED_BROKER)
            .build();
        AUTHORIZATIONS.put("gollum", gollumAuth);

        // Authorizations for user "lucky":
        IPermissionInfo lucyAuth = new DefaultPermissionInfo.AuthorizationBuilder()
            .withBroker(ALLOWED_BROKER)
            .withPublishingTo("topics/topic_13")
            .withPublishingTo("topics/topic_17")
            .build();
        AUTHORIZATIONS.put("lucky", lucyAuth);
    }

    public static AuthorizationResult validateToken(String user, String token) {
        /*
         * In a real case, the application would lookup the user-token pair on an external service
         * (or a local cache); in this demo we simply lookup the hard-coded map.
         */
        String correctToken = TOKENS.get(user);
        if ((correctToken != null) && correctToken.equals(token)) {
            return AuthorizationResult.OK;
        }

        // Return the appropriate error.
        return AuthorizationResult.INVALID_TOKEN;
    }

    public static AuthorizationResult authorizeMQTTConnection(String user, String broker) {
        /*
         * In a real case, the application would lookup the user authorizations on an external
         * service (or a local cache); in this demo we simply lookup the hard-coded map.
         */
        IPermissionInfo authorizationInfo = AUTHORIZATIONS.get(user);
        if ((authorizationInfo != null) && authorizationInfo.allowConnectionTo(broker)) {
            return AuthorizationResult.OK;
        }

        // Return the appropriate error.
        return AuthorizationResult.BROKER_CONNECTION_NOT_ALLOWED;
    }

    public static AuthorizationResult authorizePublishTo(String user, String topic) {
        /*
         * In a real case, the application would lookup the user authorizations on an external
         * service (or a local cache); in this demo we simply lookup the hard-coded map.
         */
        IPermissionInfo permissioInfo = AUTHORIZATIONS.get(user);
        if ((permissioInfo != null) && permissioInfo.allowPublishTo(topic)) {
            return AuthorizationResult.OK;
        }

        // Return the appropriate error.
        return AuthorizationResult.PUBLISHING_NOT_ALLOWED;
    }

    public static AuthorizationResult authorizeSubscribeTo(String user, String topicFilter) {
        /*
         * In a real case, the application would lookup the user authorizations on an external
         * service (or a local cache); in this demo we simply lookup the hard-coded map.
         */
        IPermissionInfo permissionInfo = AUTHORIZATIONS.get(user);
        if ((permissionInfo != null) && permissionInfo.allowSubscribeTo(topicFilter)) {
            return AuthorizationResult.OK;
        }

        return AuthorizationResult.SUBSCRIPTION_NOT_ALLOWED;
    }

    public static Map<String, Map<String, AuthorizationResult>> getUserAuthorizations(String user) {
        /*
         * In a real case, the application would lookup the user authorizations on an external
         * service (or a local cache); in this demo we simply preload a map with the possible
         * authorization results from the hard-coded map.
         */
        IPermissionInfo permissionInfos = AUTHORIZATIONS.get(user);

        Map<String, Map<String, AuthorizationResult>> results = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, AuthorizationResult> connectResults = new ConcurrentHashMap<>();
        AuthorizationResult connectionResult =
            permissionInfos.allowConnectionTo(ALLOWED_BROKER)
                ? AuthorizationResult.OK
                : AuthorizationResult.BROKER_CONNECTION_NOT_ALLOWED;
        connectResults.put(ALLOWED_BROKER, connectionResult);
        results.put("connect", connectResults);

        Map<String, AuthorizationResult> subscribeResults = new ConcurrentHashMap<>();
        Map<String, AuthorizationResult> pulishResults = new ConcurrentHashMap<>();

        String topicPrefix = "topics/topic_";
        for (int i = 0; i < 30; i++) {
            String topic = topicPrefix + "_" + i;
            AuthorizationResult subscriptionResult = permissionInfos.allowSubscribeTo(topic)
                ? AuthorizationResult.OK
                : AuthorizationResult.SUBSCRIPTION_NOT_ALLOWED;
            subscribeResults.put(topic, subscriptionResult);

            AuthorizationResult publishResult = permissionInfos.allowSubscribeTo(topic)
                ? AuthorizationResult.OK
                : AuthorizationResult.PUBLISHING_NOT_ALLOWED;
            pulishResults.put(topic, publishResult);
        }

        results.put("subscribe", subscribeResults);
        results.put("publish", pulishResults);

        return results;
    }
}
