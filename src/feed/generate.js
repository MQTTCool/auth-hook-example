var mqtt = require('mqtt');

// Connect to the MQTT broker listening at localhost on port 1883.
var client = mqtt.connect('mqtt://localhost:1883');

// Upon successul connection, start simulation.
client.on('connect', function() {

  // Publish a random message every 500 ms.
  setInterval(function() {
    for (var i = 0; i < 30; i++) {
      // The topic to which publishing the message.
      var topic = 'topics/topic_' + i;

      // Prepare a random payload.
      var message = Math.random().toString(36).substring(2,9);

      // Send the message.
      client.publish(topic, message);
    }
  }, 500);
});
