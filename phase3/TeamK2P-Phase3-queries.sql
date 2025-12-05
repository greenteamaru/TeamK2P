/* TeamK2P Phase 3 - 주요 SQL 모음
   (JDBC Application에서 사용한 쿼리들 요약) */

--------------------------
-- 1. 회원 관련 쿼리
--------------------------

-- Q1-LOGIN: 로그인 (이메일 + 비밀번호)
SELECT user_id
FROM users
WHERE email = :email
  AND password = :password;

-- Q2-SIGNUP: 회원가입
INSERT INTO users (
  name, email, password,
  nationality, nickname, language,
  created_at, is_admin
) VALUES (
  :name, :email, :password,
  :nationality, :nickname, :language,
  SYSDATE, 'N'
);

-- Q3-VIEW_PROFILE: 내 정보 조회
SELECT *
FROM users
WHERE user_id = :user_id;

-- Q4-UPDATE_PROFILE: 내 정보 일부 수정
UPDATE users
SET name        = :name,
    nationality = :nationality,
    nickname    = :nickname,
    language    = :language
WHERE user_id = :user_id;

-- Q5-CHANGE_PASSWORD
UPDATE users
SET password = :new_password
WHERE user_id = :user_id;

-- Q6-DELETE_USER (회원 탈퇴)
DELETE FROM users
WHERE user_id = :user_id;

-- Q7-ADMIN_ALL_USERS: 전체 회원 목록
SELECT user_id, name, email, is_admin
FROM users
ORDER BY user_id;

-- Q8-ADMIN_FIND_BY_EMAIL: 이메일 키워드 검색
SELECT user_id, name, email
FROM users
WHERE email LIKE :email_keyword
ORDER BY user_id;


--------------------------
-- 2. 이벤트 관련 쿼리
--------------------------

-- Q9-UPCOMING_EVENTS: 다가오는 이벤트 20개
SELECT e.event_id, e.title, e.start_time, e.end_time,
       v.campus, v.building, v.room,
       c.name AS club_name
FROM events e
JOIN venues v ON e.venue_id = v.venue_id
JOIN clubs  c ON e.club_id  = c.club_id
WHERE e.start_time >= SYSDATE
ORDER BY e.start_time ASC
FETCH FIRST 20 ROWS ONLY;

-- Q10-EVENTS_BY_CAMPUS: 캠퍼스별 이벤트
SELECT e.event_id, e.title, e.start_time,
       v.building, v.room,
       c.name AS club_name
FROM events e
JOIN venues v ON e.venue_id = v.venue_id
JOIN clubs  c ON e.club_id  = c.club_id
WHERE v.campus = :campus
ORDER BY e.start_time;

-- Q11-EVENT_DETAIL: 이벤트 상세 정보
SELECT e.event_id, e.title, e.summary,
       e.start_time, e.end_time,
       e.language, e.capacity, e.fee, e.status,
       v.campus, v.building, v.room,
       c.name AS club_name
FROM events e
JOIN venues v ON e.venue_id = v.venue_id
JOIN clubs  c ON e.club_id  = c.club_id
WHERE e.event_id = :event_id;

-- Q12-EVENT_REVIEW_SUMMARY: 리뷰 개수/평균 평점
SELECT COUNT(*) AS review_cnt,
       AVG(rating) AS avg_rating
FROM reviews
WHERE event_id = :event_id
  AND status   = 'VISIBLE';

-- Q13-MY_REGISTRATIONS: 내 신청 목록
SELECT r.event_id, e.title,
       e.start_time, e.end_time,
       r.status, r.attended
FROM registrations r
JOIN events e ON r.event_id = e.event_id
WHERE r.user_id = :user_id
ORDER BY e.start_time DESC;

-- Q14-REGISTER_EVENT: 이벤트 신청
INSERT INTO registrations (
  user_id, event_id, status, attended, registered_at
) VALUES (
  :user_id, :event_id, 'CONFIRMED', 'N', SYSDATE
);

-- Q15-CANCEL_REGISTRATION: 신청 취소
UPDATE registrations
SET status = 'CANCELLED'
WHERE user_id = :user_id
  AND event_id = :event_id;


--------------------------
-- 3. 클럽/멤버십 관련 쿼리
--------------------------

-- Q16-ALL_CLUBS: 전체 클럽 목록
SELECT c.club_id, c.name, c.club_type, c.status,
       cat.name AS category_name
FROM clubs c
LEFT JOIN categories cat
       ON c.category_id = cat.category_id
ORDER BY c.club_id;

-- Q17-CLUBS_BY_CATEGORY: 카테고리 이름으로 검색
SELECT c.club_id, c.name, c.club_type, c.status,
       cat.name AS category_name
FROM clubs c
LEFT JOIN categories cat
       ON c.category_id = cat.category_id
WHERE LOWER(cat.name) LIKE LOWER(:cat_keyword)
ORDER BY c.club_id;

-- Q18-CLUBS_BY_NAME: 이름 키워드 검색
SELECT club_id, name, club_type, status
FROM clubs
WHERE LOWER(name) LIKE LOWER(:name_keyword)
ORDER BY club_id;

-- Q19-MY_CLUBS: 내가 가입한 클럽
SELECT m.club_id, m.role, m.status,
       c.name, c.club_type
FROM memberships m
JOIN clubs c ON m.club_id = c.club_id
WHERE m.user_id = :user_id
ORDER BY c.club_id;

-- Q20-JOIN_CLUB: 클럽 가입
INSERT INTO memberships (
  user_id, club_id, role,
  joined_at, left_at, status
) VALUES (
  :user_id, :club_id, 'MEMBER',
  SYSDATE, NULL, 'ACTIVE'
);

-- Q21-LEAVE_CLUB: 클럽 탈퇴 (상태 변경)
UPDATE memberships
SET status = 'LEFT',
    left_at = SYSDATE
WHERE user_id = :user_id
  AND club_id = :club_id
  AND status  = 'ACTIVE';


--------------------------
-- 4. 리뷰 관련 쿼리
--------------------------

-- Q22-MY_REVIEWS: 내가 쓴 리뷰 목록
SELECT r.review_id, r.event_id, e.title,
       r.rating, r.review_text,
       r.created_at, r.status
FROM reviews r
JOIN events e ON r.event_id = e.event_id
WHERE r.user_id = :user_id
ORDER BY r.created_at DESC;

-- Q23-REVIEWS_BY_EVENT: 특정 이벤트 리뷰
SELECT r.review_id, u.nickname, u.name,
       r.rating, r.review_text, r.created_at
FROM reviews r
JOIN users u ON r.user_id = u.user_id
WHERE r.event_id = :event_id
  AND r.status   = 'VISIBLE'
ORDER BY r.created_at DESC;

-- Q24-WRITE_REVIEW
INSERT INTO reviews (
  user_id, event_id, rating,
  review_text, status, created_at
) VALUES (
  :user_id, :event_id, :rating,
  :text, 'VISIBLE', SYSDATE
);

-- Q25-UPDATE_REVIEW
UPDATE reviews
SET rating      = :rating,
    review_text = :text
WHERE review_id = :review_id
  AND user_id   = :user_id;

-- Q26-DELETE_REVIEW
DELETE FROM reviews
WHERE review_id = :review_id
  AND user_id   = :user_id;
