[Team K²P] Phase 2 README

------------------------------------------------------------
1. 실행 환경
- DBMS: Oracle (실습 확인: 19c / 21c 이상 호환)
- 클라이언트: SQL*Plus 또는 SQL Developer
- NLS_DATE_FORMAT은 기본값 사용 (DATE 타입은 날짜+시간 저장)

------------------------------------------------------------
2. 파일 구성
- TeamX-Phase2-1.sql  : 테이블 재생성(DDL)
- TeamX-Phase2-2.sql  : 랜덤 데이터 로딩 스크립트 (각 테이블 ≥100, 총 5,000+)
- TeamX-Phase2-3.sql  : 질의 20개 (Type 1~10 충족)
- TeamX-etr_mapping.txt: ER→Relational 매핑 문서(본 파일 별도 제공)

------------------------------------------------------------
3. 실행 순서
1) DDL 생성: TeamX-Phase2-1.sql
  · 기존 테이블이 있다면 DROP 후 재생성.
  · 테이블: users, categories, venues, clubs, events, memberships, registrations, reviews
2) 데이터 적재: TeamX-Phase2-2.sql
  · 각 테이블 ≥ 100건, 전체 ≥ 5,000건 무작위 삽입.
  · UNIQUE 충돌 등은 예외 처리 후 건너뜀.
3) 검증 쿼리 실행: 아래 “8. 검증 쿼리” 참조.
4) 질의 실행: TeamX-Phase2-3.sql (Type 1~10 총 20문)

------------------------------------------------------------

4. 스키마 핵심(요약)
- users(user_id PK, email UNIQUE, created_at DEFAULT SYSDATE)
- categories(category_id PK, name UNIQUE)
- venues(venue_id PK)
- clubs(club_id PK, creator_user_id FK→users, category_id FK→categories, - status∈{ACTIVE,INACTIVE,ARCHIVED})
- events(event_id PK, club_id FK, venue_id FK, start_time/end_time, capacity≥0, fee≥0, status∈{SCHEDULED,CANCELLED,COMPLETED,POSTPONED})
- memberships(membership_id PK, user_id FK, club_id FK, UNIQUE(user_id,club_id), status∈{ACTIVE,LEFT,BANNED})
- registrations(registration_id PK, user_id FK, event_id FK, UNIQUE(user_id,event_id), attended∈{Y,N}, status∈{PENDING,CONFIRMED,CANCELLED,WAITLISTED})
- reviews(review_id PK, user_id FK, event_id FK, rating 1~5, status∈{VISIBLE,HIDDEN,FLAGGED})
※ 동일 (user,event)에 대해 여러 리뷰 허용(UNIQUE 미적용)
  · 참고(ER→Relational):
	- ER의 **복합속성 datetime**은 물리 스키마에서 start_time, end_time 두 컬럼으로 분해 저장.

------------------------------------------------------------
5. 데이터 생성 파라미터(기본값)
- users 1000 / categories 120 / venues 120 / clubs 300 / events 1200 / memberships 1400 / registrations 1200 / reviews 800
→ 총합 6,140+
- 필요 시 TeamX-Phase2-2.sql 상단 변수로 손쉽게 조정 가능.

------------------------------------------------------------
6. 관계 및 카디널리티/참여(문서 일관성)
- CREATE (USER—CLUB): 1:N, CLUB=Total, USER=Partial
- BELONG TO (CLUB—CATEGORY): N:1, CLUB=Total, CATEGORY=Partial
- HOST (CLUB—EVENT): 1:N, EVENT=Total, CLUB=Partial
- IS HELD AT (VENUE—EVENT): 1:N, EVENT=Total, VENUE=Partial
- MEMBERSHIP (USER↔CLUB): M:N (교차 엔티티), 양쪽 Partial
- REGISTRATION (USER↔EVENT): M:N (교차 엔티티), 양쪽 Partial
- WRITE (USER—REVIEW): 1:N, REVIEW=Total, USER=Partial
- IS ABOUT (REVIEW—EVENT): N:1, REVIEW=Total, EVENT=Partial

------------------------------------------------------------
7. 성능 관련 인덱스(주요)
- FK 탐색: clubs(creator_user_id, category_id), events(club_id, venue_id), memberships(user_id, club_id), registrations(user_id, event_id), reviews(event_id)
- 시계열 조회: events(start_time)

------------------------------------------------------------
8. 검증 쿼리 (건수 점검)
SELECT 'USERS' t, COUNT(*) n FROM users
UNION ALL SELECT 'CATEGORIES', COUNT(*) FROM categories
UNION ALL SELECT 'VENUES', COUNT(*) FROM venues
UNION ALL SELECT 'CLUBS', COUNT(*) FROM clubs
UNION ALL SELECT 'EVENTS', COUNT(*) FROM events
UNION ALL SELECT 'MEMBERSHIPS', COUNT(*) FROM memberships
UNION ALL SELECT 'REGISTRATIONS', COUNT(*) FROM registrations
UNION ALL SELECT 'REVIEWS', COUNT(*) FROM reviews;

------------------------------------------------------------
9. 질의 세트 개요 (Type 1~10, 총 20문)
- Type1: 단일 테이블 S+P … 2문 (최근 가입자, 대용량 예정 이벤트)
- Type2: WHERE 조인 … 2문 (이벤트-클럽-카테고리, 활성 멤버십)
- Type3: 집계+조인+GROUP BY … 2문 (카테고리별 이벤트 수, 이벤트 평균 평점)
- Type4: 서브쿼리 … 2문 (등록 경험 사용자, 이벤트 없는 클럽)
- Type5: EXISTS … 2문 (리더 보유 사용자, 리뷰 있는 이벤트)
- Type6: IN … 2문 (특정 캠퍼스 이벤트, ko/en 사용자)
- Type7: 인라인 뷰 … 2문 (등록 Top10 사용자, 평균 평점 Top5 이벤트)
- Type8: 다중조인+ORDER … 2문 (7일 내 시작 이벤트, 최신 리뷰 20건)
- Type9: 집계+조인+GROUP BY+ORDER … 2문 (클럽별 등록 상위, 카테고리별 평균 참가비)
- Type10: 집합연산 … 2문 (멤버십은 있으나 미등록 사용자, 등록자∩리뷰어)

→ 상세 SQL은 TeamX-Phase2-3.sql에 포함.