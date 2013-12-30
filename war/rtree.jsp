<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.roger.*" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>R-Tree</title>
</head>
<body>
<%
%><p><b><%= RTree.size() %></b></p><%
String Slatitude = request.getParameter("latitude");
String Slongitude = request.getParameter("longitude");
String SDlatitude = request.getParameter("Dlatitude");
String SDlongitude = request.getParameter("Dlongitude");
if (Slatitude != null) {
	float latitude = Float.parseFloat(Slatitude);
	float longitude = Float.parseFloat(Slongitude);
	float Dlatitude = Float.parseFloat(SDlatitude);
	float Dlongitude = Float.parseFloat(SDlongitude);
	
	long time_start, time_end;
	time_start = System.currentTimeMillis();
	List<Entity> result = RTree.search(new float[] {latitude, longitude}, new float[] {Dlatitude, Dlongitude});
	for (Entity geopos : result) {
        %>
        <p><b><%= geopos.getProperty("latitude") %></b></p>
        <%
        %>
        <p><b><%= geopos.getProperty("longitude") %></b></p>
        <%
        %>
        <p><b><%= geopos.getKind() %></b></p>
        <%
        %>
        <p><b><%= geopos.getKey() %></b></p>
        <%
        }
	time_end = System.currentTimeMillis();
	%>
    <p><b><%= time_end - time_start %></b></p>
    <%
}
%>

	<form action="/insert" method="post">
      	Latitude: <input type="text" name="latitude" /><br />
		Longitude: <input type="text" name="longitude" />
      	<div><input type="submit" value="Insert" /></div>
    </form>
    <form action="/rtree.jsp" method="post">
      	Latitude: <input type="text" name="latitude" /><br />
		Longitude: <input type="text" name="longitude" /><br />
		Latitude Dimension: <input type="text" name="Dlatitude" /><br />
		Longitude Dimension: <input type="text" name="Dlongitude" />
      	<div><input type="submit" value="Search" /></div>
    </form>
    <form action="/index" method="post">
      	<div><input type="submit" value="Index" /></div>
    </form>
    <form action="/insertFile" method="post">
      	<div><input type="submit" value="Cargar" /></div>
    </form>
</body>
</html>