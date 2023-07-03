package com.ll.moizatimecalculator.boundedContext.selectedTime.service;
import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class TimeRangeWithMember implements Comparable<TimeRangeWithMember> {

    LocalDate date;
    LocalTime start;
    LocalTime end;
    List<Member> participationMembers;
    List<Member> nonParticipationMembers;

    @Override
    public int compareTo(TimeRangeWithMember o1) {
        if (o1.participationMembers.size() == participationMembers.size()) {
            if (o1.date.isEqual(date)) {
                return start.compareTo(o1.start);
            }
            return date.compareTo(o1.date);
        }
        return o1.participationMembers.size() - participationMembers.size();
    }
}