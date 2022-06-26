package io.busata.fourleftdiscord.autoposting.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
public class AutoPostTracking {

    @Id
    @GeneratedValue
    private UUID id;

    private String eventId;
    private String challengeId;
    private long entryCount;

    @Column(columnDefinition="text")
    private String memberList;

    @Column(columnDefinition="text")
    private String lastPostedMembers;

}
