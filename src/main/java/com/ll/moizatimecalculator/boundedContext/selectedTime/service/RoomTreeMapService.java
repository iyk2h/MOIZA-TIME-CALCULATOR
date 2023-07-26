package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.room.service.RoomService;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.RoomTreeMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomTreeMapService {

    @Autowired
    RoomService roomService;

    @Autowired
    EnterRoomRepository enterRoomRepository;

    private final Map<Long, RoomTreeMap> roomDataMap = new ConcurrentHashMap<>();

    public RoomTreeMap getRoomTreeMap(Long roomId) {
        return roomDataMap.computeIfAbsent(roomId, key -> new RoomTreeMap());
    }

    public synchronized void setRoomTreeMap(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        RoomTreeMap roomTreeMap = getRoomTreeMap(roomId);
        roomTreeMap.setRoomTreeMapDateWithMember(localDateTime, member);
    }

    public void delete(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        RoomTreeMap roomTreeMap = getRoomTreeMap(roomId);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        roomTreeMap.deleteTreeMap(localDateTime, member);
    }

}
