package mqttextender.auth_demo.hooks;

public enum AuthorizationResult {

    // The string-ification of these enum names are used
    // as an error code, identified by the client to react
    // appropriately on the user interface
    OK,

    INVALID_TOKEN(1),

    BROKER_CONNECTION_NOT_ALLOWED(2),

    PUBLISHING_NOT_ALLOWED(3),

    SUBSCRIPTIONS_NOT_ALLOWED(4);

    private int code;

    AuthorizationResult(int code) {
        this.code = code;
    }

    AuthorizationResult() {
        this(0);
    }

    public int getCode() {
        return code;
    }
}
