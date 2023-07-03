package com.ll.moizatimecalculator.boundedContext.selectedTime.repository;

import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SelectedTimeRepository extends JpaRepository<SelectedTime, Long> {

    @Query("SELECT st FROM SelectedTime st JOIN FETCH st.enterRoom er JOIN FETCH er.room r WHERE r = ?1 AND st.date = ?2 ORDER BY st.startTime")
    List<SelectedTime> searchSelectedTimeByRoom(Room room, LocalDate date);
}
