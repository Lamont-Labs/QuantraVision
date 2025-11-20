# Ensemble AI Models for QuantraVision

## Downloaded Models (Ready to Use!)

✅ **mobilebert_qa_squad.tflite** (25MB)
   - Purpose: Generate answers for pattern/trading questions
   - Source: MLCommons mobile_models repository
   - Used for: Fallback Q&A generation

✅ **sentence_embeddings.tflite** (22MB)  
   - Purpose: Fast similarity search against Q&A database
   - Source: HuggingFace (all-MiniLM-L6-v2 quantized)
   - Used for: Instant answer retrieval

## How to Use

1. **Transfer to your phone**: 
   - Download both files to your Samsung S23 FE
   - Save them to your Downloads folder

2. **Import in app**:
   - Open QuantraVision
   - Go to Settings → AI Models → Import
   - Select sentence_embeddings.tflite first
   - Then select mobilebert_qa_squad.tflite

3. **Test**:
   - Go to QuantraBot tab
   - Ask: "What is Head and Shoulders?"
   - Should get instant answer from knowledge base!

## Note on Intent Classifier

The intent classifier (3rd model) is **optional** - it's an optimization that routes questions more efficiently. The app works great with just these 2 models!

If you want to add it later, you can train a custom one using TensorFlow Lite Model Maker for your specific trading intents.
