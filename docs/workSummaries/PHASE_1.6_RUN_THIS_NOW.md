# 🚀 Phase 1.6: Run This Now!

**Everything is ready. Just run this command:**

---

## ⚡ Quick Command

### Windows PowerShell (Open PowerShell, run this):

```powershell
cd D:\VSCProjects\AIProject2\backend
.\deploy-and-test-phase1.6.ps1
```

---

## 📋 What Happens

1. ✅ Stops any running backend
2. ✅ Starts fresh backend in new window
3. ✅ Waits for startup (~30 seconds)
4. ✅ Tests health endpoint
5. ✅ Tests 4 validation scenarios
6. ✅ Shows results with ✅❌ indicators

**Total Time:** ~1-2 minutes

---

## ✅ Success Looks Like This

You'll see in the output:

```
[SUCCESS] Entity linked to government organization!
  - Linked to: Department of Defense
  - Verified: True
  - Confidence: 1.0
```

**If you see `[SUCCESS]` → Phase 1.6 works! 🎉**

---

## ❌ Error Looks Like This

If you see:

```
[ERROR] 405 Method Not Allowed - Endpoint not found!
```

**Fix:** Stop backend (Ctrl+C), rebuild, run script again:
```powershell
cd D:\VSCProjects\AIProject2\backend
mvnw clean install
.\deploy-and-test-phase1.6.ps1
```

---

## 🎯 That's It!

**One command. That's all you need.**

Run it now and see the validation workflow in action! ✨

---

**Questions?** See `PHASE_1.6_AUTOMATED_DEPLOYMENT.md` for full details.
