package com.example.smartstudybuddy2.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API Client for LOCAL FastAPI backend (summary, quiz, flashcards)
 * Uses local IP address (not ngrok)
 */
public class LocalApiClient {

    private static Retrofit retrofit;
    // ✅ LOCAL BACKEND - Change this IP to your local FastAPI server IP
    public static final String BASE_URL = "http://10.152.62.53:8000/";

    public static Retrofit getClient() {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .addInterceptor(new NetworkLoggingInterceptor())
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
