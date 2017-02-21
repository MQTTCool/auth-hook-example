package mqttextender.auth_demo.hooks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

interface IPermission {

    boolean isAllowed(String resource);
}

/**
 *
 */
public class AuthorizationInfo implements IAuthorizationInfo {

    private final Set<String> allowedTopicForPublishing;

    private final Set<String> allowedTopicForSubscribing;

    private Set<String> allowedBrokers;

    private AuthorizationInfo(Set<String> allowedBrokers,
                              Set<String> allowedTopicForPublishing,
                              Set<String> allowedTopicForSubscribing) {

        this.allowedBrokers = Collections.unmodifiableSet(allowedBrokers);
        this.allowedTopicForPublishing = Collections.unmodifiableSet(allowedTopicForPublishing);
        this.allowedTopicForSubscribing = Collections.unmodifiableSet(allowedTopicForSubscribing);
    }

    private AuthorizationInfo() {
        this.allowedBrokers = Collections.emptySet();
        this.allowedTopicForPublishing = Collections.emptySet();
        this.allowedTopicForSubscribing = Collections.emptySet();
    }

    @Override
    public boolean allowConnectionTo(String broker) {
        return allowedBrokers.contains(broker);
    }

    @Override
    public boolean allowPublishTo(String topic) {
        return allowedTopicForPublishing.contains(topic);
    }

    @Override
    public boolean allowSubscribeTo(String topic) {
        return allowedTopicForSubscribing.contains(topic);
    }

    public static class AuthorizationBuilder {

        public static IAuthorizationInfo NONE = new AuthorizationInfo();

        public static IAuthorizationInfo ALL = new AuthorizationInfo() {

            @Override
            public boolean allowConnectionTo(String topic) {
                return true;
            }

            @Override
            public boolean allowPublishTo(String topic) {
                return true;
            }

            @Override
            public boolean allowSubscribeTo(String topic) {
                return true;
            }
        };

        private Set<String> brokers = new HashSet<>();
        
        private Set<String> topicForPublishing = new HashSet<>();

        private Set<String> topicForSubscription = new HashSet<>();

        public AuthorizationBuilder() {
        }

        public AuthorizationBuilder withBroker(String broker) {
            brokers.add(broker);
            return this;
        }
        
        public AuthorizationBuilder withPublishingTo(String topic) {
            topicForPublishing.add(topic);
            return this;
        }

        public AuthorizationBuilder withPublishtoAll() {
            return this;
        }

        public AuthorizationBuilder withSubscribeTo(String topic) {
            topicForSubscription.add(topic);
            return this;
        }

        public IAuthorizationInfo build() {
            return new AuthorizationInfo(brokers, topicForPublishing, topicForSubscription);
        }

    }

}
