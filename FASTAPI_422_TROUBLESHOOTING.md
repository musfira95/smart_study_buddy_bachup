# FastAPI 422 Error Troubleshooting Guide

## Problem
Android app receives "Error: Server returned 422" when uploading audio to FastAPI /transcribe endpoint.

HTTP 422 = Unprocessable Entity - The server understands the request format but rejects the content.

---

## Most Common Causes & Fixes

### ✅ FIX 1: Check FastAPI Server Endpoint Definition

Your FastAPI server should have an endpoint like this:

```python
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse

app = FastAPI()

@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):
    contents = await file.read()
    # Process with Whisper model
    transcription = model.transcribe(contents)
    return {"text": transcription["text"]}
```

**Key Points:**
- Parameter name MUST be `file` (matches Android: `createFormData("file", ...)`)
- Use `UploadFile` not `bytes`
- Return JSON with `{"text": "..."}` (matches Android's `TranscriptionResponse`)

---

### ✅ FIX 2: Verify Android Multipart Request

The Android code creates the request like this:

```java
MultipartBody.Part body = MultipartBody.Part.createFormData(
    "file",                    // Field name
    audioFile.getName(),       // Filename
    requestFile                // RequestBody
);
```

This generates a request like:
```
POST /transcribe/ HTTP/1.1
Content-Type: multipart/form-data; boundary=----...

------...
Content-Disposition: form-data; name="file"; filename="audio.mp3"
Content-Type: audio/mpeg

[binary audio data]
------...--
```

**Verify this matches your FastAPI expectations!**

---

### ✅ FIX 3: Check Audio File Format

The Android code sends:
```java
MediaType.parse("audio/mpeg")  // MP3 format
```

**Verify:**
1. Selected file IS an MP3 (not WAV, M4A, etc.)
2. File size > 0 bytes
3. File is valid MP3 format (not corrupted)

---

### ✅ FIX 4: Update ApiService Interface

If still getting 422, ensure your ApiService.java has:

```java
@Multipart
@POST("transcribe/")
Call<TranscriptionResponse> uploadAudio(@Part("file") MultipartBody.Part file);
```

Notice `@Part("file")` - the explicit field name is important!

---

### ✅ FIX 5: Verify Network Connectivity

1. **Check if server is running:**
   ```bash
   curl -X POST http://<YOUR_PC_IP>:8000/transcribe/ -F "file=@test.mp3"
   ```

2. **In Android emulator, use 10.0.2.2 for localhost:**
   - ApiClient.java uses: `http://10.0.2.2:8000/`
   - On physical device, replace with your actual PC IP

3. **Check server logs for incoming requests**

---

### ✅ FIX 6: Common FastAPI Issues

#### Issue: FastAPI expects `file` but server code uses different name
```python
# ❌ WRONG
@app.post("/transcribe/")
async def transcribe(audio: UploadFile = File(...)):  # Wrong parameter name!
    ...

# ✅ CORRECT
@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):  # Parameter must be "file"
    ...
```

#### Issue: FastAPI doesn't return proper JSON
```python
# ❌ WRONG - Returns plain string
return "transcribed text here"

# ✅ CORRECT - Returns JSON
return {"text": "transcribed text here"}
```

---

## Debug Steps

### Step 1: Enable Logging
The Android code logs to Logcat. Run this command:
```bash
adb logcat | grep ProcessAudioActivity
```

### Step 2: Check Server Response
In the error callback, the app logs:
```
Server error response: [error details]
```

### Step 3: Test with curl
Before testing in Android, test your FastAPI server directly:
```bash
curl -X POST http://localhost:8000/transcribe/ \
  -F "file=@/path/to/audio.mp3"
```

Expected response:
```json
{"text": "transcribed text here"}
```

---

## If Still Getting 422

### Option A: Enable CORS (if testing from different domains)
```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

### Option B: Add Request Validation
```python
import logging
logging.basicConfig(level=logging.DEBUG)

@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):
    print(f"Received file: {file.filename}")
    print(f"Content type: {file.content_type}")
    print(f"File size: {len(await file.read())} bytes")
    # ... rest of code
```

### Option C: Accept Both GET and POST
```python
@app.api_route("/transcribe/", methods=["POST"])
async def transcribe(file: UploadFile = File(...)):
    ...
```

---

## Android Code Changes (If Needed)

If you want to try alternative request formats, modify ProcessAudioActivity:

### Alternative: Send as raw file body instead of multipart
```java
RequestBody requestFile = RequestBody.create(
    MediaType.parse("audio/mpeg"),
    audioFile
);

ApiService apiService = ApiClient.getClient().create(ApiService.class);
Call<TranscriptionResponse> call = apiService.uploadAudioRaw(requestFile);
```

---

## Summary Checklist

- [ ] FastAPI endpoint parameter name is `file`
- [ ] FastAPI returns JSON: `{"text": "..."}`
- [ ] Android audio file is valid MP3
- [ ] Server is running at http://10.0.2.2:8000 (or correct IP)
- [ ] Logcat shows complete error message
- [ ] curl test works from command line
- [ ] ApiService has `@Part("file")` annotation

---

## Get More Help

Share the following from Logcat:
1. Complete error message from ProcessAudioActivity
2. Server logs showing what request was received
3. Output of curl test command

This will help identify the exact mismatch!
