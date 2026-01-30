package com.example.smartstudybuddy2.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    // Replace <LAPTOP-IP> with your actual laptop IP (e.g., 192.168.1.100)
    private static final String BASE_URL = "http://<LAPTOP-IP>:8000/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
