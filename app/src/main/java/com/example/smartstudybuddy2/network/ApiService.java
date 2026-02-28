package com.example.smartstudybuddy2.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    // Upload audio file using MultipartBody.Part with correct field name "audio_file"
    // This properly sends the file as UploadFile to the FastAPI endpoint
    @Multipart
    @POST("transcribe/")
    Call<TranscriptionResponse> uploadAudio(@Part MultipartBody.Part audio_file);

    // Generate summary from transcribed text (JSON body)
    @POST("summarize/")
    Call<SummaryResponse> generateSummary(@Body SummaryRequest request);

    // Generate quiz from transcribed text (JSON body)
    @POST("quiz/")
    Call<QuizResponse> generateQuiz(@Body QuizRequest request);
}

