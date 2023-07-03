package com.ll.moizatimecalculator.boundedContext.selectedTime.repository;

import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SelectedTimeRepository extends JpaRepository<SelectedTime, Long> {

    @Query(" select st from SelectedTime as st where st.enterRoom.room = ?1 and st.date = ?2 order by st.startTime")
    List<SelectedTime> searchSelectedTimeByRoom(Room room, LocalDate date);

    List<SelectedTime> findAllByEnterRoom(EnterRoom enterRoom);
}
