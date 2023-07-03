package com.ll.moizatimecalculator.boundedContext.room.entity;

import com.ll.moizatimecalculator.base.entity.BaseEntity;
import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class EnterRoom extends BaseEntity {
    @ManyToOne
    private Room room;
    @ManyToOne
    private Member member;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "enterRoom", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<SelectedTime> selectedTimes = new ArrayList<>();
}
