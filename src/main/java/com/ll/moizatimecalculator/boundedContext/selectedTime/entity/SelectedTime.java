package com.ll.moizatimecalculator.boundedContext.selectedTime.entity;

import com.ll.moizatimecalculator.base.entity.BaseEntity;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class SelectedTime extends BaseEntity {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    @ManyToOne
    private EnterRoom enterRoom;

    public LocalTime getDuration() {
        return getEndTime()
                .minusHours(getStartTime().getHour())
                .minusMinutes(getStartTime().getMinute());
    }
}
