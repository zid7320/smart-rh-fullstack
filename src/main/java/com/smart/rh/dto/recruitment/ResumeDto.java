package com.smart.rh.dto.recruitment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDto {

    private Long id;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    private String phone;

    private String skills;

    private String experience;

    private String education;

    private String languages;

    @JsonProperty("sender_email")
    private String senderEmail;

    @JsonProperty("email_subject")
    private String emailSubject;

    @JsonProperty("received_at")
    private LocalDateTime receivedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
