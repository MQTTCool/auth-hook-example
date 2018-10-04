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

var mqtt = require('mqtt');

// Connect to the MQTT broker listening at localhost on port 1883.
var urlBroker = process.argv[2];
if (!urlBroker) {
  console.warn('Please specify a valid URL broker');
  process.exit(1);
}

console.info('Connecting to ' + urlBroker + ' ...');
var client = mqtt.connect(urlBroker);

// Upon successful connection, start simulation.
client.on('connect', function() {
  console.info('Connected to ' + urlBroker);

  // Publish a random message every 500 ms.
  setInterval(function() {
    for (var i = 0; i < 30; i++) {
      // The topic to which publishing the message.
      var topic = 'topics/topic_' + i;

      // Prepare a random payload.
      var message = Math.random().toString(36).substring(2, 9);

      // Send the message.
      client.publish(topic, message, function() {
        console.info('Published message [' + message + '] to <' + topic + '>');
      });
    }
  }, 500);
});

client.on('offline', function() {
  console.warn('Client offline, exiting');
  process.exit(1);
});
