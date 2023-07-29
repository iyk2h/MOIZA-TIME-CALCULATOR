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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DateTimeToMembersServiceThreadTest {

    @Autowired
    DateTimeToMembersService dateTimeToMembersService;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EnterRoomRepository enterRoomRepository;

    Room room;
    Member member1;
    Member member2;
    Member member3;

    @Test
    @DisplayName("동시성 테스트")
    @Transactional
    void multi_thread_test() {
        final int NUM_USERS = 3;

        final long ROOM_ID = room.getId();

        Member[] members = new Member[NUM_USERS + 1];

        for (int i = 1; i < NUM_USERS + 1; i++) {
            members[i] = memberRepository.findById(Long.valueOf(i)).orElse(new Member());
            System.out.println(members[i]);
        }

        for (int i = 0; i < 1; i++) {
            ExecutorService executorService = Executors.newFixedThreadPool(NUM_USERS);

            Runnable userTask = () -> {
                long userId = getAndIncrementUserId();
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(0, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(1, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(2, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(3, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(4, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(5, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(6, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(7, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(8, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(9, 0, 0), members[(int) userId]);
            };
            for (int j = 0; j < NUM_USERS; j++) {
                executorService.submit(userTask);
            }
            executorService.shutdown();

            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<TimeRangeWithMember> overlappingRanges = dateTimeToMembersService.getFindTOP10(
                    ROOM_ID);
            for (int j = 0; j < 10; j++) {
                TimeRangeWithMember tm = overlappingRanges.get(j);
                int finalJ = j;
                assertAll(
                        () -> assertThat(tm.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                        () -> assertThat(tm.getStart()).isEqualTo(LocalTime.of(finalJ, 0)),
                        () -> assertThat(tm.getEnd()).isEqualTo(LocalTime.of(finalJ + 3, 0)),
                        () -> assertThat(tm.getParticipationMembers()).isEqualTo(
                                List.of(member1, member2, member3))
                );
            }
        }

        List<TimeRangeWithMember> overlappingRanges = dateTimeToMembersService.getFindTOP10(
                ROOM_ID);

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
    }

    int userIdCounter = 0;
    private synchronized long getAndIncrementUserId() {
        return ++userIdCounter;
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

        EnterRoom enterRoom2 = EnterRoom.builder()
                .room(room)
                .member(member2)
                .build();
        enterRoomRepository.save(enterRoom2);

        EnterRoom enterRoom3 = EnterRoom.builder()
                .room(room)
                .member(member3)
                .build();
        enterRoomRepository.save(enterRoom3);
    }
}