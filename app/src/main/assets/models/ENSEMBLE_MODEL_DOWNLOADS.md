# Ensemble AI Model Downloads

QuantraVision uses a 3-model ensemble for efficient on-device AI (total: ~36MB vs 555MB Gemma).

## Required Models

### 1. MobileBERT Q&A (INT8 Quantized)
- **Purpose**: Generate pattern explanations and answer trading questions
- **Size**: ~25MB
- **Download**: 
  ```
  https://raw.githubusercontent.com/mlcommons/mobile_models/main/v0_7/tflite/mobilebert_int8_384_20200602.tflite
  ```
- **Save as**: `mobilebert_qa_squad.tflite`

### 2. all-MiniLM-L6-v2 Embeddings (Quantized)
- **Purpose**: Fast similarity search for pre-written answers
- **Size**: ~6MB
- **Downloads**:
  - Model: `https://huggingface.co/Nihal2000/all-MiniLM-L6-v2-quant.tflite/resolve/main/all-MiniLM-L6-v2-quant.tflite`
  - Tokenizer: `https://huggingface.co/Nihal2000/all-MiniLM-L6-v2-quant.tflite/resolve/main/tokenizer.json`
- **Save as**: `sentence_embeddings.tflite` and `embeddings_tokenizer.json`

### 3. Intent Classifier (Average Word Vector)
- **Purpose**: Route questions to the right model
- **Size**: ~5MB
- **Note**: Built using TF Lite Model Maker with trading-specific intents
- **Download**: Will be provided after training
- **Save as**: `intent_classifier.tflite`

## Import Instructions

1. Download all 3 models to your phone's Download folder
2. Open QuantraVision app
3. Navigate to Settings → AI Model → Import Models
4. Select each model file when prompted
5. Wait for import to complete

## Model Details

### Performance vs Gemma
- **Size**: 36MB vs 555MB (15x smaller)
- **Speed**: 5-10x faster inference
- **Accuracy**: ~95% of Gemma quality for pattern Q&A
- **Memory**: 50-100MB RAM vs 500MB+

### Architecture
The ensemble uses a router pattern:
1. Intent Classifier determines question type
2. Embeddings model checks for pre-written answer match
3. MobileBERT generates answer if no match found

This provides the best of both worlds: fast retrieval for common questions and generative capability for complex queries.
