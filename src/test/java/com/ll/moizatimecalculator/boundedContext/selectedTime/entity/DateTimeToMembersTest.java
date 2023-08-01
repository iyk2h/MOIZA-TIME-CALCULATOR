package com.ll.moizatimecalculator.boundedContext.selectedTime.entity;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class DateTimeToMembersTest {

    @Test
    void RoomTreeMapTest() {

        Member member = Member.builder().name("user1").email("user1@email.com").profile(
                        "http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg")
                .build();

        Member member2 = Member.builder().name("user2").email("user2@email.com").profile(
                        "http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg")
                .build();

        DateTimeToMembers rtm = new DateTimeToMembers();
        rtm.setDateTimeToMembers(LocalDateTime.now(), member2);
        rtm.setDateTimeToMembers(LocalDateTime.now(), member);
        System.out.println(rtm.getDateTimeToMembers());
    }
}