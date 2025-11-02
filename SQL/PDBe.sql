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