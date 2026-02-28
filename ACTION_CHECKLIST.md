# ✅ ACTION CHECKLIST - Fix FastAPI 422 Error

## 🎯 Your Task

Follow these steps in order to fix the 422 error in your Android app.

---

## STEP 1: Check Your FastAPI Code (5 minutes)

### Action Items:
- [ ] Open your FastAPI server file (e.g., `main.py`, `app.py`)
- [ ] Find the `/transcribe/` endpoint
- [ ] Check the function parameter name

### Look For:
```python
@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):  # ✅ Must be "file"
```

### If You See:
```python
@app.post("/transcribe/")
async def transcribe(audio: UploadFile = File(...)):  # ❌ WRONG!
async def transcribe(upload: UploadFile = File(...)):  # ❌ WRONG!
```

### Action:
- [ ] Change the parameter name to `file`
- [ ] Verify it returns JSON: `{"text": "..."}`

---

## STEP 2: Test FastAPI with curl (5 minutes)

### In your terminal:

```bash
# Make sure FastAPI is running
python main.py

# In a new terminal window:
curl -X POST http://localhost:8000/transcribe/ -F "file=@C:\path\to\test.mp3"

# Expected output:
# {"text":"some transcription here"}
```

### If curl returns:
- ✅ `{"text":"..."}` → Go to STEP 3
- ❌ `422 Unprocessable Entity` → Fix FastAPI parameter name and try again
- ❌ `Connection refused` → FastAPI not running, start it first

---

## STEP 3: Check Android Network Settings (2 minutes)

### For Android Emulator:
- [ ] Verify ApiClient.java has: `http://10.0.2.2:8000/`

### For Physical Android Device:
- [ ] Get your PC IP address:
  ```bash
  ipconfig  # Windows
  ```
- [ ] Find "IPv4 Address:" (e.g., 192.168.1.100)
- [ ] Update ApiClient.java:
  ```java
  private static final String BASE_URL = "http://192.168.1.100:8000/";
  ```

---

## STEP 4: Build & Run Android App (10 minutes)

### In Android Studio:
- [ ] Open the project
- [ ] Click "Build" → "Clean Project"
- [ ] Click "Build" → "Build Bundle(s)/APK(s)" → "Build APK(s)"
- [ ] Wait for build to complete
- [ ] Connect phone or launch emulator
- [ ] Click "Run" → "Run 'app'"

### Check for Errors:
- [ ] No compilation errors
- [ ] App launches without crashing

---

## STEP 5: Test Audio Upload (10 minutes)

### In the Android App:
- [ ] Go to "Upload Audio" screen
- [ ] Click "Upload MP3" and select an audio file
- [ ] Click "Process Audio"
- [ ] Watch for:
  - [ ] ProgressBar appears
  - [ ] Text shows "Uploading audio to server..."
  - [ ] After 10-30 seconds, transcription text appears
  - [ ] "View Summary" button becomes visible

### If It Works:
✅ **DONE! Your integration is working!**

### If It Still Shows Error:
Continue to STEP 6.

---

## STEP 6: Debug with Logcat (10 minutes)

### Open Android Monitor:
- [ ] In Android Studio: View → Tool Windows → Logcat
- [ ] Click the search/filter box

### Search for logs:
- [ ] Type: `ProcessAudioActivity`
- [ ] Click "Process Audio" button
- [ ] Look for these messages:

```
ProcessAudioActivity: Uploading file: /cache/audio_upload.mp3
ProcessAudioActivity: File size: 12345 bytes
NetworkInterceptor: === REQUEST ===
NetworkInterceptor: URL: http://10.0.2.2:8000/transcribe/
NetworkInterceptor: === RESPONSE ===
NetworkInterceptor: Code: 200 (or 422)
NetworkInterceptor: Body: {...}
```

### If You See Code: 200
✅ **Success! Transcription should appear on screen**

### If You See Code: 422
- [ ] Check "Body:" message - it shows what's wrong
- [ ] Most likely: `"field required"` or `"loc":["body","file"]`
- [ ] **Go back to STEP 1 and fix FastAPI parameter name**

### If You See Different Error:
- [ ] Copy the complete error message
- [ ] Share it with your supervisor

---

## STEP 7: Verify Complete Workflow (5 minutes)

Test the complete flow:

- [ ] Click "Upload Audio"
- [ ] Select MP3 file
- [ ] See file details
- [ ] Click "Process Audio"
- [ ] See transcription text
- [ ] Click "View Summary"
- [ ] Verify summary page works
- [ ] Go back and test "Generate Quiz"
- [ ] Verify quiz generation works

---

## ✅ Success Criteria

All of these must be TRUE for the integration to be complete:

- [ ] Android app is running without crashes
- [ ] Process Audio button appears
- [ ] Click Process Audio shows ProgressBar
- [ ] After 10-30 seconds, transcription text appears
- [ ] No "Error: Server returned 422" message
- [ ] Logcat shows "Code: 200" (successful response)
- [ ] Summary button appears and works
- [ ] Quiz generation works after summary

---

## 🆘 If Something Doesn't Work

### 1. Clear Cache and Rebuild
```bash
# In Terminal at project root:
./gradlew clean build
```

### 2. Restart Everything
- [ ] Stop FastAPI server (Ctrl+C)
- [ ] Close Android emulator
- [ ] Restart FastAPI: `python main.py`
- [ ] Restart emulator and app

### 3. Get Help with These Files
Collect and share:
1. **Logcat output:** `adb logcat > logcat.txt`
2. **Your FastAPI endpoint code** (copy/paste)
3. **curl test output:** `curl -X POST http://localhost:8000/transcribe/ -F "file=@test.mp3"`
4. **Your PC IP address**

---

## 🎓 What You Learned

1. **Android** → Uses Retrofit to upload files as multipart
2. **FastAPI** → Parameter name MUST match field name from Android
3. **Network** → 10.0.2.2 for emulator, actual IP for physical device
4. **Testing** → Always test with curl first, then in app
5. **Debugging** → Check logcat for detailed error messages

---

## 📚 Documentation Files Created

- **QUICK_FIX_422.md** → 1-minute quick reference
- **FIX_422_ERROR.md** → Detailed step-by-step guide
- **FASTAPI_422_TROUBLESHOOTING.md** → Complete troubleshooting
- **422_ERROR_FIX_SUMMARY.md** → What was changed and why

Read these in this order if you get stuck!

---

## 🚀 Next Steps After This Works

Once 422 error is fixed:

1. Test with multiple audio files
2. Verify transcription accuracy
3. Test Summary generation
4. Test Quiz generation
5. Test PDF export
6. Check database storage
7. Test with different network speeds
8. Optimize upload speed if needed

---

## ⏱️ Time Estimate

- **Step 1 (FastAPI Check):** 5 min
- **Step 2 (curl Test):** 5 min
- **Step 3 (Network Setup):** 2 min
- **Step 4 (Build & Run):** 10 min
- **Step 5 (First Test):** 10 min
- **Step 6 (Debug if needed):** 10 min

**Total: 30-40 minutes (if no issues)**

If you find the issue quickly (wrong parameter name), it could be just 15 minutes!

---

**GO FIX IT! You've got this! 💪**
