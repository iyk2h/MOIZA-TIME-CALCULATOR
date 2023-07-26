package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.moizatimecalculator.boundedContext.selectedTime.repository.SelectedTimeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@EnableCaching
public class SelectedTimeService {

    private final SelectedTimeRepository selectedTimeRepository;

    private final EnterRoomRepository enterRoomRepository;

    private final RoomTreeMapService roomTreeMapService;

    private static final int MEMBER_MAX_SIZE = 10;
    private static final int MIN_PARTICIPATION_MEMBER = 1;
    private static final int THIRTY_MIN = 30;
    private static final int ONE_DAY = 1;

    @Transactional
    public SelectedTime CreateSelectedTime(LocalDate day,
            LocalTime startTime,
            LocalTime endTime,
            EnterRoom enterRoom) {
        validDate(enterRoom.getRoom(), day);
        validTime(enterRoom.getRoom(), startTime, endTime);
        SelectedTime selectedTime = SelectedTime.builder()
                .date(day)
                .startTime(startTime)
                .endTime(endTime)
                .enterRoom(enterRoom)
                .build();
        setRoomTreeMap(day, startTime, endTime, enterRoom);

        enterRoom.getSelectedTimes().add(selectedTime);
        return selectedTimeRepository.save(selectedTime);
    }

    private void setRoomTreeMap(LocalDate day,
            LocalTime startTime,
            LocalTime endTime,
            EnterRoom enterRoom) {
        LocalTime meetingDuration = enterRoom.getRoom().getMeetingDuration();
        LocalTime curTime = startTime.plusHours(meetingDuration.getHour())
                .plusMinutes(meetingDuration.getMinute());

        while (!curTime.isAfter(endTime)) {
            roomTreeMapService.setRoomTreeMap(enterRoom.getRoom().getId(), day, startTime,
                    enterRoom.getMember());
            curTime = curTime.plusMinutes(30);
            startTime = startTime.plusMinutes(30);
        }
    }

    @Cacheable(value = "overlappingTimeRangesCache", key = "#room.id")
    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        List<TimeRangeWithMember> timeRangeWithMembers = new LinkedList<>();

        LocalDate curDate = room.getAvailableStartDay();

        while (!curDate.isAfter(room.getAvailableEndDay())) {
            List<TimeRangeWithMember> getTimeRangesWhitRoomAndDay = findOverlappingTimeRanges(room,
                    curDate);
            timeRangeWithMembers.addAll(getTimeRangesWhitRoomAndDay);

            curDate = curDate.plusDays(ONE_DAY);
        }

        if (!timeRangeWithMembers.isEmpty()) {
            Collections.sort(timeRangeWithMembers);
        }

        if (timeRangeWithMembers.size() > MEMBER_MAX_SIZE) {
            timeRangeWithMembers = new ArrayList<>(
                    timeRangeWithMembers.subList(0, MEMBER_MAX_SIZE));
        }

        return timeRangeWithMembers;
    }

    @CacheEvict(value = "overlappingTimeRangesCache", key = "#room.id")
    public void refreshCache(Room room) {
        // 캐시 지우기 위한 메서드
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(
            Room room, LocalDate date) {

        List<SelectedTime> selectedTimeList = selectedTimeRepository.searchSelectedTimeByRoom(room,
                date);

        if (selectedTimeList.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeRangeWithMember> overlappingRanges = new LinkedList<>();

        LocalTime startTime = room.getAvailableStartTime();

        LocalTime meetingDuration = room.getMeetingDuration();

        LocalTime endTime = startTime.plusHours(meetingDuration.getHour())
                .plusMinutes(meetingDuration.getMinute());

        while (endTime.isBefore(room.getAvailableEndTime())) {
            List<Member> participationMembers = getParticipationMembers(selectedTimeList,
                    meetingDuration,
                    startTime, endTime);

            List<Member> nonParticipationMembers = getNonParticipationMembers(room,
                    participationMembers);

            if (participationMembers.size() >= MIN_PARTICIPATION_MEMBER) {
                overlappingRanges.add(
                        new TimeRangeWithMember(date, startTime, endTime, participationMembers,
                                nonParticipationMembers));
            }

            startTime = startTime.plusMinutes(THIRTY_MIN);
            endTime = startTime.plusHours(meetingDuration.getHour()).plusMinutes(
                    meetingDuration.getMinute());
        }

        Collections.sort(overlappingRanges);

        if (overlappingRanges.size() > MEMBER_MAX_SIZE) {
            overlappingRanges = new ArrayList<>(overlappingRanges.subList(0, MEMBER_MAX_SIZE));
        }

        return overlappingRanges;
    }

    private List<Member> getParticipationMembers(List<SelectedTime> selectedTimeList,
            LocalTime meetingDuration,
            LocalTime startTime,
            LocalTime endTime) {
        return selectedTimeList.stream()
                .filter(selectedTime -> selectedTime.isParticipation(meetingDuration, startTime,
                        endTime))
                .map(SelectedTime::getEnterRoom)
                .map(EnterRoom::getMember)
                .distinct()
                .sorted(Comparator.comparing(Member::getName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private List<Member> getNonParticipationMembers(Room room, List<Member> participationMembers) {
        List<Member> allMembers = enterRoomRepository.findMembersByRoom(room);

        return allMembers.stream()
                .filter(m -> !participationMembers.contains(m))
                .collect(Collectors.toList());
    }

    private void validDate(Room room, LocalDate day) {
        if (room.getAvailableStartDay().isAfter(day)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 날짜가 아닙니다. 선택한 날짜가 가능한 날짜보다 이릅니다.");
        }
        if (room.getAvailableEndDay().isBefore(day)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 날짜가 아닙니다. 선택한 날짜가 가능한 날짜보다 늦습니다.");
        }
    }

    private void validTime(Room room, LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "시작하는 시간은 끝나는 시간보다 빠를 수 없습니다.");
        }
        if (room.getAvailableStartTime().isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 시간이 아닙니다. 선택한 시간이 가능한 시간보다 이릅니다.");
        }
        if (room.getAvailableEndTime().isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 시간이 아닙니다. 선택한 시간이 가능한 시간보다 늦습니다.");
        }
        if (endTime.minusHours(startTime.getHour()).minusMinutes(startTime.getMinute())
                .isBefore(room.getMeetingDuration())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 시간이 아닙니다. 미팅 진행 시간보다 짧은 시간입니다.");
        }
    }
}