package com.example.rentifyapp_kotlin;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    // Replace with your actual backend URL (use 10.0.2.2 for Android emulator → localhost)
    public static final String BASE_URL = "http://10.0.2.2:5000";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface CarsCallback {
        void onSuccess(List<Car> cars);
        void onError(String errorMessage);
    }

    public interface CarCallback {
        void onSuccess(Car car);
        void onError(String errorMessage);
    }

    /**
     * Fetches the Firebase ID token for the current user, then calls /api/cars.
     */
    public void getCars(android.os.Handler mainHandler, CarsCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("Not logged in");
            return;
        }

        user.getIdToken(true).addOnSuccessListener(result -> {
            String token = result.getToken();
            executor.execute(() -> {
                try {
                    List<Car> cars = fetchCars(token);
                    mainHandler.post(() -> callback.onSuccess(cars));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            });
        }).addOnFailureListener(e -> callback.onError("Failed to get token: " + e.getMessage()));
    }

    /**
     * Fetches a single car by ID from /api/cars/{id}.
     */
    public void getCarById(String carId, android.os.Handler mainHandler, CarCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("Not logged in");
            return;
        }

        user.getIdToken(true).addOnSuccessListener(result -> {
            String token = result.getToken();
            executor.execute(() -> {
                try {
                    Car car = fetchCarById(carId, token);
                    mainHandler.post(() -> callback.onSuccess(car));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            });
        }).addOnFailureListener(e -> callback.onError("Failed to get token: " + e.getMessage()));
    }

    private List<Car> fetchCars(String token) throws IOException {
        URL url = new URL(BASE_URL + "/api/cars");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Server error: " + responseCode);
        }

        String json = readResponse(conn);
        return parseCarsJson(json);
    }

    private Car fetchCarById(String carId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/api/cars/" + carId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Server error: " + responseCode);
        }

        String json = readResponse(conn);
        try {
            return parseCarJson(new JSONObject(json));
        } catch (org.json.JSONException e) {
            throw new IOException("Failed to parse car JSON: " + e.getMessage());
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private List<Car> parseCarsJson(String json) {
        List<Car> cars = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                cars.add(parseCarJson(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Parse error: " + e.getMessage());
        }
        return cars;
    }

    private Car parseCarJson(JSONObject obj) {
        try {
            return new Car(
                    obj.optString("id"),
                    obj.optString("make"),
                    obj.optString("model"),
                    obj.optString("year"),
                    obj.optDouble("price", 0),
                    obj.optString("purpose"),
                    obj.optString("description"),
                    obj.optString("imageUrl"),
                    obj.optString("location"),
                    obj.optString("ownerEmail")
            );
        } catch (Exception e) {
            return new Car();
        }
    }
}