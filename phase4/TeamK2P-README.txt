[Team K²P] Phase 4 README
=========================

0. 데모동영상  YouTube Link
-------
https://www.youtube.com/watch?v=1jORWZFXhaM

1. 개요
-------

본 프로젝트는 Phase 2/3에서 설계·구현한 Oracle DB 스키마
(users, clubs, events, memberships, registrations, reviews 등)을 기반으로,
JSP/Servlet과 JDBC를 이용해 웹 환경에서 동작하는
"Campus Club Event Portal" 웹 애플리케이션이다.

주요 목적:

- Phase 3에서 콘솔 기반으로 제공했던 핵심 기능들을
  웹 UI로 옮기고 확장
- 이벤트 신청/취소, 클럽 가입/탈퇴, 리뷰 작성 등
  대부분의 비즈니스 로직을 웹에서 수행
- 트랜잭션을 활용한 동시성 제어(Additional Task 1) 구현

주요 기능 범위:

- 회원/인증 기능
  - 로그인, 로그아웃, 회원가입
  - 내 프로필 조회/수정
  - 비밀번호 변경, 회원 탈퇴
- 이벤트 기능
  - 이벤트 목록(향후 7일 / 전체 / 검색)
  - 이벤트 상세 조회
  - 참가 신청/취소
  - 내 신청 목록
  - 이벤트별 리뷰 조회
- 클럽 기능
  - 클럽 목록 조회(+ 정렬)
  - 내 클럽 목록
  - 클럽 가입/탈퇴
  - 클럽 상세 (클럽 평점, 관련 이벤트 목록)
- 리뷰 기능
  - 내가 쓴 리뷰 목록
  - 리뷰 작성/수정/삭제
- 관리자 기능
  - 회원 목록 조회(관리자 전용 화면)
- 동시성 제어
  - 이벤트 신청 시 정원 초과/중복 신청 방지 로직 구현


2. 실행 환경
------------

- OS        : Windows 10 (64bit) 기준으로 개발 및 테스트
- JDK       : JDK 17
- WAS       : Apache Tomcat 10.1 (Jakarta Servlet 5)
- DBMS      : Oracle (실습 서버 기준, 19c)
- JDBC 드라이버:
    - ojdbc10.jar
    - orai18n.jar (DB 문자셋이 KO16MSWIN949인 경우 필요)
- 프론트엔드:
    - JSP + JSTL
    - Bootstrap 5.3 (CDN) 사용

프로젝트 구조(요약):

- TeamK2P-Phase4/
  - src/main/java/
    - teamk2p/db/
      - DBUtil.java
      - UserDao.java
      - ClubDao.java
      - EventDao.java
    - teamk2p/web/
      - LoginServlet.java
      - LogoutServlet.java
      - SignupServlet.java
      - MyProfileServlet.java
      - MyEventsServlet.java
      - MyClubsServlet.java
      - MyReviewsServlet.java
      - MyDeleteServlet.java
      - EventListServlet.java
      - EventDetailServlet.java
      - EventRegisterServlet.java
      - EventCancelServlet.java
      - EventReviewsServlet.java
      - ClubListServlet.java
      - ClubDetailServlet.java
      - ClubJoinServlet.java
      - ClubLeaveServlet.java
      - AdminUserServlet.java
      (실제 제출 코드 기준으로 파일명/구성은 약간 다를 수 있음)
  - src/main/webapp/
    - index.jsp
    - /views/
      - login.jsp
      - signup.jsp
      - events.jsp
      - event_detail.jsp
      - event_reviews.jsp
      - my_events.jsp
      - my_clubs.jsp
      - my_profile.jsp
      - my_reviews.jsp
      - my_delete.jsp
      - club_detail.jsp
      - admin_users.jsp
      - /common/
        - header.jspf
        - footer.jspf (선택)


3. DB 사전 준비
----------------

Phase 4는 Phase 2/3에서 사용한 PROJECT 스키마를 그대로 사용한다.

1) Oracle DB에 PROJECT 스키마 및 테이블 생성
   - TeamK2P-Phase2-1.sql (테이블/제약조건 생성)
   - TeamK2P-Phase2-2.sql (랜덤 데이터 로딩)

2) Phase 3에서 사용한 데이터 재생성 스크립트
   - TeamK2P-Phase3-data.sql (또는 과제에서 제출한 데이터 로딩 스크립트)
   - users, clubs, events, memberships, registrations, reviews 등
     Phase 2와 유사한 규모로 생성되며, 관리자(admin) 계정을 1개 포함한다.

3) 관리자 계정 예시
   - email   : admin@k2p.com
   - password: admin1234
   - USERS 테이블 내 is_admin 컬럼(또는 유사 플래그)을 'Y'로 세팅

4) JDBC 접속 정보 (DBUtil.java)

DBUtil.java 상단 상수 예시:

    private static final String URL  = "jdbc:oracle:thin:@//localhost:1521/ORCL";
    private static final String USER = "project";
    private static final String PASSWORD = "project";

실제 환경에 맞게 호스트/포트/SID, 계정/비밀번호를 수정한 뒤
서버를 재기동하면 된다.


4. 실행 방법
------------

1) Oracle DB에 PROJECT 스키마 및 예제 데이터 준비
2) IDE(Eclipse/IntelliJ 등)에서 TeamK2P-Phase4 웹 프로젝트를 Import
3) Tomcat 10.x 서버에 프로젝트를 Deploy
4) 브라우저에서 다음 주소로 접속

   - 예시: http://localhost:8080/TeamK2P_phase4/

5) 로그인
   - USERS 테이블에 있는 일반 유저 계정 또는 admin 계정으로 로그인

6) 주요 화면 흐름(예시)

- 메인 페이지 (/index.jsp)
  - “클럽 찾기” → /clubs
  - “향후 7일 이벤트” → /events
  - “전체 이벤트 보기” → /events?all=true

- 상단 헤더 (header.jspf)
  - 비로그인 상태:
    - 로그인, 회원가입 메뉴
  - 로그인 상태:
    - 프로필 / 설정 (/me/profile)
    - 내 이벤트 (/me/events)
    - 내 클럽 (/me/clubs)
    - 내 리뷰 (/me/reviews)
    - (관리자일 경우) 관리자: 회원 관리 (/admin/users)
    - 로그아웃 (/logout)


5. 주요 기능 설명
-----------------

5.1 회원/인증 기능 (UserDao + Authentication 관련 서블릿)
--------------------------------------------------------

- 회원가입 (/signup)
  - signup.jsp에서 이름, 이메일, 비밀번호, 닉네임, 국적, 언어 등을 입력
  - USERS 테이블에 INSERT
  - 이메일 UNIQUE 제약으로 중복 가입 방지

- 로그인 (/login)
  - 입력: 이메일, 비밀번호
  - UserDao.login()으로 users 테이블 조회
  - 성공 시 LoginUser DTO를 세션에 저장 (session.setAttribute("loginUser", …))

- 로그아웃 (/logout)
  - 세션 무효화 후 메인 페이지 또는 로그인 화면으로 리다이렉트

- 프로필 조회/수정 (/me/profile)
  - UserDao.getProfile() 을 통해 현재 로그인 사용자의 프로필 정보 조회
  - 이름, 닉네임, 국적, 언어 등을 수정 가능
  - UserDao.updateProfile() 로 users 테이블 UPDATE

- 비밀번호 변경
  - 기존 비밀번호 확인 후, 새 비밀번호로 UPDATE
  - Phase 3에서 구현했던 로직과 동일하게 JDBC 기반 처리

- 회원 탈퇴 (/me/delete)
  - 내 계정을 삭제하거나, 논리 삭제(상태 변경)하도록 구현
  - 삭제 후 세션 무효화 및 메인 페이지로 이동


5.2 클럽 기능 (ClubDao + 관련 서블릿/JSP)
----------------------------------------

- 클럽 목록 (/clubs)
  - ClubDao.listActiveClubs() 를 통해 ACTIVE 상태의 클럽 목록 조회
  - 카테고리, 타입, 상태, 생성일, 현재 멤버 수, 누적 멤버 수 등을 표시
  - 필요 시 정렬 옵션(이름순 / 멤버수순 / 최신순 등) 지원

- 내 클럽 (/me/clubs)
  - ClubDao.listMyClubs()
  - memberships + clubs JOIN으로
    내가 가입한 클럽, 내 역할(role), 내 membership 상태(status) 조회
  - 각 행에서 해당 클럽 상세(/clubs/detail?club_id=...)로 이동 가능

- 클럽 상세 (/clubs/detail)
  - ClubDao.getClubDetail()
  - 표시 내용:
    - 클럽 기본 정보 (이름, 카테고리, 유형, 상태, 개설일, 소개)
    - 현재 활동 중인 멤버 수, 누적 멤버 수
    - 이 클럽 이벤트 전체에 대한 평균 평점, 리뷰 개수
    - (로그인 사용자의 경우) 내 멤버십 정보 (역할, 상태, 가입/탈퇴 일시)
    - 이 클럽의 향후 이벤트 / 지난 이벤트 목록
      (EventDao.listClubUpcomingEvents / listClubPastEvents)

- 클럽 가입 (/clubs/join)
  - ClubDao.joinClub()
  - 이미 가입 이력이 있는 경우 상태(status)만 ACTIVE로 되살리는 로직 포함
  - 결과 코드: "JOINED", "REJOIN", "ALREADY" 등을 활용

- 클럽 탈퇴 (/clubs/leave)
  - ClubDao.leaveClub()
  - 현재 ACTIVE 상태인 memberships 행을 찾아 status='LEFT', left_at=SYSDATE로 UPDATE


5.3 이벤트 기능 (EventDao + 관련 서블릿/JSP)
--------------------------------------------

- 이벤트 목록 (/events)
  - 기본: 향후 7일 이내의 이벤트 (EventDao.listUpcomingEvents())
  - `?all=true` : 전체 이벤트 (EventDao.listAllEvents())
  - `?q=키워드` : 제목/클럽명/장소/캠퍼스명으로 검색 (EventDao.searchEvents())
  - 각 이벤트 카드에 제목, 클럽명, 장소, 시작 시간, 정원, 현재 신청 인원 표시

- 이벤트 상세 (/events/detail?event_id=...)
  - EventDao.getEventDetail()
  - 표시 내용:
    - 이벤트 제목, 소속 클럽, 장소
    - 시작/종료 시간
    - 정원, 현재 신청 인원, 남은 자리
    - 참가비(fee), 상태(status)
    - 이벤트 평균 평점, 리뷰 개수
    - 상세 설명(summary)

- 이벤트 신청 (/events/register)  **[동시성 제어 대상]**
  - EventRegisterServlet → EventDao.registerForEvent()
  - 정원 및 중복 신청을 트랜잭션으로 제어
  - 결과 코드:
    - "OK"      : 신청 성공
    - "FULL"    : 정원 초과
    - "DUP"     : 이미 신청한 사용자
    - "NO_EVENT": 잘못된 event_id
  - 이벤트가 이미 종료되었거나 정원이 0인 경우 버튼 비활성화

- 신청 취소 (/events/cancel)
  - EventDao.cancelRegistration()
  - 현재 PENDING/CONFIRMED/WAITLISTED 상태인 신청을 CANCELLED로 변경

- 내 이벤트 (/me/events)
  - EventDao.listMyEvents()
  - 내가 신청한 이벤트 목록을 시간 역순으로 표시
  - venues와 JOIN하여 장소명도 함께 출력


5.4 리뷰 기능 (EventDao 리뷰 관련 메서드)
----------------------------------------

- 내가 쓴 리뷰 (/me/reviews)
  - EventDao.listMyReviews()
  - 내가 작성한 리뷰들을 이벤트 제목과 함께 조회

- 리뷰 작성/수정 (이벤트 상세 화면 내 폼)
  - EventDao.writeOrUpdateReview(userId, eventId, rating, reviewText)
  - 해당 (user_id, event_id)에 기존 리뷰가 없으면 INSERT,
    있으면 UPDATE 하는 upsert 패턴
  - rating 범위 1~5 보정, status='VISIBLE' 기본값

- 리뷰 삭제
  - EventDao.deleteMyReview(userId, eventId)
  - 현재 로그인 사용자가 해당 이벤트에 남긴 리뷰를 삭제

- 이벤트별 전체 리뷰 (/events/reviews?event_id=...)
  - EventDao.listEventReviews()
  - 해당 이벤트의 VISIBLE 리뷰 목록을 조회
  - 작성자 이름, 평점, 내용, 작성일을 테이블로 표시


5.5 관리자 기능 (AdminUserServlet)
----------------------------------

- 관리자 전용 회원 목록 (/admin/users)
  - UserDao.listUsersForAdmin() (예시) 를 통해 전체 사용자 목록 조회
  - user_id, 이름, 이메일, 관리자 여부(is_admin) 등을 표시
  - header.jspf에서 `loginUser.isAdmin()` 이 true일 때만 메뉴 표시

- 추가적인 수정/삭제 기능은 구현하지 않고,
  “관리자 전용 조회 화면”이라는 점을 강조하는 용도의 페이지이다.


6. 동시성 제어 (Additional Task 1 요약)
----------------------------------------

- 대상 기능: 이벤트 신청 (/events/register)
- 핵심 구현:
  - `SELECT ... FOR UPDATE` 를 이용하여
    특정 event_id 행에 대한 행 잠금을 걸고 capacity / current_cnt 조회
  - capacity 를 초과하면 INSERT 없이 `"FULL"` 코드 반환
  - REGISTRATIONS 테이블에 UNIQUE(user_id, event_id) 제약을 두고,
    ORA-00001 발생 시 `"DUP"` 코드로 처리
  - 서블릿 레벨에서 commit / rollback 을 명시적으로 수행하여
    정원 체크 + INSERT 를 하나의 트랜잭션으로 보장

자세한 설명은
`TeamK2P-Additional_task1.txt`
파일에 따로 정리하였다.


7. 유의 사항
------------

- DB 접속 정보(URL, USER, PASSWORD)는 DBUtil.java에서 환경에 맞게 수정해야 한다.
- DB 문자셋이 KO16MSWIN949인 경우 orai18n.jar를 WEB-INF/lib에 포함해야
  한글 데이터 입출력이 깨지지 않는다.
- 실제 서비스 환경이라면 회원 탈퇴, 리뷰 삭제 등에서
  FK 제약 및 데이터 보존 정책(논리 삭제, soft delete 등)을
  더 엄격히 설계해야 하나,
  본 과제에서는 과제 범위에 맞춰 단순 삭제 또는 상태 변경으로 처리하였다.
- UI는 Bootstrap 5의 기본 컴포넌트를 활용하여
  최소한의 반응형 레이아웃만 구성하였다.
  (추가적인 디자인·CSS 튜닝은 과제 필수 요구 사항에서 제외)

