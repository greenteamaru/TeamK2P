[Team K²P] Phase 3 README
=========================

1. 개요
-------

본 프로그램은 Phase 2에서 설계/구현한 Oracle DB 스키마
(users, clubs, events, memberships, registrations, reviews 등)을 대상으로,
JDBC를 이용하여 콘솔 기반에서 실제 데이터를 조회/삽입/수정/삭제할 수 있는
Application이다.

주요 기능은 다음과 같다.

- 회원 기능: 회원가입, 로그인, 내 정보 조회/수정, 비밀번호 변경, 회원 탈퇴
- 이벤트 기능: 이벤트 목록 조회, 캠퍼스별 조회, 상세 조회, 이벤트 신청/취소, 내 신청 목록
- 클럽 기능: 클럽 목록/검색, 카테고리별 검색, 내 클럽 목록, 클럽 가입/탈퇴
- 리뷰 기능: 내가 쓴 리뷰 조회, 이벤트별 리뷰 조회, 리뷰 작성/수정/삭제
- 관리자 기능: 전체 회원 조회, 이메일 키워드로 회원 검색

즉, Phase 2에서 정의한 대부분의 릴레이션에 대해
SELECT/INSERT/UPDATE/DELETE가 JDBC를 통해 동적으로 수행된다.


2. 실행 환경
------------

- OS        : Windows 10 (64bit) 기준으로 개발 및 테스트
- JDK       : JDK 17
- IDE       : Eclipse IDE
- DBMS    : Oracle (실습 서버 기준, 19c)
- JDBC 드라이버:
    - ojdbc10.jar
    - orai18n.jar   (DB 문자셋이 KO16MSWIN949인 경우 필요)

프로젝트 구조(요약):

- TeamK2P-Phase3/
  - src/
    - MainMenu.java
    - db/DBUtil.java
    - user/
        - UserMenu.java
        - UserService.java
    - event/
        - EventMenu.java
        - EventService.java
    - club/
        - ClubMenu.java
        - ClubService.java
    - review/
        - ReviewMenu.java
        - ReviewService.java
  - lib/
    - ojdbc10.jar
    - orai18n.jar
  - .classpath / .project 등 Eclipse 설정 파일


3. DB 사전 준비
----------------

본 Application은 Phase 2에서 생성한 PROJECT 스키마를 그대로 사용한다.

1) Oracle DB에 PROJECT 스키마 및 테이블 생성
   - TeamK2P-Phase2-1.sql 실행 (테이블/제약조건 생성)
   - TeamK2P-Phase2-2.sql 실행 (랜덤 데이터 로딩)

2) Phase 3에서 사용한 데이터 재생성 스크립트
   - TeamK2P-Phase3-data.sql (제출 시 첨부)
   - users 1000명 이상, clubs/events 등 Phase2와 유사한 규모로 생성되며
     admin 계정 1개를 포함한다.

3) 관리자 계정
   - email   : admin@k2p.com
   - password: admin1234

4) JDBC 접속 정보 (DBUtil.java)

DBUtil.java 상단 상수:

    private static final String URL  = "jdbc:oracle:thin:@//localhost:1521/ORCL";
    private static final String USER = "project";
    private static final String PASSWORD = "project";

실행 환경에 따라 ORCL / XE, 계정명/비밀번호가 다를 경우
위 상수를 수정 후 다시 실행하면 된다.


4. 실행 방법
------------

1) Oracle DB에 PROJECT 스키마와 예제 데이터가 준비된 상태에서
2) Eclipse에서 프로젝트 TeamK2P-Phase3를 Import (또는 zip 해제 후 Open) 한다.
3) ojdbc10.jar, orai18n.jar가 lib/에 포함되어 있으며,
   .classpath에 이미 추가되어 있어 별도 설정 없이 실행 가능하다.
4) MainMenu.java를 우클릭하여
   Run As → Java Application 으로 실행한다.

콘솔 메뉴 흐름:

- MAIN MENU
  - 1. 로그인
  - 2. 회원가입
  - 0. 종료

- 로그인 성공 후 (일반 사용자)
  - 1. 내 정보 조회
  - 2. 내 정보 수정
  - 3. 비밀번호 변경
  - 4. 회원 탈퇴
  - 5. 이벤트 기능
  - 6. 클럽 기능
  - 7. 리뷰 기능
  - 0. 로그아웃

- 로그인 성공 후 (관리자: admin@k2p.com)
  - 1. 내 정보 조회
  - 2. 비밀번호 변경
  - 3. 전체 회원 조회
  - 4. 이메일로 회원 검색
  - 0. 로그아웃


5. 주요 기능 설명
-----------------

5.1 회원 기능 (UserService, UserMenu)
--------------------------------------

- 회원가입
  - 입력: 이름, 이메일(ID), 비밀번호, 국적, 닉네임, 언어
  - 기능: users 테이블에 새로운 회원 INSERT
  - 이메일(email)은 UNIQUE로 제약되어 있어 중복 시 예외 처리

- 로그인
  - 입력: 이메일, 비밀번호
  - 기능: users에서 이메일+비번으로 user_id 조회
  - 성공 시 user_id 반환, 실패 시 오류 메시지 출력

- 내 정보 조회/수정
  - 내 정보 조회: users에서 PK(user_id)로 전체 컬럼 조회
  - 내 정보 수정: 이름/국적/닉네임/언어 중 선택하여 UPDATE

- 비밀번호 변경
  - 현재 user_id 기준으로 password 컬럼 UPDATE

- 회원 탈퇴
  - users에서 해당 user_id를 DELETE
  - FK 제약으로 인해 실제 서비스라면 CASCADE 처리가 필요하지만,
    본 과제에서는 단순 삭제로 구현.


5.2 이벤트 기능 (EventService, EventMenu)
-----------------------------------------

- 다가오는 이벤트 보기
  - events, venues, clubs를 JOIN하여
    현재 시각 이후(start_time >= SYSDATE)인 이벤트 20개까지 출력.

- 캠퍼스별 이벤트 보기
  - 입력: 캠퍼스(KNU, Med, Eng 등)
  - venues.campus = ? 조건으로 필터링

- 이벤트 상세보기
  - 입력: event_id
  - events + venues + clubs JOIN으로 상세 정보 출력
  - 추가로 reviews에서 COUNT/AVG를 구해 리뷰 개수와 평균 평점 표시

- 내 신청 목록 보기
  - registrations + events JOIN
  - 현재 로그인 사용자(user_id)의 신청 내역을 시간 역순으로 출력

- 이벤트 신청
  - 입력: event_id
  - registrations에 (user_id, event_id, status='CONFIRMED', attended='N') INSERT
  - UNIQUE 제약으로 이중 신청 시 예외 처리

- 신청 취소
  - registrations에서 (user_id, event_id) 행의 status를 'CANCELLED'로 UPDATE


5.3 클럽 기능 (ClubService, ClubMenu)
-------------------------------------

- 전체 클럽 목록 보기
  - clubs LEFT JOIN categories
  - 클럽 ID, 이름, 유형, 카테고리, 상태 출력

- 카테고리로 클럽 검색
  - 입력: 카테고리 이름 키워드
  - categories.name LIKE '%키워드%' 조건으로 필터링

- 이름으로 클럽 검색
  - clubs.name LIKE '%키워드%' 검색

- 내가 가입한 클럽 보기
  - memberships JOIN clubs
  - 내 역할(role), 상태(status) 포함 출력

- 클럽 가입
  - memberships에 (user_id, club_id, role='MEMBER', status='ACTIVE') INSERT
  - UNIQUE(user_id, club_id) 제약으로 중복 가입 방지

- 클럽 탈퇴
  - memberships의 status='ACTIVE'인 행을 찾아
    status='LEFT', left_at=SYSDATE로 UPDATE


5.4 리뷰 기능 (ReviewService, ReviewMenu)
-----------------------------------------

- 내가 쓴 리뷰 목록
  - reviews JOIN events
  - 리뷰 ID, 이벤트 제목, 평점, 내용, 작성일, 상태 출력

- 특정 이벤트 리뷰 보기
  - 입력: event_id
  - reviews JOIN users
  - 해당 이벤트의 VISIBLE 리뷰만 조회하여 작성자 정보와 함께 출력

- 리뷰 작성
  - 입력: event_id, 평점(1~5), 리뷰 내용
  - reviews에 INSERT (status='VISIBLE', created_at=SYSDATE)

- 리뷰 수정
  - 입력: review_id, 새 평점, 새 내용
  - 자신의 리뷰(user_id 일치)에 한해 rating, review_text UPDATE

- 리뷰 삭제
  - 입력: review_id
  - 자신의 리뷰(user_id 일치)에 한해 DELETE


6. 쿼리 수정/사용 내역 (Phase 2 → Phase 3)
-------------------------------------------

Phase 2의 TeamK2P-Phase2-3.sql에 포함된 20개 질의 중,
다음과 같은 형태로 수정/응용하여 프로그램에 사용하였다.

예시:

- Q2 (T1) "용량 큰(SCHEDULED) 이벤트"
  → EventService.listUpcomingEvents에서
    status 필터 대신 start_time >= SYSDATE 조건 및
    venues, clubs와 JOIN, FETCH FIRST 20 ROWS ONLY 추가.

- Q5, Q6 (조인 기반 조회)
  → 이벤트 상세보기, 캠퍼스별 이벤트 조회 등에서
    events–venues–clubs 조인으로 확장 사용.

- Q9 (T5) 'LEADER' 멤버십 존재 여부
  → 클럽 가입/내 클럽 조회 기능에서
    memberships와 clubs를 JOIN하여 멤버십 상태 확인/출력하는 데 활용.

- Q10 (T5) 리뷰가 1개 이상 존재하는 이벤트
  → 이벤트 상세보기에서 reviews COUNT/AVG를 구해
    리뷰 유무 및 평균 평점을 함께 표시.

그 외:
- 사용자 검색(이메일 LIKE), 전체 회원 조회, 이벤트 신청/취소, 리뷰 작성/수정/삭제 등
  Phase 2에서 정의한 스키마 및 제약조건을 그대로 따른
  SELECT/INSERT/UPDATE/DELETE 문을 추가로 사용하였다.

자세한 SQL은 별도의 파일
`TeamK2P-Phase3-queries.sql` 에 정리하여 함께 제출한다.


7. 유의 사항
------------

- DB 연결 정보(URL, USER, PASSWORD)는 DBUtil.java에서 수정 가능하다.
- DB 문자셋이 KO16MSWIN949인 경우 orai18n.jar가 필요하며,
  프로젝트의 lib 폴더에 포함되어 있다.
- 실제 서비스라면 회원 탈퇴 시 FK 제약으로 인한 데이터 정리가 필요하지만,
  본 과제에서는 단순 DELETE로 구현하였다.
- 콘솔 입력 시 숫자(ID 등)를 잘못 입력하면
  NumberFormatException 방지를 위해 기본적인 검증만 수행한다.
