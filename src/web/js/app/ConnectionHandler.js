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

define(['mqttcool/Message', './Constants'],
  function(Message, Constants) {
    // The MQTT client instance used to interact with the target MQTT broker.
    var mqttClient;

    // Message shown in the topic rows.
    var CLICK_TO_PUBLISH = 'click to publish';
    var CLICK_TO_SUBSCRIBE = 'click to susbcribe';

    /**
     * Simple utility function to retrieve the row id from the message
     * destination.
     *
     * @param {Message} message - The message instance (arrived or delivered)
     *  from which to retrieve the row id.
     * @returns {string} The row id.
     */
    function getRow(message) {
      var topic = message.destinationName;
      var whichRow = topic.substring(topic.lastIndexOf('_') + 1);
      return whichRow;
    }

    /**
     * Callback invoked upon message delivery.
     *
     * @param {Message} message - The delivered message.
     */
    function onMessageDelivered(message) {
      var id = '#publish' + getRow(message);
      $(id)
        .css('background-color', 'yellow')
        .text('Published [' + message.payloadString + '], click to publish ' +
        'again to ' + message.destinationName);
    }

    /**
     * Callback invoked upon message arriving.
     *
     * @param {Message} message - The arrived message.
     */
    function onMessageArrived(message) {
      var id = '#subscription' + getRow(message);
      $(id).text(message.destinationName + ':' + message.payloadString);
      if (message.payloadString.indexOf('ByClient') != -1) {
        $(id).css('background-color', 'orange');
      } else {
        $(id).css('background-color', 'yellow');
      }
    }

    /**
     * Callback invoked upon message not authorized for publishing.
     *
     * @param {!Message} message - The message not authorized for publishing.
     * @param {Object=} responseObject - The object with details about
     *   authorization failure.
     */
    function onMessageNotAuthorized(message, responseObject) {
      // MQTT.Cool refused publishing: probably this user is not enabled to
      // publish to this topic.
      if (responseObject) {
        // Show the custom error message.
        var topic = message.destinationName;
        var whichTopic = topic.substring(topic.length - 1);
        var id = '#publish' + whichTopic;
        $(id)
          .css('background-color', 'red')    // Background to red
          .text(responseObject.errorMessage) // Show the error message
          .off('click');                     // Remove the event handler
      }
    }

    /**
     * Callback invoked upon subscription not authorized.
     *
     * @param {string} id - The row id of not authorized topic.
     * @param {Object=} responseObject - The object with details about
     *   authorization failure.
     */
    function onSubscriptionNotAuthorized(id, responseObject) {
      // MQTT.Cool refused subscription: probably this user is not
      // enabled to subscribe to this topic filter.
      if (responseObject) {
        // Show the custom error message.
        $('#' + id)
          .css('background-color', 'red')    // Background to red
          .text(responseObject.errorMessage) // Show the error message
          .off('click');                     // Remove the event handler
      }
    }

    /**
     * Callback invoked upon connection lost.
     *
     * @param {any} responseObj - The object with details about connection lost.
     */
    function onConnectionLost(responseObj) {
      // Do not show message error in case of explicit session close (12) or
      // client disconnection (0);
      switch (responseObj.errorCode) {
        case 0:
        case 12:
          break;

        case 10:
        case 11:
          jError(responseObj.errorMessage,
            Constants.J_NOTIFY_OPTIONS_CONNECTION_ERR);
          resetGrids();
          break;

      }
    }

    /**
     * Creates a callback to be provided to jQuery as event handler when the
     * user clicks to connect to the MQTT broker.
     *
     * @returns {function} A callback called upon user click.
     */
    function connect() {
      return function() {
        mqttClient.connect({
          onSuccess: function() {
            $('#connect')
              .off('click') // Avoid to dispatch more than one event next time
              .hide();
            $('#publish').slideDown();
            $('#subscribe').slideDown();
          },

          onNotAuthorized: function(responseObj) {
            // MQTT.Cool refused connection: probably this user is not
            // enabled to connect to the target MQTT broker.
            var message = responseObj ? JSON.stringify(responseObj) : '';
            jError('Authorization to MQTT broker failed:' + message,
              Constants.J_NOTIFY_OPTIONS_CONNECTION_ERR);
          },

          onFailure: function(responseObj) {
            jError(JSON.stringify(responseObj),
              Constants.J_NOTIFY_OPTIONS_CONNECTION_ERR);
          }
        });
      };
    }

    /**
     * Creates a callback to be provided to jQuery as event handler when the
     * user clicks on a row to publish a message.
     *
     * @param {any} topic - The topic to which the message is to be published.
     * @returns {function} A callback called upon user click.
     */
    function publishTo(topic) {
      return function() {
        // Prepare a random payload.
        var payload = 'ByClient_' + Math.random().toString(36).substring(2, 9);

        // Prepare the Message instance, specifying the destination
        // (QoS 0 by default).
        var message = new Message(payload);
        message.destinationName = topic;

        // Send the message.
        mqttClient.send(message);
      };
    }

    /**
     * Creates a callback to be provided to jQuery as event handler when the
     * user clicks on a row to subscribe to a topic filter.
     *
     * @param {string} id - The row id
     * @param {string} topicFilter - The topic filter to subscribe to.
     * @returns {function} A callback called upon user click.
     */
    function subscribeTo(id, topicFilter) {
      return function() {
        // Tell the user the selected subscription is being requested.
        $('#' + id).text('Subscribing to ' + topicFilter);
        mqttClient.subscribe(topicFilter, {
          onSuccess: function(responseObject) {
            $('#' + id)
              .css('background-color', 'yellow')
              .text('Start receiving message from "' + topicFilter + '"');
          },

          onFailure: function(responseObject) {
            jError('Subscription to "' + topicFitler + '" Failed: ',
              Constants.J_NOTIFY_OPTIONS_ERR);
          },

          onNotAuthorized: function(responseObject) {
            onSubscriptionNotAuthorized(id, responseObject);
          }
        });
      };
    }

    /**
     * Reset the contents the application container.
     */
    function resetGrids() {
      $('#connect')
        .click(connect()) // Add client event handler
        .show();          // Show the link

      // Hide and clear subpanels.
      $('#publish').hide();
      $('#subscribe').hide();
      $('#publishingTopics').empty();
      $('#subscriptionTopics').empty();

      // Generate Constants.NUMBER_OF_TOPICS rows to be clicked by the user,
      // for subscribing and publishing to the selected topic.
      for (var i = 1; i <= Constants.NUMBER_OF_TOPICS; i++) {
        // Create progressive topic names.
        var topic = Constants.TOPIC_PREFIX + i;

        // The id of the row for the publishing to the topic.
        var publishRowId = 'publish' + i;
        $('#publishingTopics').append(
          $('<div id="' + publishRowId + '" class="itemrow button">')
            // Show instructions
            .text(CLICK_TO_PUBLISH + ' to ' + topic)
            // Add click event handler
            .click(publishTo(topic))
        );

        // The id of the row for the subscription to the topic.
        var subscriptionRowId = 'subscription' + i;
        $('#subscriptionTopics').append(
          $('<div id="' + subscriptionRowId + '" class="itemrow button">')
            // Show instructions
            .text(CLICK_TO_SUBSCRIBE + ' to ' + topic)
            // Add click event handler
            .click(subscribeTo(subscriptionRowId, topic))
        );
      }
    }

    return {
      init: function(client) {
        // Initialize the MQTT client reference
        mqttClient = client;

        // Set the callback for handling connection interruptions.
        mqttClient.onConnectionLost = onConnectionLost;

        // Set the callback function for handling events related to delivery
        // and arriving of messages.
        mqttClient.onMessageDelivered = onMessageDelivered;
        mqttClient.onMessageArrived = onMessageArrived;
        mqttClient.onMessageNotAuthorized = onMessageNotAuthorized;

        // Reset the application grids.
        resetGrids();
      }
    };
  });