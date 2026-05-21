-- ================================================================
-- Catalog Service — Test / Seed Data
-- ================================================================
-- Prerequisites: run schema.sql first (catalog + booking).
-- Re-runnable: truncates all catalog data, then re-inserts.
--
-- Data volumes:
--   City                  :   2
--   Language              :   5
--   Format                :   4
--   Movie                 :  20
--   Movie_Language_Format : 190
--   Theatre               :  20  (10 per city)
--   Screen                :  60  (2-4 per theatre)
--   Seat                  : 1942 (30-35 per screen)
--   Show                  : ~220 (3-4 per screen)
--   Show_Seat             : ~(220 × avg 32) per-show seat status
-- ================================================================

BEGIN;

TRUNCATE Show_Seat, Show, Seat, Screen, Theatre,
         Movie_Language_Format, Movie, Format, Language, City
         CASCADE;

-- ============================================================
-- Lookup tables
-- ============================================================

INSERT INTO City (id, name, state, country, latitude, longitude) VALUES
    (1, 'Mumbai',    'Maharashtra', 'India', 19.076090, 72.877426),
    (2, 'Bengaluru', 'Karnataka',   'India', 12.971599, 77.594566);

INSERT INTO Language (id, name, code) VALUES
    (1, 'Hindi',   'hi'),
    (2, 'English', 'en'),
    (3, 'Telugu',  'te'),
    (4, 'Tamil',   'ta'),
    (5, 'Kannada', 'kn');

INSERT INTO Format (id, name) VALUES
    (1, '2D'),
    (2, '3D'),
    (3, 'IMAX'),
    (4, 'Dolby Atmos');

-- ============================================================
-- Movie (20)
-- ============================================================
INSERT INTO Movie (id, title, description, duration_min, genre, rating, release_date, poster_url) VALUES
    ('00000000-0000-4000-a000-000000000001', 'Vikram 2',            'Karnan hunts a new network of masked assassins.',                          155, 'Action/Thriller',   84, '2026-01-14', NULL),
    ('00000000-0000-4000-a000-000000000002', 'Ponniyin Selvan 3',   'The Chola empire faces an unprecedented civil war.',                       168, 'Historical/Drama',  87, '2025-12-25', NULL),
    ('00000000-0000-4000-a000-000000000003', 'Beast Returns',       'A RAW agent is trapped inside a mall with hostages again.',                142, 'Action/Thriller',   62, '2026-02-14', NULL),
    ('00000000-0000-4000-a000-000000000004', 'Jailer 2',            'Muthuvel Pandian comes out of retirement for one last mission.',           160, 'Action/Drama',      78, '2026-03-25', NULL),
    ('00000000-0000-4000-a000-000000000005', 'Vettaiyan',           'An encounter specialist questions his own methods.',                       150, 'Action/Drama',      71, '2026-04-14', NULL),
    ('00000000-0000-4000-a000-000000000006', 'Indian 3',            'Senapathy returns to fight corruption in the digital age.',                165, 'Action/Drama',      65, '2026-05-01', NULL),
    ('00000000-0000-4000-a000-000000000007', 'Kanguva',             'A warrior from 1070 AD is reborn in modern times.',                        148, 'Fantasy/Action',    73, '2026-08-15', NULL),
    ('00000000-0000-4000-a000-000000000008', 'Thangalaan',          'Gold miners battle British exploitation in colonial India.',               158, 'Period/Drama',      81, '2026-06-20', NULL),
    ('00000000-0000-4000-a000-000000000009', 'Vidaamuyarchi',       'A man searches for his kidnapped wife in a foreign land.',                 145, 'Action/Thriller',   75, '2026-01-26', NULL),
    ('00000000-0000-4000-a000-00000000000a', 'Coolie',              'A porter rises from the railway station to become a kingpin.',             162, 'Action/Crime',      80, '2025-11-01', NULL),
    ('00000000-0000-4000-a000-00000000000b', 'Thalapathy 69',       'A schoolteacher leads a revolution against a political dynasty.',          155, 'Drama/Action',      86, '2026-04-10', NULL),
    ('00000000-0000-4000-a000-00000000000c', 'Amaran',              'The true story of Major Mukund Varadarajan''s bravery.',                   140, 'War/Biography',     89, '2026-07-04', NULL),
    ('00000000-0000-4000-a000-00000000000d', 'Soorarai Pottru 2',   'Maara continues to disrupt the aviation industry.',                       152, 'Drama/Biography',   83, '2026-04-25', NULL),
    ('00000000-0000-4000-a000-00000000000e', 'Aayirathil Oruvan 2', 'An expedition into an uncharted ancient civilization.',                    170, 'Adventure/Fantasy', 79, '2026-08-14', NULL),
    ('00000000-0000-4000-a000-00000000000f', 'Maamannan 2',         'The political battle between two ideologies intensifies.',                 138, 'Political/Drama',   76, '2026-03-14', NULL),
    ('00000000-0000-4000-a000-000000000010', 'Kamal 65',            'A retired intelligence officer uncovers a global conspiracy.',             163, 'Spy/Thriller',      82, '2026-10-02', NULL),
    ('00000000-0000-4000-a000-000000000011', 'Raayan 2',            'A man torn between family loyalty and justice.',                           147, 'Action/Drama',      74, '2026-09-15', NULL),
    ('00000000-0000-4000-a000-000000000012', 'Thunivu 2',           'A Robin Hood heist crew targets the corrupt banking system.',              150, 'Heist/Thriller',    77, '2026-12-25', NULL),
    ('00000000-0000-4000-a000-000000000013', 'Mandela Returns',     'A barber once again becomes kingmaker in his village.',                    132, 'Comedy/Satire',     85, '2026-11-21', NULL),
    ('00000000-0000-4000-a000-000000000014', 'Enthiran Reloaded',   'Chitti faces a shape-shifting AI adversary in Chennai.',                   160, 'Sci-Fi/Action',     70, '2026-06-01', NULL);

-- ============================================================
-- Movie_Language_Format (190 rows)
-- ============================================================
DO $$
DECLARE
    movies UUID[];
    mv     UUID;
    i      INT;
BEGIN
    SELECT array_agg(id ORDER BY id) INTO movies FROM Movie;

    FOR i IN 1..5 LOOP
        mv := movies[i];
        INSERT INTO Movie_Language_Format (movie_id, language_id, format_id) VALUES
            (mv,1,1),(mv,1,2),(mv,1,3),(mv,1,4),
            (mv,2,1),(mv,2,2),(mv,2,3),(mv,2,4),
            (mv,3,1),(mv,3,2),
            (mv,4,1),(mv,4,2),
            (mv,5,1);
    END LOOP;

    FOR i IN 6..10 LOOP
        mv := movies[i];
        INSERT INTO Movie_Language_Format (movie_id, language_id, format_id) VALUES
            (mv,1,1),(mv,1,2),(mv,1,3),
            (mv,2,1),(mv,2,4),
            (mv,3,1),
            (mv,4,1),(mv,4,2);
    END LOOP;

    FOR i IN 11..15 LOOP
        mv := movies[i];
        INSERT INTO Movie_Language_Format (movie_id, language_id, format_id) VALUES
            (mv,1,1),(mv,1,2),(mv,1,4),
            (mv,2,1),(mv,2,3),
            (mv,4,1),
            (mv,5,1),(mv,5,2);
    END LOOP;

    FOR i IN 16..20 LOOP
        mv := movies[i];
        INSERT INTO Movie_Language_Format (movie_id, language_id, format_id) VALUES
            (mv,1,1),(mv,1,3),(mv,1,4),
            (mv,2,1),(mv,2,2),
            (mv,3,1),(mv,3,2),
            (mv,4,1),
            (mv,5,1);
    END LOOP;
END $$;

-- ============================================================
-- Theatre (20 — 10 per city)
-- ============================================================
-- owner_id refers to a ROLE_THEATRE_OWNER user in the Auth-Service "user" table.
-- For end-to-end testing, create the matching users in Auth-Service with these UUIDs:
--   d000-0001  PVR Group
--   d000-0002  INOX Leisure
--   d000-0003  Cinepolis India
--   d000-0004  Carnival Cinemas
--   d000-0005  Independent Operators
INSERT INTO Theatre (id, name, address, city_id, latitude, longitude, owner_id) VALUES
    ('00000000-0000-4000-b000-000000000001', 'PVR Phoenix',           'Lower Parel, Mumbai 400013',            1, 18.994370, 72.826050, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-000000000002', 'INOX Nariman Point',    'Nariman Point, Mumbai 400021',          1, 18.925460, 72.824820, '00000000-0000-4000-d000-000000000002'),
    ('00000000-0000-4000-b000-000000000003', 'PVR IMAX Malad',        'Inorbit Mall, Malad West 400064',       1, 19.177530, 72.838880, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-000000000004', 'Cinepolis Andheri',     'Fun Republic, Andheri West 400053',     1, 19.126830, 72.826790, '00000000-0000-4000-d000-000000000003'),
    ('00000000-0000-4000-b000-000000000005', 'PVR Juhu',              'Juhu Tara Road, Juhu 400049',           1, 19.098490, 72.826580, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-000000000006', 'Carnival Wadala',       'R-City Mall, Ghatkopar West 400086',    1, 19.091760, 72.916710, '00000000-0000-4000-d000-000000000004'),
    ('00000000-0000-4000-b000-000000000007', 'INOX Megaplex',         'Inorbit Mall, Malad West 400064',       1, 19.177200, 72.838500, '00000000-0000-4000-d000-000000000002'),
    ('00000000-0000-4000-b000-000000000008', 'Movietime Hub',         'Hub Mall, Goregaon East 400063',        1, 19.163580, 72.858920, '00000000-0000-4000-d000-000000000005'),
    ('00000000-0000-4000-b000-000000000009', 'PVR Icon Oberoi',       'Oberoi Mall, Goregaon East 400063',     1, 19.173480, 72.863580, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-00000000000a', 'Miraj Cinemas',         'Dombivli East, Thane 421201',           1, 19.218330, 73.088610, '00000000-0000-4000-d000-000000000005'),
    ('00000000-0000-4000-b000-00000000000b', 'PVR Orion',             'Orion Mall, Rajajinagar, 560010',       2, 13.011420, 77.554960, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-00000000000c', 'INOX Garuda Mall',      'Magrath Road, Ashok Nagar 560025',      2, 12.970120, 77.607440, '00000000-0000-4000-d000-000000000002'),
    ('00000000-0000-4000-b000-00000000000d', 'PVR Forum',             'Forum Mall, Koramangala 560095',        2, 12.934530, 77.610800, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-00000000000e', 'Cinepolis Meenakshi',   'Royal Meenakshi Mall, Hulimavu 560076', 2, 12.878960, 77.597460, '00000000-0000-4000-d000-000000000003'),
    ('00000000-0000-4000-b000-00000000000f', 'PVR VR Bengaluru',      'Whitefield Main Road, 560066',          2, 12.996920, 77.745570, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-000000000010', 'INOX Mantri Square',    'Mantri Square, Malleswaram 560003',     2, 12.991740, 77.570090, '00000000-0000-4000-d000-000000000002'),
    ('00000000-0000-4000-b000-000000000011', 'Innovative Multiplex',  'Marathahalli, Bengaluru 560037',        2, 12.956920, 77.701560, '00000000-0000-4000-d000-000000000005'),
    ('00000000-0000-4000-b000-000000000012', 'PVR Phoenix Whitefield','Phoenix Mall, Whitefield 560066',       2, 12.997700, 77.741870, '00000000-0000-4000-d000-000000000001'),
    ('00000000-0000-4000-b000-000000000013', 'Cinepolis Nexus',       'Nexus Mall, Koramangala 560095',        2, 12.935580, 77.612470, '00000000-0000-4000-d000-000000000003'),
    ('00000000-0000-4000-b000-000000000014', 'Rex Theatre',           'Brigade Road, Bengaluru 560001',        2, 12.972150, 77.607560, '00000000-0000-4000-d000-000000000005');

-- ============================================================
-- Screen (60 — 2 to 4 per theatre)
-- ============================================================
DO $$
DECLARE
    t UUID[];
BEGIN
    SELECT array_agg(id ORDER BY id) INTO t FROM Theatre;

    INSERT INTO Screen (name, theatre_id, total_seats, format_id) VALUES
        ('Screen 1',    t[1],  35, 1),
        ('Screen 2',    t[1],  30, 2),
        ('Screen 3',    t[1],  32, 1),
        ('Screen 1',    t[2],  35, 1),
        ('Screen 2',    t[2],  30, 1),
        ('Screen 3',    t[2],  32, 2),
        ('IMAX Hall',   t[2],  30, 3),
        ('Screen 1',    t[3],  35, 1),
        ('Dolby Atmos', t[3],  30, 4),
        ('Screen 1',    t[4],  32, 1),
        ('Screen 2',    t[4],  30, 2),
        ('Screen 3',    t[4],  35, 1),
        ('Screen 1',    t[5],  35, 1),
        ('Screen 2',    t[5],  30, 1),
        ('IMAX Hall',   t[5],  32, 3),
        ('Screen 4',    t[5],  30, 2),
        ('Screen 1',    t[6],  35, 1),
        ('Screen 2',    t[6],  30, 1),
        ('Screen 1',    t[7],  35, 1),
        ('Dolby Atmos', t[7],  30, 4),
        ('Screen 3',    t[7],  32, 2),
        ('Screen 1',    t[8],  35, 1),
        ('Screen 2',    t[8],  30, 1),
        ('Screen 3',    t[8],  35, 2),
        ('Screen 4',    t[8],  32, 1),
        ('Screen 1',    t[9],  35, 1),
        ('Screen 2',    t[9],  30, 2),
        ('Screen 1',    t[10], 32, 1),
        ('Screen 2',    t[10], 30, 1),
        ('IMAX Hall',   t[10], 35, 3),
        ('Screen 1',    t[11], 35, 1),
        ('Screen 2',    t[11], 32, 2),
        ('Screen 3',    t[11], 30, 1),
        ('Screen 1',    t[12], 35, 1),
        ('Dolby Atmos', t[12], 30, 4),
        ('Screen 3',    t[12], 32, 1),
        ('Screen 4',    t[12], 35, 2),
        ('Screen 1',    t[13], 35, 1),
        ('Screen 2',    t[13], 30, 1),
        ('Screen 1',    t[14], 32, 1),
        ('IMAX Hall',   t[14], 35, 3),
        ('Screen 3',    t[14], 30, 2),
        ('Screen 1',    t[15], 35, 1),
        ('Screen 2',    t[15], 30, 1),
        ('Dolby Atmos', t[15], 32, 4),
        ('Screen 4',    t[15], 35, 2),
        ('Screen 1',    t[16], 35, 1),
        ('Screen 2',    t[16], 30, 1),
        ('Screen 1',    t[17], 32, 1),
        ('Screen 2',    t[17], 35, 2),
        ('Screen 3',    t[17], 30, 1),
        ('Screen 1',    t[18], 35, 1),
        ('IMAX Hall',   t[18], 30, 3),
        ('Screen 3',    t[18], 32, 1),
        ('Screen 4',    t[18], 30, 2),
        ('Screen 1',    t[19], 35, 1),
        ('Dolby Atmos', t[19], 30, 4),
        ('Screen 1',    t[20], 30, 1),
        ('Screen 2',    t[20], 35, 1),
        ('Screen 3',    t[20], 32, 2);
END $$;

-- ============================================================
-- Seat (~1942 rows)
-- ============================================================
DO $$
DECLARE
    scr         RECORD;
    num_rows    INT;
    seats_per_row INT;
    r           INT;
    s           INT;
BEGIN
    FOR scr IN SELECT id, total_seats FROM Screen ORDER BY id LOOP
        CASE scr.total_seats
            WHEN 30 THEN num_rows := 5; seats_per_row := 6;
            WHEN 32 THEN num_rows := 4; seats_per_row := 8;
            WHEN 35 THEN num_rows := 5; seats_per_row := 7;
            ELSE          num_rows := 5; seats_per_row := 6;
        END CASE;

        FOR r IN 1..num_rows LOOP
            FOR s IN 1..seats_per_row LOOP
                INSERT INTO Seat (screen_id, row_label, seat_number)
                VALUES (scr.id, chr(64 + r), s);
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

-- ============================================================
-- Show (~220 rows)
-- ============================================================
DO $$
DECLARE
    scr           RECORD;
    show_count    INT;
    i             INT;
    scr_idx       INT      := 0;
    mlf_ref       UUID;
    slot_hours    INT[]    := ARRAY[10, 13, 17, 21];
    slot_mins     INT[]    := ARRAY[0, 30, 0, 0];
    base_price    DECIMAL;
    show_date     DATE     := '2026-02-16';
    movie_offset  INT      := 0;
    movies        UUID[];
BEGIN
    SELECT array_agg(id ORDER BY id) INTO movies FROM Movie;

    FOR scr IN SELECT id, format_id FROM Screen ORDER BY id LOOP
        scr_idx := scr_idx + 1;
        show_count := CASE WHEN scr_idx % 3 = 0 THEN 3 ELSE 4 END;

        FOR i IN 1..show_count LOOP
            SELECT mlf.id INTO mlf_ref
            FROM Movie_Language_Format mlf
            WHERE mlf.movie_id = movies[(movie_offset % 20) + 1]
              AND mlf.format_id = scr.format_id
            ORDER BY mlf.language_id
            LIMIT 1;

            movie_offset := movie_offset + 1;

            base_price := CASE scr.format_id
                WHEN 1 THEN 150.00 + (i * 20)
                WHEN 2 THEN 250.00 + (i * 20)
                WHEN 3 THEN 400.00 + (i * 20)
                WHEN 4 THEN 350.00 + (i * 20)
            END;

            INSERT INTO Show (mlf_id, screen_id, start_time, end_time, price)
            VALUES (
                mlf_ref,
                scr.id,
                show_date + make_time(slot_hours[i], slot_mins[i], 0),
                show_date + make_time(slot_hours[i], slot_mins[i], 0) + INTERVAL '150 minutes',
                base_price
            );
        END LOOP;
    END LOOP;
END $$;

-- ============================================================
-- Show_Seat (per-show seat status — all start AVAILABLE)
-- ============================================================
INSERT INTO Show_Seat (show_id, seat_id, status)
SELECT sh.id, seat.id, 'AVAILABLE'
FROM Show sh
JOIN Seat seat ON seat.screen_id = sh.screen_id;

COMMIT;
