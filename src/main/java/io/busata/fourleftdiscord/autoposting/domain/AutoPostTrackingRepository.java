package io.busata.fourleftdiscord.autoposting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AutoPostTrackingRepository extends JpaRepository<AutoPostTracking, UUID> {
    Optional<AutoPostTracking> findByEventIdAndChallengeId(String eventId, String challengeId);
}
