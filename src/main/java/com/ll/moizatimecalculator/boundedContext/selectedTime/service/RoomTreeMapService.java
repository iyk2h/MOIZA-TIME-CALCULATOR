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
import java.util.Set;
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

    public void setRoomTreeMap(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        RoomTreeMap roomTreeMap = getRoomTreeMap(roomId);
        roomTreeMap.setRoomTreeMapDateWithMember(localDateTime, member);
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(Long roomId) {
        RoomTreeMap roomTreeMap = getRoomTreeMap(roomId);

        List<Entry<LocalDateTime, Set<Member>>> entries = getSortedEntries(roomTreeMap);

        return getFindTOP10(roomId, entries);
    }

    public void delete(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        RoomTreeMap roomTreeMap = getRoomTreeMap(roomId);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        roomTreeMap.deleteTreeMap(localDateTime, member);
    }

    private List<TimeRangeWithMember> getFindTOP10(Long roomId,
            List<Entry<LocalDateTime, Set<Member>>> entries) {
        List<TimeRangeWithMember> list = new ArrayList<>();
        Room room = roomService.getRoom(roomId);
        List<Member> members = enterRoomRepository.findMembersByRoom(room);

        for (Entry<LocalDateTime, Set<Member>> entry : entries) {
            List<Member> contain = new ArrayList<>(entry.getValue());
            List<Member> noContain = geNoCinTaiontMembers(members, contain);

            list.add(new TimeRangeWithMember(entry.getKey().toLocalDate(),
                    entry.getKey().toLocalTime(),
                    entry.getKey().toLocalTime().plusHours(room.getMeetingDuration().getHour())
                            .plusMinutes(room.getMeetingDuration().getMinute()), contain,
                    noContain));

            if (list.size() == 10) {
                break;
            }
        }
        return list;
    }

    private List<Entry<LocalDateTime, Set<Member>>> getSortedEntries(RoomTreeMap roomTreeMap) {
        List<Entry<LocalDateTime, Set<Member>>> entries = new ArrayList<>(
                roomTreeMap.getRoomTreeMap().entrySet());

        entries.sort((o1, o2) -> {
            if (o1.getValue().size() == o2.getValue().size()) {
                return o1.getKey().compareTo(o2.getKey());
            }
            return o2.getValue().size() - o1.getValue().size();
        });
        return entries;
    }

    private List<Member> geNoCinTaiontMembers(List<Member> members, List<Member> contain) {
        List<Member> noContain = new ArrayList<>();

        for (Member m : members) {
            if (!contain.contains(m)) {
                noContain.add(m);
            }
        }
        return noContain;
    }
}
