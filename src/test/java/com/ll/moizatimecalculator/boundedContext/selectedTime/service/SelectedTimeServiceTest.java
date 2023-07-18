package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.member.repository.MemberRepository;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.room.repository.RoomRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableCaching
@ActiveProfiles("test")
@Transactional
class SelectedTimeServiceTest {

    @Autowired
    RoomRepository roomRepository;
    @Autowired
    SelectedTimeService selectedTimeService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EnterRoomRepository enterRoomRepository;

    static Room room;
    static Member member1;
    static Member member2;
    static Member member3;

    @Test
    @DisplayName("현재 + 6일의 _ 겹치는_시간_조회")
    void findOverlappingTimeRanges_test() {

        List<TimeRangeWithMember> overlappingRanges = selectedTimeService.findOverlappingTimeRanges(
                room, LocalDate.now().plusDays(6)
        );

        for (TimeRangeWithMember t : overlappingRanges) {
            System.out.println(t.getDate() + " | " + t.getStart() + "~" + t.getEnd());
            System.out.print("참여자 : ");
            for (Member m : t.getParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();

            System.out.print("불참자 : ");
            for (Member m : t.getNonParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
        }

        TimeRangeWithMember t1 = overlappingRanges.get(0);
        TimeRangeWithMember t2 = overlappingRanges.get(1);
        TimeRangeWithMember t3 = overlappingRanges.get(2);
        TimeRangeWithMember t4 = overlappingRanges.get(3);
        TimeRangeWithMember t5 = overlappingRanges.get(4);
        assertAll(
                () -> assertThat(t1.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t1.getStart()).isEqualTo(LocalTime.of(7, 0)),
                () -> assertThat(t1.getEnd()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(t1.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2, member3)),

                () -> assertThat(t2.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t2.getStart()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t2.getEnd()).isEqualTo(LocalTime.of(14, 0)),
                () -> assertThat(t2.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2, member3)),

                () -> assertThat(t3.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t3.getStart()).isEqualTo(LocalTime.of(7, 30)),
                () -> assertThat(t3.getEnd()).isEqualTo(LocalTime.of(10, 30)),
                () -> assertThat(t3.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2)),
                () -> assertThat(t3.getNonParticipationMembers()).isEqualTo(
                        List.of(member3)),

                () -> assertThat(t4.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t4.getStart()).isEqualTo(LocalTime.of(8, 0)),
                () -> assertThat(t4.getEnd()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t4.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2)),
                () -> assertThat(t4.getNonParticipationMembers()).isEqualTo(
                        List.of(member3)),

                () -> assertThat(t5.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t5.getStart()).isEqualTo(LocalTime.of(8, 30)),
                () -> assertThat(t5.getEnd()).isEqualTo(LocalTime.of(11, 30)),
                () -> assertThat(t5.getParticipationMembers()).isEqualTo(List.of(member1, member2)),
                () -> assertThat(t5.getNonParticipationMembers()).isEqualTo(List.of(member3))
        );
    }

    @Test
    @DisplayName("모임전체_겹치는_시간_조회")
    void all_findOverlappingTimeRanges_test() {

        List<TimeRangeWithMember> overlappingRanges = selectedTimeService.findOverlappingTimeRanges(
                room);

        for (TimeRangeWithMember t : overlappingRanges) {
            System.out.println(t.date + " | " + t.start + "~" + t.end);
            System.out.print("참가자 : ");
            for (Member m : t.getParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
            System.out.print("불참자 : ");
            for (Member m : t.getNonParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
        }

        TimeRangeWithMember t1 = overlappingRanges.get(0);
        TimeRangeWithMember t2 = overlappingRanges.get(1);
        TimeRangeWithMember t3 = overlappingRanges.get(2);
        TimeRangeWithMember t4 = overlappingRanges.get(3);
        TimeRangeWithMember t5 = overlappingRanges.get(4);
        assertAll(
                () -> assertThat(t1.getDate()).isEqualTo(LocalDate.now().plusDays(5)),
                () -> assertThat(t1.getStart()).isEqualTo(LocalTime.of(7, 0)),
                () -> assertThat(t1.getEnd()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(t1.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2, member3)),

                () -> assertThat(t2.getDate()).isEqualTo(LocalDate.now().plusDays(5)),
                () -> assertThat(t2.getStart()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t2.getEnd()).isEqualTo(LocalTime.of(14, 0)),
                () -> assertThat(t2.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2, member3)),

                () -> assertThat(t3.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t3.getStart()).isEqualTo(LocalTime.of(7, 0)),
                () -> assertThat(t3.getEnd()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(t3.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2, member3)),

                () -> assertThat(t4.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t4.getStart()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t4.getEnd()).isEqualTo(LocalTime.of(14, 0)),
                () -> assertThat(t4.getParticipationMembers()).isEqualTo(
                        List.of(member1, member2, member3)),

                () -> assertThat(t5.getDate()).isEqualTo(LocalDate.now().plusDays(5)),
                () -> assertThat(t5.getStart()).isEqualTo(LocalTime.of(7, 30)),
                () -> assertThat(t5.getEnd()).isEqualTo(LocalTime.of(10, 30)),
                () -> assertThat(t5.getParticipationMembers()).isEqualTo(List.of(member1, member2)),
                () -> assertThat(t5.getNonParticipationMembers()).isEqualTo(List.of(member3))
                // 불참자
        );

    }

    @Test
    @DisplayName("캐싱 및 중간에 값 변경시 캐시 초기화")
    void CacheTest() {
        int firstSecDiffTime = 0;
        int secondSecDiffTime = 0;
        int afterCleanCache = 0;

        // 중간에 새로운 값 추가
        for (int i = 1; i < 6; i++) {

            if (i == 3) {
                selectedTimeService.refreshCache(room);
            }

            long beforeTime = System.currentTimeMillis(); // 코드 실행 시작 시간 받아오기

            List<TimeRangeWithMember> overlappingTimeRanges = selectedTimeService.findOverlappingTimeRanges(
                    room);

            long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
            long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
            int sec = (int) (secDiffTime / 1000);
            int ms = (int) (secDiffTime - sec * 1000);
            System.out.println("All_findOverlappingTimeRanges 시간 : " + sec + "." + ms + "초");

            TimeRangeWithMember tw = overlappingTimeRanges.get(0);
            System.out.println(tw.date + "::" + tw.start + "~" + tw.end);
            for (Member m : tw.participationMembers) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();

            switch (i) {
                case 1 -> firstSecDiffTime = sec * 1000 + ms;
                case 2 -> secondSecDiffTime = sec * 1000 + ms;
                case 3 -> afterCleanCache = sec * 1000 + ms;
                default -> {
                }
            }
        }

        final int finalFirstSecDiffTime = firstSecDiffTime;
        final int finalSecondSecDiffTime = secondSecDiffTime;
        final int finalAfterCleanCache = afterCleanCache;

        assertAll(
                () -> assertThat(
                        Long.compare(finalFirstSecDiffTime, finalSecondSecDiffTime)).isEqualTo(1),
                () -> assertThat(
                        Long.compare(finalSecondSecDiffTime, finalAfterCleanCache)).isEqualTo(-1)
        );

    }

    @BeforeEach
    void init() {
        member1 = Member.builder().name("user1").email("user1@email.com").profile(
                        "http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg")
                .build();
        memberRepository.save(member1);
        member2 = Member.builder().name("user2").email("user2@email.com").profile(
                        "http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg")
                .build();
        memberRepository.save(member2);
        member3 = Member.builder().name("user3").email("user3@email.com").profile(
                        "http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg")
                .build();
        memberRepository.save(member3);

        room = Room.builder()
                .leader(member1)
                .name("테스트1")
                .description("description")
                .availableStartDay(LocalDate.now().plusDays(5))
                .availableEndDay(LocalDate.now().plusDays(7))
                .availableStartTime(LocalTime.of(0, 0))
                .availableEndTime(LocalTime.of(23, 0))
                .meetingDuration(LocalTime.of(3, 0))
                .deadLine(LocalDateTime.now().plusDays(2))
                .accessCode(UUID.randomUUID().toString())
                .build();
        roomRepository.save(room);

        EnterRoom enterRoom = EnterRoom.builder()
                .room(room)
                .member(member1)
                .build();
        enterRoomRepository.save(enterRoom);

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(7, 0),
                LocalTime.of(14, 0),
                enterRoom
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(14, 0),
                LocalTime.of(19, 0),
                enterRoom
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(5),
                LocalTime.of(7, 0),
                LocalTime.of(14, 0),
                enterRoom
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(5),
                LocalTime.of(14, 0),
                LocalTime.of(19, 0),
                enterRoom
        );

        EnterRoom enterRoom2 = EnterRoom.builder()
                .room(room)
                .member(member2)
                .build();
        enterRoomRepository.save(enterRoom2);

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(6, 0),
                LocalTime.of(14, 0),
                enterRoom2
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(15, 0),
                LocalTime.of(18, 0),
                enterRoom2
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(5),
                LocalTime.of(6, 0),
                LocalTime.of(14, 0),
                enterRoom2
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(5),
                LocalTime.of(15, 0),
                LocalTime.of(18, 0),
                enterRoom2
        );

        EnterRoom enterRoom3 = EnterRoom.builder()
                .room(room)
                .member(member3)
                .build();
        enterRoomRepository.save(enterRoom3);

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(7, 0),
                LocalTime.of(10, 0),
                enterRoom3
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(11, 0),
                LocalTime.of(14, 0),
                enterRoom3
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(5),
                LocalTime.of(7, 0),
                LocalTime.of(10, 0),
                enterRoom3
        );

        selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(5),
                LocalTime.of(11, 0),
                LocalTime.of(14, 0),
                enterRoom3
        );
    }
}