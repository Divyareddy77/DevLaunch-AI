package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a single private interview note entry on a job application.
 * <p>
 * Notes support multiple entries per application (e.g. what was asked in
 * each interview, what needs revision), each with its own creation
 * timestamp so the user can keep a running log.
 * </p>
 */
@Entity
@Table(name = "interview_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "application")
@EqualsAndHashCode(callSuper = true)
public class InterviewNote extends BaseEntity {

    /**
     * The job application this note belongs to.
     * <p>
     * An application may have many interview notes.
     * </p>
     */
    @NotNull(message = "Application is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    /**
     * The note content (e.g. "Asked Java Streams", "Need to revise
     * multithreading").
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Note content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

}
