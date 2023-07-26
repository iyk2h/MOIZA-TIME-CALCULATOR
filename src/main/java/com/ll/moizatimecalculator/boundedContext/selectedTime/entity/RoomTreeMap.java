package com.ll.moizatimecalculator.boundedContext.selectedTime.entity;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.TreeSet;

public class RoomTreeMap {

    private final TreeMap<LocalDateTime, TreeSet<Member>> roomTreeMap = new TreeMap<>();

    public TreeMap<LocalDateTime, TreeSet<Member>> getRoomTreeMap() {
        return roomTreeMap;
    }

    public synchronized void setRoomTreeMapDateWithMember(LocalDateTime localDateTime, Member member) {
        TreeSet<Member> members = roomTreeMap.getOrDefault(localDateTime, new TreeSet<>());
        members.add(member);
        roomTreeMap.put(localDateTime, members);
    }
}
