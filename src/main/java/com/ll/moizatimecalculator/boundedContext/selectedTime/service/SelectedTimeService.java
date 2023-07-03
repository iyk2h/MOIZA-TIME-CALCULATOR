package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.moizatimecalculator.boundedContext.selectedTime.repository.SelectedTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SelectedTimeService {

    private final SelectedTimeRepository selectedTimeRepository;

    private final EnterRoomRepository enterRoomRepository;

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

        if (timeRangeWithMembers.size() > 10) {
            return new ArrayList<>(timeRangeWithMembers.subList(0, 10));
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

        if (overlappingRanges.size() > 10) {
            return new ArrayList<>(overlappingRanges.subList(0, 10));
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