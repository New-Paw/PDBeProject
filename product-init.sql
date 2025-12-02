DROP TABLE product;

CREATE TABLE product (
  code NUMBER (3) PRIMARY KEY,
  title VARCHAR2 (64),
  image ORDSYS.ORDImage,
  image_si ORDSYS.SI_StillImage,
  image_ac ORDSYS.SI_AverageColor,
  image_ch ORDSYS.SI_ColorHistogram,
  image_pc ORDSYS.SI_PositionalColor,
  image_tx ORDSYS.SI_Texture
);
