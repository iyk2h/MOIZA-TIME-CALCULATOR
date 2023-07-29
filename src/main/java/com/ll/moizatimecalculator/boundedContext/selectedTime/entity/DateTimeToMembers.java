package com.ll.moizatimecalculator.boundedContext.selectedTime.entity;

import com.ll.moizatimecalculator.boundedContext.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DateTimeToMembers {

    private final Map<LocalDateTime, Set<Member>> dateTimeToMembers = new ConcurrentHashMap<>();

    public Map<LocalDateTime, Set<Member>> getDateTimeToMembers() {
        return dateTimeToMembers;
    }

    public synchronized void setDateTimeToMembers(LocalDateTime localDateTime, Member member) {
        dateTimeToMembers.computeIfAbsent(localDateTime, key -> new ConcurrentSkipListSet<>()).add(member);
    }

    public synchronized void deleteDateTimeToMembers(LocalDateTime localDateTime, Member member) {
        Set<Member> members = dateTimeToMembers.get(localDateTime);
        if (members != null) {
            members.remove(member);
            if (members.isEmpty()) {
                dateTimeToMembers.remove(localDateTime);
            }
        }
    }
}

/*
synchronized 키워드를 사용하는 것은 일반적으로 허용되지만 여러 스레드가 동기화된 블록에 자주 액세스하는 경우 잠재적으로 성능 병목 현상이 발생할 수 있습니다.
synchronized에 의존하지 않고 java.util.concurrent 패키지에서 제공하는 동시 데이터 구조를 사용합니다.
 */
