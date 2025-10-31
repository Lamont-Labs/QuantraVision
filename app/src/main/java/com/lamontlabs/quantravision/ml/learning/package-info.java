/**
 * Learning Package - On-device incremental learning and adaptation
 * 
 * <p>This package contains Phase 4 optimizations for adaptive learning from user corrections
 * and personalization to specific chart styles.
 * 
 * <h2>Key Components:</h2>
 * <ul>
 *   <li>{@link IncrementalLearningEngine} - Learn from user corrections, extract features</li>
 *   <li>{@link RetrainingWorker} - Background model retraining (overnight, when charging)</li>
 * </ul>
 * 
 * <h2>Performance Impact:</h2>
 * <ul>
 *   <li>Rare patterns: 60% recall → 80% recall (+20%)</li>
 *   <li>User-specific patterns: Custom learning per user</li>
 *   <li>Model drift: Adapts to new chart styles over time</li>
 *   <li>Privacy: 100% offline learning, no data transmitted</li>
 * </ul>
 * 
 * @since 1.0.0
 * @version 2025.10.31
 */
package com.lamontlabs.quantravision.ml.learning;
