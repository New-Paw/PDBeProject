<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>Web Map</title>

  <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>

  <style>
    body, html {
      margin: 0;
      padding: 0;
      height: 100%;
      font-family: Arial, sans-serif;
    }

    #container {
      display: flex;
      flex-direction: row;
      height: 100vh;
    }

    /* Left pane: map */
    #left-pane {
      flex: 2;
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
      flex: 1;
      border: 1px solid #ccc;
      background-color: #e6e6e6;
    }

    /* Right pane: upload + edit + filter + thumbnails */
    #right-pane {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 10px;
    }

    #right-title {
      font-weight: bold;
      margin-bottom: 8px;
      border-bottom: 1px solid #ccc;
      padding-bottom: 4px;
    }

    #upload-panel,
    #edit-panel,
    #filter-panel,
    #spatial-panel {
      margin-bottom: 10px;
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

    #edit-panel {
      display: none;
    }

    #image-panel {
      flex: 1;
      overflow-y: auto;
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 6px;
      background-color: #fff;
    }

    .thumb-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 8px;
      border: 1px solid #eee;
      padding: 4px;
      border-radius: 4px;
      background-color: #fafafa;
      cursor: pointer;
    }

    .thumb-item img {
      max-width: 100%;
      max-height: 120px;
      object-fit: contain;
      display: block;
      margin-bottom: 4px;
    }

    .thumb-title {
      font-size: 12px;
      text-align: center;
      color: #333;
      word-break: break-all;
    }

    /* Modal styles for spatial entity create/edit/delete */
    .modal {
      position: fixed;
      z-index: 999;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      background: rgba(0,0,0,0.35);
      display: none;
      align-items: center;
      justify-content: center;
    }

    .modal-content {
      background-color: #fdfcff;
      padding: 16px 20px 20px 20px;
      border-radius: 10px;
      width: 360px;
      box-shadow: 0 8px 20px rgba(0,0,0,0.2);
      font-size: 14px;
    }

    .modal-content h3 {
      margin-top: 0;
      color: #333;
    }

    .modal-content label {
      display: inline-block;
      width: 90px;
      font-size: 13px;
      color: #555;
    }

    .modal-content input,
    .modal-content select {
      width: calc(100% - 100px);
      box-sizing: border-box;
      padding: 4px 6px;
      margin-top: 2px;
      margin-bottom: 4px;
      border-radius: 4px;
      border: 1px solid #ccc;
      font-size: 13px;
    }

    .modal-content .button-row {
      margin-top: 8px;
    }

    .modal-content button {
      padding: 6px 12px;
      border-radius: 16px;
      border: 1px solid #d1cdfa;
      background: #f4f0ff;
      color: #6f3dba;
      cursor: pointer;
      font-size: 13px;
    }

    .modal-content button + button {
      margin-left: 6px;
    }
  </style>
</head>
<body>

<div id="container">
  <div id="left-pane">
    <div id="map"></div>
  </div>

  <div id="right-pane">
    <div id="right-title">click a marker</div>

    <!-- The button of the create/edit/delete */ -->
    <div id="spatial-panel">
      <div class="upload-row">
        <button type="button" onclick="openCreateSpatialModal()">Add entity</button>
        <button type="button" onclick="openEditSpatialModal()">Edit selected</button>
        <button type="button" onclick="openDeleteSpatialModal()">Delete selected</button>
      </div>
      <div style="font-size:12px;color:#666;">
        Coordinates are in local map space (0–500).
        Please click a marker before editing or deleting.
      </div>
    </div>

    <!-- Upload panel -->
    <div id="upload-panel">
      <form id="upload-form"
            method="post"
            action="<%= request.getContextPath() %>/uploadImage"
            enctype="multipart/form-data">

        <input type="hidden" name="sid" id="upload-sid">

        <div class="upload-row">
          <label for="upload-title">Title</label>
          <input type="text" name="title" id="upload-title" placeholder="input title">
        </div>

        <div class="upload-row">
          <label for="upload-time">Date</label>
          <input type="date" name="tokentime" id="upload-time">
        </div>

        <div class="upload-row">
          <label for="upload-file">Select file:</label>
          <input type="file" name="image" id="upload-file" accept="image/*">
        </div>

        <div class="upload-row">
          <button type="submit">upload</button>
        </div>
      </form>
    </div>

    <!-- Edit selected image -->
    <div id="edit-panel">
      <input type="hidden" id="edit-mid">

      <div class="upload-row">
        <label>Selected:</label>
        <span id="edit-img-title"></span>
      </div>

      <div class="upload-row">
        <label for="edit-title">New title:</label>
        <input type="text" id="edit-title" placeholder="edit title">
      </div>

      <div class="upload-row">
        <button type="button" onclick="saveTitle()">Save Title</button>
        <button type="button" onclick="deleteImage()">Delete</button>
        <!-- Similar image search trigger -->
        <button type="button" onclick="findSimilar()">Find Similar</button>
      </div>
    </div>

    <!-- Time range filter -->
    <div id="filter-panel">
      <div class="upload-row">
        <label for="filter-start">From</label>
        <input type="date" id="filter-start">
      </div>
      <div class="upload-row">
        <label for="filter-end">To</label>
        <input type="date" id="filter-end">
      </div>
      <div class="upload-row">
        <button type="button" onclick="applyFilter()">Filter</button>
        <button type="button" onclick="clearFilter()">Clear</button>
      </div>
    </div>

    <div id="image-panel">
      none marker selected
    </div>
  </div>
</div>

<!-- Create spatial entity modal -->
<div id="spatial-create-modal" class="modal">
  <div class="modal-content">
    <h3>Create spatial entity</h3>
    <form method="post" action="<%= request.getContextPath() %>/createSpatial">
      <div class="upload-row">
        <label for="spatial-create-sid">SID</label>
        <input type="number" id="spatial-create-sid" name="sid" required/>
      </div>
      <div class="upload-row">
        <label for="spatial-create-name">Name</label>
        <input type="text" id="spatial-create-name" name="entityName" required/>
      </div>
      <div class="upload-row">
        <label for="spatial-create-type">Type</label>
        <input type="text" id="spatial-create-type" name="entityType" required/>
      </div>
      <div class="upload-row">
        <label for="spatial-create-shape">Shape</label>
        <select id="spatial-create-shape" name="shape"
                onchange="onSpatialShapeChange('create')">
          <option value="point">Point</option>
          <option value="rectangle">Rectangle</option>
        </select>
      </div>
      <!-- Point coordinates -->
      <div id="spatial-create-point-fields">
        <div class="upload-row">
          <label for="spatial-create-x">X</label>
          <input type="number" step="0.1" id="spatial-create-x" name="x"/>
        </div>
        <div class="upload-row">
          <label for="spatial-create-y">Y</label>
          <input type="number" step="0.1" id="spatial-create-y" name="y"/>
        </div>
      </div>
      <!-- Rectangle coordinates -->
      <div id="spatial-create-rect-fields" style="display:none;">
        <div class="upload-row">
          <label for="spatial-create-x1">X1</label>
          <input type="number" step="0.1" id="spatial-create-x1" name="x1"/>
        </div>
        <div class="upload-row">
          <label for="spatial-create-y1">Y1</label>
          <input type="number" step="0.1" id="spatial-create-y1" name="y1"/>
        </div>
        <div class="upload-row">
          <label for="spatial-create-x2">X2</label>
          <input type="number" step="0.1" id="spatial-create-x2" name="x2"/>
        </div>
        <div class="upload-row">
          <label for="spatial-create-y2">Y2</label>
          <input type="number" step="0.1" id="spatial-create-y2" name="y2"/>
        </div>
      </div>
      <div class="button-row">
        <button type="submit">Save</button>
        <button type="button" onclick="closeCreateSpatialModal()">Cancel</button>
      </div>
    </form>
  </div>
</div>

<!-- Edit spatial entity modal -->
<div id="spatial-edit-modal" class="modal">
  <div class="modal-content">
    <h3>Edit spatial entity</h3>
    <form method="post" action="<%= request.getContextPath() %>/updateSpatial">
      <div class="upload-row">
        <label for="spatial-edit-sid">SID</label>
        <input type="number" id="spatial-edit-sid" name="sid" required/>
      </div>
      <div class="upload-row">
        <label for="spatial-edit-name">Name</label>
        <input type="text" id="spatial-edit-name" name="entityName" required/>
      </div>
      <div class="upload-row">
        <label for="spatial-edit-type">Type</label>
        <input type="text" id="spatial-edit-type" name="entityType" required/>
      </div>
      <div class="upload-row">
        <label for="spatial-edit-shape">Shape</label>
        <select id="spatial-edit-shape" name="shape"
                onchange="onSpatialShapeChange('edit')">
          <option value="">Do not change geometry</option>
          <option value="point">Point</option>
          <option value="rectangle">Rectangle</option>
        </select>
      </div>
      <!-- Point coords for edit -->
      <div id="spatial-edit-point-fields" style="display:none;">
        <div class="upload-row">
          <label for="spatial-edit-x">X</label>
          <input type="number" step="0.1" id="spatial-edit-x" name="x"/>
        </div>
        <div class="upload-row">
          <label for="spatial-edit-y">Y</label>
          <input type="number" step="0.1" id="spatial-edit-y" name="y"/>
        </div>
      </div>
      <!-- Rectangle coords for edit -->
      <div id="spatial-edit-rect-fields" style="display:none;">
        <div class="upload-row">
          <label for="spatial-edit-x1">X1</label>
          <input type="number" step="0.1" id="spatial-edit-x1" name="x1"/>
        </div>
        <div class="upload-row">
          <label for="spatial-edit-y1">Y1</label>
          <input type="number" step="0.1" id="spatial-edit-y1" name="y1"/>
        </div>
        <div class="upload-row">
          <label for="spatial-edit-x2">X2</label>
          <input type="number" step="0.1" id="spatial-edit-x2" name="x2"/>
        </div>
        <div class="upload-row">
          <label for="spatial-edit-y2">Y2</label>
          <input type="number" step="0.1" id="spatial-edit-y2" name="y2"/>
        </div>
      </div>
      <div class="button-row">
        <button type="submit">Save</button>
        <button type="button" onclick="closeEditSpatialModal()">Cancel</button>
      </div>
    </form>
  </div>
</div>

<!-- Delete spatial entity modal -->
<div id="spatial-delete-modal" class="modal">
  <div class="modal-content">
    <h3>Delete spatial entity</h3>
    <form method="post" action="<%= request.getContextPath() %>/deleteSpatial">
      <div class="upload-row">
        <label for="spatial-delete-sid">SID</label>
        <input type="number" id="spatial-delete-sid" name="sid" required/>
      </div>
      <p style="color:red;font-size:13px;">
        This operation cannot be undone. Are you sure?
      </p>
      <div class="button-row">
        <button type="submit">Delete</button>
        <button type="button" onclick="closeDeleteSpatialModal()">Cancel</button>
      </div>
    </form>
  </div>
</div>

<script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>

<script>
  // Leaflet map using simple CRS (0–500 local coordinates)
  var map = L.map('map', {
    crs: L.CRS.Simple
  });

  map.fitBounds([[0, 0], [500, 500]]);

  const baseUrl = '<%= request.getContextPath() %>';

  // currentSid == null -> show all images
  let currentSid = null;
  let currentEntityName = '';
  let selectedImageMid = null;

  // hold currently selected spatial entity (from SEntities)
  let selectedEntity = null;

  // Initial load (map + all images)
  loadEntities();
  loadImagesForCurrent();

  // Load spatial entities and draw markers/geometries
  function loadEntities() {
    fetch(baseUrl + '/api/Sentities')
            .then(resp => resp.json())
            .then(data => {
              if (!data || data.length === 0) return;

              data.forEach(function (e) {
                // Center marker
                if (typeof e.lat === "number" && typeof e.lon === "number") {
                  const lat = e.lat;
                  const lon = e.lon;
                  const name = e.name || "";

                  const marker = L.marker([lat, lon]).addTo(map)
                          .bindPopup(
                                  name + "<br/>(" +
                                  lat.toFixed(2) + ", " +
                                  lon.toFixed(2) + ")"
                          );

                  marker.on('click', function () {
                    onMarkerClick(e);
                  });
                }

                // Optional geometry overlay (polyline/polygon/point)
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
                    L.marker(geom.coords[0]).addTo(map);
                  }
                }
              });
            })
            .catch(function (err) {
              console.error("loadEntities error:", err);
            });
  }

  // Marker click handler: restrict images to given SID
  function onMarkerClick(entity) {
    const sid = entity.sid;
    const name = entity.name || ('SID ' + sid);

    currentSid = sid;
    currentEntityName = name;
    selectedEntity = entity;  // remember selected spatial entity

    selectedImageMid = null;
    document.getElementById('edit-panel').style.display = 'none';

    document.getElementById('filter-start').value = '';
    document.getElementById('filter-end').value = '';

    const sidInput = document.getElementById('upload-sid');
    sidInput.value = sid;

    loadImagesForCurrent();
  }

  /**
   * Load image list according to currentSid and optional date filter.
   *   currentSid == null -> /api/Simages (all images)
   *   currentSid != null -> /api/Simages?sid=...
   */
  function loadImagesForCurrent() {
    const panel = document.getElementById('image-panel');
    const titleDiv = document.getElementById('right-title');

    if (currentSid == null) {
      titleDiv.textContent = 'All images';
      document.getElementById('upload-sid').value = '';
    } else {
      titleDiv.textContent = 'Selected: ' + currentEntityName + ' (SID=' + currentSid + ')';
    }

    panel.innerHTML = 'loading...';

    const start = document.getElementById('filter-start')
            ? document.getElementById('filter-start').value
            : '';
    const end = document.getElementById('filter-end')
            ? document.getElementById('filter-end').value
            : '';

    let url = baseUrl + '/api/Simages';
    const params = [];

    if (currentSid != null) {
      params.push('sid=' + encodeURIComponent(currentSid));
    }
    if (start) {
      params.push('from=' + encodeURIComponent(start));
    }
    if (end) {
      params.push('to=' + encodeURIComponent(end));
    }

    if (params.length > 0) {
      url += '?' + params.join('&');
    }

    fetch(url)
            .then(resp => resp.json())
            .then(images => {
              renderImages(images);
            })
            .catch(err => {
              console.error('load images error:', err);
              panel.textContent = 'error...';
            });
  }

  // Render image thumbnails into the right-hand panel
  function renderImages(images) {
    const panel = document.getElementById('image-panel');
    panel.innerHTML = '';

    if (!images || images.length === 0) {
      panel.textContent = 'No images found';
      return;
    }

    images.forEach(function (img) {
      // img: { id: MID, title: Title, tokentime?: 'yyyy-MM-dd' }
      const div = document.createElement('div');
      div.className = 'thumb-item';

      const imageElem = document.createElement('img');
      imageElem.src = baseUrl + '/image?mid=' + img.id;
      imageElem.alt = img.title || '';

      const caption = document.createElement('div');
      caption.className = 'thumb-title';

      let text = img.title || ('Image ' + img.id);
      if (img.tokentime) {
        text += ' (' + img.tokentime + ')';
      }
      caption.textContent = text;

      div.appendChild(imageElem);
      div.appendChild(caption);

      div.addEventListener('click', function () {
        onThumbClick(img);
      });

      panel.appendChild(div);
    });
  }

  // Trigger similar-image search based on selectedImageMid
  function findSimilar() {
    if (!selectedImageMid) {
      alert("No image selected.");
      return;
    }

    const panel = document.getElementById('image-panel');
    panel.innerHTML = 'searching similar images...';

    const url = baseUrl + '/api/similar?mid=' + encodeURIComponent(selectedImageMid);

    fetch(url)
            .then(resp => resp.json())
            .then(images => {
              // Similar servlet returns an array (even if it contains only one image)
              renderImages(images);
            })
            .catch(err => {
              console.error('find similar error:', err);
              panel.textContent = 'Error when searching similar images.';
            });
  }

  // Thumbnail click -> show edit panel for this MID
  function onThumbClick(img) {
    selectedImageMid = img.id;

    const editPanel = document.getElementById('edit-panel');
    editPanel.style.display = 'block';

    const label = document.getElementById('edit-img-title');
    label.textContent = img.title || ('Image ' + img.id);

    const titleInput = document.getElementById('edit-title');
    titleInput.value = img.title || '';
  }

  // Update image title via /updateImageTitle
  function saveTitle() {
    if (!selectedImageMid) {
      alert("No image selected.");
      return;
    }
    const newTitle = document.getElementById('edit-title').value || '';

    fetch(baseUrl + '/updateImageTitle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
      },
      body: 'mid=' + encodeURIComponent(selectedImageMid) +
              '&title=' + encodeURIComponent(newTitle)
    })
            .then(resp => {
              if (!resp.ok) throw new Error('HTTP ' + resp.status);
              loadImagesForCurrent();
            })
            .catch(err => {
              console.error('update title error:', err);
              alert('Update failed');
            });
  }

  // Delete image via /deleteImage
  function deleteImage() {
    if (!selectedImageMid) {
      alert("No image selected.");
      return;
    }
    if (!confirm('Are you sure you want to delete this image?')) return;

    fetch(baseUrl + '/deleteImage', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
      },
      body: 'mid=' + encodeURIComponent(selectedImageMid)
    })
            .then(resp => {
              if (!resp.ok) throw new Error('HTTP ' + resp.status);
              selectedImageMid = null;
              document.getElementById('edit-panel').style.display = 'none';
              loadImagesForCurrent();
            })
            .catch(err => {
              console.error('delete image error:', err);
              alert('Delete failed');
            });
  }

  // Apply time range filter
  function applyFilter() {
    loadImagesForCurrent();
  }

  // Clear time range filter
  function clearFilter() {
    document.getElementById('filter-start').value = '';
    document.getElementById('filter-end').value = '';
    loadImagesForCurrent();
  }

  /**
   * Parse simple WKT geometries:
   *   POINT (x y)
   *   LINESTRING (x1 y1, x2 y2, ...)
   *   POLYGON ((x1 y1, x2 y2, ...))
   * Oracle WKT: (X Y) = (lon, lat), Leaflet: [lat, lon]
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
        return [y, x];
      });
      return {
        type: 'linestring',
        coords: coords
      };
    }

    // POLYGON
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

  // ===== Spatial entity modal helpers =====

  function openCreateSpatialModal() {
    // Reset fields
    document.getElementById('spatial-create-sid').value = '';
    document.getElementById('spatial-create-name').value = '';
    document.getElementById('spatial-create-type').value = '';
    document.getElementById('spatial-create-shape').value = 'point';
    onSpatialShapeChange('create');
    document.getElementById('spatial-create-modal').style.display = 'flex';
  }

  function closeCreateSpatialModal() {
    document.getElementById('spatial-create-modal').style.display = 'none';
  }

  function openEditSpatialModal() {
    if (!selectedEntity) {
      alert("Please click a spatial marker first.");
      return;
    }
    // Prefill SID, name, type from selectedEntity
    document.getElementById('spatial-edit-sid').value = selectedEntity.sid;
    document.getElementById('spatial-edit-name').value = selectedEntity.name || '';
    document.getElementById('spatial-edit-type').value = selectedEntity.type || '';

    // By default do not change geometry
    document.getElementById('spatial-edit-shape').value = '';
    onSpatialShapeChange('edit');

    document.getElementById('spatial-edit-modal').style.display = 'flex';
  }

  function closeEditSpatialModal() {
    document.getElementById('spatial-edit-modal').style.display = 'none';
  }

  function openDeleteSpatialModal() {
    if (!selectedEntity) {
      alert("Please click a spatial marker first.");
      return;
    }
    // Prefill SID for delete
    document.getElementById('spatial-delete-sid').value = selectedEntity.sid;
    document.getElementById('spatial-delete-modal').style.display = 'flex';
  }

  function closeDeleteSpatialModal() {
    document.getElementById('spatial-delete-modal').style.display = 'none';
  }

  // Switch shape between point and rectangle (for create/edit modals)
  function onSpatialShapeChange(prefix) {
    var shapeSelect = document.getElementById('spatial-' + prefix + '-shape');
    if (!shapeSelect) return;
    var shape = shapeSelect.value;

    var pointFields = document.getElementById('spatial-' + prefix + '-point-fields');
    var rectFields  = document.getElementById('spatial-' + prefix + '-rect-fields');
    if (!pointFields || !rectFields) return;

    if (shape === 'point') {
      pointFields.style.display = 'block';
      rectFields.style.display = 'none';
    } else if (shape === 'rectangle') {
      pointFields.style.display = 'none';
      rectFields.style.display = 'block';
    } else {
      pointFields.style.display = 'none';
      rectFields.style.display = 'none';
    }
  }
</script>

</body>
</html>
