#!/usr/bin/env node

const mqtt = require('mqtt');

// Connect to HiveMQ Cloud
const client = mqtt.connect('ssl://eb62037ac5254d2784d2c1078b1cb5ac.s1.eu.hivemq.cloud:8883', {
  username: 'smart-rh-backend',
  password: 'SmartRH@2025!',
  protocol: 'mqtts'
});

client.on('connect', () => {
  console.log('✓ Connected to HiveMQ Cloud');

  // Publish CO2 reading using DEVICE ID (not numeric sensor ID)
  const co2Payload = JSON.stringify({
    sensorId: 'sensor-co2-01',  // Device ID, not numeric ID
    co2Level: 900,
    gasConcentration: 111,
    timestamp: new Date().toISOString()
  });

  client.publish('smartrh/devices/sensor-co2-01/environment/co2', co2Payload, {qos: 1}, (err) => {
    if (err) {
      console.error('✗ Publish error:', err);
    } else {
      console.log('✓ Published CO2 message:');
      console.log('  Topic: smartrh/devices/sensor-co2-01/environment/co2');
      console.log('  Payload:', co2Payload);
    }
    client.end();
  });
});

client.on('error', (err) => {
  console.error('✗ Connection error:', err);
  process.exit(1);
});
