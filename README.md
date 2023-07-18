# MOIZA-TIME-CALCULATOR

## 📌서비스의 핵심 기능
모이자 서비스에서 제공하는 모임 내에서 모임원들이 입력한 시간 중 최대로 겹치는 시간대를 계산해줍니다.

참석자의 가용 시간과 선택된 시간대를 분석함으로써 회의실에 예약할 수 있는 시간대를 정확하게 판단할 수 있습니다.

참석자를 참여 멤버와 비참여 멤버로 분류하여 회의 참석자의 수를 파악할 수 있으며, 참석자의 가용성에 따라 최적의 일정을 조정할 수 있습니다.

캐시를 적용해 반복적인 계산을 줄여 후속 요청에 대한 응답 시간을 개선했습니다.

[MOIZA 서비스](https://github.com/llBackend7/MOIZA) 서버에서 [계산 프로세스 분리 과정 Wiki 보기](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/wiki/%EA%B2%B9%EC%B9%98%EB%8A%94-%EC%8B%9C%EA%B0%84-TOP10-%EA%B3%84%EC%82%B0-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4-%EB%B6%84%EB%A6%AC-%EA%B3%BC%EC%A0%95)


<img width="942" alt="image" src="https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/assets/17765939/ab448e27-2b4c-4a51-ae7d-833dbb11252a">

---

## 서비스 로직

### 사용자 간 겹치는 시간대 식별

<img width="854" alt="image" src="https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/assets/17765939/14c5bc93-5081-426e-8d15-5bd55253cb64">

[코드](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/blob/782f82a738f142b3e7f0143939d1c29b9b0c723f/src/main/java/com/ll/moizatimecalculator/boundedContext/selectedTime/service/SelectedTimeService.java#L36)

<img width="1079" alt="image" src="https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/assets/17765939/a1e0bbd3-f13c-46f6-ad20-0234cb3f8989">

[코드](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/blob/782f82a738f142b3e7f0143939d1c29b9b0c723f/src/main/java/com/ll/moizatimecalculator/boundedContext/selectedTime/service/SelectedTimeService.java#L63)

## ⚙️개발 환경 및 기술 스택
Back-end

Java 17

Spring Boot 3.1.0

Spring Data JPA, MySQL

Test

Junit5

## 기술 적용 및 트러블 슈팅

- [중복 시간 조회 오류](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/wiki/%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%87%E1%85%A9%E1%86%A8-%E1%84%89%E1%85%B5%E1%84%80%E1%85%A1%E1%86%AB-%E1%84%8C%E1%85%A9%E1%84%92%E1%85%AC-%E1%84%8B%E1%85%A9%E1%84%85%E1%85%B2)
