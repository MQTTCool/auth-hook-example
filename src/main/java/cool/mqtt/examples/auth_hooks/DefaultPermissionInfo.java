/*
 * MQTT.Cool - https://mqtt.cool
 * 
 * Authentication and Authorization Demo
 *
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package cool.mqtt.examples.auth_hooks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@code IPermissionInfo} interface, which wraps a predefined permissions
 * set.
 */
class DefaultPermissionInfo implements PermissionInfo {

  /** Set of contactable broker addresses */
  private Set<String> allowedBrokers;

  /** Set of subscribable topics */
  private final Set<String> allowedTopicForSubscribing;

  /** Set of allowed topics for publishing */
  private final Set<String> allowedTopicForPublishing;

  private DefaultPermissionInfo(Set<String> allowedBrokers, Set<String> allowedTopicForSubscribing,
      Set<String> allowedTopicForPublishing) {

    this.allowedBrokers = Collections.unmodifiableSet(allowedBrokers);
    this.allowedTopicForSubscribing = Collections.unmodifiableSet(allowedTopicForSubscribing);
    this.allowedTopicForPublishing = Collections.unmodifiableSet(allowedTopicForPublishing);
  }

  private DefaultPermissionInfo() {
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
  static class AuthorizationBuilder {

    /**
     * Short-cut representing granted permissions on everything.
     */
    public final static PermissionInfo ALL = new DefaultPermissionInfo() {

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
     * Grant the permission to connect to the specified MQTT broker address.
     *
     * @param broker the MQTT broker address to connect to
     * @return a reference to this object
     */
    public AuthorizationBuilder withBroker(String broker) {
      contactableBrokers.add(broker);
      return this;
    }

    /**
     * Grant the permission to connect to the specified MQTT broker addresses.
     *
     * @param set containing the MQTT broker addresses to connect to
     * @return a reference to this object
     */
    public AuthorizationBuilder withBrokers(Set<String> brokers) {
      contactableBrokers.addAll(brokers);
      return this;
    }

    /**
     * Grant the permission to subscribe to the specified topic.
     *
     * @param topic the subscribable topic
     * @return a reference to this object
     */
    public AuthorizationBuilder withSubscribeTo(String topic) {
      subscribableTopics.add(topic);
      return this;
    }

    /**
     * Grant the permission to publish to the specified topic.
     *
     * @param topic the topic allowed for publishing
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
    public PermissionInfo build() {
      return new DefaultPermissionInfo(contactableBrokers, subscribableTopics, publishableTopics);
    }

  }

}
