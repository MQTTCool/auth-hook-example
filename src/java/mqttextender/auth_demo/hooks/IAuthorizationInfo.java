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

/**
 * Simple interface which exposes queries to check authorized resources.
 *
 */
public interface IAuthorizationInfo {

    /**
     * Asks if connection to the specified MQTT broker address is allowed.
     * 
     * @param broker
     *            The MQTT broker address to connect to.
     * @return {@code true} if connection to the specified MQTT broker address is allowed,
     *         {@code false} otherwise.
     */
    boolean allowConnectionTo(String broker);

    /**
     * Asks if publishing to the specified topic is allowed.
     * 
     * @param topic
     *            The topic to which publishing a MQTT message.
     * @return {@code true} if publishing to the specified topic is allowed, {@code false}
     *         otherwise.
     */
    boolean allowPublishTo(String topic);

    /**
     * Asks if subscription to the specified topic is allowed.
     * 
     * @param topic
     *            The topic to subscribe to.
     * @return {@code true} if subscription to the specified topic is allowed, {@code false}
     *         otherwise.
     */
    boolean allowSubscribeTo(String topic);

}