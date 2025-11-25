DROP TABLE map;

CREATE TABLE map (
    entity_Id NUMBER PRIMARY KEY,
    entity_Name VARCHAR2(32),
    entity_Type VARCHAR2(32),
    geometry SDO_GEOMETRY
);


INSERT INTO map VALUES(
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

INSERT INTO map VALUES (
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

INSERT INTO map VALUES (
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

INSERT INTO map VALUES (
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

INSERT INTO map VALUES (
    5, 'central_road', 'road',
    SDO_GEOMETRY(
    2003, 
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(245,0, 260,500)
  )
);


INSERT INTO map VALUES (
    6, 'aquarium', 'building',
    SDO_GEOMETRY(
    2003,
    NULL, 
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(300,370, 350,470)
  )
);

INSERT INTO map VALUES (
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

INSERT INTO map VALUES (
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


INSERT INTO map VALUES (
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

INSERT INTO map VALUES (
    10, 'staff_office', 'building',
    SDO_GEOMETRY(
    2003, NULL, NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(380,40, 470,130)
  )
);

INSERT INTO map VALUES (
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

INSERT INTO map VALUES (
  12, 'zoo', 'border',
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

DELETE FROM map
WHERE LOWER(entity_Name) = LOWER('bridge_north');

UPDATE map
SET geometry = SDO_GEOMETRY(
    2003, NULL, NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,3),
    SDO_ORDINATE_ARRAY(30,280, 100,340)
)
WHERE entity_Name = 'flowerbed_A';

SELECT entity_Name, entity_Type
FROM map
WHERE LOWER(entity_Type) = LOWER('building');

SELECT entity_Id, entity_Name, entity_Type, geometry
FROM map
WHERE LOWER(entity_Name) = LOWER('panda_House');

SELECT entity_Name,
       SDO_GEOM.SDO_AREA(geometry, 0.005) AS area
FROM map
WHERE SDO_GEOM.SDO_AREA(geometry, 0.005) > 10000;

SELECT a.entity_Name FROM map a, map b
WHERE a.entity_Type = 'building'
  AND b.entity_Name = 'river'
  AND SDO_RELATE(a.geometry, b.geometry, 'mask=ANYINTERACT') = 'TRUE';


DROP TABLE MEntities;

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

CREATE OR REPLACE TRIGGER entities_generateFeatures
  BEFORE INSERT OR UPDATE OF photo ON products
  FOR EACH ROW
DECLARE
  si ORDSYS.SI_StillImage;
BEGIN
  -- if there is any photo (it is not an empty image) then generate its features
  IF :NEW.photo.height IS NOT NULL THEN
    si := new SI_StillImage(:NEW.photo.getContent());
    :NEW.photo_si := si;
    :NEW.photo_ac := SI_AverageColor(si);
    :NEW.photo_ch := SI_ColorHistogram(si);
    :NEW.photo_pc := SI_PositionalColor(si);
    :NEW.photo_tx := SI_Texture(si);
  END IF;
END;
/

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

SELECT 
    m.entity_id,
    m.entity_name,
    m.entity_type,
    SDO_GEOM.SDO_CENTROID(m.geometry, 0.005).sdo_point.x AS x,
    SDO_GEOM.SDO_CENTROID(m.geometry, 0.005).sdo_point.y AS y,
    e.Tokentime,
    e.Image
FROM map m
JOIN MEntities e
  ON e.SID_ref = m.entity_id 
WHERE LOWER(m.entity_name) = LOWER(:name);
