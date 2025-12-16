# Python 3.11 Setup Guide for Reasoning Service

## Issue

The reasoning service requires Python 3.11 due to a known compatibility issue between:
- spaCy 3.8.x (uses internal Pydantic v1)
- Python 3.12.6 (updated typing system)

**Error:** `TypeError: ForwardRef._evaluate() missing 1 required keyword-only argument: 'recursive_guard'`

## Solution: Use Miniconda with Python 3.11 (Recommended)

**This project uses the `newsanalyzer` conda environment with Python 3.11.**

### Quick Start (Environment Already Configured)

If the `newsanalyzer` conda environment is already set up:

```powershell
# Activate the environment
conda activate newsanalyzer

# Navigate to reasoning service
cd D:\VSCProjects\AIProject2\reasoning-service

# Start the service
uvicorn app.main:app --reload --port 8000

# Test entity validation
python test_entity_validation.py
```

### First-Time Setup with Miniconda

#### Step 1: Create Conda Environment

```powershell
# Create environment with Python 3.11
conda create -n newsanalyzer python=3.11 -y

# Activate the environment
conda activate newsanalyzer

# Verify Python version
python --version
# Should show: Python 3.11.x
```

#### Step 2: Install Dependencies

```powershell
# Navigate to reasoning service
cd D:\VSCProjects\AIProject2\reasoning-service

# Upgrade pip
python -m pip install --upgrade pip

# Install all dependencies
pip install -r requirements.txt

# Download spaCy model
python -m spacy download en_core_web_sm
```

#### Step 3: Configure VS Code

Add to `.vscode/settings.json`:

```json
{
  "python.defaultInterpreterPath": "C:/Users/sowoo/miniconda3/envs/newsanalyzer/python.exe",
  "python.terminal.activateEnvironment": true
}
```

This ensures VS Code automatically activates the `newsanalyzer` environment.

#### Step 4: Test the Setup

```powershell
# Run pytest tests
pytest -v

# Test entity validation
python test_entity_validation.py
```

#### Step 5: Start the Service

```powershell
# Start FastAPI server
uvicorn app.main:app --reload --port 8000

# Or test in the background
python test_entity_validation.py
```

## Managing the Environment

```powershell
# List all conda environments
conda env list

# Activate newsanalyzer environment
conda activate newsanalyzer

# Deactivate current environment
conda deactivate

# Update dependencies
conda activate newsanalyzer
pip install -r requirements.txt --upgrade

# Remove environment (if needed)
conda env remove -n newsanalyzer
```

## Alternative: Using Python venv (Not Recommended)

If you don't want to use Miniconda, you can use standard Python venv:

```powershell
# Install Python 3.11 from python.org
py -3.11 -m venv venv

# Activate venv
.\venv\Scripts\Activate

# Install dependencies
pip install -r requirements.txt
python -m spacy download en_core_web_sm
```

**Note:** The conda approach is recommended as it provides better environment management.

## Verification Checklist

- [x] Conda environment `newsanalyzer` created with Python 3.11
- [x] Dependencies installed successfully
- [x] spaCy model downloaded (en_core_web_sm)
- [x] FastAPI server starts without errors
- [x] Entity extraction endpoint works
- [x] Government organization validation tested and working

## Future Notes

Once spaCy 3.9 is released with Python 3.12 support, you can:
1. Upgrade to Python 3.12
2. Recreate the venv
3. Update `requirements.txt` to use `spacy>=3.9.0`

## Current Dependencies (requirements.txt)

```txt
spacy>=3.8.0  # Works with Python 3.11
torch==2.2.2
transformers==4.38.0
fastapi==0.109.0
# ... (other dependencies)
```
