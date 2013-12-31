package com.pq.pebcast.referenceimpl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherTaskProcessorServlet extends HttpServlet {
	static final String FORECAST_IO_API_KEY = "<secret key assigned by forecast io>";
	static final String REGION_US = "us";
	private static final Logger log = Logger.getLogger(WeatherTaskProcessorServlet.class.getName());

	static final char REPLACEMENT_CHAR = 0xFFFD;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		DataInputStream dataIn = new DataInputStream(req.getInputStream());
		//we are taking the length of Content type data
		int formDataLength = req.getContentLength();
		byte dataBytes[] = new byte[formDataLength];
		int byteRead = 0;
		int totalBytesRead = 0;
		//this loop converting the uploaded file into byte code
		while (totalBytesRead < formDataLength) {
		  byteRead = dataIn.read(dataBytes, totalBytesRead,formDataLength);
		  totalBytesRead += byteRead;
		}
		String file = new String(dataBytes);
		JsonParser parser = new JsonParser();
		JsonObject user =  (JsonObject)parser.parse(file);
		
		String uomPreference = user.get("uom").getAsString();
		String username = user.get("useralias").getAsString();
		Double latD = null;
		Double lonD = null;
		try {
			latD = Double.valueOf(user.get("milliLat").getAsString()) / 10000;
			lonD = Double.valueOf(user.get("milliLon").getAsString()) / 10000;
		} catch(Exception ex) {
			log.severe("Location not found for user " + username);
			JsonObject response;
			try {
				String weather = "Location not sent by watchapp, cant provide weather. Please allow HTTPebble to use your location";
				PebcastPublishUtil.publishToCastUser(
						WeatherForecastCastServlet.PEBCAST_API_KEY, username, weather,
						null, "Pebcast Weather Reference Impl", "n", 24, null);	
				log.severe("No location for user " + username + ". " + weather);
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.severe(e.getMessage());
			}
		}
		JsonObject response;
		try {
			response = getWeather(latD, lonD, uomPreference);
			String weather = formattedDailyWeather(response, uomPreference);
			PebcastPublishUtil.publishToCastUser(
					WeatherForecastCastServlet.PEBCAST_API_KEY, username, weather,
					null, "Pebcast Weather Reference Impl", "n", 24, null);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.severe(stackTrace(e));
		}
	}
	
	
	public JsonObject getWeather(double latD, double lonD, String uom) throws Exception {
		if(uom.equals("standard"))
			uom = "si";
		String requestUrl = 
				"https://api.forecast.io/forecast/" + FORECAST_IO_API_KEY + "/" + latD + "," + lonD +
				"?exclude=hourly,alerts&units=" + uom;
				//"?exclude=hourly,daily,alerts";
		return PebcastPublishUtil.getJsonFromUrl(requestUrl);
	}

	public String formattedDailyWeather(JsonObject json, String uom) {
			JsonObject daily = json.get("daily").getAsJsonObject();
			JsonObject firstDay = daily.get("data").getAsJsonArray().get(0).getAsJsonObject();
			StringBuilder weather = new StringBuilder()
						.append((int)firstDay.get("temperatureMin").getAsDouble())
						.append("|")
						.append((int)firstDay.get("temperatureMax").getAsDouble());
			if(uom.equals(REGION_US))
				weather.append("F,");
			else
				weather.append("C,");
			weather.append(daily.get("summary").getAsString());
			if(firstDay.get("precipProbability").getAsDouble() > 0) {
				weather.append(" ")
				.append((int)(firstDay.get("precipProbability").getAsDouble() * 100)).append("% ")
				.append(firstDay.get("precipType").getAsString())
				.append(" chance today.");
			}
			String output = weather.toString();
			if(uom.equals(REGION_US))
				output = output.replace("" + REPLACEMENT_CHAR + REPLACEMENT_CHAR, "F");
			else
				output = output.replace("" + REPLACEMENT_CHAR + REPLACEMENT_CHAR, "C");
			return output;

	}
	private String formattedCurrentWeather(JsonObject json, String uom) {
			JsonObject currently = json.get("currently").getAsJsonObject();
			StringBuilder weather = new StringBuilder(currently.get("summary").getAsString())
						.append((int)currently.get("temperature").getAsDouble());
			if(uom.equals(REGION_US))
				weather.append("F,");
			else
				weather.append("C,");
			if(currently.get("precipProbability").getAsDouble() > 0)
				weather.append(",").append((int)currently.get("precipProbability").getAsDouble()).append("% ")
				.append(currently.get("precipType").getAsString())
				.append(" chance today.");
			
			String output = weather.toString();
			if(uom.equals(REGION_US))
				output = output.replace("" + REPLACEMENT_CHAR + REPLACEMENT_CHAR, "F");
			else
				output = output.replace("" + REPLACEMENT_CHAR + REPLACEMENT_CHAR, "C");
			return output;
	}


	public static String stackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}

}
