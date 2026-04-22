package com.example.smartstudybuddy2.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://devotee-portly-flock.ngrok-free.dev/";
    
    public static Retrofit getClient() {

        // ✅ Interceptor to add ngrok-skip-browser-warning header
        Interceptor ngrokInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request requestWithHeader = originalRequest.newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build();
            return chain.proceed(requestWithHeader);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)  // INCREASED from 60
                .writeTimeout(600, TimeUnit.SECONDS)    // INCREASED from 300
                .readTimeout(600, TimeUnit.SECONDS)     // INCREASED from 300
                .addInterceptor(new NetworkLoggingInterceptor())  // Add logging for debugging
                .addInterceptor(ngrokInterceptor)  // ✅ Add ngrok header to all requests
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
