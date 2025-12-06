<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>Web Map (Gray Background)</title>

  <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>

  <style>
    body, html {
      margin: 0;
      padding: 0;
      height: 100%;
      font-family: Arial, sans-serif;
    }

    /* 整体左右布局容器 */
    #container {
      display: flex;
      flex-direction: row;
      height: 100vh; /* 占满窗口高度 */
    }

    /* 左侧：工具栏 + 地图 */
    #left-pane {
      flex: 2;              /* 左侧占2份宽度 */
      display: flex;
      flex-direction: column;
      border-right: 1px solid #ccc;
    }

    #toolbar {
      padding: 10px;
      border-bottom: 1px solid #ccc;
      background-color: #f5f5f5;
    }

    #map {
      flex: 1;              /* 地图占满左侧剩余空间 */
      border: 1px solid #ccc;
      background-color: #e6e6e6;
    }

    /* 右侧：缩略图面板 */
    #right-pane {
      flex: 1;              /* 右侧占1份宽度 */
      display: flex;
      flex-direction: column;
      padding: 10px;
    }

    #right-title {
      font-weight: bold;
      margin-bottom: 10px;
      border-bottom: 1px solid #ccc;
      padding-bottom: 5px;
    }

    #image-panel {
      flex: 1;
      overflow-y: auto;
    }

    #upload-panel {
      margin: 10px 0;
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
      background-color: #f9f9f9;
      font-size: 14px;
    }

    .upload-row {
      margin-bottom: 6px;
    }

    .upload-row label {
      display: inline-block;
      width: 80px;
    }


    .thumb-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 10px;
      border: 1px solid #ddd;
      padding: 5px;
      border-radius: 4px;
      background-color: #fafafa;
    }

    .thumb-item img {
      max-width: 100%;
      max-height: 120px;
      display: block;
      margin-bottom: 4px;
      object-fit: contain;
    }

    .thumb-title {
      font-size: 12px;
      text-align: center;
      color: #333;
      word-break: break-all;
    }
  </style>
</head>
<body>

<div id="container">
  <!-- 左侧：地图 -->
  <div id="left-pane">
    <div id="toolbar">
      <input type="text" id="search" placeholder="Search place...">
      <button onclick="doSearch()">Search</button>
    </div>
    <div id="map"></div>
  </div>

  <div id="right-pane">
    <div id="right-title">请选择左侧地图上的地点</div>

    <!-- 🔽 新增：上传区域 -->
    <div id="upload-panel">
      <form id="upload-form"
            method="post"
            action="<%= request.getContextPath() %>/uploadImage"
            enctype="multipart/form-data">

        <!-- 当前选中的 SID，会在 JS 里动态写入 -->
        <input type="hidden" name="sid" id="upload-sid">

        <div class="upload-row">
          <label for="upload-title">图片标题：</label>
          <input type="text" name="title" id="upload-title" placeholder="输入图片标题">
        </div>

        <div class="upload-row">
          <label for="upload-file">选择图片：</label>
          <input type="file" name="image" id="upload-file" accept="image/*">
        </div>

        <div class="upload-row">
          <button type="submit">上传图片</button>
        </div>
      </form>
    </div>
    <!-- 🔼 新增结束 -->

    <div id="image-panel">
      暂无选中地点。
    </div>
  </div>


<script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>

<script>
  // 使用简单坐标系：本地 0~500
  var map = L.map('map', {
    crs: L.CRS.Simple
  });

  // 初始视图范围（根据你数据修改）
  map.fitBounds([[0, 0], [500, 500]]);

  // 方便 JS 里拼 URL
  const baseUrl = '<%= request.getContextPath() %>';

  loadEntities();

  function loadEntities() {
    fetch(baseUrl + '/api/Sentities')
            .then(resp => resp.json())
            .then(data => {
              if (!data || data.length === 0) return;

              data.forEach(function (e) {
                // 1. 中心点 marker
                if (typeof e.lat === "number" && typeof e.lon === "number") {
                  const lat = e.lat;
                  const lon = e.lon;
                  const name = e.name || "";

                  const marker = L.marker([lat, lon]).addTo(map)
                          .bindPopup(
                                  name + "<br/>(" +
                                  lat.toFixed(2) + ", " +    // 两位小数
                                  lon.toFixed(2) + ")"
                          );

                  // 点击 marker 时，右侧联动：加载图片
                  marker.on('click', function () {
                    onMarkerClick(e);
                  });
                }

                // 2. 如果有 WKT，画出该记录自己的几何边框
                if (e.wkt) {
                  const geom = parseWKT(e.wkt);
                  if (!geom || !geom.coords || geom.coords.length === 0) {
                    return;
                  }

                  if (geom.type === 'linestring') {
                    L.polyline(geom.coords).addTo(map);
                  } else if (geom.type === 'polygon') {
                    L.polygon(geom.coords).addTo(map);
                  } else if (geom.type === 'point') {
                    // 若几何本身是点，看需求是否额外画
                    // L.marker(geom.coords[0]).addTo(map);
                  }
                }
              });
            })
            .catch(function (err) {
              console.error("loadEntities error:", err);
            });
  }

  /**
   * marker 点击后的联动逻辑：
   *  1. 更新右侧标题
   *  2. 去后端按 SID 查询缩略图（/api/Simages?sid=...）
   *  3. 把返回的图片列表渲染到右侧 image-panel
   */
  function onMarkerClick(entity) {
    const sid = entity.sid;
    const name = entity.name || ('SID ' + sid);

    // 1) 更新右侧标题
    const titleDiv = document.getElementById('right-title');
    titleDiv.textContent = '选中地点：' + name + ' (SID=' + sid + ')';

  // 2) 设置上传表单的 SID（隐藏字段）
  const sidInput = document.getElementById('upload-sid');
  sidInput.value = sid;

  // 可以顺便预填一下标题，例如：
  const titleInput = document.getElementById('upload-title');
  if (!titleInput.value) {
    titleInput.value = name + ' 的新图片';
  }

  // 3) 清空图片面板，先显示“加载中…”
  const panel = document.getElementById('image-panel');
  panel.innerHTML = '正在加载图片...';

    // 3) 调用后端接口，根据 SID 查图片
    fetch(baseUrl + '/api/Simages?sid=' + encodeURIComponent(sid))
            .then(resp => resp.json())
            .then(images => {
              panel.innerHTML = '';

              if (!images || images.length === 0) {
                panel.textContent = '没有找到相关图片。';
                return;
              }

      images.forEach(function (img) {
        // img 结构：{ id: MID, title: Title }

        const div = document.createElement('div');
        div.className = 'thumb-item';

        const imageElem = document.createElement('img');
        // 显示实际图像：由 /image?mid=... 输出 ORDSYS.ORDImage 的二进制
        imageElem.src = baseUrl + '/image?mid=' + img.id;
        imageElem.alt = img.title || '';

                const caption = document.createElement('div');
                caption.className = 'thumb-title';
                caption.textContent = img.title || ('Image ' + img.id);

                div.appendChild(imageElem);
                div.appendChild(caption);

        panel.appendChild(div);
      });
    })
    .catch(err => {
      console.error('load images error:', err);
      panel.textContent = '加载图片时出错。';
    });
}

  /**
   * 通用 WKT 解析：
   * 支持：
   *   POINT (x y)
   *   LINESTRING (x1 y1, x2 y2, ...)
   *   POLYGON ((x1 y1, x2 y2, ...))
   * 返回：
   *   { type: 'point' | 'linestring' | 'polygon',
   *     coords: [[lat, lon], ...] }
   * 注意：Oracle WKT 是 (X Y) = (lon, lat)，Leaflet 要 [lat, lon]，
   *       所以这里做了 (y, x) 对调。
   */
  function parseWKT(wkt) {
    if (!wkt) return null;
    wkt = wkt.trim();

    // POINT
    var mPoint = wkt.match(/^POINT\s*\(\s*([-0-9.+]+)\s+([-0-9.+]+)\s*\)$/i);
    if (mPoint) {
      var x = parseFloat(mPoint[1]);
      var y = parseFloat(mPoint[2]);
      return {
        type: 'point',
        coords: [[y, x]]
      };
    }

    // LINESTRING
    var mLine = wkt.match(/^LINESTRING\s*\((.+)\)$/i);
    if (mLine) {
      var coordPart = mLine[1].trim();
      var pairs = coordPart.split(",");

      var coords = pairs.map(function (p) {
        var nums = p.trim().split(/\s+/);
        var x = parseFloat(nums[0]);
        var y = parseFloat(nums[1]);
        return [y, x];  // Leaflet: [lat, lon]
      });
      return {
        type: 'linestring',
        coords: coords
      };
    }

    // POLYGON（只取外环）
    var mPoly = wkt.match(/^POLYGON\s*\(\(\s*(.+?)\s*\)\)$/i);
    if (mPoly) {
      var coordPart2 = mPoly[1].trim();
      var pairs2 = coordPart2.split(",");

      var coords2 = pairs2.map(function (p) {
        var nums = p.trim().split(/\s+/);
        var x = parseFloat(nums[0]);
        var y = parseFloat(nums[1]);
        return [y, x];
      });
      return {
        type: 'polygon',
        coords: coords2
      };
    }

    console.warn("Unsupported WKT:", wkt);
    return null;
  }

  function doSearch() {
    alert("Search not implemented");
  }
</script>

</body>
</html>
