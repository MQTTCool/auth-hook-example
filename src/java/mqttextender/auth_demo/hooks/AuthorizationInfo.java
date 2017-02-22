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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@code IAuthorizationInfo} interface, which wraps a predefined
 * authorization set.
 */
public class AuthorizationInfo implements IAuthorizationInfo {

    /** Set of contactable broker addresses */
    private Set<String> allowedBrokers;

    /** Set of subscribable topics */
    private final Set<String> allowedTopicForSubscribing;

    /** Set of allowed topics for publishing */
    private final Set<String> allowedTopicForPublishing;

    private AuthorizationInfo(Set<String> allowedBrokers,
                              Set<String> allowedTopicForSubscribing,
                              Set<String> allowedTopicForPublishing) {

        this.allowedBrokers = Collections.unmodifiableSet(allowedBrokers);
        this.allowedTopicForSubscribing = Collections.unmodifiableSet(allowedTopicForSubscribing);
        this.allowedTopicForPublishing = Collections.unmodifiableSet(allowedTopicForPublishing);
    }

    private AuthorizationInfo() {
        this.allowedBrokers = Collections.emptySet();
        this.allowedTopicForSubscribing = Collections.emptySet();
        this.allowedTopicForPublishing = Collections.emptySet();
    }

    @Override
    public boolean allowConnectionTo(String broker) {
        return allowedBrokers.contains(broker);
    }

    @Override
    public boolean allowSubscribeTo(String topic) {
        return allowedTopicForSubscribing.contains(topic);
    }

    @Override
    public boolean allowPublishTo(String topic) {
        return allowedTopicForPublishing.contains(topic);
    }

    /**
     * Simple builder class for making instance of {@code IAuthorizationInfo}.
     */
    public static class AuthorizationBuilder {

        /**
         * Short-cut representing "ALL" authorizations.
         */
        public final static IAuthorizationInfo ALL = new AuthorizationInfo() {

            @Override
            public boolean allowConnectionTo(String topic) {
                return true;
            }

            @Override
            public boolean allowSubscribeTo(String topic) {
                return true;
            }

            @Override
            public boolean allowPublishTo(String topic) {
                return true;
            }
        };

        /** Set of contactable broker addresses */
        private Set<String> contactableBrokers = new HashSet<>();

        /** Set of subscribable topics */
        private Set<String> subscribableTopics = new HashSet<>();

        /** Set of allowed topics for publishing */
        private Set<String> publishableTopics = new HashSet<>();

        /**
         * Add the specified MQTT broker address as contactable to the authorizations set.
         * 
         * @param broker
         *            the MQTT broker address to connect to
         * @return a reference to this object
         */
        public AuthorizationBuilder withBroker(String broker) {
            contactableBrokers.add(broker);
            return this;
        }

        /**
         * Add the specified topic as subscriable to the set of authorizations.
         * 
         * @param topic
         *            the subscribable topic
         * @return a reference to this object
         */
        public AuthorizationBuilder withSubscribeTo(String topic) {
            subscribableTopics.add(topic);
            return this;
        }

        /**
         * Add the specified topic as allowed for publishing, to the set of authorizations
         * 
         * @param topic
         *            the topic allowed for publishing
         * @return a reference to this object
         */
        public AuthorizationBuilder withPublishingTo(String topic) {
            publishableTopics.add(topic);
            return this;
        }

        /**
         * Builds a new instance of {@code IAuthorizationInfo} which contains all provided
         * authorizations.
         * 
         * @return an instance of {@code IAuthorizationInfo}.
         */
        public IAuthorizationInfo build() {
            return new AuthorizationInfo(contactableBrokers, subscribableTopics, publishableTopics);
        }

    }

}
