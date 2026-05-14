package com.smart.rh.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@ToString
public class Resume extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "skills", columnDefinition = "LONGTEXT")
    private String skills;

    @Column(name = "experience", columnDefinition = "LONGTEXT")
    private String experience;

    @Column(name = "education", columnDefinition = "LONGTEXT")
    private String education;

    @Column(name = "languages", length = 500)
    private String languages;

    @Column(name = "sender_email", length = 255)
    private String senderEmail;

    @Column(name = "email_subject", length = 500)
    private String emailSubject;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;
}
