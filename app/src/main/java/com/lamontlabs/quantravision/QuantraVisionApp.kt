package com.lamontlabs.quantravision

import android.app.Application

class QuantraVisionApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Batch 1: Stub only
        // Future batches will initialize:
        // - Apex Engine Mobile
        // - Local vision models
        // - Billing manager
        // - Quota gate
    }
}
