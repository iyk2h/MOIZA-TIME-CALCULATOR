package com.ll.moizatimecalculator.boundedContext.selectedTime.repository;

import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

@EnableCaching
public interface SelectedTimeRepository extends JpaRepository<SelectedTime, Long> {

    @Cacheable(value = "selectedTimeList", key = "{ #room.id, #date }")
    @Query("SELECT st FROM SelectedTime st JOIN FETCH st.enterRoom er JOIN FETCH er.room r WHERE r = ?1 AND st.date = ?2 ORDER BY st.startTime")
    List<SelectedTime> searchSelectedTimeByRoom(Room room, LocalDate date);
}
