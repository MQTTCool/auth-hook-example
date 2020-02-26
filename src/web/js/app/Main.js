/*
 * MQTT.Cool - https://mqtt.cool
 *
 * Authentication and Authorization Demo
 *
 * Demo Copyright (c) Lightstreamer Srl Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */


/*
 * In this example we authenticate via JavaScript by sending an "Ajax" request
 * to a WebServer that will answer with a session token (or refusing the
 * request). As the WebServer is not actually deployed, authentication on the
 * client will only be simulated (see the Authentication function below).
 */

const Constants = {
  // Target MQTT.Cool address. Change it with if required.
  MQTT_COOL_URL: 'http://localhost:8080',

  // Topics configuration
  TOPIC_PREFIX: 'topics/topic_',
  NUMBER_OF_TOPICS: 30,

  //jNotify options
  J_NOTIFY_OPTIONS_ERR: {
    autoHide: true,
    TimeShown: 3000,
    clickOverlay: true,
    HorizontalPosition: 'center',
    VerticalPosition: 'center'
  },
  J_NOTIFY_OPTIONS_CONNECTION_ERR: {
    autoHide: true,
    TimeShown: 3500,
    clickOverlay: true,
    HorizontalPosition: 'center',
    VerticalPosition: 'center'
  },

  // Message shown in the topic rows.
  CLICK_TO_PUBLISH: 'click to publish',
  CLICK_TO_SUBSCRIBE: 'click to susbcribe'
};

// The MQTT client instance used to interact with the target MQTT broker.
var mqttClient;

$(function() {
  // The reference to the session opened against the MQTT.Cool server.
  var coolSession = null;

  // Instantiate the authentication simulator.
  var authentication = new Authentication();

  $('#login_form :submit').click(function(event) {
    // The user wants to authenticate.

    // In this case the authorization is handled via JS so prevent the form from
    // submitting.
    event.preventDefault();

    // Disable the form while trying to authenticate.
    $('input').attr('disabled', false);

    // Get credentials.
    var user = $('#user').val();
    var password = $('#password').val();

    // Let's call the WebServer to ask for an authentication token.
    // This demo we call a login.js script which is an empty file to fake the
    // authentication on the client.
    $.ajax({
      url: 'js/app/login.js',
      type: 'POST',
      data: {
        user: user,
        password: password
      },

      error: function(obj, errorText) {
        jError('Authentication Failed: ' + errorText, Constants.J_NOTIFY_OPTIONS_ERR);
      },

      success: function() {
        // The token is expected to be sent by the WebServer; in this case the
        // token "generation" is simulated by the Authentication function.
        // Note that while the Authentication function will always return the
        // same token for a certain user, the WebServer would actually
        // generate a different token every time (or at least will refresh it
        // from time to time).
        var token = authentication.getToken(user, password);

        if (token == null) {
          jError('Authentication Failed: wrong user/password', Constants.J_NOTIFY_OPTIONS_ERR);
        } else {
          // Now it is possible to connect to MQTT.Cool, by sending the
          // token, not the password.
          mqttcool.openSession(Constants.MQTT_COOL_URL, user, token, {
            // Intercept potential errors on the MQTT.Cool Server, e.g.: the
            // token expired while connecting.
            onConnectionFailure: function(errorType, responseObj) {
              var customMsg = responseObj ? JSON.stringify(responseObj) : '';
              jError('Connection to MQTT.Cool refused: ' + errorType +
                ' ' + customMsg, Constants.J_NOTIFY_OPTIONS_ERR);
            },

            onConnectionSuccess: function(mqttCoolSession) {
              // Update the reference to the session, in order to be closed
              // later once requested.
              coolSession = mqttCoolSession;

              // Show application container.
              showApplication();

              // Create a new client instance.
              mqttClient = mqttCoolSession.createClient('mosquitto');

              // Start managing MQTT connection.
              manageMQTTConnection();
            }
          });
        }
      },

      complete: function() {
        $('input').attr('disabled', false);
      }
    });

  });

  // Enable the login form.
  $('input').attr('disabled', false);

  // Setup the logout button.
  $('#logout').click(function() {
    hideApplication(coolSession);
  });
});

/**
 * Simulates an authentication request to the WebServer and wraps the
 * fake token got back.
 */
function Authentication() {
  // Here is the list of the user/password/token.
  // These info, excluding the password, are shared with the Hook.
  const users = {
    user1: {
      password: 'wow',
      token: 'ikgdfigdfhihdsih',
      canConnect: 'yes',
      canSubscribe: 'topics/topic_1, topics/topic_2, topics/topc_3',
      canPublish: 'topics/topic_4, topics/topic_5, topics/topc_6'
    },

    user2: {
      password: 'wow',
      token: 'slaoejkauekalkew',
      canConnect: 'no',
      canSubscribe: '',
      canPublish: ''
    },

    patient0: {
      password: 'suchpassword',
      token: 'imwrongtoken',
      permissions: 'the token of this user will result expired on the server'
    },

    leto: {
      password: 'sosecurity',
      token: 'powerfultoken',
      canConnect: 'yes',
      canSubscribe: 'all',
      canPublish: 'all'
    },

    gollum: {
      password: 'veryauth',
      token: 'toobadforyou',
      canConnect: 'yes',
      canSubscribe: 'none',
      canPublish: 'none'
    },

    lucky: {
      password: 'muchhappy',
      token: 'srsly',
      canConnect: 'yes',
      canSubscribe: 'none',
      canPublish: 'topics/topic_13, topics/topic_17'
    }
  };

  function userClicked(user) {
    return function() {
      $('#user').val(user);
      $('#password').val(users[user].password);
    };
  }

  // Show the list of available user/password pairs on the page: I would not do
  // that on a production site ;)
  for (var user in users) {
    $('#userlist').append(
      $('<tr class=\'button\'>')
        .append($('<td>').text(user))
        .append($('<td>').text(users[user].password))
        .append($('<td>').text(users[user].canConnect))
        .append($('<td>').text(users[user].canSubscribe))
        .append($('<td>').text(users[user].canPublish))
        .click(userClicked(user)));
  }

  this.getToken = function(user, password) {
    if (user in users) {
      if (users[user].password == password) {
        return users[user].token;
      }
    }
    return null;
  };
}

/**
 * Shows the application form and hides the login from.
 */
function showApplication() {
  $('#userListContainer').slideUp();
  $('#login_form').hide();
  $('#application').slideDown();
  $('#logout').show();
}

/**
 * Hide the application form, shows the login form and close the session.
 *
 * @param {MQTTCoolSession} session - The open session.
 */
function hideApplication(session) {
  $('#userListContainer').slideDown();
  $('#login_form').show();
  $('#application').slideUp();
  $('#logout').hide();

  // Close the MQTT.Cool session.
  session.close();
}

/**
 * Manages a new MQTT connection.
 */
function manageMQTTConnection() {
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
 * Simple utility function to retrieve the row id from the message destination.
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
 * Resets the contents the application container.
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
        .text(Constants.CLICK_TO_PUBLISH + ' to ' + topic)
        // Add click event handler
        .click(publishTo(topic))
    );

    // The id of the row for the subscription to the topic.
    var subscriptionRowId = 'subscription' + i;
    $('#subscriptionTopics').append(
      $('<div id="' + subscriptionRowId + '" class="itemrow button">')
        // Show instructions
        .text(Constants.CLICK_TO_SUBSCRIBE + ' to ' + topic)
        // Add click event handler
        .click(subscribeTo(subscriptionRowId, topic))
    );
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
    var message = new mqttcool.Message(payload);
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
      .css('background-color', 'red')    // Background to red.
      .text(responseObject.errorMessage) // Show the error message.
      .off('click');                     // Remove the event handler.
  }
}

