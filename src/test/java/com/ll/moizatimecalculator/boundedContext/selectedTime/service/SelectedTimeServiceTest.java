package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import com.ll.moizatimecalculator.boundedContext.member.repository.MemberRepository;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.repository.EnterRoomRepository;
import com.ll.moizatimecalculator.boundedContext.room.service.RoomService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
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
    RoomService roomService;
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
    @DisplayName("캐싱 및 중간에 값 변경시 캐시 초기화")
    void CacheTest() {
        Room room5 = roomService.getRoom(2L);

        int cnt = 15;
        Member[] members = new Member[cnt];
        EnterRoom[] enterRooms = new EnterRoom[cnt];
        for (int k = 0; k < cnt; k++) {
            members[k] = Member.builder().name(k+"userASDASD"+k).email(k+"sdfesdfasdr@email.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            try {
                memberRepository.save(members[k]);
            } catch (Exception ignored) {
            }


            enterRooms[k] = enterRoomRepository.save(EnterRoom.builder()
                    .room(room5)
                    .member(members[k])
                    .build());

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(7, 0),
                    LocalTime.of(10, 0),
                    enterRooms[k]
            );
        }

        // 중간에 새로운 값 추가
        for (int i = 1; i < 6; i++) {

            if (i == 3) {
                int cnt2 = 15;
                Member[] members2 = new Member[cnt2];
                EnterRoom[] enterRooms2 = new EnterRoom[cnt2];
                for (int k = 0; k < cnt2; k++) {
                    members2[k] = Member.builder().name(k+"D"+k).email(k+"dr@email.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
                    try {
                        memberRepository.save(members2[k]);
                    } catch (Exception ignored) {
                    }


                    enterRooms2[k] = enterRoomRepository.save(EnterRoom.builder()
                            .room(room5)
                            .member(members2[k])
                            .build());

                    selectedTimeService.CreateSelectedTime(
                            LocalDate.now().plusDays(5),
                            LocalTime.of(6, 0),
                            LocalTime.of(9, 0),
                            enterRooms2[k]
                    );
                }
            }

            long beforeTime = System.currentTimeMillis(); // 코드 실행 시작 시간 받아오기

            List<TimeRangeWithMember> overlappingTimeRanges = selectedTimeService.findOverlappingTimeRanges(
                    room5);

            long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
            long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
            int sec = (int) (secDiffTime / 1000);
            int ms = (int) (secDiffTime - sec * 1000);
            System.out.println("All_findOverlappingTimeRanges 시간 : " + sec + "." + ms + "초");

            TimeRangeWithMember tw = overlappingTimeRanges.get(0);
            System.out.println(tw.date +"::"+ tw.start +"~"+ tw.end);
            for (Member m : tw.participationMembers) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
        }
    }

    @Test
    void roomGetAvailableDayList() {
        Room room5 = roomService.getRoom(5L);
        for (LocalDate cur : room5.getAvailableDayList()) {
            System.out.println(cur);
        }

    }
}