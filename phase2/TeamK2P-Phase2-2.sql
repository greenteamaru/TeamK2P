SET SERVEROUTPUT ON
DECLARE
  -- ====== 레코드 수(라이트 세팅) ======
  v_users       PLS_INTEGER := 1000;
  v_categories  PLS_INTEGER := 120;
  v_venues      PLS_INTEGER := 120;
  v_clubs       PLS_INTEGER := 300;
  v_events      PLS_INTEGER := 1200;
  v_memberships PLS_INTEGER := 1400;
  v_regs        PLS_INTEGER := 1200;
  v_reviews     PLS_INTEGER := 800;

  -- ====== 범위/카운트 ======
  u_min NUMBER; u_max NUMBER; u_cnt NUMBER;
  c_min NUMBER; c_max NUMBER; c_cnt NUMBER;
  v_min NUMBER; v_max NUMBER; v_cnt NUMBER;
  cb_min NUMBER; cb_max NUMBER; cb_cnt NUMBER;
  e_min NUMBER; e_max NUMBER; e_cnt NUMBER;

  -- ====== 작업 변수 ======
  r_user NUMBER; r_cat NUMBER; r_venue NUMBER; r_club NUMBER; r_event NUMBER;
  d_start DATE; dur_hours NUMBER; d_join DATE; d_left DATE;

  dup EXCEPTION; PRAGMA EXCEPTION_INIT(dup, -00001);
BEGIN
  DBMS_RANDOM.SEED(TO_NUMBER(TO_CHAR(SYSTIMESTAMP,'FF6')));

  -- 1) CATEGORIES (name UNIQUE)
  FOR i IN 1 .. v_categories LOOP
    INSERT INTO categories(name, description)
    VALUES ('Category '||TO_CHAR(i,'FM0000'), 'Random category #'||i);
  END LOOP;
  COMMIT;

  -- 2) USERS (email UNIQUE)
  FOR i IN 1 .. v_users LOOP
    INSERT INTO users(name, email, nationality, nickname, language, created_at)
    VALUES (
      'User '||TO_CHAR(i,'FM0000'),
      'user'||TO_CHAR(i,'FM0000')||'@ex.com',
      CASE MOD(i,8)
        WHEN 0 THEN 'KR' WHEN 1 THEN 'US' WHEN 2 THEN 'JP' WHEN 3 THEN 'CN'
        WHEN 4 THEN 'VN' WHEN 5 THEN 'DE' WHEN 6 THEN 'FR' ELSE 'IN' END,
      'nick'||i,
      CASE MOD(i,6)
        WHEN 0 THEN 'ko' WHEN 1 THEN 'en' WHEN 2 THEN 'ja'
        WHEN 3 THEN 'zh' WHEN 4 THEN 'de' ELSE 'en' END,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0, 365))
    );
  END LOOP;
  COMMIT;

  -- 3) VENUES
  FOR i IN 1 .. v_venues LOOP
    INSERT INTO venues(name, campus, building, room, map_url)
    VALUES (
      'Venue '||TO_CHAR(i,'FM0000'),
      CASE MOD(i,3) WHEN 0 THEN 'KNU' WHEN 1 THEN 'Med' ELSE 'Eng' END,
      CHR(65 + MOD(i,5)),                           -- A~E
      CASE MOD(i,4) WHEN 0 THEN '101' WHEN 1 THEN '201' WHEN 2 THEN '3F' ELSE 'B1' END,
      NULL
    );
  END LOOP;
  COMMIT;

  -- ID/COUNT 범위 취득
  SELECT MIN(user_id), MAX(user_id), COUNT(*) INTO u_min, u_max, u_cnt FROM users;
  SELECT MIN(category_id), MAX(category_id), COUNT(*) INTO c_min, c_max, c_cnt FROM categories;
  SELECT MIN(venue_id),   MAX(venue_id),   COUNT(*) INTO v_min, v_max, v_cnt FROM venues;

  -- 4) CLUBS (FK: creator_user_id, category_id)
  FOR i IN 1 .. v_clubs LOOP
    r_user := TRUNC(DBMS_RANDOM.VALUE(u_min, u_max + 1));
    r_cat  := TRUNC(DBMS_RANDOM.VALUE(c_min, c_max + 1));

    INSERT INTO clubs(name, club_type, description, created_at, status,
                      main_image_url, contact_info, creator_user_id, category_id)
    VALUES (
      'Club '||TO_CHAR(i,'FM0000'),
      CASE MOD(i,5) WHEN 0 THEN 'sports' WHEN 1 THEN 'culture'
                    WHEN 2 THEN 'volunteer' WHEN 3 THEN 'academic' ELSE 'other' END,
      'Random club #'||i,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0, 180)),
      CASE MOD(i,20) WHEN 0 THEN 'INACTIVE' WHEN 1 THEN 'ARCHIVED' ELSE 'ACTIVE' END,
      NULL,
      'contact'||i||'@ex.com',
      r_user,
      r_cat
    );
  END LOOP;
  COMMIT;

  SELECT MIN(club_id), MAX(club_id), COUNT(*) INTO cb_min, cb_max, cb_cnt FROM clubs;

  -- 5) EVENTS (FK: club_id, venue_id) + 시간/수치 CHECK 만족
  FOR i IN 1 .. v_events LOOP
    r_club  := TRUNC(DBMS_RANDOM.VALUE(cb_min, cb_max + 1));
    r_venue := TRUNC(DBMS_RANDOM.VALUE(v_min,  v_max  + 1));

    d_start   := TRUNC(SYSDATE) + TRUNC(DBMS_RANDOM.VALUE(-30, 60)); -- 과거~미래
    dur_hours := TRUNC(DBMS_RANDOM.VALUE(1, 5));                      -- 1..4시간

    INSERT INTO events(club_id, venue_id, title, summary, start_time, end_time,
                       language, capacity, fee, status, created_at)
    VALUES (
      r_club,
      r_venue,
      'Event '||TO_CHAR(i,'FM000000'),
      'Auto generated event #'||i,
      d_start,
      d_start + (dur_hours/24),
      CASE MOD(i,4) WHEN 0 THEN 'en' WHEN 1 THEN 'ko' WHEN 2 THEN 'ja' ELSE 'en' END,
      20 + MOD(i, 181),
      CASE WHEN MOD(i,7)=0 THEN TRUNC(DBMS_RANDOM.VALUE(1, 50)) ELSE 0 END,
      CASE MOD(i,25) WHEN 0 THEN 'CANCELLED' WHEN 1 THEN 'POSTPONED'
                     WHEN 2 THEN 'COMPLETED' ELSE 'SCHEDULED' END,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0, 120))
    );
  END LOOP;
  COMMIT;

  SELECT MIN(event_id), MAX(event_id), COUNT(*) INTO e_min, e_max, e_cnt FROM events;

  -- 6) MEMBERSHIPS (UNIQUE(user_id, club_id) 충돌 시 건너뜀)
  FOR i IN 1 .. v_memberships LOOP
    BEGIN
      r_user := TRUNC(DBMS_RANDOM.VALUE(u_min,  u_max  + 1));
      r_club := TRUNC(DBMS_RANDOM.VALUE(cb_min, cb_max + 1));

      d_join := SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0, 365));
      d_left := NULL;
      -- 일부는 LEFT/BANNED로 표시
      DECLARE vstatus VARCHAR2(12);
      BEGIN
        vstatus := CASE MOD(i,40) WHEN 0 THEN 'BANNED'
                                   WHEN 1 THEN 'LEFT'
                                   ELSE 'ACTIVE' END;
        IF vstatus = 'LEFT' THEN
          d_left := d_join + TRUNC(DBMS_RANDOM.VALUE(1, 120));
        END IF;

        INSERT INTO memberships(user_id, club_id, role, joined_at, left_at, status)
        VALUES (
          r_user,
          r_club,
          CASE MOD(i,8) WHEN 0 THEN 'LEADER' WHEN 1 THEN 'MANAGER' ELSE 'MEMBER' END,
          d_join, d_left, vstatus
        );
      END;
    EXCEPTION WHEN dup THEN NULL; END;
  END LOOP;
  COMMIT;

  -- 7) REGISTRATIONS (UNIQUE(user_id, event_id) 충돌 시 건너뜀)
  FOR i IN 1 .. v_regs LOOP
    BEGIN
      r_user  := TRUNC(DBMS_RANDOM.VALUE(u_min, u_max + 1));
      r_event := TRUNC(DBMS_RANDOM.VALUE(e_min, e_max + 1));

      INSERT INTO registrations(user_id, event_id, status, attended, registered_at)
      VALUES (
        r_user,
        r_event,
        CASE MOD(i,10) WHEN 0 THEN 'CANCELLED'
                       WHEN 1 THEN 'WAITLISTED'
                       WHEN 2 THEN 'PENDING'
                       ELSE 'CONFIRMED' END,
        CASE WHEN MOD(i,3)=0 THEN 'Y' ELSE 'N' END,
        SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0, 30))
      );
    EXCEPTION WHEN dup THEN NULL; END;
  END LOOP;
  COMMIT;

  -- 8) REVIEWS (EVENT 전용, 동일 (user,event) 다수 허용)
  FOR i IN 1 .. v_reviews LOOP
    r_user  := TRUNC(DBMS_RANDOM.VALUE(u_min, u_max + 1));
    r_event := TRUNC(DBMS_RANDOM.VALUE(e_min, e_max + 1));

    INSERT INTO reviews(user_id, event_id, rating, review_text, status, created_at)
    VALUES (
      r_user, r_event,
      1 + MOD(i,5),
      'Review #'||i||' on event '||r_event,
      CASE WHEN MOD(i,20)=0 THEN 'HIDDEN'
           WHEN MOD(i,25)=0 THEN 'FLAGGED'
           ELSE 'VISIBLE' END,
      SYSDATE - TRUNC(DBMS_RANDOM.VALUE(0, 60))
    );
  END LOOP;
  COMMIT;

  DBMS_OUTPUT.PUT_LINE('Done. Random data inserted (light setting).');
END;
/

commit;