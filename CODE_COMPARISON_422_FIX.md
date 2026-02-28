# 🔴 FastAPI 422 Error - Side-by-Side Code Comparison

## The Core Issue

Your Android app sends a multipart request with field name `"file"`.  
Your FastAPI endpoint must have a parameter named `file` to receive it.

---

## ❌ WRONG CODE (Causes 422 Error)

### FastAPI:
```python
@app.post("/transcribe/")
async def transcribe(audio: UploadFile = File(...)):  # ❌ Parameter is "audio"
    contents = await audio.read()
    # Process...
    return "transcription"  # ❌ Returns string, not JSON
```

### What Happens:
1. Android sends: `field name = "file"`
2. FastAPI expects: `parameter name = "audio"`
3. FastAPI says: "I don't have a parameter named 'file'!"
4. **Result: 422 Error** ❌

---

## ✅ CORRECT CODE (Works)

### FastAPI:
```python
@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):  # ✅ Parameter is "file"
    contents = await file.read()
    # Process...
    return {"text": "transcription"}  # ✅ Returns JSON with "text" field
```

### What Happens:
1. Android sends: `field name = "file"`
2. FastAPI expects: `parameter name = "file"`
3. FastAPI says: "Perfect match!"
4. **Result: 200 OK, transcription received** ✅

---

## Complete Working Example

### FastAPI Server (`main.py`):

```python
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import whisper
import os
import tempfile

app = FastAPI()

@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):
    """
    Receive audio file and return transcription
    
    ✅ KEY POINTS:
    - Parameter name MUST be "file" (not "audio", "upload", etc.)
    - Must accept UploadFile
    - Must return JSON with "text" field
    """
    
    # Validate file
    if not file:
        return {"error": "No file provided"}, 400
    
    # Save uploaded file temporarily
    with tempfile.NamedTemporaryFile(delete=False, suffix=".mp3") as temp:
        contents = await file.read()
        temp.write(contents)
        temp_path = temp.name
    
    try:
        # Load Whisper model (base, small, medium, large-v3)
        model = whisper.load_model("base")
        
        # Transcribe
        result = model.transcribe(temp_path)
        
        # Return in format Android expects
        return {"text": result["text"]}
        
    finally:
        # Clean up
        os.remove(temp_path)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

**Run it:**
```bash
python main.py
# Server starts at http://localhost:8000
```

---

### Android Code (`ProcessAudioActivity.java`):

```java
private void uploadAudioToServer() {
    // ... validation code ...
    
    try {
        File audioFile = convertUriToFile(audioUri);
        
        // ✅ Create multipart request
        RequestBody requestFile = RequestBody.create(
            MediaType.parse("audio/mpeg"),
            audioFile
        );
        
        // ✅ Field name MUST be "file" - matches FastAPI parameter name
        MultipartBody.Part body = MultipartBody.Part.createFormData(
            "file",              // ⬅️ This must match FastAPI parameter: file: UploadFile
            audioFile.getName(), // Filename for server
            requestFile
        );
        
        // ✅ Upload to server
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<TranscriptionResponse> call = apiService.uploadAudio(body);
        
        call.enqueue(new Callback<TranscriptionResponse>() {
            @Override
            public void onResponse(Call<TranscriptionResponse> call, 
                    Response<TranscriptionResponse> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Parse response JSON: {"text": "..."}
                    String transcription = response.body().getText();
                    transcriptionText.setText(transcription);
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                } else {
                    // Show server error
                    transcriptionText.setText("Error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<TranscriptionResponse> call, Throwable t) {
                transcriptionText.setText("Connection error: " + t.getMessage());
            }
        });
        
    } catch (Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
```

---

### ApiService.java:

```java
public interface ApiService {
    @Multipart
    @POST("transcribe/")
    Call<TranscriptionResponse> uploadAudio(
        @Part("file") MultipartBody.Part file  // ✅ Explicit field name
    );
}
```

---

### TranscriptionResponse.java:

```java
public class TranscriptionResponse {
    public String text;  // ✅ Must match FastAPI JSON field name
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
```

---

## The Data Flow

### Request (Android → FastAPI):

```
POST /transcribe/ HTTP/1.1
Host: 10.0.2.2:8000
Content-Type: multipart/form-data; boundary=----

------boundary
Content-Disposition: form-data; name="file"; filename="audio.mp3"
Content-Type: audio/mpeg

[binary MP3 audio data]
------boundary--
```

### Response (FastAPI → Android):

```json
{
  "text": "this is the transcribed audio content"
}
```

---

## Common Mistakes & Fixes

### ❌ Mistake 1: Parameter Name
```python
async def transcribe(audio: UploadFile):  # ❌ WRONG - should be "file"
async def transcribe(upload: UploadFile):  # ❌ WRONG - should be "file"
async def transcribe(file: UploadFile):    # ✅ CORRECT
```

### ❌ Mistake 2: Response Format
```python
return "transcription"                    # ❌ WRONG - not JSON
return {"transcription": "..."}           # ❌ WRONG - should be "text"
return {"text": "transcription"}          # ✅ CORRECT
```

### ❌ Mistake 3: Multipart Field Name
```java
.createFormData("audio", ...)   // ❌ WRONG - should be "file"
.createFormData("upload", ...)  // ❌ WRONG - should be "file"
.createFormData("file", ...)    // ✅ CORRECT
```

### ❌ Mistake 4: Missing @Part Annotation
```java
Call<TranscriptionResponse> uploadAudio(MultipartBody.Part file);  // ❌ Missing annotation

Call<TranscriptionResponse> uploadAudio(
    @Part MultipartBody.Part file  // ⚠️ Works but less explicit
);

Call<TranscriptionResponse> uploadAudio(
    @Part("file") MultipartBody.Part file  // ✅ BEST - explicit field name
);
```

---

## Quick Test: curl vs Android

### Test 1: FastAPI Running?
```bash
curl http://localhost:8000/docs
# Should show Swagger UI
```

### Test 2: Upload Test File
```bash
curl -X POST http://localhost:8000/transcribe/ \
  -F "file=@C:\path\to\audio.mp3"

# Success (200):
# {"text":"transcribed content here"}

# Failure (422):
# {"detail":[{"loc":["body","file"],"msg":"field required",...}]}
# ⬆️ This means parameter name is wrong!
```

### Test 3: Wrong Parameter Name
```bash
# If FastAPI has: async def transcribe(audio: UploadFile)
curl -X POST http://localhost:8000/transcribe/ \
  -F "audio=@audio.mp3"
# Returns 200 ✅

# But Android sends:
curl -X POST http://localhost:8000/transcribe/ \
  -F "file=@audio.mp3"
# Returns 422 ❌ - Field "file" not expected!
```

---

## Step-by-Step Fix

1. **Open your FastAPI file**
   ```
   Find: async def transcribe(???: UploadFile)
   Change ??? to: file
   ```

2. **Restart FastAPI**
   ```bash
   Ctrl+C
   python main.py
   ```

3. **Test with curl**
   ```bash
   curl -X POST http://localhost:8000/transcribe/ -F "file=@test.mp3"
   # Should return: {"text":"..."}
   ```

4. **Run Android app**
   - Click Process Audio
   - Should see transcription in 10-30 seconds

---

## Debugging Output Examples

### ✅ Working Case:
```
NetworkInterceptor: === REQUEST ===
NetworkInterceptor: URL: http://10.0.2.2:8000/transcribe/
NetworkInterceptor: Method: POST
NetworkInterceptor: Body size: 245600 bytes

NetworkInterceptor: === RESPONSE ===
NetworkInterceptor: Code: 200
NetworkInterceptor: Body: {"text":"this is the transcribed audio"}

ProcessAudioActivity: Transcription received: this is the transcribed audio
```

### ❌ 422 Error Case:
```
NetworkInterceptor: === REQUEST ===
NetworkInterceptor: URL: http://10.0.2.2:8000/transcribe/
NetworkInterceptor: Method: POST
NetworkInterceptor: Body size: 245600 bytes

NetworkInterceptor: === RESPONSE ===
NetworkInterceptor: Code: 422
NetworkInterceptor: Body: {"detail":[{"loc":["body","file"],"msg":"field required",...}]}

ProcessAudioActivity: Server error response: {"detail":[...]}
```

This error message tells you: **"field 'file' is required but not found"**  
= Your FastAPI parameter is not named "file"

---

## Summary

| Component | Must Be | Why |
|-----------|---------|-----|
| FastAPI parameter name | `file` | Matches Android field name |
| FastAPI return type | `{"text": "..."}` | Matches TranscriptionResponse |
| Android field name | `"file"` | Must match FastAPI parameter |
| Android response class | `TranscriptionResponse` | Has String text field |
| ApiService @Part | `@Part("file")` | Explicit field name mapping |

---

**Copy this file and run curl test first. It will tell you exactly what's wrong!**

```bash
curl -X POST http://localhost:8000/transcribe/ -F "file=@audio.mp3"
```

- Returns JSON with "text" field? ✅ FastAPI is correct, check Android network
- Returns 422 error? ❌ FastAPI parameter name is wrong, fix it

Good luck! 🚀
