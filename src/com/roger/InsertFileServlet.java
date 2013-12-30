package com.roger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class InsertFileServlet extends HttpServlet {
	public boolean isNumeric(String input) {
		try {
			Float.parseFloat(input);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		
		resp.getWriter().println("Hello, world");
		
		DatastoreService datastore = DSF.getDatastoreService();
		
		//resp.getWriter().println("file:/" + req.getParameter("path"));
		
		
		
		File nombre = new File("WEB-INF/data.txt");
		BufferedReader entrada = new BufferedReader(new FileReader(nombre));
		String texto, temp = "";
		int i = 0;
		float latitude = 0, longitude = 0;
		Entity geopos;
		
		long time_start, time_end;
		time_start = System.currentTimeMillis();
		
		while ((texto = entrada.readLine()) != null) {
			//resp.getWriter().println(texto);
			String[] somethings = texto.split(" ");
			for(int j = 0; j < somethings.length; ++j) {
				
				if(isNumeric(somethings[j])) {
					if(i == 0) {
						latitude = Float.parseFloat(somethings[j]);
						++i;
					} else {
						longitude = Float.parseFloat(somethings[j]);
						//resp.getWriter().println(temp);
						//resp.getWriter().println(latitude);
						//resp.getWriter().println(longitude);
						
						geopos = new Entity("GeoPos");
						geopos.setProperty("name", temp);
				    	geopos.setProperty("latitude", latitude);
				    	geopos.setProperty("longitude", longitude);
				    	datastore.put(geopos);
						
				    	i = 0;
						temp = "";
					}
				}
				else 
					temp += somethings[j] + " ";
			}
		}
		time_end = System.currentTimeMillis();
		resp.getWriter().println(( time_end - time_start ));
		//resp.sendRedirect("/rtree.jsp");
		//BufferedReader bf = new BufferedReader(new FileReader("file:/" + req.getParameter("path")));
	}
}
