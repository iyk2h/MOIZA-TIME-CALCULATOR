package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.moizatimecalculator.boundedContext.selectedTime.repository.SelectedTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@EnableCaching
public class SelectedTimeService {

    private final SelectedTimeRepository selectedTimeRepository;

    private final EnterRoomRepository enterRoomRepository;
    private static final int MEMBER_MAX_SIZE = 10;
    private static final int MIN_PARTICIPATION_MEMBER = 1;
    private static final int THIRTY_MIN = 30;

    @Cacheable(value = "overlappingTimeRangesCache", key = "#room.id")
    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        List<TimeRangeWithMember> timeRangeWithMembers = new LinkedList<>();

        for (LocalDate curDate : room.getAvailableDayList()) {
            List<TimeRangeWithMember> getTimeRangesWhitRoomAndDay = findOverlappingTimeRanges(room, curDate);

            timeRangeWithMembers.addAll(getTimeRangesWhitRoomAndDay);
        }

        if (!timeRangeWithMembers.isEmpty())
            Collections.sort(timeRangeWithMembers);

        if (timeRangeWithMembers.size() > MEMBER_MAX_SIZE) {
            timeRangeWithMembers = new ArrayList<>(timeRangeWithMembers.subList(0, MEMBER_MAX_SIZE));
        }

        return timeRangeWithMembers;
    }

    @CacheEvict(value = "overlappingTimeRangesCache", key = "#room.id")
    public void refreshCache(Room room){
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

            if (participationMembers.size() >= MIN_PARTICIPATION_MEMBER) {
                overlappingRanges.add(
                        new TimeRangeWithMember(date, basicStartTime, basicEndTime, participationMembers, nonParticipationMembers));
            }

            startTime = basicStartTime.plusMinutes(THIRTY_MIN);
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
                .filter(selectedTime -> isAfterOrEqual(selectedTime.getDuration(), meetingDuration))
                .filter(selectedTime -> isBeforeOrEqual(selectedTime.getStartTime(), endTime))
                .filter(selectedTime -> isWithinTimeRange(selectedTime, startTime, endTime))
                .map(SelectedTime::getEnterRoom)
                .map(EnterRoom::getMember)
                .distinct()
                .sorted(Comparator.comparing(Member::getName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<Member> getNonParticipationMembers(Room room, List<Member> participationMembers) {
        List<Member> allMembers = enterRoomRepository.findMembersByRoom(room);

        return allMembers.stream()
                .filter(m -> !participationMembers.contains(m))
                .collect(Collectors.toList());
    }

    private boolean isWithinTimeRange(SelectedTime selectedTime, LocalTime startTime, LocalTime endTime) {
        return isBeforeOrEqual(selectedTime.getStartTime(), startTime) && isAfterOrEqual(selectedTime.getEndTime(), endTime);
    }

    private boolean isAfterOrEqual(LocalTime left, LocalTime right) {
        // left >= right
        return !left.isBefore(right);
    }

    private boolean isBeforeOrEqual(LocalTime left, LocalTime right) {
        // left <= right
        return !left.isAfter(right);
    }
}