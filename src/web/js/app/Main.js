/*
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

// In this example we authenticate via JavaScript by sending an "Ajax" request
// to a WebServer that will answer with a session token (or refusing the
// request). As the WebServer is not actually deployed, authentication on the
// client will only be simulated (see the js/Authentication.js).
require(['MQTTExtender', 'app/Authentication', 'app/SessionHandler',
  'app/Constants'],
  function(MQTTExtender, Authentication, ConnectionHandler, Constants) {

    // The reference to the session opened against the NQTT Extender.
    var extenderSession = null;
    $('#login_form :submit').click(function(event) {
      // The user wants to authenticate.

      // In this case the auth is handled via JS so prevent the form from
      // submitting.
      event.preventDefault();

      // Disable the form while trying to authenticate.
      $('input').attr('disabled', false);

      // Trim input values.
      var user = $('#user').val().replace(Constants.TRIM_REGEXP, '$1');
      var password = $('#password').val().replace(Constants.TRIM_REGEXP, '$1');

      // Let's call the webserver to ask for an authentication token.
      // This demo we call a longin.js script which is an empty file to fake the
      // authentication on the client.
      $.ajax({
        url: 'js/app/login.js',
        type: 'POST',
        data: {
          user: user,
          password: password
        },

        error: function(obj, errorText) {
          jError('Authentication Failed: ' + errorText,
            Constants.J_NOTIFY_OPTIONS_ERR);
        },

        success: function() {
          // The token is expected to be sent by the WebServer; in this case the
          // token "generation" is simulated by the Authentication module.
          // Note that while the Authentication module will always return the
          // same token for a certain user, the WebServer would actually
          // generate a different token every time (or at least will refresh it
          // from time to time).
          var token = Authentication.getToken(user, password);

          if (token == null) {
            jError('Authentication Failed: wrong user/password',
              Constants.J_NOTIFY_OPTIONS_ERR);
          } else {
            // Now it is possible to connect to MQTT Extender, by sending the
            // token, not the password.
            MQTTExtender.connect(Constants.SERVER, user, token, {
              // Intercept potential errors on the MQTT Extender, e.g.: the
              // token expired while connecting.
              onConnectionFailure: function(errorType, responseObj) {
                var customMsg = responseObj ? JSON.stringify(responseObj) : '';
                jError('Connection to MQTT Extender refused: ' + errorType +
                  ' ' + customMsg, Constants.J_NOTIFY_OPTIONS_ERR);
              },

              onConnectionSuccess: function(mqttExtenderSession) {
                // Update the reference to the session, in order to be closed
                // later once requested.
                extenderSession = mqttExtenderSession;

                // Show application container.
                showApplication();

                // Create a new client instance.
                var mqttClient = mqttExtenderSession.createClient('mosquitto');

                // Start managing MQTT connection.
                ConnectionHandler.init(mqttClient);
              }

              /*onCoonectionFailure: function() {
                console.log("Error");
              },

              onLsClient: function(lsClient) {
                console.log("LS CLient set");
              }*/
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
      hideApplication(extenderSession);
    });

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
     * @param {MQTTExtenderSession} session - The open session.
     */
    function hideApplication(session) {
      $('#userListContainer').slideDown();
      $('#login_form').show();
      $('#application').slideUp();
      $('#logout').hide();

      // Close the MQTT Extender session.
      session.close();
    }
  });



