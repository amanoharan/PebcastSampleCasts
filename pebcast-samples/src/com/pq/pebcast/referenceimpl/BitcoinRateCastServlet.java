package com.pq.pebcast.referenceimpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BitcoinRateCastServlet extends HttpServlet {
	private static final String BTC_API_KEY = "<Pebcast assigned secret key here>";
	private static final Logger log = Logger.getLogger(BitcoinRateCastServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			Execute(req.getParameter("type"));
			resp.getWriter().write("Job run successfully");
		} catch (Exception e) {
			System.out.println(stackTrace(e));
			
		}
	}

	private JsonObject getBTCRates() throws Exception {
		String requestUrl = "https://coinbase.com/api/v1/currencies/exchange_rates";
		return PebcastPublishUtil.getJsonFromUrl(requestUrl);
	}

	private String formattedCurrentUSDPrice(JsonObject prices, String currency) {
		String exchRate = "" + (int)Double.parseDouble(prices.get("btc_to_" + currency)
						.getAsString()); //+ " from gae"; // + " @ " + time;
		if(currency.equals("usd")) 
			return "BTC : $" + exchRate;
		else
			return "BTC : " + exchRate + " " + currency;

	}
	
	private boolean checkRange(JsonObject prices, String currency,
			String floor, String ceil) {
		double rate = Double.parseDouble(prices.get("btc_to_" + currency).getAsString());
		double floorD = 0.0; 
		double ceilD = 100000.0;
		if(floor != null && !floor.trim().equals(""))
			floorD = Double.parseDouble(floor);
		if(ceil != null && !ceil.trim().equals(""))
			ceilD = Double.parseDouble(ceil);
		if(rate <= floorD || rate >= ceilD)
			return true;
		else
			return false;
	}

	public void Execute(String type) throws Exception {
			String subscribers = PebcastPublishUtil.getSubscribers(BTC_API_KEY, type);
			JsonParser parser = new JsonParser();
			JsonObject subs =  (JsonObject)parser.parse(subscribers);
			JsonObject response = new JsonObject();
			System.out.println("Subscribers" + subs.toString());
			if(subs.has("users") && subs.get("users").getAsJsonArray().size() > 0) {
				response.addProperty("id", BTC_API_KEY);
				response.addProperty("sender", "Pebcast BTCPrices Reference Impl");
				JsonArray messages = new JsonArray();
				JsonObject btcRates = getBTCRates();
				
				JsonArray users = subs.get("users").getAsJsonArray();

				for(int i=0;i<users.size();i++) {
					String currencyPreference = users.get(i).getAsJsonObject().get("currency").getAsString();
					String floor = users.get(i).getAsJsonObject().get("floor") != null ? users.get(i).getAsJsonObject().get("floor").getAsString() : null;
					String ceil = users.get(i).getAsJsonObject().get("ceil") != null ? users.get(i).getAsJsonObject().get("ceil").getAsString() : null;
					
					//System.out.println("some junk info");
					String username = users.get(i).getAsJsonObject().get("useralias").getAsString();
					String ratesMessage = formattedCurrentUSDPrice(btcRates, currencyPreference);
					boolean shouldVibrate = checkRange(btcRates, currencyPreference, floor, ceil);
					JsonObject message = new JsonObject();
					message.addProperty("id2", username);
					message.addProperty("text", ratesMessage);
					if(shouldVibrate) {
						message.addProperty("text", ratesMessage + " (breached your threshold!)");
						message.addProperty("vibrate", "y");
					}
					else {
						message.addProperty("text", ratesMessage);
						message.addProperty("vibrate", "n");
					}
					 DateTime dt = new DateTime();
					 dt = dt.plusHours(12);
					 DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
					 String dateStr = fmt.print(dt);
					 message.addProperty("expires", dateStr);
					
					messages.add(message);
				}
				response.add("messages", messages);
				System.out.println("Response: " + response.toString());
				PebcastPublishUtil.postToCast(response);
				log.info("Posted prices for " + users.size() + " subscribers");
			} else {
				log.info("No " + type + " users! Skipped processing!");
			}

	}

	public static String stackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}

}

