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
    private static final int ONE_DAY = 1;

    @Cacheable(value = "overlappingTimeRangesCache", key = "#room.id")
    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        List<TimeRangeWithMember> timeRangeWithMembers = new LinkedList<>();

        LocalDate curDate = room.getAvailableStartDay();

        while (!curDate.isAfter(room.getAvailableEndDay())) {
            List<TimeRangeWithMember> getTimeRangesWhitRoomAndDay = findOverlappingTimeRanges(room, curDate);
            timeRangeWithMembers.addAll(getTimeRangesWhitRoomAndDay);

            curDate = curDate.plusDays(ONE_DAY);
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

        LocalTime startTime = room.getAvailableStartTime();

        LocalTime meetingDuration = room.getMeetingDuration();

        LocalTime endTime = startTime.plusHours(meetingDuration.getHour())
                .plusMinutes(meetingDuration.getMinute());

        while (endTime.isBefore(room.getAvailableEndTime())) {
            List<Member> participationMembers = getParticipationMembers(selectedTimeList, meetingDuration,
                    startTime, endTime);

            List<Member> nonParticipationMembers = getNonParticipationMembers(room, participationMembers);

            if (participationMembers.size() >= MIN_PARTICIPATION_MEMBER) {
                overlappingRanges.add(
                        new TimeRangeWithMember(date, startTime, endTime, participationMembers, nonParticipationMembers));
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
                .filter(selectedTime -> selectedTime.isParticipation(meetingDuration, startTime, endTime))
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
}