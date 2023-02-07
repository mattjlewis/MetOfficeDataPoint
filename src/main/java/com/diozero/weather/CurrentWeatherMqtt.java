package com.diozero.weather;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.diozero.location.GeographicLocation;
import com.diozero.weather.openweather.OpenWeather;

public class CurrentWeatherMqtt {
	public static void main(String[] args) throws MqttException, IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println("Usage: " + CurrentWeatherMqtt.class.getName()
					+ " <city-name,[us-state-code],iso3166-country-code> <api-key>");
			System.exit(1);
		}

		String[] location_parts = args[0].split(",");
		if (location_parts.length != 3) {
			System.out.println(
					"Location must be of the format '<city-name,[us-state-code],iso3166-country-code>', e.g. London,,GB");
			System.exit(1);
		}

		final OpenWeather owm = new OpenWeather(args[1]);

		List<GeographicLocation> locations = owm.getLocation(location_parts[0], location_parts[1], location_parts[2]);
		if (locations == null || locations.size() == 0) {
			System.out.println("Unable to resolve location '" + String.join(",", location_parts) + "'");
			System.exit(1);
			return; // To prevent potential null pointer warning, sigh
		}

		final GeographicLocation location = locations.get(0);
		final String topic_root = "home/"
				+ String.join("-", location.getCityName(), location.getCountryCode() + "/sensor/");
		final String mqtt_server_uri = "tcp://cm4.home:1883";
		final String mqtt_client_id = "current-weather-reporter";
		final int freq_mins = 10;

		try (MqttClientPersistence persistence = new MemoryPersistence();
				MqttClient mqtt_client = new MqttClient(mqtt_server_uri, mqtt_client_id, persistence)) {
			MqttConnectOptions conn_opts = new MqttConnectOptions();
			conn_opts.setCleanSession(true);

			mqtt_client.connect(conn_opts);

			while (true) {
				Report weather = owm.getCurrentWeather(location);
				System.out.println("Got weather report: " + weather);

				MqttMessage message = new MqttMessage(Double.toString(weather.getTemperature()).getBytes());
				String topic = topic_root + "temperature/state";
				mqtt_client.publish(topic, message);
				System.out.format("Published temperature message to '%s' with value %.2f%n", topic,
						Double.valueOf(weather.getTemperature()));

				message = new MqttMessage(Integer.toString(weather.getPressure()).getBytes());
				topic = topic_root + "pressure/state";
				mqtt_client.publish(topic, message);
				System.out.format("Published pressure message to '%s' with value %,d%n", topic,
						Integer.valueOf(weather.getPressure()));

				message = new MqttMessage(Integer.toString(weather.getRelativeHumidity()).getBytes());
				topic = topic_root + "humidity/state";
				mqtt_client.publish(topic, message);
				System.out.format("Published relative humidty message to '%s' with value %d%n", topic,
						Integer.valueOf(weather.getRelativeHumidity()));

				// Fetch and publish every 10 mins
				Thread.sleep(TimeUnit.MINUTES.toMillis(freq_mins));
			}
		}
	}
}
