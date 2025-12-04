<%--
  Created by IntelliJ IDEA.
  User: zhaomengran
  Date: 2025/12/3/星期三
  Time: 23:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <meta charset="UTF-8">
  <title>Web Map (Gray Background)</title>

  <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>

  <style>
    #map {
      width: 100%;
      height: 600px;
      border: 1px solid #ccc;
      background-color: #e6e6e6;  /* 淡灰色背景 */
    }
    #toolbar {
      padding: 10px;
    }
  </style>
</head>
<body>

<div id="toolbar">
  <input type="text" id="search" placeholder="Search place...">
  <button onclick="doSearch()">Search</button>
</div>

<div id="map"></div>

<script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>

<script>

  var map = L.map('map', {
    crs: L.CRS.Simple   // 关键：使用简单坐标系，而不是地理坐标
  }).setView([0, 0], 0);

  // 加载数据库点
  loadEntities();

  function loadEntities() {
    fetch('<%= request.getContextPath() %>/api/Sentities')
            .then(resp => resp.json())
            .then(data => {
              if (!data || data.length === 0) return;

              data.forEach(e => {
                L.marker([e.lat, e.lon]).addTo(map)
                        .bindPopup(e.name);
              });
            });
  }

  function doSearch() {
    alert("Search not implemented");
  }
</script>

</body>
</html>
