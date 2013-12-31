package com.pq.pebcast.referenceimpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InspirationalQuotesCastServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			Execute();
			resp.getWriter().write("Job run successfully");
		} catch (Exception e) {
			PrintWriter out = resp.getWriter();
			e.printStackTrace(out);
			
		}
	}

	public void Execute() throws Exception {
				PebcastPublishUtil.publishToCast("<pebcast assigned secret key for your cast>", InspirationalMessages.randomMessage(), null, "Pebcast reference impl", "n", 24, null);
	}
}
