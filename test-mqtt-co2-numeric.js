const mqtt = require('mqtt');

// HiveMQ Cloud configuration
const brokerUrl = 'ssl://eb62037ac5254d2784d2c1078b1cb5ac.s1.eu.hivemq.cloud:8883';
const username = 'smart-rh-backend';
const password = 'SmartRH@2025!';

const client = mqtt.connect(brokerUrl, {
  username,
  password,
  protocol: 'mqtts',
  rejectUnauthorized: false
});

client.on('connect', () => {
  console.log('✓ Connected to HiveMQ Cloud');

  // Publish CO2 message with numeric sensor ID (string "1")
  const topic = 'smartrh/devices/1/environment/co2';
  const payload = JSON.stringify({
    sensorId: '1',
    co2Level: 950,
    gasConcentration: 120,
    timestamp: new Date().toISOString()
  });

  client.publish(topic, payload, { qos: 1 }, (err) => {
    if (err) {
      console.error('✗ Publish error:', err);
    } else {
      console.log('✓ Published CO2 message:');
      console.log('  Topic:', topic);
      console.log('  Payload:', payload);
    }
    client.end();
  });
});

client.on('error', (err) => {
  console.error('✗ Connection error:', err.message);
  client.end();
});
