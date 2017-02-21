package mqttextender.auth_demo.hooks;

import java.util.concurrent.ConcurrentHashMap;

public class AuthorizationRequest {

    /** List of user-token pairs, shared are shared with the demo client. */
    private static final ConcurrentHashMap<String, String> TOKENS = new ConcurrentHashMap<>();

    static {
        TOKENS.put("user1", "ikgdfigdfhihdsih");
        TOKENS.put("user2", "slaoejkauekalkew");
        TOKENS.put("patient0", "lookihaveanewtokenhere");
        TOKENS.put("leto", "powerfultoken");
        TOKENS.put("gollum", "toobadforyou");
        TOKENS.put("lucky", "srsly");
    }

    /** URI of the allowed MQTT broker to connect to. */
    private static final String ALLOWED_BROKER = "tcp://localhost:1883";

    /**
     * List of user-authorization pairs, shared with the demo client (the client simply shows these
     * infos in the interface, does not directly use them)
     */
    private static final ConcurrentHashMap<String, IAuthorizationInfo> AUTHORIZATIONS =
        new ConcurrentHashMap<>();

    // Initialize authorizations for each user.
    static {
        // Authorizations for user "user1":
        IAuthorizationInfo user1Auth = new AuthorizationInfo.AuthorizationBuilder()
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
        IAuthorizationInfo user2Auth = new AuthorizationInfo.AuthorizationBuilder().build();
        AUTHORIZATIONS.put("user2", user2Auth);

        // Authorizations for user "patient0", which will never be able to open a new session.

        // Authorizations for user "leto", which will be able to authorized to do everything.
        IAuthorizationInfo letoAuth = AuthorizationInfo.AuthorizationBuilder.ALL;
        AUTHORIZATIONS.put("leto", letoAuth);

        // Authorizations for user "gollum", which will only be able to connect to the MQTT broker.
        IAuthorizationInfo gollumAuth = new AuthorizationInfo.AuthorizationBuilder()
            .withBroker(ALLOWED_BROKER)
            .build();
        AUTHORIZATIONS.put("gollum", gollumAuth);

        // Authorizations for user "lucky":
        IAuthorizationInfo lucyAuth = new AuthorizationInfo.AuthorizationBuilder()
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
        IAuthorizationInfo authorizationInfo = AUTHORIZATIONS.get(user);
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
        IAuthorizationInfo authorizationInfo = AUTHORIZATIONS.get(user);
        if ((authorizationInfo != null) && authorizationInfo.allowPublishTo(topic)) {
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
        IAuthorizationInfo authorizationInfo = AUTHORIZATIONS.get(user);
        if ((authorizationInfo != null) && authorizationInfo.allowSubscribeTo(topicFilter)) {
            return AuthorizationResult.OK;
        }

        return AuthorizationResult.SUBSCRIPTIONS_NOT_ALLOWED;
    }
}
