package com.roger;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class InsertServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(InsertServlet.class.getName());
    
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	float latitude = Float.parseFloat(req.getParameter("latitude"));
    	float longitude = Float.parseFloat(req.getParameter("longitude"));
    	Entity geopos = new Entity("GeoPos");
    	geopos.setProperty("latitude", latitude);
    	geopos.setProperty("longitude", longitude);
    	
    	DatastoreService datastore = DSF.getDatastoreService();
        datastore.put(geopos);
        
        RTree.insert(new float[] {latitude, longitude}, new float[] {0.0f, 0.0f}, geopos);
        
        resp.sendRedirect("/rtree.jsp");
    }
}