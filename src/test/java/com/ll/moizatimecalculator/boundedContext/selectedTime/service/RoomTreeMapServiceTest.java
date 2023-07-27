package com.ll.moizatimecalculator.boundedContext.selectedTime.service;

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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
@Transactional
class RoomTreeMapServiceTest {

    @Autowired
    RoomTreeMapService roomTreeMapService;
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
    @DisplayName("삽입 및 삭제 테스트")
    @Transactional
    void RoomTreeMapServiceTest() {

        long roomId = room.getId();

        TreeMap<LocalDateTime, TreeSet<Member>> treeMap = roomTreeMapService.getRoomTreeMap(roomId)
                .getRoomTreeMap();

        System.out.println(treeMap);

        roomTreeMapService.setRoomTreeMap(roomId, LocalDate.now().plusDays(3), LocalTime.of(0, 0, 0),
                member1);
        roomTreeMapService.setRoomTreeMap(roomId, LocalDate.now().plusDays(3), LocalTime.of(0, 0, 0),
                member2);
        roomTreeMapService.setRoomTreeMap(roomId, LocalDate.now().plusDays(3), LocalTime.of(0, 0, 0),
                member3);
        roomTreeMapService.setRoomTreeMap(roomId, LocalDate.now().plusDays(2), LocalTime.of(0, 0, 0),
                member2);
        roomTreeMapService.setRoomTreeMap(roomId, LocalDate.now().plusDays(1), LocalTime.of(0, 0, 0),
                member3);

        System.out.println(treeMap);

        List<TimeRangeWithMember> overlappingRanges = roomTreeMapService.findOverlappingTimeRanges(
                roomId);

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

        roomTreeMapService.delete(roomId, LocalDate.now().plusDays(1), LocalTime.of(0, 0, 0), member3);

        List<TimeRangeWithMember> overlappingRanges2 = roomTreeMapService.findOverlappingTimeRanges(
                roomId);

        for (TimeRangeWithMember t : overlappingRanges2) {
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