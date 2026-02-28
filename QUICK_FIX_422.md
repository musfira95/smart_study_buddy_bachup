# 🔴 FASTAPI 422 ERROR - QUICK FIX

## The Problem
Your Android app sends audio to FastAPI but gets **422 Unprocessable Entity**

---

## 99% Solution: Check Your FastAPI Endpoint

### ❌ LIKELY WRONG CODE:
```python
@app.post("/transcribe/")
async def transcribe(audio: UploadFile = File(...)):  # ❌ Parameter name is "audio"
    ...
```

### ✅ CORRECT CODE:
```python
@app.post("/transcribe/")
async def transcribe(file: UploadFile = File(...)):  # ✅ Parameter name must be "file"
    ...
```

---

## Why?
Your Android code sends:
```java
MultipartBody.Part.createFormData("file", ...)  // Sends field named "file"
```

FastAPI expects:
```python
file: UploadFile = File(...)  # Looking for parameter named "file"
```

If names don't match → 422 Error!

---

## Test This Immediately

1. **Update your FastAPI endpoint** to use `file:` parameter
2. **Restart FastAPI server**
3. **Run in Android again**

That's it! 99% of 422 errors are fixed by this.

---

## If Still Getting 422

### Check Response Format
Your Android expects:
```json
{"text": "transcription result"}
```

Make sure FastAPI returns:
```python
return {"text": "..."}  # ✅ Correct
```

### Not This:
```python
return {"transcription": "..."}  # ❌ Wrong field name
return "just text"               # ❌ Not JSON
```

---

## Test with curl
```bash
curl -X POST http://localhost:8000/transcribe/ -F "file=@audio.mp3"

# Should return:
# {"text":"transcription here"}
```

If curl returns 422, your FastAPI code is wrong.  
If curl works but Android gets 422, it's a network issue (use correct IP in ApiClient.java)

---

## Done! 🎉

Change 1 word in FastAPI and 99% of cases are fixed.
