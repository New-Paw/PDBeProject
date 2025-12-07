Technology stack:
Backend: Java Servlet（Jakarta Servlet）, Oracle DB (Spatial + interMedia/ORDImage)
Frontend: JSP + Leaflet.js

Main tables:
SEntities:( SID NUMBER PRIMARY KEY, entity_Name VARCHAR2(32), entity_Type VARCHAR2(32), geometry SDO_GEOMETRY )
MEntities:( MID INTEGER PRIMARY KEY, SID_ref INTEGER REFERENCES SEntities(SID), Title VARCHAR2(100), Image ORDSYS.ORDImage, Tokentime DATE, Image_SI ORDSYS.SI_StillImage, Image_ac ORDSYS.SI_AverageColor, Image_ch ORDSYS.SI_ColorHistogram, Image_pc ORDSYS.SI_PositionalColor, Image_tx ORDSYS.SI_Texture)

Servlet mapping:
/api/Sentities → SpatialServlet
/api/Simages → SImagesServlet
/image → ImageServlet
/uploadImage → UploadServlet
/updateImageTitle → UpdateServlet
/deleteImage → DeleteServlet
/api/similar → SimilarServlet

Deployment:
1. Tomcat 10.1.30 + Java Servlet
2. Run Tomcat first
3. Visit http://localhost:8080/oracle-lab-multimedia/map.jsp in browser
