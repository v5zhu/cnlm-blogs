<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="sitemesh" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
	<title>CHOS:<sitemesh:title/></title>
	<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
	<meta http-equiv="Cache-Control" content="no-store" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Expires" content="0" />
	<link rel="stylesheet" href="${ctx}/bower_components/bootstrap/dist/css/bootstrap.min.css"/>
	<link rel="stylesheet" href="${ctx}/bower_components/bootstrap/dist/css/bootstrap-theme.min.css"/>
<%----%>
	<%--<link rel="stylesheet" href="${ctx}/bower_components/bootstrap/dist/css/amazeui.min.css"/>--%>
	<%--<link rel="stylesheet" href="${ctx}/bower_components/amazeui/dist/css/amazeui.flat.min.css"/>--%>

	<sitemesh:head/>
</head>

<body>

<div class="container">
	<%@ include file="/WEB-INF/layouts/header.jsp"%>
	<div id="content" style="min-height: 80%">
		<sitemesh:body/>
	</div>
	<%@ include file="/WEB-INF/layouts/footer.jsp"%>
</div>

<%--<script src="${ctx}/bower_components/jquery/dist/jquery.min.js"></script>--%>
<%--<script src="${ctx}/bower_components/amazeui/dist/js/amazeui.min.js"></script>--%>
<%--<script src="${ctx}/bower_components/amazeui/dist/js/amazeui.legacy.js"></script>--%>
</body>
</html>