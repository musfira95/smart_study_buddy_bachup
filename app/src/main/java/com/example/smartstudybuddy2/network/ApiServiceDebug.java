package com.example.smartstudybuddy2.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Alternative API Service with multiple method signatures for debugging
 * Use this if the main ApiService doesn't work with your FastAPI server
 */
public interface ApiServiceDebug {

    // Method 1: Standard multipart without part name
    @Multipart
    @POST("transcribe/")
    Call<TranscriptionResponse> uploadAudio(@Part MultipartBody.Part file);

    // Method 2: Multipart without explicit field name (fallback)
    @Multipart
    @POST("transcribe/")
    Call<TranscriptionResponse> uploadAudioAlt(@Part MultipartBody.Part file);

    // Method 3: Direct RequestBody (if server expects raw body)
    @POST("transcribe/")
    Call<TranscriptionResponse> uploadAudioRaw(@Body RequestBody file);
}
