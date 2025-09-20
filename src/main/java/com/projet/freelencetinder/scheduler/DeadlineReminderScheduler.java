package com.projet.freelencetinder.scheduler;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Notification.*;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.servcie.NotificationService;

@Component
public class DeadlineReminderScheduler {

    private final MissionRepository   missionRepo;
    private final NotificationService notificationService;

    public DeadlineReminderScheduler(MissionRepository missionRepo,
                                     NotificationService notificationService) {
        this.missionRepo         = missionRepo;
        this.notificationService = notificationService;
    }

    /**
     * Tous les jours à 9 h, avertit les freelances dont la deadline est dans 48 h.
     */
    @Scheduled(cron = "0 0 9 * * *")  // 09:00 chaque jour
    public void remindDeadlines() {

        LocalDate target = LocalDate.now().plusDays(2);

        missionRepo.findByStatutAndFreelanceSelectionneIsNotNull(Statut.EN_COURS)
                   .stream()
                   .filter(m -> target.equals(m.getDelaiLivraison()))
                   .forEach(m -> notificationService.push(
                           NotificationType.DEADLINE_PROCHE,
                           m.getFreelanceSelectionne().getId(),     // destinataire
                           null,                                    // système
                           m.getId(),
                           "Deadline dans 48 h",
                           "La mission « " + m.getTitre() + " » arrive bientôt à échéance.",
                           Priority.HIGH, Channel.WEB,
                           Map.of("missionId", m.getId()),
                           m.getDelaiLivraison().atStartOfDay()));
    }
}
