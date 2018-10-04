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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles authorization requests issued by the user.
 */
class AuthorizationHandler {

  /** User-token map, shared with the demo client. */
  private final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();

  /** Set of URIs of the allowed MQTT brokers to connect to. */
  private final Set<String> allowedBrokers;

  /**
   * User-authorization map, shared with the demo client (the client simply shows these infos in the
   * interface, does not directly use them).
   */
  private Map<String, PermissionInfo> authorizations;

  public AuthorizationHandler(Set<String> allowedBrokers) {
    this.allowedBrokers = Collections.unmodifiableSet(allowedBrokers);
    initUserTokenMap();
    initAuthorizations();
  }

  private void initUserTokenMap() {
    // To be used from all other live demos, as this Hook is deployed into
    // "https://cloud.mqtt.cool".
    tokens.put("demouser", "");

    // Expected users from the "Authentication and Authorization" demo client.
    tokens.put("user1", "ikgdfigdfhihdsih");
    tokens.put("user2", "slaoejkauekalkew");
    tokens.put("patient0", "lookihaveanewtokenhere");
    tokens.put("leto", "powerfultoken");
    tokens.put("gollum", "toobadforyou");
    tokens.put("lucky", "srsly");
  }

  private void initAuthorizations() {
    Map<String, PermissionInfo> userAuthorizations = new HashMap<>();

    // Authorizations for user "user1":
    PermissionInfo user1Auth =
        new DefaultPermissionInfo.AuthorizationBuilder().withBrokers(allowedBrokers)
            .withSubscribeTo("topics/topic_1").withSubscribeTo("topics/topic_2")
            .withSubscribeTo("topics/topic_3").withPublishingTo("topics/topic_4")
            .withPublishingTo("topics/topic_5").withPublishingTo("topics/topic_6").build();
    userAuthorizations.put("user1", user1Auth);

    /*
     * Authorizations for user "user2", which will be able to open a session but not to connect to
     * the MQTT broker.
     */
    PermissionInfo user2Auth = new DefaultPermissionInfo.AuthorizationBuilder().build();
    userAuthorizations.put("user2", user2Auth);

    // Authorizations for user "leto", which will be able to authorized to do everything.
    PermissionInfo letoAuth = DefaultPermissionInfo.AuthorizationBuilder.ALL;
    userAuthorizations.put("leto", letoAuth);

    // Authorizations for user "gollum", which will only be able to connect to the MQTT brokers.
    PermissionInfo gollumAuth =
        new DefaultPermissionInfo.AuthorizationBuilder().withBrokers(allowedBrokers).build();
    userAuthorizations.put("gollum", gollumAuth);

    // Authorizations for user "lucky":
    PermissionInfo lucyAuth =
        new DefaultPermissionInfo.AuthorizationBuilder().withBrokers(allowedBrokers)
            .withPublishingTo("topics/topic_13").withPublishingTo("topics/topic_17").build();
    userAuthorizations.put("lucky", lucyAuth);

    // Authorizations for user "demouser", used for all other live demos.
    PermissionInfo demouserAuth = DefaultPermissionInfo.AuthorizationBuilder.ALL;
    userAuthorizations.put("demouser", demouserAuth);

    // As last note, user "patient0" will never be able to open a new session, therefore
    // there is no need to specify any PerimissionInfo.

    authorizations = Collections.unmodifiableMap(userAuthorizations);
  }

  public AuthorizationResult validateToken(String user, String token) {
    /*
     * In a real case, the application would lookup the user-token pair on an external service (or a
     * local cache); in this demo we simply lookup the hard-coded map.
     */
    String correctToken = tokens.get(user);
    if ((correctToken != null) && correctToken.equals(token)) {
      return AuthorizationResult.OK;
    }

    // Return the appropriate error.
    return AuthorizationResult.INVALID_TOKEN;
  }

  public AuthorizationResult authorizeMQTTConnection(String user, String broker) {
    /*
     * In a real case, the application would lookup the user authorizations on an external service
     * (or a local cache); in this demo we simply lookup the hard-coded map.
     */
    PermissionInfo authorizationInfo = authorizations.get(user);
    if ((authorizationInfo != null) && authorizationInfo.allowConnectionTo(broker)) {
      return AuthorizationResult.OK;
    }

    // Return the appropriate error.
    return AuthorizationResult.BROKER_CONNECTION_NOT_ALLOWED;
  }

  public AuthorizationResult authorizePublishTo(String user, String topic) {
    /*
     * In a real case, the application would lookup the user authorizations on an external service
     * (or a local cache); in this demo we simply lookup the hard-coded map.
     */
    PermissionInfo permissioInfo = authorizations.get(user);
    if ((permissioInfo != null) && permissioInfo.allowPublishTo(topic)) {
      return AuthorizationResult.OK;
    }

    // Return the appropriate error.
    return AuthorizationResult.PUBLISHING_NOT_ALLOWED;
  }

  public AuthorizationResult authorizeSubscribeTo(String user, String topicFilter) {
    /*
     * In a real case, the application would lookup the user authorizations on an external service
     * (or a local cache); in this demo we simply lookup the hard-coded map.
     */
    PermissionInfo permissionInfo = authorizations.get(user);
    if ((permissionInfo != null) && permissionInfo.allowSubscribeTo(topicFilter)) {
      return AuthorizationResult.OK;
    }

    return AuthorizationResult.SUBSCRIPTION_NOT_ALLOWED;
  }

  public Map<String, Map<String, AuthorizationResult>> getUserAuthorizations(String user) {
    /*
     * In a real case, the application would lookup the user authorizations on an external service
     * (or a local cache); in this demo we simply preload a map with the possible authorization
     * results from the hard-coded map.
     */
    PermissionInfo permissionInfos = authorizations.get(user);

    ConcurrentMap<String, AuthorizationResult> connectResults = allowedBrokers.stream()
        .collect(Collectors.toConcurrentMap(Function.identity(), brokerAddress -> {
          AuthorizationResult connectionResult =
              permissionInfos.allowConnectionTo(brokerAddress) ? AuthorizationResult.OK
                  : AuthorizationResult.BROKER_CONNECTION_NOT_ALLOWED;
          return connectionResult;
        }));

    Map<String, Map<String, AuthorizationResult>> results = new ConcurrentHashMap<>();
    results.put("connect", connectResults);

    Map<String, AuthorizationResult> subscribeResults = new ConcurrentHashMap<>();
    Map<String, AuthorizationResult> pulishResults = new ConcurrentHashMap<>();

    String topicPrefix = "topics/topic_";
    for (int i = 0; i < 30; i++) {
      String topic = topicPrefix + i;
      AuthorizationResult subscriptionResult =
          permissionInfos.allowSubscribeTo(topic) ? AuthorizationResult.OK
              : AuthorizationResult.SUBSCRIPTION_NOT_ALLOWED;
      subscribeResults.put(topic, subscriptionResult);

      AuthorizationResult publishResult =
          permissionInfos.allowSubscribeTo(topic) ? AuthorizationResult.OK
              : AuthorizationResult.PUBLISHING_NOT_ALLOWED;
      pulishResults.put(topic, publishResult);
    }

    results.put("subscribe", subscribeResults);
    results.put("publish", pulishResults);

    return results;
  }
}
