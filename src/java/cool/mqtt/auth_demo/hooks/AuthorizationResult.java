/*
 * MQTT.Cool - http://www.lightstreamer.com
 * Authentication and Authorization Demo
 *
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cool.mqtt.auth_demo.hooks;

/**
 * Specifies whether access to the a requested resource is authorized.
 */
enum AuthorizationResult {

    /**
     * Result in case access to the requested resource is authorized.
     */
    OK,

    /**
     * Result in case of invalid token.
     */
    INVALID_TOKEN(1),

    /**
     * Result in case connection to supplied MQTT broker address is not authorized.
     */
    BROKER_CONNECTION_NOT_ALLOWED(2),

    /**
     * Result in case publishing to the requested topic is not authorized.
     */
    PUBLISHING_NOT_ALLOWED(3),

    /**
     * Result in case subscription to the requested topic is not authorized.
     */
    SUBSCRIPTION_NOT_ALLOWED(4);

    /** Code sent to the client to react appropriately on the user interface */
    private int code;

    AuthorizationResult(int code) {
        this.code = code;
    }

    AuthorizationResult() {
        this(0);
    }

    /**
     * Returns the code of this result.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }
}
