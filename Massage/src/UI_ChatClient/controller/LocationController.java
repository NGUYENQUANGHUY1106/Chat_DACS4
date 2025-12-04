package UI_ChatClient.controller;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

/**
 * Controller xử lý vị trí và thời tiết
 */
public class LocationController {
    
    private static final String WEATHER_API_KEY = "e7fe7321fd09c00fdf69fafc3126c0d6";
    
    public interface LocationCallback {
        void onLocationReceived(String lat, String lon, String weather, String mapLink);
        void onLocationError(String message);
    }
    
    public void getLocationAndWeather(LocationCallback callback) {
        new Thread(() -> {
            try {
                String[] coords = getLocationFromIP();
                if (coords == null) {
                    callback.onLocationError("Không thể lấy vị trí.");
                    return;
                }
                String lat = coords[0];
                String lon = coords[1];
                String weatherInfo = getWeather(lat, lon);
                String mapLink = String.format("https://www.google.com/maps?q=%s,%s", lat, lon);
                
                callback.onLocationReceived(lat, lon, weatherInfo, mapLink);
            } catch (Exception ex) {
                callback.onLocationError("Lỗi khi lấy vị trí: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }
    
    private String[] getLocationFromIP() throws IOException {
        URL url = new URL("http://ip-api.com/json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) return null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String json = reader.lines().reduce("", String::concat);
            String lat = parseJsonValue(json, "lat");
            String lon = parseJsonValue(json, "lon");
            if (lat != null && lon != null)
                return new String[] { lat, lon };
        } finally {
            conn.disconnect();
        }
        return null;
    }
    
    private String getWeather(String lat, String lon) throws IOException {
        String urlString = String.format(
            "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric&lang=vi",
            lat, lon, WEATHER_API_KEY
        );
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) return "Không lấy được thời tiết";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String json = reader.lines().reduce("", String::concat);
            String description = parseJsonValue(json, "description");
            String temp = parseJsonValue(json, "temp");
            if (description != null && temp != null) {
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
                return String.format("%s, %s°C", description, temp);
            }
        } finally {
            conn.disconnect();
        }
        return "Không rõ";
    }
    
    private String parseJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*(\"[^\"]*\"|[^,}]*)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            if (value.startsWith("\"") && value.endsWith("\""))
                return value.substring(1, value.length() - 1);
            return value;
        }
        return null;
    }
}
