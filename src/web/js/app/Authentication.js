/*
  MQTT.cool - http://www.lightstreamer.com
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
  // Here is the list of the user/password/token.
  // These info, excluding the password, are shared with the Hook.
  var users = {
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

  return {
    getToken: function(user, password) {
      if (user in users) {
        if (users[user].password == password) {
          return users[user].token;
        }
      }
      return null;
    }
  };
});

