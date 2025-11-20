package com.lamontlabs.quantravision.ai.ensemble.models

import com.google.gson.annotations.SerializedName

data class QAEntry(
    @SerializedName("question")
    val question: String,
    
    @SerializedName("answer")
    val answer: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("keywords")
    val keywords: List<String>
)
