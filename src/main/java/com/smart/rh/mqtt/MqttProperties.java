package com.smart.rh.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Binds to {@code app.mqtt.*} properties.
 *
 * <p>
 * Set {@code app.mqtt.enabled=true} (or env {@code APP_MQTT_ENABLED=true})
 * to activate the entire MQTT subscriber module. When {@code enabled=false}
 * (the default) no Paho client is created and no broker connection is
 * attempted.
 * </p>
 *
 * <h3>Typical dev override</h3>
 * 
 * <pre>
 *   app.mqtt.enabled=true
 *   app.mqtt.broker-url=tcp://localhost:1883
 * </pre>
 *
 * <h3>Docker override (docker-compose)</h3>
 * 
 * <pre>
 *   APP_MQTT_ENABLED=true
 *   APP_MQTT_BROKER_URL=tcp://mqtt:1883
 * </pre>
 */
@ConfigurationProperties(prefix = "app.mqtt")
public record MqttProperties(

                /** Master switch — module is completely inert when false (default). */
                @DefaultValue("false") boolean enabled,

                /** Broker URL; supports {@code tcp://}, {@code ssl://}, {@code ws://}. */
                @DefaultValue("tcp://localhost:1883") String brokerUrl,

                /** MQTT client identifier — must be unique per broker connection. */
                @DefaultValue("smart-rh-backend") String clientId,

                /** Broker username (leave blank for anonymous). */
                String username,

                /** Broker password (leave blank for anonymous). */
                String password,

                /** Enable TLS/SSL encryption (use ssl:// scheme in brokerUrl when true). */
                @DefaultValue("false") boolean useTls,

                /**
                 * Comma-separated topics to subscribe to at startup.
                 * <ul>
                 * <li>{@code smartrh/attendance/recognition} — AI face-recognition events</li>
                 * <li>{@code smartrh/attendance/raw} — raw camera frames (logged only)</li>
                 * <li>{@code smartrh/system/heartbeat} — IoT device heartbeats</li>
                 * </ul>
                 */
                @DefaultValue("smartrh/attendance/recognition,smartrh/attendance/raw,smartrh/system/heartbeat") String[] topics,

                /**
                 * QoS level for all subscriptions: 0 at-most-once, 1 at-least-once, 2
                 * exactly-once.
                 */
                @DefaultValue("1") int qos,

                /** Milliseconds to wait for Paho operations to complete. */
                @DefaultValue("5000") long completionTimeout,

                /**
                 * When {@code true}, messages on the recognition topic are deserialized
                 * and forwarded to {@code AttendanceService#recognize}.
                 * When {@code false}, all messages are logged only.
                 */
                @DefaultValue("true") boolean forwardRecognition) {
}
