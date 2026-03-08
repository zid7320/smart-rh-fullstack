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

import java.util.Arrays;

/**
 * MQTT subscriber infrastructure — loaded ONLY when
 * {@code app.mqtt.enabled=true}.
 *
 * <p>When the property is absent or {@code false} none of these beans
 * are registered and no Paho connection is attempted, so the application
 * starts cleanly without a running broker.</p>
 *
 * <h3>Spring Integration wiring</h3>
 * <pre>
 *   Broker ──► MqttPahoMessageDrivenChannelAdapter
 *                       │
 *               mqttInputChannel (DirectChannel)
 *                       │
 *           @ServiceActivator ──► MqttMessageRouter#handle()
 * </pre>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true")
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    private final MqttProperties    props;
    private final MqttMessageRouter router;

    // ── Paho client factory ───────────────────────────────────────────────────

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{ props.brokerUrl() });
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        if (props.username() != null && !props.username().isBlank()) {
            options.setUserName(props.username());
        }
        if (props.password() != null && !props.password().isBlank()) {
            options.setPassword(props.password().toCharArray());
        }

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);

        log.info("MQTT client factory configured — broker={}", props.brokerUrl());
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

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        props.clientId(), factory, props.topics());

        adapter.setCompletionTimeout(props.completionTimeout());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(props.qos());
        adapter.setOutputChannel(mqttInputChannel());

        log.info("MQTT subscriber active — broker={} topics={}",
                props.brokerUrl(), Arrays.toString(props.topics()));
        return adapter;
    }

    // ── Service activator (message dispatcher) ───────────────────────────────

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return router::handle;
    }
}
