DROP TABLE MEntities;
DROP TABLE SEntities;

CREATE TABLE SEntities (
    SID NUMBER PRIMARY KEY,
    SNAME VARCHAR2(32),
    TYPE VARCHAR2(32),
    GEOMETRY SDO_GEOMETRY
);

INSERT INTO SEntities VALUES (
  0, 'zoo', 'border',
  SDO_GEOMETRY(
    2002,
    NULL,
    NULL,
    SDO_ELEM_INFO_ARRAY(1,2,1),
    SDO_ORDINATE_ARRAY(
      0,0,
      500,0,
      500,500,
      0,500,
      0,0
    )
  )
);

INSERT INTO SEntities VALUES(
    1, 'panda_House', 'building',
    SDO_GEOMETRY(
    2003,
    NULL,
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(
        30,360, 
        220,480
    )
    )
);

INSERT INTO SEntities VALUES (
    2, 'flowerbed_A', 'garden',
    SDO_GEOMETRY(
    2003, 
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(
        40,295, 
        90,330)
  )
);

INSERT INTO SEntities VALUES (
    3, 'flowerbed_B', 'garden',
    SDO_GEOMETRY(
    2003, 
    NULL,
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(
        150,295, 
        200,330)
  )
);

INSERT INTO SEntities VALUES (
    4, 'fountain', 'poi',
    SDO_GEOMETRY(
    2003, 
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,4),
    SDO_ORDINATE_ARRAY(
        120,255, 
        140,255,
        120,275)
  )
);

INSERT INTO SEntities VALUES (
    5, 'central_road', 'road',
    SDO_GEOMETRY(
    2003, 
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(245,0, 260,500)
  )
);


INSERT INTO SEntities VALUES (
    6, 'aquarium', 'building',
    SDO_GEOMETRY(
    2003,
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(300,370, 350,470)
  )
);

INSERT INTO SEntities VALUES (
    7, 'river', 'water',
    SDO_GEOMETRY(
    2003, 
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,1),
    SDO_ORDINATE_ARRAY(
      370,500,
      390,500,
      390,330,
      350,330,
      350,0,
      330,0,
      330,350,
      370,350,
      370,500
    )
  )
);

INSERT INTO SEntities VALUES (
    8, 'bridge_north', 'building',
    SDO_GEOMETRY(
    2003,
    NULL,
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(
        355,415, 
        405,445)
  )
);


INSERT INTO SEntities VALUES (
    9, 'bridge_south', 'building',
    SDO_GEOMETRY(
    2003, 
    NULL,
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(
        315,135, 
        385,165)
  )
);

INSERT INTO SEntities VALUES (
    10, 'staff_office', 'building',
    SDO_GEOMETRY(
    2003, NULL, NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(380,40, 470,130)
  )
);

INSERT INTO SEntities VALUES (
    11, 'L_building', 'building',
    SDO_GEOMETRY(
    2003, NULL, NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,1),
    SDO_ORDINATE_ARRAY(
      40,40,
      150,40,
      150,80,
      90,80,
      90,140,
      40,140,
      40,40 
    )
  )
);



DELETE FROM SEntities
WHERE LOWER(SNAME) = LOWER('bridge_north');

UPDATE SEntities
SET geometry = SDO_GEOMETRY(
    2003, NULL, NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(30,280, 100,340)
)
WHERE SNAME = 'flowerbed_A';

SELECT SNAME, TYPE
FROM SEntities
WHERE LOWER(TYPE) = LOWER('building');

SELECT SID, SNAME, TYPE, GEOMETRY
FROM SEntities
WHERE LOWER(SNAME) = LOWER('panda_House');

SELECT SNAME,
       SDO_GEOM.SDO_AREA(geometry, 0.005) AS area
FROM SEntities
WHERE SDO_GEOM.SDO_AREA(geometry, 0.005) > 10000;

SELECT a.SNAME FROM SEntities a, SEntities b
WHERE a.TYPE = 'building'
  AND b.SNAME = 'river'
  AND SDO_RELATE(a.GEOMETRY, b.GEOMETRY, 'mask=ANYINTERACT') = 'TRUE';




CREATE TABLE MEntities (
    MID        INTEGER PRIMARY KEY,
    SID_ref  INTEGER REFERENCES SEntities(SID),
    Title       VARCHAR2(100),
    Image       ORDSYS.ORDImage,
    Tokentime       DATE,
    Image_SI ORDSYS.SI_StillImage,
    Image_ac ORDSYS.SI_AverageColor,
    Image_ch ORDSYS.SI_ColorHistogram,
    Image_pc ORDSYS.SI_PositionalColor,
    Image_tx ORDSYS.SI_Texture
);

-- CREATE OR REPLACE TRIGGER entities_generateFeatures
--   BEFORE INSERT OR UPDATE OF photo ON products
--   FOR EACH ROW
-- DECLARE
--   si ORDSYS.SI_StillImage;
-- BEGIN
--   -- if there is any photo (it is not an empty image) then generate its features
--   IF :NEW.photo.height IS NOT NULL THEN
--     si := new SI_StillImage(:NEW.photo.getContent());
--     :NEW.photo_si := si;
--     :NEW.photo_ac := SI_AverageColor(si);
--     :NEW.photo_ch := SI_ColorHistogram(si);
--     :NEW.photo_pc := SI_PositionalColor(si);
--     :NEW.photo_tx := SI_Texture(si);
--   END IF;
-- END;
-- /

INSERT INTO MEntities (MID, SID_ref, Title, Image, Tokentime)
VALUES (0,1,'with Rose',ORDSYS.ORDImage.init(),TO_DATE('2025-11-02 15:30:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MEntities (MID, SID_ref, Title, Image, Tokentime) 
VALUES (1, 1, 'beautiful', ordsys.ordimage.init(), TO_DATE('2025-11-02 18:00:00', 'YYYY-MM-DD HH24:MI:SS'));


INSERT INTO MEntities (MID, SID_ref, Title, Image, Tokentime) 
VALUES (2, 1, 'beach', ordsys.ordimage.init(), TO_DATE('2025-11-02 21:30:00', 'YYYY-MM-DD HH24:MI:SS'));


UPDATE MEntities SET Tokentime = TO_DATE('2025-11-02 18:30:00', 'YYYY-MM-DD HH24:MI:SS') WHERE MID = 1;
COMMIT;

UPDATE MEntities SET SID_ref  = 0 WHERE MID = 1;
COMMIT;

UPDATE MEntities SET Title = 'Sunset Scene' WHERE MID = 1;
COMMIT;

UPDATE MEntities SET Title = 'With my best friend Rose' WHERE MID = 0;
COMMIT;

DELETE FROM MEntities WHERE MID = 0;
COMMIT;

SELECT image FROM MEntities WHERE SID_ref = 1;

SELECT Image FROM MEntities WHERE Tokentime BETWEEN TO_DATE('2025-11-02 14:30:00', 'YYYY-MM-DD HH24:MI:SS') AND TO_DATE('2025-11-02 20:30:00', 'YYYY-MM-DD HH24:MI:SS');

SELECT MID, Title, Tokentime, Image FROM MEntities WHERE LOWER(Title) LIKE LOWER('%sunset%');

SELECT * FROM MEntities;

