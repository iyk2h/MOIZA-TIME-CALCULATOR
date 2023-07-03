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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SelectedTimeService {

    private final SelectedTimeRepository selectedTimeRepository;

    private final EnterRoomRepository enterRoomRepository;

    private static final int MEMBER_MAX_SIZE = 100000;

    // todo 캐시 작업 필요
    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        long beforeTime = System.currentTimeMillis(); // 코드 실행 시작 시간 받아오기
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

        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        int sec = (int) (secDiffTime / 1000);
        int ms = (int) (secDiffTime - sec * 1000);
        System.out.println("시간차이 : " + sec + "." + ms + "초");
        return timeRangeWithMembers;
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(
            Room room, LocalDate date) {

        // todo 캐시 작업 필요
        long beforeTime = System.currentTimeMillis(); // 코드 실행 시작 시간 받아오기
        List<SelectedTime> selectedTimeList = selectedTimeRepository.searchSelectedTimeByRoom(room,
                date);
        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        int sec = (int) (secDiffTime / 1000);
        int ms = (int) (secDiffTime - sec * 1000);
        System.out.println("쿼리 시간 : " + sec + "." + ms + "초");

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