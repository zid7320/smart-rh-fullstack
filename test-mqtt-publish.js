const mqtt = require('mqtt');

const client = mqtt.connect('mqtts://eb62037ac5254d2784d2c1078b1cb5ac.s1.eu.hivemq.cloud:8883', {
  username: 'smart-rh-backend',
  password: 'SmartRH@2025!',
  clientId: 'test-publisher',
  rejectUnauthorized: false
});

client.on('connect', () => {
  console.log('Connected to MQTT broker');
  
  // Publish temperature message
  const tempMsg = {
    sensorId: 1,
    temperature: 31.5,
    humidity: 72
  };
  
  client.publish('smartrh/devices/sensor/1/temperature', JSON.stringify(tempMsg), {qos: 1}, (err) => {
    if (err) {
      console.error('Publish error:', err);
    } else {
      console.log('Temperature message published:', tempMsg);
    }
    
    // Publish CO2 message
    const co2Msg = {
      sensorId: 1,
      co2Level: 450,
      gasConcentration: 0.045
    };
    
    client.publish('smartrh/devices/sensor/1/co2', JSON.stringify(co2Msg), {qos: 1}, (err) => {
      if (err) {
        console.error('Publish error:', err);
      } else {
        console.log('CO2 message published:', co2Msg);
      }
      
      // Publish occupancy message
      const occMsg = {
        sensorId: 1,
        isOccupied: true,
        motionDuration: 120,
        confidenceLevel: 0.95
      };
      
      client.publish('smartrh/devices/sensor/1/occupancy', JSON.stringify(occMsg), {qos: 1}, (err) => {
        if (err) {
          console.error('Publish error:', err);
        } else {
          console.log('Occupancy message published:', occMsg);
        }
        
        // Disconnect after all messages sent
        setTimeout(() => {
          client.end();
          console.log('Test messages sent. Disconnecting...');
          process.exit(0);
        }, 1000);
      });
    });
  });
});

client.on('error', (err) => {
  console.error('MQTT Error:', err);
  process.exit(1);
});
