package com.smart.rh.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.Arrays;

/**
 * MQTT subscriber infrastructure with TLS/SSL support for production
 * Loaded ONLY when {@code app.mqtt.enabled=true}.
 *
 * <p>Features:
 * <ul>
 *   <li>TLS 1.2+ encryption</li>
 *   <li>Per-device authentication (username/password)</li>
 *   <li>Automatic reconnection with exponential backoff</li>
 *   <li>QoS 1 (at-least-once delivery)</li>
 *   <li>Graceful error handling</li>
 * </ul>
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true")
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    private final MqttProperties props;
    private final MqttMessageRouter router;

    // ── Paho client factory with TLS ──────────────────────────────────────────

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();

        // Server configuration
        options.setServerURIs(new String[]{ props.brokerUrl() });
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setMaxInflight(100); // Allow 100 in-flight messages

        // Timeouts
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        // Authentication
        if (props.username() != null && !props.username().isBlank()) {
            options.setUserName(props.username());
        }
        if (props.password() != null && !props.password().isBlank()) {
            options.setPassword(props.password().toCharArray());
        }

        // TLS Configuration
        if (props.useTls()) {
            try {
                // Use system default SSL context (loaded from truststore)
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);

                options.setSocketFactory(sslContext.getSocketFactory());
                log.info("MQTT TLS/SSL enabled — TLS version: TLSv1.2");
            } catch (Exception e) {
                log.error("Failed to configure TLS for MQTT", e);
                throw new RuntimeException("MQTT TLS configuration failed", e);
            }
        }

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);

        log.info("MQTT client factory configured — broker={} tls={} qos={}",
                props.brokerUrl(), props.useTls(), props.qos());
        return factory;
    }

    // ── Spring Integration channel ────────────────────────────────────────────

    /**
     * Direct (synchronous, single-threaded) channel between the inbound
     * adapter and the service activator.
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // ── Inbound adapter (subscriber) ─────────────────────────────────────────

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter(
            MqttPahoClientFactory factory) {

        // Dynamic topic subscriptions from properties
        // Default pattern: smartrh/devices/+/+/+ plus legacy attendance topics
        String[] subscriptionTopics = props.topics();

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        props.clientId(), factory, subscriptionTopics);

        adapter.setCompletionTimeout(props.completionTimeout());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(new int[]{ props.qos() });
        adapter.setOutputChannel(mqttInputChannel());

        log.info("MQTT inbound adapter started — broker={} topics={} qos={}",
                props.brokerUrl(), Arrays.toString(subscriptionTopics), props.qos());
        return adapter;
    }

    // ── Service activator (message dispatcher) ───────────────────────────────

    /**
     * Routes incoming MQTT messages to the MqttMessageRouter
     * for device-specific processing
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return router::handle;
    }
}
