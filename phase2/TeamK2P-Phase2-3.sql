
/* ======================
   Type 1: Single-table
   ====================== */

-- Q1 (T1) 최근 14일 신규 사용자
SELECT user_id, name, email, created_at
FROM users
WHERE created_at >= TRUNC(SYSDATE) - 14;

-- Q2 (T1) 용량 큰(SCHEDULED) 이벤트
SELECT event_id, title, capacity, start_time
FROM events
WHERE status = 'SCHEDULED'
  AND capacity >= 100;


/* ============================================
   Type 2: Multi-way join (WHERE에 조인식)
   ============================================ */

-- Q3 (T2) 이벤트 + 클럽 + 카테고리명
SELECT e.event_id, e.title, c.name AS club_name, cat.name AS category_name
FROM events e, clubs c, categories cat
WHERE e.club_id = c.club_id
  AND c.category_id = cat.category_id;

-- Q4 (T2) 활성 멤버십 + 사용자/클럽
SELECT m.membership_id, u.name AS user_name, c.name AS club_name, m.role
FROM memberships m, users u, clubs c
WHERE m.user_id = u.user_id
  AND m.club_id = c.club_id
  AND m.status = 'ACTIVE';


/* ==========================================================
   Type 3: Agg + Multi-join + GROUP BY  (예: TPC-H Q5 느낌)
   ========================================================== */

-- Q5 (T3) 카테고리별 이벤트 수
SELECT cat.name AS category, COUNT(*) AS event_cnt
FROM events e
JOIN clubs c   ON e.club_id = c.club_id
JOIN categories cat ON c.category_id = cat.category_id
GROUP BY cat.name;

-- Q6 (T3) 이벤트별 평균 평점 및 리뷰 수
SELECT e.event_id, e.title, AVG(r.rating) AS avg_rating, COUNT(*) AS review_cnt
FROM events e
JOIN reviews r ON r.event_id = e.event_id
GROUP BY e.event_id, e.title;


/* ======================
   Type 4: Subquery
   ====================== */

-- Q7 (T4) 한 번이라도 등록(registration)한 사용자
SELECT u.user_id, u.name, u.email
FROM users u
WHERE u.user_id IN (SELECT DISTINCT r.user_id FROM registrations r);

-- Q8 (T4) 이벤트가 한 건도 없는 클럽
SELECT c.club_id, c.name
FROM clubs c
WHERE c.club_id NOT IN (SELECT DISTINCT e.club_id FROM events e);


/* ============================
   Type 5: EXISTS Subquery
   ============================ */

-- Q9 (T5) 'LEADER' 멤버십이 존재하는 사용자
SELECT u.user_id, u.name
FROM users u
WHERE EXISTS (
  SELECT 1 FROM memberships m
  WHERE m.user_id = u.user_id AND m.role = 'LEADER'
);

-- Q10 (T5) 리뷰가 1개 이상 존재하는 이벤트
SELECT e.event_id, e.title
FROM events e
WHERE EXISTS (SELECT 1 FROM reviews r WHERE r.event_id = e.event_id);


/* =======================================================
   Type 6: Selection + Projection + IN predicates
   ======================================================= */

-- Q11 (T6) KNU/Eng 캠퍼스에서 열리는 이벤트
SELECT e.event_id, e.title, v.campus, v.building
FROM events e
JOIN venues v ON e.venue_id = v.venue_id
WHERE v.campus IN ('KNU', 'Eng');

-- Q12 (T6) 한국어/영어 사용자 목록
SELECT user_id, name, language
FROM users
WHERE language IN ('ko','en');


/* ======================================
   Type 7: Inline view (인라인 뷰 활용)
   ====================================== */

-- Q13 (T7) 사용자별 등록 건수 Top 10
SELECT *
FROM (
  SELECT u.user_id, u.name, COUNT(r.registration_id) AS reg_cnt
  FROM users u
  LEFT JOIN registrations r ON r.user_id = u.user_id
  GROUP BY u.user_id, u.name
  ORDER BY reg_cnt DESC
)
WHERE ROWNUM <= 10;

-- Q14 (T7) 이벤트별 평균 평점 상위 5
SELECT *
FROM (
  SELECT e.event_id, e.title, AVG(r.rating) AS avg_rating, COUNT(*) AS cnt
  FROM events e
  JOIN reviews r ON r.event_id = e.event_id
  GROUP BY e.event_id, e.title
  HAVING COUNT(*) >= 3
  ORDER BY avg_rating DESC, cnt DESC
)
WHERE ROWNUM <= 5;


/* ===================================================
   Type 8: Multi-join + ORDER BY (WHERE에 조인식)
   =================================================== */

-- Q15 (T8) 향후 7일 내 시작 이벤트(시작시간 오름차순)
SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, e.start_time
FROM events e, clubs c, venues v
WHERE e.club_id = c.club_id
  AND e.venue_id = v.venue_id
  AND e.start_time BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7
ORDER BY e.start_time ASC;

-- Q16 (T8) 최신 리뷰 20건 (이벤트 제목 포함, 최신순)
SELECT *
FROM (
  SELECT r.review_id, e.title, r.rating, r.created_at
  FROM reviews r, events e
  WHERE r.event_id = e.event_id
  ORDER BY r.created_at DESC
)
WHERE ROWNUM <= 20;


/* ======================================================================
   Type 9: Agg + Multi-join + GROUP BY + ORDER BY (예: TPC-H Q3 느낌)
   ====================================================================== */

-- Q17 (T9) 클럽별 등록 수(=이벤트 신청 수) 상위 10
SELECT *
FROM (
  SELECT c.name AS club_name, COUNT(r.registration_id) AS reg_cnt
  FROM registrations r
  JOIN events e ON r.event_id = e.event_id
  JOIN clubs  c ON e.club_id  = c.club_id
  GROUP BY c.name
  ORDER BY reg_cnt DESC, c.name
)
WHERE ROWNUM <= 10;

-- Q18 (T9) 카테고리별 평균 참가비와 이벤트 수 (많은 순)
SELECT cat.name AS category, ROUND(AVG(e.fee),2) AS avg_fee, COUNT(*) AS event_cnt
FROM events e
JOIN clubs c      ON e.club_id = c.club_id
JOIN categories cat ON c.category_id = cat.category_id
GROUP BY cat.name
ORDER BY event_cnt DESC, avg_fee DESC;


/* ============================================
   Type 10: SET operations (UNION/MINUS/INTERSECT)
   ============================================ */

-- Q19 (T10) 멤버십은 있으나 등록은 한 번도 안 한 사용자
SELECT u.user_id, u.name
FROM users u
JOIN memberships m ON m.user_id = u.user_id
GROUP BY u.user_id, u.name
MINUS
SELECT u.user_id, u.name
FROM users u
JOIN registrations r ON r.user_id = u.user_id
GROUP BY u.user_id, u.name;

-- Q20 (T10) "등록자" ∩ "리뷰어"(이벤트 리뷰 남긴 사용자)
SELECT user_id, name
FROM (
  SELECT DISTINCT u.user_id, u.name
  FROM users u JOIN registrations r ON r.user_id = u.user_id
)
INTERSECT
SELECT user_id, name
FROM (
  SELECT DISTINCT u.user_id, u.name
  FROM users u JOIN reviews rv ON rv.user_id = u.user_id
);