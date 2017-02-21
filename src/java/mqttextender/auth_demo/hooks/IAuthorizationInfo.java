package mqttextender.auth_demo.hooks;

public interface IAuthorizationInfo {

    boolean allowConnectionTo(String broker);

    boolean allowPublishTo(String topic);

    boolean allowSubscribeTo(String topic);

}