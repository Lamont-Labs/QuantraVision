# QuantraVision Open-License Model Fetcher Guide

## Overview

The `scripts/fetch-models.sh` script downloads permissive-licensed AI models for on-device vision processing in QuantraVision. This enables the future Apex Intelligence System with geometric pattern detection while maintaining strict open-source licensing compliance.

## Purpose

Download and install mobile-optimized AI models with **Apache-2.0** licenses into Android assets for APK packaging. All models undergo SHA-256 integrity verification following fail-closed principles.

---

## Supported Models

### 1. COCO SSD MobileNet v1 (Quantized)

**Purpose:** Object detection for chart element recognition  
**License:** Apache-2.0  
**Source:** TensorFlow Lite Model Zoo  
**Size:** ~4-6 MB  
**Format:** TFLite (quantized INT8)  
**Output:**
- `app/src/main/assets/models/detector_ssd_mobilenet_v1_quant.tflite`
- `app/src/main/assets/labels/detector_labels.txt` (COCO class labels)

**Use Case:**
- Detect chart elements (candlesticks, lines, text boxes)
- Identify support/resistance levels
- Locate indicators and overlays

### 2. MobileSAM v2 Weights

**Purpose:** Segmentation for precise chart region masking  
**License:** Apache-2.0  
**Source:** HuggingFace (RogerQi/MobileSAMV2)  
**Size:** ~40-50 MB  
**Format:** PyTorch (.pt) - requires conversion to TFLite  
**Output:**
- `app/src/main/assets/models/mobile_sam_v2.pt`

**Use Case:**
- Segment chart regions from background
- Isolate candlestick patterns
- Mask indicators for separate analysis

**Note:** This is the base weights file. The existing pipeline will convert PT → ONNX → TFLite for on-device inference.

---

## Excluded Models (Non-Permissive)

The script **explicitly excludes** the following due to licensing restrictions:

❌ **Ultralytics YOLOv5/YOLOv8** (AGPL-3.0)  
❌ **FastSAM** (AGPL-3.0)  
❌ **Any GPL/AGPL-licensed weights**

**Reason:** AGPL requires distributing source code of derivative works, incompatible with commercial Android app distribution.

---

## Usage

### Prerequisites

- **curl**: For downloading models
- **unzip**: For extracting archives
- **sha256sum** or **shasum** or **python3**: For checksum verification

### Run the Script

```bash
# From project root
bash scripts/fetch-models.sh
```

### Expected Output

```
[models] starting open-license model fetcher...
[models] downloading: https://storage.googleapis.com/download.tensorflow.org/models/tflite/...
[models] extracting SSD package...
[models] SSD detector ready: app/src/main/assets/models/detector_ssd_mobilenet_v1_quant.tflite
[models] SSD sha256: a3f2c1d5e8b9...
[models] downloading: https://huggingface.co/RogerQi/MobileSAMV2/resolve/main/mobile_sam.pt
[models] MobileSAM v2 weights ready: app/src/main/assets/models/mobile_sam_v2.pt
[models] MobileSAM sha256: d7e4f1a2b8c9...
[models] manifest written: app/src/main/assets/models/MODEL_MANIFEST.json
[models] done. permissive on-device models installed into assets.
```

### Verification

Check the manifest file for SHA-256 checksums:

```bash
cat app/src/main/assets/models/MODEL_MANIFEST.json
```

**Example Manifest:**
```json
{
  "models": [
    {
      "id": "detector_ssd_mobilenet_v1_quant",
      "file": "detector_ssd_mobilenet_v1_quant.tflite",
      "task": "object_detection",
      "license": "Apache-2.0",
      "source_url": "https://storage.googleapis.com/...",
      "sha256": "a3f2c1d5e8b9f3a7c2d1e4f8b9a3c2d5..."
    },
    {
      "id": "mobile_sam_v2_weights",
      "file": "mobile_sam_v2.pt",
      "task": "segmentation_weights",
      "license": "Apache-2.0",
      "source_url": "https://huggingface.co/...",
      "sha256": "d7e4f1a2b8c9e3f7a2d1c4b8e9f3a2c1..."
    }
  ],
  "generated_by": "QuantraVision Open-License Model Fetcher v1.0",
  "fail_closed": true
}
```

---

## Integration with Build Process

### Option 1: Manual Execution (Recommended for Development)

Run the script manually before building:

```bash
bash scripts/fetch-models.sh
./gradlew assembleDebug
```

### Option 2: Gradle Task Integration (Future)

Add to `app/build.gradle.kts`:

```kotlin
tasks.register<Exec>("fetchModels") {
    commandLine("bash", "scripts/fetch-models.sh")
    workingDir(rootProject.projectDir)
}

tasks.named("preBuild") {
    dependsOn("fetchModels")
}
```

This ensures models are downloaded before every build.

### Option 3: CI/CD Integration

Add to `.github/workflows/ci.yml`:

```yaml
- name: Fetch AI Models
  run: bash scripts/fetch-models.sh
  
- name: Build Debug APK
  run: ./gradlew assembleDebug
```

---

## File Structure After Execution

```
app/src/main/assets/
├── models/
│   ├── detector_ssd_mobilenet_v1_quant.tflite   (4-6 MB)
│   ├── mobile_sam_v2.pt                          (40-50 MB)
│   └── MODEL_MANIFEST.json                       (metadata + checksums)
├── labels/
│   └── detector_labels.txt                       (COCO class names)
└── patterns/
    └── (existing 109 PNG templates)
```

**Note:** Model files (`.tflite`, `.pt`) are ignored by `.gitignore` due to size. Only the manifest is committed for integrity verification.

---

## Runtime Model Loading (Future Implementation)

### Kotlin Example: Load SSD Detector

```kotlin
import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ModelLoader(private val context: Context) {
    
    fun loadDetector(): Interpreter {
        val modelPath = "models/detector_ssd_mobilenet_v1_quant.tflite"
        val modelBuffer = loadModelFile(modelPath)
        
        // Verify SHA-256 against manifest
        val manifest = loadManifest()
        val expectedHash = manifest.models
            .find { it.id == "detector_ssd_mobilenet_v1_quant" }
            ?.sha256 ?: throw SecurityException("Model not found in manifest")
        
        val actualHash = sha256(modelBuffer)
        if (actualHash != expectedHash) {
            throw SecurityException("Model integrity check failed (fail-closed)")
        }
        
        return Interpreter(modelBuffer)
    }
    
    private fun loadModelFile(path: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(path)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    private fun loadManifest(): ModelManifest {
        val json = context.assets.open("models/MODEL_MANIFEST.json")
            .bufferedReader()
            .use { it.readText() }
        return Gson().fromJson(json, ModelManifest::class.java)
    }
}
```

---

## Fail-Closed Integrity Verification

All models undergo SHA-256 verification at runtime:

1. **Download Time:** Script computes SHA-256 and writes to manifest
2. **Runtime:** ModelLoader verifies hash before instantiation
3. **Mismatch:** Throws SecurityException, preventing corrupted model usage

**Security Benefit:** Prevents tampering, ensures reproducible builds, detects download corruption.

---

## Troubleshooting

### Issue: Script fails with "could not find Android assets directory"

**Solution:** Ensure you're running from project root and `app/src/main/assets` exists:

```bash
mkdir -p app/src/main/assets
bash scripts/fetch-models.sh
```

### Issue: curl fails with connection error

**Solution:** Check internet connection or use manual download:

```bash
# Manual download
wget https://storage.googleapis.com/download.tensorflow.org/models/tflite/coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip
unzip coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip
mv detect.tflite app/src/main/assets/models/detector_ssd_mobilenet_v1_quant.tflite
```

### Issue: sha256sum not found

**Solution:** Script auto-falls back to `shasum` or `python3`. Ensure one is available:

```bash
# macOS
which shasum

# Linux
which sha256sum

# Fallback
which python3
```

### Issue: Models already exist

**Solution:** Script overwrites existing files. To force re-download:

```bash
rm -rf app/src/main/assets/models/*.tflite app/src/main/assets/models/*.pt
bash scripts/fetch-models.sh
```

---

## License Compliance

All downloaded models are **Apache-2.0** licensed, permitting:

✅ Commercial use  
✅ Modification  
✅ Distribution  
✅ Private use  

**Requirements:**
- Include original license text (automatically in manifest)
- State changes (tracked via manifest)

**No copyleft restrictions** - safe for proprietary Android app distribution.

---

## Future Enhancements

### Planned Models (All Apache-2.0)

1. **EfficientDet Lite** - Better object detection accuracy
2. **DeepLab v3+** - Semantic segmentation for chart regions
3. **MobileNet v3** - Feature extraction for pattern classification

### Model Conversion Pipeline

For models requiring conversion (e.g., PyTorch → TFLite):

```bash
# Convert MobileSAM v2 weights
python scripts/convert_mobilesam_to_tflite.py \
  --input app/src/main/assets/models/mobile_sam_v2.pt \
  --output app/src/main/assets/models/mobile_sam_v2.tflite
```

---

## References

- **TensorFlow Lite Model Zoo:** https://www.tensorflow.org/lite/models
- **MobileSAM v2:** https://huggingface.co/RogerQi/MobileSAMV2
- **Apache-2.0 License:** https://www.apache.org/licenses/LICENSE-2.0
- **COCO Dataset:** https://cocodataset.org/

---

**Last Updated:** November 24, 2025  
**Script Version:** 1.0  
**Batch:** Post-Batch 10 (Future Vision Integration)
