/*
  MQTT.Cool - http://www.lightstreamer.com
  Authentication and Authorization Demo

  Copyright (c) Lightstreamer Srl

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

define(function() {
  var protocolToUse = document.location.protocol != 'file:' ?
    document.location.protocol : 'http:';

  // Target MQTT.Cool address. Change it with if required.
  var SERVER_ADDRESS = 'localhost:8080';
  return {
    SERVER: protocolToUse + '//' + SERVER_ADDRESS,
    TOPIC_PREFIX: 'topics/topic_',
    NUMBER_OF_TOPICS: 30,
    J_NOTIFY_OPTIONS_ERR: {
      autoHide: true, // added in v2.0
      TimeShown: 3000,
      clickOverlay: true,
      HorizontalPosition: 'center',
      VerticalPosition: 'center'
    },
    J_NOTIFY_OPTIONS_CONNECTION_ERR: {
      autoHide: true, // added in v2.0
      TimeShown: 3500,
      clickOverlay: true,
      HorizontalPosition: 'center',
      VerticalPosition: 'center'
    },
    TRIM_REGEXP: new RegExp('^\\s*([\\s\\S]*?)\\s*$')
  };

});