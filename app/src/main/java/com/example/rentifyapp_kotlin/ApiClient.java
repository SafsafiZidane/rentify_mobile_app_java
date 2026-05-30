package com.example.rentifyapp_kotlin;

import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    public static final String BASE_URL = "http://10.0.2.2:5004";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ─── Callback interfaces ───────────────────────────────────────────────────

    public interface CarsCallback {
        void onSuccess(List<Car> cars);
        void onError(String errorMessage);
    }

    public interface CarCallback {
        void onSuccess(Car car);
        void onError(String errorMessage);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    // ─── GET /api/cars ─────────────────────────────────────────────────────────

    public void getCars(Handler mainHandler, CarsCallback callback) {
        withToken(mainHandler, callback, token -> {
            List<Car> cars = fetchCars(token, null);
            mainHandler.post(() -> callback.onSuccess(cars));
        });
    }

    public void getCarsByPurpose(String purpose, Handler mainHandler, CarsCallback callback) {
        withToken(mainHandler, callback, token -> {
            List<Car> cars = fetchCars(token, purpose);
            mainHandler.post(() -> callback.onSuccess(cars));
        });
    }

    // ─── GET /api/cars/:id ─────────────────────────────────────────────────────

    public void getCarById(String carId, Handler mainHandler, CarCallback callback) {
        withToken(mainHandler, callback, token -> {
            Car car = fetchCarById(carId, token);
            mainHandler.post(() -> callback.onSuccess(car));
        });
    }

    // ─── GET /api/my-listings ──────────────────────────────────────────────────

    public void getMyListings(Handler mainHandler, CarsCallback callback) {
        withToken(mainHandler, callback, token -> {
            List<Car> cars = fetchMyListings(token);
            mainHandler.post(() -> callback.onSuccess(cars));
        });
    }

    // ─── POST /api/cars/add ────────────────────────────────────────────────────

    public void addCar(Car car, Handler mainHandler, SimpleCallback callback) {
        withToken(mainHandler, callback, token -> {
            String result = postCar(car, token);
            mainHandler.post(() -> callback.onSuccess(result));
        });
    }

    // ─── PATCH /api/cars/:id ───────────────────────────────────────────────────

    public void updateCar(String carId, Car car, Handler mainHandler, SimpleCallback callback) {
        withToken(mainHandler, callback, token -> {
            String result = patchCar(carId, car, token);
            mainHandler.post(() -> callback.onSuccess(result));
        });
    }

    // ─── DELETE /api/cars/:id ──────────────────────────────────────────────────

    public void deleteCar(String carId, Handler mainHandler, SimpleCallback callback) {
        withToken(mainHandler, callback, token -> {
            String result = deleteCarById(carId, token);
            mainHandler.post(() -> callback.onSuccess(result));
        });
    }

    // ─── POST /api/save-token ──────────────────────────────────────────────────

    public void saveFcmToken(String fcmToken, Handler mainHandler, SimpleCallback callback) {
        withToken(mainHandler, callback, token -> {
            JSONObject body = new JSONObject();
            body.put("fcmToken", fcmToken);
            String result = postJson("/api/save-token", body.toString(), token);
            mainHandler.post(() -> callback.onSuccess(result));
        });
    }

    // ─── Internal HTTP helpers ─────────────────────────────────────────────────

    private List<Car> fetchCars(String token, String purpose) throws IOException {
        String endpoint = "/api/cars" + (purpose != null ? "?purpose=" + purpose : "");
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = openGetConnection(url, token);
        checkResponse(conn);
        return parseCarsJson(readResponse(conn));
    }

    private Car fetchCarById(String carId, String token) throws Exception {
        URL url = new URL(BASE_URL + "/api/cars/" + carId);
        HttpURLConnection conn = openGetConnection(url, token);
        checkResponse(conn);
        return parseCarJson(new JSONObject(readResponse(conn)));
    }

    private List<Car> fetchMyListings(String token) throws IOException {
        URL url = new URL(BASE_URL + "/api/my-listings");
        HttpURLConnection conn = openGetConnection(url, token);
        checkResponse(conn);
        return parseCarsJson(readResponse(conn));
    }

    private String postCar(Car car, String token) throws Exception {
        JSONObject body = new JSONObject();
        body.put("make", car.getMake());
        body.put("model", car.getModel());
        body.put("year", car.getYear());
        body.put("price", car.getPrice());
        body.put("purpose", car.getPurpose());
        body.put("description", car.getDescription());
        body.put("imageUrl", car.getImageUrl());
        body.put("location", car.getLocation());
        return postJson("/api/cars/add", body.toString(), token);
    }

    private String patchCar(String carId, Car car, String token) throws Exception {
        JSONObject body = new JSONObject();
        body.put("make", car.getMake());
        body.put("model", car.getModel());
        body.put("year", car.getYear());
        body.put("price", car.getPrice());
        body.put("purpose", car.getPurpose());
        body.put("description", car.getDescription());
        body.put("imageUrl", car.getImageUrl());
        body.put("location", car.getLocation());
        return patchJson("/api/cars/" + carId, body.toString(), token);
    }

    private String deleteCarById(String carId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/api/cars/" + carId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        checkResponse(conn);
        return readResponse(conn);
    }

    private String postJson(String path, String jsonBody, String token) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }
        checkResponse(conn);
        return readResponse(conn);
    }

    private String patchJson(String path, String jsonBody, String token) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }
        checkResponse(conn);
        return readResponse(conn);
    }

    private HttpURLConnection openGetConnection(URL url, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }

    private void checkResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) throw new IOException("Server error: " + code);
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    // ─── JSON parsers ──────────────────────────────────────────────────────────

    private List<Car> parseCarsJson(String json) {
        List<Car> cars = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++)
                cars.add(parseCarJson(arr.getJSONObject(i)));
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
                    obj.optString("ownerEmail"),
                    obj.optString("ownerUid")
            );
        } catch (Exception e) {
            return new Car();
        }
    }

    // ─── Token helper ──────────────────────────────────────────────────────────

    @FunctionalInterface
    interface TokenTask {
        void run(String token) throws Exception;
    }

    private <C> void withToken(Handler mainHandler, C callback, TokenTask task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            postError(mainHandler, callback, "Not logged in");
            return;
        }
        user.getIdToken(true)
                .addOnSuccessListener(result -> executor.execute(() -> {
                    try {
                        task.run(result.getToken());
                    } catch (Exception e) {
                        postError(mainHandler, callback, e.getMessage());
                    }
                }))
                .addOnFailureListener(e -> postError(mainHandler, callback, "Token error: " + e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    private <C> void postError(Handler mainHandler, C callback, String msg) {
        mainHandler.post(() -> {
            if (callback instanceof CarsCallback)   ((CarsCallback)   callback).onError(msg);
            else if (callback instanceof CarCallback)    ((CarCallback)    callback).onError(msg);
            else if (callback instanceof SimpleCallback) ((SimpleCallback) callback).onError(msg);
        });
    }
}