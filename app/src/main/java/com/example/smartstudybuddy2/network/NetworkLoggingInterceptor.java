package com.example.smartstudybuddy2.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import android.util.Log;
import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * Network Interceptor for debugging HTTP requests and responses
 * Add this to OkHttpClient to see all network activity
 */
public class NetworkLoggingInterceptor implements Interceptor {
    private static final String TAG = "NetworkInterceptor";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        // Log request details
        Log.d(TAG, "=== REQUEST ===");
        Log.d(TAG, "URL: " + request.url());
        Log.d(TAG, "Method: " + request.method());
        Log.d(TAG, "Headers: " + request.headers());

        // For multipart requests, we can't easily log body
        if (request.body() != null) {
            Log.d(TAG, "Body Content-Type: " + request.body().contentType());
            Log.d(TAG, "Body size: " + request.body().contentLength() + " bytes");
        }

        long startTime = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            Log.e(TAG, "Request failed: " + e.getMessage(), e);
            throw e;
        }

        long duration = (System.nanoTime() - startTime) / 1000000; // Convert to ms

        // Log response details
        Log.d(TAG, "=== RESPONSE ===");
        Log.d(TAG, "Code: " + response.code());
        Log.d(TAG, "Message: " + response.message());
        Log.d(TAG, "Headers: " + response.headers());
        Log.d(TAG, "Duration: " + duration + "ms");

        // Log response body (carefully, as it can be large)
        if (response.body() != null) {
            try {
                String responseBody = response.body().string();
                Log.d(TAG, "Body: " + responseBody);

                // Return new response with same body (since we consumed it)
                return response.newBuilder()
                        .body(ResponseBody.create(responseBody, response.body().contentType()))
                        .build();
            } catch (Exception e) {
                Log.e(TAG, "Error reading response body", e);
            }
        }

        return response;
    }
}
