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
import org.springframework.cache.CacheManager;
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

    private final CacheManager cacheManager;

    private static final int MEMBER_MAX_SIZE = 10;

    @Transactional
    public SelectedTime CreateSelectedTime(
            LocalDate day,
            LocalTime startTime,
            LocalTime endTime,
            EnterRoom enterRoom
    ) {
        validDate(enterRoom.getRoom(), day);
        validTime(enterRoom.getRoom(), startTime, endTime);
        SelectedTime selectedTime = SelectedTime.builder()
                .date(day)
                .startTime(startTime)
                .endTime(endTime)
                .enterRoom(enterRoom)
                .build();

        // 새로운 시간 입력시 캐시 초기화
        cacheManager.getCache("overlappingTimeRangesCache").clear();
        return selectedTimeRepository.save(selectedTime);
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

    @Cacheable(value = "overlappingTimeRangesCache", key = "#room.id")
    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        List<TimeRangeWithMember> timeRangeWithMembers = new LinkedList<>();
        LocalDate startDay = room.getAvailableStartDay();
        LocalDate endDay = room.getAvailableEndDay();

        while (!startDay.isAfter(endDay)) {
            List<TimeRangeWithMember> getTimeRangesWhitRoomAndDay = findOverlappingTimeRanges(room,
                    startDay);

            timeRangeWithMembers.addAll(getTimeRangesWhitRoomAndDay);

            startDay = startDay.plusDays(1);
        }

        if (!timeRangeWithMembers.isEmpty())
            Collections.sort(timeRangeWithMembers);

        if (timeRangeWithMembers.size() > MEMBER_MAX_SIZE) {
            timeRangeWithMembers = new ArrayList<>(timeRangeWithMembers.subList(0, MEMBER_MAX_SIZE));
        }

        return timeRangeWithMembers;
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(
            Room room, LocalDate date) {

        List<SelectedTime> selectedTimeList = selectedTimeRepository.searchSelectedTimeByRoom(room,
                date);

        if (selectedTimeList.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeRangeWithMember> overlappingRanges = new LinkedList<>();

        // 탐색 시간 기준 시작점
        LocalTime startTime = room.getAvailableStartTime();

        LocalTime meetingDuration = room.getMeetingDuration();

        while (startTime.isBefore(room.getAvailableEndTime())) {

            LocalTime basicStartTime = startTime;
            LocalTime basicEndTime = startTime.plusHours(meetingDuration.getHour())
                    .plusMinutes(meetingDuration.getMinute());

            List<Member> participationMembers = getContainedMember(selectedTimeList, meetingDuration,
                    basicStartTime,
                    basicEndTime);

            List<Member> nonParticipationMembers = getNonParticipationMembers(room, participationMembers);

            if (participationMembers.size() >= 1) {
                overlappingRanges.add(
                        new TimeRangeWithMember(date, basicStartTime, basicEndTime, participationMembers, nonParticipationMembers));
            }

            startTime = basicStartTime.plusMinutes(30);
        }

        Collections.sort(overlappingRanges);

        if (overlappingRanges.size() > MEMBER_MAX_SIZE) {
            overlappingRanges = new ArrayList<>(overlappingRanges.subList(0, MEMBER_MAX_SIZE));
        }

        return overlappingRanges;
    }

    public List<Member> getContainedMember(List<SelectedTime> selectedTimeList,
            LocalTime meetingDuration,
            LocalTime startTime,
            LocalTime endTime) {
        return selectedTimeList.stream()
                .filter(selectedTime -> {
                    LocalTime selectedDuration = selectedTime.getDuration();
                    return isAfterOrEqual(selectedDuration, meetingDuration);
                })
                .filter(selectedTime -> isBeforeOrEqual(selectedTime.getStartTime(), endTime))
                .filter(selectedTime ->
                        isBeforeOrEqual(selectedTime.getStartTime(), startTime)
                                && isAfterOrEqual(selectedTime.getEndTime(),endTime)
                )
                .map(SelectedTime::getEnterRoom)
                .map(EnterRoom::getMember)
                .distinct()
                .sorted(Comparator.comparing(Member::getName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean isAfterOrEqual(LocalTime left, LocalTime right) {
        // left >= right
        return !left.isBefore(right);
    }

    private boolean isBeforeOrEqual(LocalTime left, LocalTime right) {
        // left <= right
        return !left.isAfter(right);
    }

    public List<Member> getNonParticipationMembers(Room room, List<Member> participationMembers) {
        List<Member> allMembers = enterRoomRepository.findMembersByRoom(room);

        return allMembers.stream()
                .filter(m -> !participationMembers.contains(m))
                .collect(Collectors.toList());
    }
}