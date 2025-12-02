<%--
  Created by IntelliJ IDEA.
  User: zhaomengran
  Date: 2025/12/2/星期二
  Time: 0:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>View Product Image</title></head>
<body>
<%
  String code = request.getParameter("code");
  if (code == null) {
    code = "1"; // 默认看看 1 号
  }
%>
<h1>Product <%= code %> Image</h1>

<img src="<%= request.getContextPath() %>/image?code=<%= code %>" alt="Product image"/>

<br/><br/>
<a href="<%= request.getContextPath() %>/upload">Upload another image</a>

</body>
</html>
