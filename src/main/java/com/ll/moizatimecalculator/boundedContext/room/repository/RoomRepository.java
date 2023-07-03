package com.ll.moizatimecalculator.boundedContext.room.repository;


import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
