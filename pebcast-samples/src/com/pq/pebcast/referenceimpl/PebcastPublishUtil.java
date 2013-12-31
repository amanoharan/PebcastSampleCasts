package com.pq.pebcast.referenceimpl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class PebcastPublishUtil {
	public static final String HOST = "http://pebcast.com/";
	public static final String PEBCAST_PUBLISH_URL = HOST + "pub";
	public static final String PEBCAST_SUBSCRIBERS_URL = HOST + "list";
	public static final String PEBCAST_MAILRULES_URL = HOST + "rules";
	// public static final String PEBCAST_PUBLISH_URL =
	// "http://localhost:8080/pebcast-web2/pub";
	public static final String PEBCAST_PUBLISH_SUCCESS_MSG = "Message published successfully";

	public static void publishToCastUser(String id, String id2, String text, String value, String sender, String vibrate, int expiresAfterHours, String optionalDetails)
			throws Exception {
		StringBuilder urlText = new StringBuilder(PEBCAST_PUBLISH_URL);
		urlText.append("?id=").append(URLEncoder.encode(id == null ? "" : id));
		urlText.append("&id2=").append(URLEncoder.encode(id2 == null? "" : id2));
		urlText.append("&text=").append(URLEncoder.encode(text == null? "" : text))
				.append("&value=").append(URLEncoder.encode(value == null ? "" : value))
				.append("&sender=").append(URLEncoder.encode(sender == null ? "" : sender))
				.append("&vibrate=").append(URLEncoder.encode(vibrate == null ? "n" : vibrate));
		if(urlText.length() < 2000) {
			int available = 2000 - urlText.length();
			if(optionalDetails != null && optionalDetails.length() > 0) {
				if(optionalDetails.length() < available) {
					urlText.append("&opt=").append(URLEncoder.encode(optionalDetails));
				} else {
					urlText.append("&opt=").append(URLEncoder.encode(optionalDetails).substring(0, available));
				}
			}
		}
		if(expiresAfterHours > 0) {
			 DateTime dt = new DateTime();
			 dt = dt.plusHours(expiresAfterHours);
			 DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
			 String dateStr = fmt.print(dt);
			 urlText.append("&expires=").append(URLEncoder.encode(dateStr));
		}
			
		String response = sendMessage(urlText.toString());
		if (response.equals(PEBCAST_PUBLISH_SUCCESS_MSG))
			return;
		else
			throw new Exception("Publish failed:" + response.toString());

	}
	
	public static void postToCast(JsonObject response) throws Exception {
		postJsonMessage(PEBCAST_PUBLISH_URL, response);
	}
	
	public static void publishToCast(String id, String text, String value, String sender, String vibrate, int expiresAfterHours, String optionalDetails)
			throws Exception {
		StringBuilder urlText = new StringBuilder(PEBCAST_PUBLISH_URL);
		urlText.append("?id=").append(URLEncoder.encode(id == null ? "" : id));
		urlText.append("&text=").append(URLEncoder.encode(text == null? "" : text))
		.append("&value=").append(URLEncoder.encode(value == null ? "" : value))
		.append("&sender=").append(URLEncoder.encode(sender == null ? "" : sender))
		.append("&vibrate=").append(URLEncoder.encode(vibrate == null ? "n" : vibrate));
		if(expiresAfterHours > 0) {
			 DateTime dt = new DateTime();
			 dt = dt.plusHours(expiresAfterHours);
			 DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
			 String dateStr = fmt.print(dt);
			 urlText.append("&expires=").append(URLEncoder.encode(dateStr));
		}
		if(urlText.length() < 2000) {
			int available = 2000 - urlText.length();
			if(optionalDetails != null && optionalDetails.length() > 0) {
				if(optionalDetails.length() < available) {
					urlText.append("&opt=").append(URLEncoder.encode(optionalDetails));
				} else {
					urlText.append("&opt=").append(URLEncoder.encode(optionalDetails).substring(0, available));
				}
			}
		}
		String response = sendMessage(urlText.toString());
		if (response.equals(PEBCAST_PUBLISH_SUCCESS_MSG))
			return;
		else
			throw new Exception("Publish failed:" + response.toString());

	}
	
	public static String getAllSubscribers(String apiKey) throws Exception {
		return getSubscribers(apiKey, "all");
	}
	
	public static String getNewSubscribers(String apiKey) throws Exception {
		return getSubscribers(apiKey, "new");
	}
	
	public static String getSubscribers(String apiKey, String type) throws Exception {
		StringBuilder urlText = new StringBuilder(PEBCAST_SUBSCRIBERS_URL);
		urlText.append("?id=").append(URLEncoder.encode(apiKey == null ? "" : apiKey));
		urlText.append("&type=").append(type);
		String response = sendMessage(urlText.toString());
		return response;
	}
	


	public static JsonObject getJsonFromUrl(String requestUrl) throws Exception {
			String jsonString = sendMessage(requestUrl);
			JsonParser parser = new JsonParser();
			System.out.println(jsonString);
			JsonObject response = (JsonObject)parser.parse(jsonString);
			// System.out.println(response);
			return response;
	}

	private static String postJsonMessage(String urlText, JsonObject json) throws Exception {
		URL url;
		url = new URL(urlText);
		URLConnection connection = null;
		connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection
		.addRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

		writer.write(json.toString());
		writer.flush();
		
		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			// JSON data get stored as a string
			response.append(inputLine);

		}
		writer.close();
		in.close();
		return response.toString();
	}
	public static String sendMessage(String urlText) throws Exception {
		URL url;
			url = new URL(urlText);
			URLConnection urlConnection = null;
			urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(60000);
			urlConnection.setReadTimeout(60000);
			urlConnection.setUseCaches(false);
			urlConnection
			.addRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

			urlConnection.connect();
			StringBuilder response = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				// JSON data get stored as a string
				response.append(inputLine);

			}
			in.close();
			return response.toString();

	}
	
	public static String[] sendMessageGetResponseRows(String urlText) throws Exception {
		URL url;
		url = new URL(urlText);
		URLConnection urlConnection = null;
		urlConnection = url.openConnection();
		urlConnection
		.addRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

		urlConnection.connect();
		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream()));
		String[] inputLines = new String[10];
		String inputLine;
		int index = 0;
		while ((inputLine = in.readLine()) != null) {
			inputLines[index] = inputLine;
			index++;
			if(index >=10)
				break;
		}
		in.close();
		return inputLines;

}

}
