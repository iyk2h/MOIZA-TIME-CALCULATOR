package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.member.repository.MemberRepository;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.service.RoomService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest()
@ActiveProfiles("test")
@Transactional
class SelectedTimeServiceTest {

    @Autowired
    RoomService roomService;
    @Autowired
    SelectedTimeService selectedTimeService;
    @Autowired
    MemberRepository memberRepository;

    static Room room;
    static Member member1;
    static Member member2;
    static Member member3;

    @BeforeEach
    void init() {
        member1 = memberRepository.findByName("user1").orElse(new Member());
        room = roomService.getRoom(1L);

        member2 = memberRepository.findByName("user2").orElse(new Member());
        member3 = memberRepository.findByName("이은혜").orElse(new Member());
    }

    @Test
    @DisplayName("겹치는_시간_조회")
    void findOverlappingTimeRanges_test() {

        List<TimeRangeWithMember> overlappingRanges = selectedTimeService.findOverlappingTimeRanges(
                roomService.getRoom(1L), LocalDate.now().plusDays(6)
        );

        for (TimeRangeWithMember t : overlappingRanges) {
            System.out.println(t.getDate() + " | " + t.getStart() + "~" + t.getEnd());
            System.out.print("참여자 : ");
            for (Member m : t.getParticipationMembers()) {
                System.out.print(m.getName()+ " ");
            }
            System.out.println();

            System.out.print("불참자 : ");
            for (Member m : t.getNonParticipationMembers()) {
                System.out.print(m.getName()+ " ");
            }
            System.out.println();
        }
    }

    @Test
    @DisplayName("전체_겹치는_시간_조회")
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
                () -> assertThat(t1.getParticipationMembers()).isEqualTo(List.of(member1, member2, member3)),

                () -> assertThat(t2.getDate()).isEqualTo(LocalDate.now().plusDays(5)),
                () -> assertThat(t2.getStart()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t2.getEnd()).isEqualTo(LocalTime.of(14, 0)),
                () -> assertThat(t2.getParticipationMembers()).isEqualTo(List.of(member1, member2, member3)),

                () -> assertThat(t3.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t3.getStart()).isEqualTo(LocalTime.of(7, 0)),
                () -> assertThat(t3.getEnd()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(t3.getParticipationMembers()).isEqualTo(List.of(member1, member2, member3)),

                () -> assertThat(t4.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t4.getStart()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t4.getEnd()).isEqualTo(LocalTime.of(14, 0)),
                () -> assertThat(t4.getParticipationMembers()).isEqualTo(List.of(member1, member2, member3)),

                () -> assertThat(t5.getDate()).isEqualTo(LocalDate.now().plusDays(5)),
                () -> assertThat(t5.getStart()).isEqualTo(LocalTime.of(7, 30)),
                () -> assertThat(t5.getEnd()).isEqualTo(LocalTime.of(10, 30)),
                () -> assertThat(t5.getParticipationMembers()).isEqualTo(List.of(member1, member2)),
                () -> assertThat(t5.getNonParticipationMembers()).isEqualTo(List.of(member3))            // 불참자
        );

    }

    @Test
    void CacheTest() {
        Room cacheTestRoom = roomService.getRoom(4L);
        for (int i = 0; i < 5; i++) {
            selectedTimeService.findOverlappingTimeRanges(cacheTestRoom);
        }
    }
}