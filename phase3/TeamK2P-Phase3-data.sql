SET SERVEROUTPUT ON;

------------------------------------------------------------
-- 0) 모든 테이블 데이터 삭제 (FK 순서 지킴)
------------------------------------------------------------
DELETE FROM reviews;
DELETE FROM registrations;
DELETE FROM memberships;
DELETE FROM events;
DELETE FROM clubs;
DELETE FROM venues;
DELETE FROM categories;
DELETE FROM users;
COMMIT;

------------------------------------------------------------
-- 1) USERS 다시 생성 (admin + 랜덤 유저)
------------------------------------------------------------
-- 1-1) admin
INSERT INTO users(name, email, password, nationality, nickname, language, created_at, is_admin)
VALUES ('Admin', 'admin@k2p.com', 'admin1234', 'KR', '관리자', 'ko', SYSDATE, 'Y');
COMMIT;

-- 1-2) 랜덤 유저
DECLARE
  v_users PLS_INTEGER := 1000;
BEGIN
  FOR i IN 1 .. v_users LOOP
    INSERT INTO users(
      name, email, password, nationality, nickname, language, created_at, is_admin
    ) VALUES (
      'User '||TO_CHAR(i,'FM0000'),
      'user'||TO_CHAR(i,'FM0000')||'@ex.com',
      'pass1234',
      CASE MOD(i,8)
        WHEN 0 THEN 'KR' WHEN 1 THEN 'US' WHEN 2 THEN 'JP' WHEN 3 THEN 'CN'
        WHEN 4 THEN 'VN' WHEN 5 THEN 'DE' WHEN 6 THEN 'FR' ELSE 'IN' END,
      'nick'||i,
      CASE MOD(i,6)
        WHEN 0 THEN 'ko' WHEN 1 THEN 'en' WHEN 2 THEN 'ja'
        WHEN 3 THEN 'zh' WHEN 4 THEN 'de' ELSE 'en' END,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0,365)),
      'N'
    );
  END LOOP;
  COMMIT;
END;
/

------------------------------------------------------------
-- 2) 나머지 테이블 랜덤 데이터 생성
------------------------------------------------------------
DECLARE
  v_categories  PLS_INTEGER := 120;
  v_venues      PLS_INTEGER := 120;
  v_clubs       PLS_INTEGER := 300;
  v_events      PLS_INTEGER := 1200;
  v_memberships PLS_INTEGER := 1400;
  v_regs        PLS_INTEGER := 1200;
  v_reviews     PLS_INTEGER := 800;

  -- ID 범위
  c_min NUMBER; c_max NUMBER;
  v_min NUMBER; v_max NUMBER;
  cb_min NUMBER; cb_max NUMBER;
  e_min NUMBER; e_max NUMBER;

  -- 실제 user_id / club_id / event_id 목록 저장용
  TYPE t_num_arr IS TABLE OF NUMBER;

  v_user_ids  t_num_arr;
  v_club_ids  t_num_arr;
  v_event_ids t_num_arr;

  r_user  NUMBER;
  r_cat   NUMBER;
  r_venue NUMBER;
  r_club  NUMBER;
  r_event NUMBER;

  d_start    DATE;
  d_join     DATE;
  d_left     DATE;
  dur_hours  NUMBER;

  dup EXCEPTION;
  PRAGMA EXCEPTION_INIT(dup, -00001);
BEGIN
  DBMS_RANDOM.SEED(TO_NUMBER(TO_CHAR(SYSTIMESTAMP,'FF6')));

  --------------------------------------------------------
  -- 2-1) CATEGORIES
  --------------------------------------------------------
  FOR i IN 1 .. v_categories LOOP
    INSERT INTO categories(name, description)
    VALUES (
      'Category '||TO_CHAR(i,'FM0000'),
      'Random category #'||i
    );
  END LOOP;
  COMMIT;

  --------------------------------------------------------
  -- 2-2) VENUES
  --------------------------------------------------------
  FOR i IN 1 .. v_venues LOOP
    INSERT INTO venues(name, campus, building, room, map_url)
    VALUES (
      'Venue '||TO_CHAR(i,'FM0000'),
      CASE MOD(i,3)
        WHEN 0 THEN 'KNU'
        WHEN 1 THEN 'Med'
        ELSE 'Eng' END,
      CHR(65 + MOD(i,5)),
      CASE MOD(i,4)
        WHEN 0 THEN '101'
        WHEN 1 THEN '201'
        WHEN 2 THEN '3F'
        ELSE 'B1' END,
      NULL
    );
  END LOOP;
  COMMIT;

  --------------------------------------------------------
  -- 2-3) ID/목록 읽기
  --------------------------------------------------------
  SELECT MIN(category_id), MAX(category_id)
    INTO c_min, c_max
    FROM categories;

  SELECT MIN(venue_id), MAX(venue_id)
    INTO v_min, v_max
    FROM venues;

  -- user_id 목록 전체를 배열에 담기 (구멍 나도 상관없게)
  SELECT user_id BULK COLLECT INTO v_user_ids
    FROM users;

  --------------------------------------------------------
  -- 2-4) CLUBS (creator_user_id는 v_user_ids에서 랜덤 선택)
  --------------------------------------------------------
  FOR i IN 1 .. v_clubs LOOP
    -- 실제 존재하는 user_id 중 하나 랜덤 선택
    r_user := v_user_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_user_ids.COUNT + 1)));
    r_cat  := TRUNC(DBMS_RANDOM.VALUE(c_min, c_max + 1));
    r_venue:= TRUNC(DBMS_RANDOM.VALUE(v_min, v_max + 1));

    INSERT INTO clubs(
      name, club_type, description, created_at, status,
      main_image_url, contact_info, creator_user_id, category_id
    ) VALUES (
      'Club '||TO_CHAR(i,'FM0000'),
      CASE MOD(i,5)
        WHEN 0 THEN 'sports'
        WHEN 1 THEN 'culture'
        WHEN 2 THEN 'volunteer'
        WHEN 3 THEN 'academic'
        ELSE 'other' END,
      'Random club #'||i,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0,180)),
      CASE MOD(i,10)
        WHEN 0 THEN 'INACTIVE'
        WHEN 1 THEN 'ARCHIVED'
        ELSE 'ACTIVE' END,
      NULL,
      'contact'||i||'@ex.com',
      r_user,
      r_cat
    );
  END LOOP;
  COMMIT;

  -- club_id 목록도 배열에 담기
  SELECT MIN(club_id), MAX(club_id)
    INTO cb_min, cb_max
    FROM clubs;

  SELECT club_id BULK COLLECT INTO v_club_ids
    FROM clubs;

  --------------------------------------------------------
  -- 2-5) EVENTS
  --------------------------------------------------------
  FOR i IN 1 .. v_events LOOP
    r_club  := v_club_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_club_ids.COUNT + 1)));
    r_venue := TRUNC(DBMS_RANDOM.VALUE(v_min, v_max + 1));

    d_start   := TRUNC(SYSDATE) + TRUNC(DBMS_RANDOM.VALUE(-30, 60));
    dur_hours := TRUNC(DBMS_RANDOM.VALUE(1, 5));

    INSERT INTO events(
      club_id, venue_id, title, summary,
      start_time, end_time, language,
      capacity, fee, status, created_at
    ) VALUES (
      r_club,
      r_venue,
      'Event '||TO_CHAR(i,'FM000000'),
      'Generated event #'||i,
      d_start,
      d_start + (dur_hours / 24),
      CASE MOD(i,4)
        WHEN 0 THEN 'en'
        WHEN 1 THEN 'ko'
        WHEN 2 THEN 'ja'
        ELSE 'en' END,
      20 + MOD(i,181),
      CASE WHEN MOD(i,7)=0 THEN TRUNC(DBMS_RANDOM.VALUE(1,50)) ELSE 0 END,
      CASE MOD(i,20)
        WHEN 0 THEN 'CANCELLED'
        WHEN 1 THEN 'POSTPONED'
        WHEN 2 THEN 'COMPLETED'
        ELSE 'SCHEDULED' END,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0,120))
    );
  END LOOP;
  COMMIT;

  SELECT MIN(event_id), MAX(event_id)
    INTO e_min, e_max
    FROM events;

  SELECT event_id BULK COLLECT INTO v_event_ids
    FROM events;

  --------------------------------------------------------
  -- 2-6) MEMBERSHIPS
  --------------------------------------------------------
  FOR i IN 1 .. v_memberships LOOP
    BEGIN
      r_user := v_user_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_user_ids.COUNT + 1)));
      r_club := v_club_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_club_ids.COUNT + 1)));

      d_join := SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0,365));
      d_left := NULL;

      DECLARE
        vstatus VARCHAR2(20);
      BEGIN
        vstatus := CASE MOD(i,30)
                     WHEN 0 THEN 'LEFT'
                     WHEN 1 THEN 'BANNED'
                     ELSE 'ACTIVE'
                   END;

        IF vstatus = 'LEFT' THEN
          d_left := d_join + TRUNC(DBMS_RANDOM.VALUE(1,120));
        END IF;

        INSERT INTO memberships(
          user_id, club_id, role, joined_at, left_at, status
        ) VALUES (
          r_user,
          r_club,
          CASE MOD(i,8)
            WHEN 0 THEN 'LEADER'
            WHEN 1 THEN 'MANAGER'
            ELSE 'MEMBER' END,
          d_join,
          d_left,
          vstatus
        );
      END;
    EXCEPTION
      WHEN dup THEN NULL;
    END;
  END LOOP;
  COMMIT;

  --------------------------------------------------------
  -- 2-7) REGISTRATIONS
  --------------------------------------------------------
  FOR i IN 1 .. v_regs LOOP
    BEGIN
      r_user  := v_user_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_user_ids.COUNT + 1)));
      r_event := v_event_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_event_ids.COUNT + 1)));

      INSERT INTO registrations(
        user_id, event_id, status, attended, registered_at
      ) VALUES (
        r_user,
        r_event,
        CASE MOD(i,10)
          WHEN 0 THEN 'CANCELLED'
          WHEN 1 THEN 'WAITLISTED'
          WHEN 2 THEN 'PENDING'
          ELSE 'CONFIRMED' END,
        CASE WHEN MOD(i,3)=0 THEN 'Y' ELSE 'N' END,
        SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0,30))
      );
    EXCEPTION
      WHEN dup THEN NULL;
    END;
  END LOOP;
  COMMIT;

  --------------------------------------------------------
  -- 2-8) REVIEWS
  --------------------------------------------------------
  FOR i IN 1 .. v_reviews LOOP
    r_user  := v_user_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_user_ids.COUNT + 1)));
    r_event := v_event_ids(TRUNC(DBMS_RANDOM.VALUE(1, v_event_ids.COUNT + 1)));

    INSERT INTO reviews(
      user_id, event_id, rating, review_text, status, created_at
    ) VALUES (
      r_user,
      r_event,
      1 + MOD(i,5),
      'Review #'||i,
      CASE
        WHEN MOD(i,20)=0 THEN 'HIDDEN'
        WHEN MOD(i,25)=0 THEN 'FLAGGED'
        ELSE 'VISIBLE' END,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0,60))
    );
  END LOOP;
  COMMIT;

  DBMS_OUTPUT.PUT_LINE('=== COMPLETE: FULL RESET + REGEN ===');
END;
/
