package com.roger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class IndexServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		long time_start, time_end;
		
		DatastoreService datastore = DSF.getDatastoreService();
		Query query = new Query("GeoPos");
		time_start = System.currentTimeMillis();
		PreparedQuery pq = datastore.prepare(query);
		//List<Entity> result = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
		for (Entity geopos : pq.asIterable()) {
			//RTree.insert(new float[] {Float.parseFloat(geopos.getProperty("latitude").toString()), Float.parseFloat(geopos.getProperty("longitude").toString())}, new float[] {0.0f, 0.0f}, geopos);
			//resp.getWriter().println(geopos.getProperty("name"));
			//resp.getWriter().println(geopos.getProperty("latitude"));
			//resp.getWriter().println(geopos.getProperty("longitude"));
			RTree.insert(new float[] {Float.parseFloat(geopos.getProperty("latitude").toString()), Float.parseFloat(geopos.getProperty("longitude").toString())}, new float[] {0.0f, 0.0f}, geopos);
		}
		time_end = System.currentTimeMillis();
		resp.getWriter().println(( time_end - time_start ));
		
		//resp.sendRedirect("/rtree.jsp");
	}
}
