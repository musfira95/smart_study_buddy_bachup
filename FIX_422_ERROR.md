# ✅ STEP-BY-STEP GUIDE: FIX FastAPI 422 ERROR

## 📋 What You Did
- Android app uploads MP3 to FastAPI server at `http://10.0.2.2:8000/transcribe/`
- Server returns **Error 422** (Unprocessable Entity)

---

## 🔍 Root Causes (In Order of Probability)

### 1️⃣ **Most Likely: FastAPI Parameter Name Mismatch**

Your Android code sends:
```java
MultipartBody.Part body = MultipartBody.Part.createFormData(
    "file",  // ⬅️ FIELD NAME IS "file"
    audioFile.getName(),
    requestFile
);
```

Your FastAPI server MUST have:
```python
@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):  # ✅ Parameter MUST be "file"
    ...
```

**❌ WRONG:**
```python
@app.post("/transcribe/")
async def transcribe(audio: UploadFile = File(...)):  # ❌ Wrong name!
```

---

### 2️⃣ **Second Most Likely: Response Format Mismatch**

Android expects JSON response:
```java
public class TranscriptionResponse {
    public String text;  // ⬅️ Field must be named "text"
}
```

FastAPI MUST return:
```python
return {"text": "transcription result here"}  # ✅ MUST have "text" field
```

**❌ WRONG:**
```python
return {"transcription": "result"}  # ❌ Wrong field name!
return "just a string"              # ❌ Not JSON!
```

---

### 3️⃣ **Third: Audio File Content Type**

Android sends as:
```java
MediaType.parse("audio/mpeg")  // MP3 format
```

Make sure your FastAPI server can handle MP3:
```python
@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):
    # ✅ No content-type validation needed, FastAPI handles it
    contents = await file.read()
    # Process with Whisper
    ...
```

---

## 🛠️ STEP 1: Check Your FastAPI Code

### Minimal Working Example

Create a file `test_api.py`:

```python
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import os
import subprocess

app = FastAPI()

@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):
    """
    ✅ Correct FastAPI endpoint
    - Parameter name: file (matches Android: createFormData("file", ...))
    - Returns: {"text": "transcription"}
    """
    # Save uploaded file temporarily
    temp_file = f"/tmp/{file.filename}"
    with open(temp_file, "wb") as f:
        f.write(await file.read())
    
    # Process with Whisper
    result = subprocess.run(
        ["whisper", temp_file, "--output_format", "json"],
        capture_output=True,
        text=True
    )
    
    # Parse result and return
    import json
    whisper_output = json.loads(result.stdout)
    
    # Clean up
    os.remove(temp_file)
    
    # Return in format Android expects
    return {"text": whisper_output["text"]}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

---

## 🛠️ STEP 2: Test FastAPI Server Directly

Before testing in Android, test with curl:

```bash
# Test 1: Check if server is running
curl http://localhost:8000/docs

# Test 2: Upload a test MP3 file
curl -X POST http://localhost:8000/transcribe/ \
  -F "file=@/path/to/audio.mp3"

# Expected response:
# {"text":"transcribed text here"}
```

If curl returns 422:
- Your server code is incorrect (see examples above)

If curl works but Android doesn't:
- Network connectivity issue (see Step 3)

---

## 🛠️ STEP 3: Fix Android Network Setup

### For Android Emulator:
Use `10.0.2.2` (already set in ApiClient.java):
```java
private static final String BASE_URL = "http://10.0.2.2:8000/";
```

### For Physical Android Device:
Replace with your PC's actual IP:
```bash
# Find your PC's IP address
ipconfig  # Windows
ifconfig  # Mac/Linux
```

Then update ApiClient.java:
```java
private static final String BASE_URL = "http://192.168.1.100:8000/";  // Replace with your IP
```

---

## 🛠️ STEP 4: Enable Debug Logging

The network logging interceptor is already added to ApiClient.java.

Run logcat to see debug messages:
```bash
adb logcat | grep -E "ProcessAudioActivity|NetworkInterceptor"
```

Look for these messages:
```
NetworkInterceptor: === REQUEST ===
NetworkInterceptor: URL: http://10.0.2.2:8000/transcribe/
NetworkInterceptor: Method: POST
NetworkInterceptor: Body size: 12345 bytes

NetworkInterceptor: === RESPONSE ===
NetworkInterceptor: Code: 422
NetworkInterceptor: Body: {"detail":[{"loc":["body","file"],"msg":"field required",...}]}
```

---

## 🔥 COMMON ERROR MESSAGES & FIXES

### Error: `"field required"` or `"loc":["body","file"]`
**Cause:** Field name mismatch

**Fix:** Ensure FastAPI has `file: UploadFile = File(...)`

### Error: `"value is not a valid URL"`
**Cause:** Wrong base URL

**Fix:** Update ApiClient.java BASE_URL to match your server IP

### Error: `"Connection refused"`
**Cause:** Server not running

**Fix:** Start FastAPI server:
```bash
python test_api.py
```

### Error: `Cannot parse response body`
**Cause:** FastAPI returning wrong format

**Fix:** Make sure response is:
```python
return {"text": "..."} # Not return "text" or {"transcription": "..."}
```

---

## ✅ FINAL CHECKLIST

- [ ] FastAPI endpoint exists at `/transcribe/`
- [ ] Endpoint parameter is named `file` (not `audio`, `upload`, etc.)
- [ ] Endpoint returns JSON: `{"text": "..."}`
- [ ] Server is running: `python test_api.py` or `uvicorn main:app --reload`
- [ ] curl test succeeds: `curl -X POST http://localhost:8000/transcribe/ -F "file=@test.mp3"`
- [ ] Android BASE_URL is correct (10.0.2.2 for emulator, actual IP for physical device)
- [ ] INTERNET permission is in AndroidManifest.xml
- [ ] No firewall blocking port 8000

---

## 🆘 IF STILL NOT WORKING

### Option 1: Try Alternative Request Format

Modify ApiService.java to try without explicit field name:

```java
@Multipart
@POST("transcribe/")
Call<TranscriptionResponse> uploadAudio(@Part MultipartBody.Part file);
```

### Option 2: Enable CORS in FastAPI

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

### Option 3: Add FastAPI Request Logging

```python
import logging
logging.basicConfig(level=logging.DEBUG)

@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):
    print(f"Received: filename={file.filename}, content_type={file.content_type}")
    print(f"Headers: {file.headers}")
    contents = await file.read()
    print(f"File size: {len(contents)} bytes")
    ...
```

---

## 📞 SHARE THESE LOGS FOR HELP

1. **Full logcat output:**
   ```bash
   adb logcat | grep ProcessAudioActivity > logcat.txt
   ```

2. **FastAPI server output (console logs)**

3. **curl test result:**
   ```bash
   curl -v -X POST http://localhost:8000/transcribe/ -F "file=@test.mp3" 2>&1 > curl_output.txt
   ```

Share these 3 things and the issue will be fixed!

---

## 🎯 Summary

**422 Error = The server doesn't like your request format**

Most likely fixes in order:
1. Change FastAPI parameter name to `file`
2. Change FastAPI response to `{"text": "..."}`
3. Test with curl first
4. Check Android network URL and permissions
5. Enable logging to see exact error

Follow the steps above and your integration will work! 🚀
