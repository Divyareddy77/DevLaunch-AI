package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateStudyPlannerRequest;
import com.devlaunch.dto.request.UpdateStudyPlannerRequest;
import com.devlaunch.dto.response.StudyPlannerResponse;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.StudyPlannerMapper;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.NotificationService;
import com.devlaunch.service.interfaces.StudyPlannerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StudyPlannerService} providing study planner
 * creation, retrieval, update, and deletion operations for the currently
 * authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link StudyPlannerRepository} and {@link StudyPlannerMapper}
 * respectively. A user may have many study planner entries. Update and
 * delete operations verify that the entry belongs to the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class StudyPlannerServiceImpl implements StudyPlannerService {

    private final StudyPlannerRepository studyPlannerRepository;
    private final UserRepository userRepository;
    private final StudyPlannerMapper studyPlannerMapper;
    private final NotificationService notificationService;

    /**
     * Constructs the study planner service with the required dependencies.
     *
     * @param studyPlannerRepository repository for study planner data access
     * @param userRepository         repository for user data access
     * @param studyPlannerMapper     mapper for DTO-entity conversions
     * @param notificationService    service for creating user notifications
     */
    public StudyPlannerServiceImpl(final StudyPlannerRepository studyPlannerRepository,
                                   final UserRepository userRepository,
                                   final StudyPlannerMapper studyPlannerMapper,
                                   final NotificationService notificationService) {
        this.studyPlannerRepository = studyPlannerRepository;
        this.userRepository = userRepository;
        this.studyPlannerMapper = studyPlannerMapper;
        this.notificationService = notificationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StudyPlannerResponse createStudyPlanner(final CreateStudyPlannerRequest request) {
        final User user = getAuthenticatedUser();

        // Map request DTO to a new StudyPlanner entity
        final StudyPlanner studyPlanner = studyPlannerMapper.toStudyPlanner(request);

        // Associate the study planner entry with the authenticated user
        studyPlanner.setUser(user);

        // Persist the new study planner entry
        final StudyPlanner savedStudyPlanner = studyPlannerRepository.save(studyPlanner);

        // Return the study planner data
        return studyPlannerMapper.toStudyPlannerResponse(savedStudyPlanner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<StudyPlannerResponse> getAllStudyPlanners() {
        final User user = getAuthenticatedUser();
        final List<StudyPlanner> studyPlanners = studyPlannerRepository.findByUser(user);
        return studyPlanners.stream()
                .map(studyPlannerMapper::toStudyPlannerResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public StudyPlannerResponse getStudyPlannerById(final Long id) {
        final StudyPlanner studyPlanner = getStudyPlannerOwnedByAuthenticatedUser(id);
        return studyPlannerMapper.toStudyPlannerResponse(studyPlanner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StudyPlannerResponse updateStudyPlanner(final Long id,
                                                   final UpdateStudyPlannerRequest request) {
        final StudyPlanner studyPlanner = getStudyPlannerOwnedByAuthenticatedUser(id);

        final StudyStatus previousStatus = studyPlanner.getStatus();

        // Update the editable fields
        studyPlanner.setTitle(request.getTitle());
        studyPlanner.setDescription(request.getDescription());
        studyPlanner.setStudyDate(request.getStudyDate());
        studyPlanner.setStartTime(request.getStartTime());
        studyPlanner.setEndTime(request.getEndTime());
        studyPlanner.setPriority(request.getPriority());
        studyPlanner.setStatus(request.getStatus());

        // Persist the updated study planner entry
        final StudyPlanner savedStudyPlanner = studyPlannerRepository.save(studyPlanner);

        // Notify the user of study milestones when a task is newly completed.
        // The completion date is passed explicitly (today) so the milestone
        // counts never depend on when the audit timestamp flushes.
        final StudyStatus newStatus = studyPlanner.getStatus();
        if (StudyStatus.COMPLETED.equals(newStatus) && !StudyStatus.COMPLETED.equals(previousStatus)) {
            notifyCompletionMilestones(studyPlanner.getUser(), studyPlanner.getTitle(),
                    LocalDate.now());
        }

        // Return the updated study planner data
        return studyPlannerMapper.toStudyPlannerResponse(savedStudyPlanner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteStudyPlanner(final Long id) {
        final StudyPlanner studyPlanner = getStudyPlannerOwnedByAuthenticatedUser(id);
        studyPlannerRepository.delete(studyPlanner);
    }

    /**
     * Creates study milestone notifications for a newly completed task:
     * the daily goal, a weekly target when a multiple of five completion
     * days are reached in the current week, and a study streak milestone
     * at three days and then every seven days.
     * <p>
     * The task completed just now is passed as an explicit completion date
     * so the milestone counts never depend on the audit-timestamp flush.
     * </p>
     *
     * @param user         the user to notify
     * @param taskTitle    the title of the completed task
     * @param completedOn  the date the task was completed (today)
     */
    private void notifyCompletionMilestones(final User user, final String taskTitle,
                                            final LocalDate completedOn) {
        notificationService.createNotification(user, NotificationType.STUDY,
                "Daily goal completed",
                "Task \"" + taskTitle + "\" was marked as completed. Keep up the momentum!");

        final List<StudyPlanner> completedTasks = studyPlannerRepository.findByUser(user).stream()
                .filter(task -> StudyStatus.COMPLETED.equals(task.getStatus()))
                .toList();

        // Distinct completion days across all completed tasks, with the task
        // completed just now counted on its explicit completion date.
        final Set<LocalDate> completionDays = completedTasks.stream()
                .map(this::completionDate)
                .collect(Collectors.toSet());
        completionDays.add(completedOn);

        // Weekly target: 5, 10, 15, … completion days in the current week
        final LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        final long weeklyDays = completionDays.stream()
                .filter(day -> !day.isBefore(weekStart))
                .count();
        if (weeklyDays >= 5 && weeklyDays % 5 == 0) {
            notificationService.createNotification(user, NotificationType.STUDY,
                    "Weekly target achieved",
                    "You completed tasks on " + weeklyDays
                            + " days this week — weekly target reached!");
        }

        // Streak milestone: 3 days, then every 7 days (3, 7, 14, 21, …)
        final int streak = calculateStreak(completionDays);
        if (streak >= 3 && (streak == 3 || streak % 7 == 0)) {
            notificationService.createNotification(user, NotificationType.STUDY,
                    "Study streak milestone",
                    streak + "-day study streak! You're on fire — keep it going.");
        }
    }

    /**
     * Computes the current consecutive-day completion streak from the set
     * of completion days.
     * <p>
     * A streak continues today, or from yesterday if today has no
     * completion yet, and counts backwards over consecutive days.
     * </p>
     *
     * @param completionDays the days on which the user completed tasks
     * @return the length of the current streak in days
     */
    private int calculateStreak(final Set<LocalDate> completionDays) {
        LocalDate cursor = LocalDate.now();
        if (!completionDays.contains(cursor)) {
            cursor = cursor.minusDays(1);
            if (!completionDays.contains(cursor)) {
                return 0;
            }
        }

        int streak = 0;
        while (completionDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * Resolves the date on which a task was completed, preferring the
     * auditing timestamp and falling back to the scheduled study date.
     *
     * @param task the completed task
     * @return the completion date
     */
    private LocalDate completionDate(final StudyPlanner task) {
        if (task.getUpdatedAt() != null) {
            return task.getUpdatedAt().toLocalDate();
        }
        if (task.getStudyDate() != null) {
            return task.getStudyDate();
        }
        return LocalDate.now();
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

    /**
     * Retrieves a study planner entry by ID and verifies it belongs to the currently
     * authenticated user.
     * <p>
     * Fetches the authenticated user first, then looks up the study planner entry by ID.
     * Throws a {@link ResourceNotFoundException} if the study planner entry does not exist
     * or if it belongs to a different user.
     * </p>
     *
     * @param id the study planner entry ID to retrieve
     * @return the {@link StudyPlanner} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the study planner entry is not found or does not
     *                                   belong to the authenticated user
     */
    private StudyPlanner getStudyPlannerOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final StudyPlanner studyPlanner = studyPlannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study planner with id " + id + " not found"));

        // Verify ownership: the study planner entry must belong to the authenticated user
        if (!studyPlanner.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Study planner with id " + id + " not found for the authenticated user");
        }

        return studyPlanner;
    }

}
