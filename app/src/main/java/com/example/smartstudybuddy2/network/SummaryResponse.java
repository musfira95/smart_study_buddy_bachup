package com.example.smartstudybuddy2.network;

import com.google.gson.annotations.SerializedName;

public class SummaryResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("summary")
    public String summary;

    @SerializedName("original_length")
    public int originalLength;

    @SerializedName("summary_length")
    public int summaryLength;

    @SerializedName("compression_ratio")
    public float compressionRatio;

    public SummaryResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSummary() {
        return summary;
    }

    public int getOriginalLength() {
        return originalLength;
    }

    public int getSummaryLength() {
        return summaryLength;
    }

    public float getCompressionRatio() {
        return compressionRatio;
    }
}
