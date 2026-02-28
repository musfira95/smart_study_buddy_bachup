package com.example.smartstudybuddy2.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.46.38.53:8000/";  // ✅ PC IP address on network
    public static Retrofit getClient() {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)  // INCREASED from 60
                .writeTimeout(600, TimeUnit.SECONDS)    // INCREASED from 300
                .readTimeout(600, TimeUnit.SECONDS)     // INCREASED from 300
                .addInterceptor(new NetworkLoggingInterceptor())  // Add logging for debugging
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
