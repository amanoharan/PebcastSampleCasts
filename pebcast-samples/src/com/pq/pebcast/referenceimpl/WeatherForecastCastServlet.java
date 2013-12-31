package com.pq.pebcast.referenceimpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherForecastCastServlet extends HttpServlet {
	static final String PEBCAST_API_KEY = "<pebcast assigned secret key>";
	private static final Logger log = Logger.getLogger(WeatherTaskProcessorServlet.class.getName());
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			Execute(req.getParameter("type"));
			resp.getWriter().write("Job run successfully");
		} catch (Exception e) {
			PrintWriter out = resp.getWriter();
			e.printStackTrace(out);
		}
	}


	public void Execute(String type) throws Exception {
		String subscribers = PebcastPublishUtil.getSubscribers(PEBCAST_API_KEY, type);
		JsonParser parser = new JsonParser();
		JsonObject subs =  (JsonObject)parser.parse(subscribers);
		Queue queue = QueueFactory.getQueue("WeatherQueue");

		if(subs.has("users")) {
			 // Create Task and push it into Task Queue
			JsonArray users = subs.get("users").getAsJsonArray();
			for(int i=0;i<users.size();i++) {
				JsonObject user = users.get(i).getAsJsonObject();
				TaskOptions taskOptions = TaskOptions.Builder.withUrl("/task/weather")
						.payload(user.toString())
                        //.param("json", user.toString())
                        .method(Method.POST);
				queue.add(taskOptions);
			}
			log.info("Processing weather for " + users.size() + " subscribers");

			
		}

		
		
			
			

			
	}
}
