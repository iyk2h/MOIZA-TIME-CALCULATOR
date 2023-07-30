# MOIZA-TIME-CALCULATOR

## 📌서비스의 핵심 기능
모이자 서비스에서 제공하는 모임 내에서 모임 참여자가 각자 가능한 시간대를 입력합니다. 입력된 시간을 순회하면서 최대로 겹치는 시간대를 계산해줍니다.

참석자의 가용 시간과 선택된 시간대를 분석함으로써 회의실에 예약할 수 있는 시간대를 정확하게 판단할 수 있습니다.

참석자를 참여 멤버와 비참여 멤버로 분류하여 회의 참석자의 수를 파악할 수 있으며, 참석자의 가용성에 따라 최적의 일정을 조정할 수 있습니다.

[MOIZA 서비스](https://github.com/llBackend7/MOIZA) 서버에서 [계산 프로세스 분리 과정 Wiki 보기](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/wiki/%EA%B2%B9%EC%B9%98%EB%8A%94-%EC%8B%9C%EA%B0%84-TOP10-%EA%B3%84%EC%82%B0-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4-%EB%B6%84%EB%A6%AC-%EA%B3%BC%EC%A0%95)

---

### 모임 참여자 간 겹치는 시간대 식별

사용자가 시간을 입력하는 시점에서 LocalDateTime을 키로 하고 Member를 원소로 갖는 ConcurrentHashMap를 사용하여 각 시간대별 회원 정보를 관리.(DateTimeToMembers)

모임(Room)에 해당하는 DateTimeToMembers 객체를 가져오고, 해당 객체의 엔트리들을 회원 수에 따라 정렬한 뒤, 상위 10개를 반환

---

### DateTimeToMembersService의 주요기능 4가지
CalculatorService는 dateTimeToMembers에 값을 입력, 삭제, 수정을 하고, 최대로 겹치는 시간대를 계산합니다.

```java
Map<LocalDateTime, Set<Member>> dateTimeToMembers = new ConcurrentHashMap<>();
```

getDateTimeToMembers, setDateTimeToMembers, deleteDateTimeToMembers, getFindTOP10



### getDateTimeToMembers()

ConcurrentHashMap의 computeIfAbsent 메서드를 사용해 동시 다중 스레드 환경에서 사용하도록 설계되었으며 한 번에 하나의 스레드만 특정 키의 값을 계산하도록 합니다.

```java
// DateTimeToMembersService
public DateTimeToMembers getDateTimeToMembers(Long roomId) {
    return roomDataMap.computeIfAbsent(roomId, key -> new DateTimeToMembers());
}
```



### setDateTimeToMembers

roomId에 DateTimeToMembers을 설정하고 여기에 Member를 추가하는 메소드입니다.
주어진 roomId와 연관된 DateTimeToMembers을 검색합니다.
주어진 LocalDateTime를 가지고 setDateTimeToMembersDateWithMember() 메서드를 사용하여 Member를 추가합니다.

```java
// DateTimeToMembersService
public void setDateTimeToMembers(Long roomId, LocalDate localDate, LocalTime localTime, Member member) {

    DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
    dateTimeToMembers.setDateTimeToMembersDateWithMember(LocalDateTime.of(localDate, localTime), member);
}
```

```java
// DateTimeToMembers
private final ConcurrentHashMap<LocalDateTime, Set<Member>> dateTimeToMembers = new ConcurrentHashMap<>();

public ConcurrentHashMap<LocalDateTime, Set<Member>> getDateTimeToMembers() {
    return dateTimeToMembers;
}

public synchronized void setDateTimeToMembersDateWithMember(LocalDateTime localDateTime, Member member) {
    dateTimeToMembers.computeIfAbsent(localDateTime, key -> new ConcurrentSkipListSet<>()).add(member);
}
//ConcurrentSkipListSet을 사용해 Member 중복 및 오름차순 정렬 적용 및 Thread-Safe합니다.
```



---

### deleteDateTimeToMembers

주어진 localDateTime에서 특정 roomId와 연결된 DateTimeToMembers에서 Member를 제거하는 데 사용됩니다.

```java
// DateTimeToMembersService
public void deleteDateTimeToMembers(Long roomId, LocalDate localDate, LocalTime localTime, Member member) {
    DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
    dateTimeToMembers.deleteDateTimeToMembers(LocalDateTime.of(localDate, localTime), member);
}
```

```java
// DateTimeToMembers
private final ConcurrentHashMap<LocalDateTime, Set<Member>> dateTimeToMembers = new ConcurrentHashMap<>();

public ConcurrentHashMap<LocalDateTime, Set<Member>> getDateTimeToMembers() {
    return dateTimeToMembers;
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
```



---

### getFindTOP10

roomId에 대한 구성원이 있는 상위 10개 시간 범위를 찾습니다.

이미 정렬되어 있는 DateTimeToMembers을 RoomId를 통해서 가져옵니다.
이 메서드는 정렬된 항목을 반복하며 각 항목에 대해 시작 시간, 종료 시간, 해당 시간 범위의 구성원 및 해당 시간 범위에 없는 구성원을 포함하는 TimeRangeWithMember 개체를 구성합니다.
이러한 시간 범위를 10개 찾을 때까지 이 과정을 계속한 다음 TimeRangeWithMember 목록을 반환합니다.



```java
// DateTimeToMembersService
public List<TimeRangeWithMember> getFindTOP10(Long roomId) {
    DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
    Room room = roomService.getRoom(roomId);

    return dateTimeToMembers.getSortedEntries().stream()
            // 상위 RANK_NUM(10)개의 엔트리만 남깁니다.
            .limit(RANK_NUM)
            // 각 엔트리에 대해 아래와 같은 변환을 수행하여 TimeRangeWithMember 객체로 매핑합니다.
            .map(entry -> {
                List<Member> contain = new ArrayList<>(entry.getValue());
                List<Member> noContain = getNotContaion(room, contain);

                // TimeRangeWithMember 객체를 생성하여 반환합니다.
                return new TimeRangeWithMember(
                        entry.getKey().toLocalDate(),
                        entry.getKey().toLocalTime(),
                        entry.getKey().toLocalTime().plusHours(room.getMeetingDuration().getHour())
                                .plusMinutes(room.getMeetingDuration().getMinute()),
                        new ArrayList<>(contain),
                        new ArrayList<>(noContain)
                );
            })
            .collect(Collectors.toList());
}

// 포함되지 않는 맴버리스트 가져옵니다.
private List<Member> getNotContaion(Room room, List<Member> contain) {
    List<Member> noContain = enterRoomRepository.findMembersByRoom(room);

    contain.forEach(noContain::remove);
    return noContain;
}
```

```java
// DateTimeToMembers
public List<Entry<LocalDateTime, Set<Member>>> getSortedEntries() {
    List<Entry<LocalDateTime, Set<Member>>> entries = new ArrayList<>(dateTimeToMembers.entrySet());

    entries.sort((o1, o2) -> {
        int sizeComparison = Integer.compare(o2.getValue().size(), o1.getValue().size());
        return (sizeComparison != 0) ? sizeComparison : o1.getKey().compareTo(o2.getKey());
    });

    return entries;
}
```

## ⚙️개발 환경 및 기술 스택
Back-end

Java 17

Spring Boot 3.1.0

Spring Data JPA, MySQL

Test

Junit5

## 기술 적용 및 트러블 슈팅

- [중복 시간 조회 오류](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/wiki/%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%87%E1%85%A9%E1%86%A8-%E1%84%89%E1%85%B5%E1%84%80%E1%85%A1%E1%86%AB-%E1%84%8C%E1%85%A9%E1%84%92%E1%85%AC-%E1%84%8B%E1%85%A9%E1%84%85%E1%85%B2)
