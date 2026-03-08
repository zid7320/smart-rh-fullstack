package com.smart.rh.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Routes inbound MQTT messages to the appropriate processing pipeline.
 *
 * <p>Only instantiated when {@code app.mqtt.enabled=true}.</p>
 *
 * <h3>Topic contract</h3>
 * <table border="1" cellpadding="4">
 *   <tr><th>Topic</th><th>Payload</th><th>Action</th></tr>
 *   <tr>
 *     <td>{@code smartrh/attendance/recognition}</td>
 *     <td>JSON matching {@link AttendanceRequest}</td>
 *     <td>Logged + forwarded to {@link AttendanceService#recognize}
 *         (when {@code app.mqtt.forward-recognition=true})</td>
 *   </tr>
 *   <tr>
 *     <td>{@code smartrh/attendance/raw}</td>
 *     <td>Binary / large base64 image</td>
 *     <td>Logged (payload length only — never decoded)</td>
 *   </tr>
 *   <tr>
 *     <td>{@code smartrh/system/heartbeat}</td>
 *     <td>Any string / JSON</td>
 *     <td>Logged</td>
 *   </tr>
 *   <tr>
 *     <td>Any other topic</td>
 *     <td>Any</td>
 *     <td>Logged at DEBUG level</td>
 *   </tr>
 * </table>
 *
 * <h3>Expected recognition payload</h3>
 * <pre>{@code
 * {
 *   "employeId"  : 42,
 *   "type"       : "IN",
 *   "clockedAt"  : "2025-06-01T08:00:00Z",   // optional
 *   "confidence" : 0.97,
 *   "cameraId"   : "cam-01",
 *   "siteId"     : "site-A"
 * }
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true")
public class MqttMessageRouter {

    private static final String RECOGNITION_SUFFIX = "attendance/recognition";
    private static final String RAW_SUFFIX         = "attendance/raw";
    private static final String HEARTBEAT_SUFFIX   = "system/heartbeat";

    private final MqttProperties    props;
    private final ObjectMapper      objectMapper;
    private final AttendanceService attendanceService;

    // ── Entry point (called by @ServiceActivator in MqttConfig) ──────────────

    public void handle(Message<?> message) {
        String topic   = extractTopic(message);
        String payload = message.getPayload().toString();

        if (topic == null) {
            log.warn("MQTT message received with no topic header — dropping. payload={}", payload);
            return;
        }

        if (topic.endsWith(RECOGNITION_SUFFIX)) {
            log.info("MQTT ← [recognition] topic={} payload={}", topic, payload);
            handleRecognition(topic, payload);

        } else if (topic.endsWith(RAW_SUFFIX)) {
            // Payload can be large binary data — log length, not content
            log.info("MQTT ← [raw] topic={} payloadLength={} bytes", topic, payload.length());

        } else if (topic.endsWith(HEARTBEAT_SUFFIX)) {
            log.info("MQTT ← [heartbeat] topic={} payload={}", topic, payload);

        } else {
            log.debug("MQTT ← [unrecognized] topic={} payload={}", topic, payload);
        }
    }

    // ── Recognition forwarding ───────────────────────────────────────────────

    private void handleRecognition(String topic, String payload) {
        if (!props.forwardRecognition()) {
            log.debug("Recognition forwarding disabled (app.mqtt.forward-recognition=false) — logged only");
            return;
        }
        try {
            AttendanceRequest req = objectMapper.readValue(payload, AttendanceRequest.class);
            attendanceService.recognize(req);
            log.debug("MQTT recognition forwarded to AttendanceService [topic={}]", topic);
        } catch (Exception ex) {
            // Never let an individual bad message crash the subscriber loop
            log.warn("MQTT recognition forwarding failed [topic={}]: {} — raw payload: {}",
                    topic, ex.getMessage(), payload);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private String extractTopic(Message<?> message) {
        Object header = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        return header != null ? header.toString() : null;
    }
}
