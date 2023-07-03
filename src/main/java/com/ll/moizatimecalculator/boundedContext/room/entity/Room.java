package com.ll.moizatimecalculator.boundedContext.room.entity;

import com.ll.moizatimecalculator.base.entity.BaseEntity;
import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode
public class Room extends BaseEntity {
    private String name;
    private String description;
    private LocalDate availableStartDay;
    private LocalDate availableEndDay;
    private LocalTime availableStartTime;
    private LocalTime availableEndTime;
    @Setter
    private LocalDateTime deadLine;
    private LocalTime meetingDuration;
    private String accessCode;
    @Setter
    private boolean mailSent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<EnterRoom> enterRoom = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "leaderId")
    private Member leader;
}
