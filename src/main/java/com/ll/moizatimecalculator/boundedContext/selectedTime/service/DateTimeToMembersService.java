package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.room.service.RoomService;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.DateTimeToMembers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DateTimeToMembersService {

    static final int RANK_NUM = 10;
    @Autowired
    RoomService roomService;

    @Autowired
    EnterRoomRepository enterRoomRepository;

    private final Map<Long, DateTimeToMembers> dateTimeToMembersStorage = new ConcurrentHashMap<>();

    public DateTimeToMembers getDateTimeToMembers(Long roomId) {
        return dateTimeToMembersStorage.computeIfAbsent(roomId, key -> new DateTimeToMembers());
    }

    public void setDateTimeToMembers(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        dateTimeToMembers.setDateTimeToMembers(localDateTime, member);
    }

    public void deleteDateTimeToMembers(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        dateTimeToMembers.deleteDateTimeToMembers(localDateTime, member);
    }

    public List<TimeRangeWithMember> getFindTOP10(Long roomId) {
        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        Room room = roomService.getRoom(roomId);

        return dateTimeToMembers.getSortedEntries().stream()
                .limit(RANK_NUM)
                .map(entry -> {
                    List<Member> contain = new ArrayList<>(entry.getValue());
                    List<Member> noContain = getNotContaion(room, contain);

                    return new TimeRangeWithMember(
                            entry.getKey().toLocalDate(),
                            entry.getKey().toLocalTime(),
                            entry.getKey().toLocalTime()
                                    .plusHours(room.getMeetingDuration().getHour())
                                    .plusMinutes(room.getMeetingDuration().getMinute()),
                            new ArrayList<>(contain),
                            new ArrayList<>(noContain)
                    );
                })
                .collect(Collectors.toList());
    }

    private List<Member> getNotContaion(Room room, List<Member> contain) {
        List<Member> noContain = enterRoomRepository.findMembersByRoom(room);

        contain.forEach(noContain::remove);
        return noContain;
    }
}
