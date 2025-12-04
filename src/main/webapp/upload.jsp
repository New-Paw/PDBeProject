<%--
  Created by IntelliJ IDEA.
  User: zhaomengran
  Date: 2025/12/2/星期二
  Time: 0:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Upload Product Image</title></head>
<body>
<h1>Upload Product Image</h1>

<form method="post" action="${pageContext.request.contextPath}/upload"
      enctype="multipart/form-data">
    Code: <input type="number" name="MID" required><br/>
    SID_ref: <input type="number" name="SID_ref" required><br/>
    Title: <input type="text" name="title" required><br/>
    Tokentime: <input type="Date" name="Tokentime" required><br/>
    Image: <input type="file" name="image" accept="image/*" required><br/>
    <button type="submit">Upload</button>
</form>

</body>
</html>
