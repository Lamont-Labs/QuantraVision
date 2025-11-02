#!/usr/bin/env python3
"""
Generate comprehensive YAML configuration files and template images 
for all 109 chart patterns in QuantraVision.
"""

import os
import yaml
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
from pathlib import Path

# Output directory
OUTPUT_DIR = Path("app/src/main/assets/pattern_templates")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Pattern definitions: [name, threshold, scale_range, scale_stride, timeframes, min_bars, aspect_tol]
PATTERNS = {
    # REVERSAL PATTERNS (40)
    "reversal": [
        ("Head_and_Shoulders", 0.78, [0.7, 1.5], 0.15, ["15m", "1h", "4h", "1d"], 20, None),
        ("Inverse_Head_and_Shoulders", 0.78, [0.7, 1.5], 0.15, ["15m", "1h", "4h", "1d"], 20, None),
        ("Double_Top", 0.75, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, None),
        ("Double_Bottom", 0.75, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, None),
        ("Triple_Top", 0.76, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 20, None),
        ("Triple_Bottom", 0.76, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 20, None),
        ("Rounding_Top", 0.72, [0.6, 1.3], 0.15, ["1h", "4h", "1d", "1w"], 30, 0.3),
        ("Rounding_Bottom", 0.72, [0.6, 1.3], 0.15, ["1h", "4h", "1d", "1w"], 30, 0.3),
        ("V_Top", 0.70, [0.7, 1.5], 0.15, ["5m", "15m", "1h", "4h"], 8, None),
        ("V_Bottom", 0.70, [0.7, 1.5], 0.15, ["5m", "15m", "1h", "4h"], 8, None),
        ("Diamond_Top", 0.80, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.2),
        ("Diamond_Bottom", 0.80, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.2),
        ("Broadening_Top", 0.74, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.25),
        ("Broadening_Bottom", 0.74, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.25),
        ("Island_Reversal_Top", 0.73, [0.7, 1.5], 0.15, ["15m", "1h", "4h"], 10, None),
        ("Island_Reversal_Bottom", 0.73, [0.7, 1.5], 0.15, ["15m", "1h", "4h"], 10, None),
        ("Adam_Eve_Double_Top", 0.77, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Eve_Adam_Double_Top", 0.77, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Adam_Adam_Double_Top", 0.76, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Eve_Eve_Double_Top", 0.76, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Adam_Eve_Double_Bottom", 0.77, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Eve_Adam_Double_Bottom", 0.77, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Adam_Adam_Double_Bottom", 0.76, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Eve_Eve_Double_Bottom", 0.76, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Saucer_Top", 0.71, [0.6, 1.3], 0.15, ["1h", "4h", "1d"], 25, 0.3),
        ("Saucer_Bottom", 0.71, [0.6, 1.3], 0.15, ["1h", "4h", "1d"], 25, 0.3),
        ("Spike_and_Channel_Reversal", 0.74, [0.7, 1.4], 0.15, ["15m", "1h", "4h"], 12, None),
        ("Bump_and_Run_Reversal_Top", 0.75, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 18, None),
        ("Bump_and_Run_Reversal_Bottom", 0.75, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 18, None),
        ("Complex_Head_and_Shoulders", 0.79, [0.7, 1.5], 0.15, ["1h", "4h", "1d"], 25, None),
        ("Complex_Inverse_Head_and_Shoulders", 0.79, [0.7, 1.5], 0.15, ["1h", "4h", "1d"], 25, None),
        ("Megaphone_Top", 0.73, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.2),
        ("Megaphone_Bottom", 0.73, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.2),
        ("Key_Reversal_Up", 0.68, [0.7, 1.5], 0.12, ["5m", "15m", "1h"], 2, None),
        ("Key_Reversal_Down", 0.68, [0.7, 1.5], 0.12, ["5m", "15m", "1h"], 2, None),
        ("Pipe_Top", 0.70, [0.7, 1.5], 0.15, ["5m", "15m", "1h"], 5, None),
        ("Pipe_Bottom", 0.70, [0.7, 1.5], 0.15, ["5m", "15m", "1h"], 5, None),
        ("Two_Bar_Reversal_Up", 0.67, [0.7, 1.5], 0.12, ["5m", "15m", "1h"], 2, None),
        ("Two_Bar_Reversal_Down", 0.67, [0.7, 1.5], 0.12, ["5m", "15m", "1h"], 2, None),
        ("Horn_Top", 0.72, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 12, None),
        ("Horn_Bottom", 0.72, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 12, None),
    ],
    
    # CONTINUATION PATTERNS (35)
    "continuation": [
        ("Ascending_Triangle", 0.75, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 12, 0.2),
        ("Descending_Triangle", 0.75, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 12, 0.2),
        ("Symmetrical_Triangle", 0.74, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 12, 0.2),
        ("Rising_Wedge", 0.76, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 12, 0.15),
        ("Falling_Wedge", 0.76, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 12, 0.15),
        ("Bull_Flag", 0.73, [0.7, 1.5], 0.15, ["5m", "15m", "1h", "4h"], 8, 0.2),
        ("Bear_Flag", 0.73, [0.7, 1.5], 0.15, ["5m", "15m", "1h", "4h"], 8, 0.2),
        ("Bull_Pennant", 0.74, [0.7, 1.5], 0.15, ["5m", "15m", "1h", "4h"], 8, 0.15),
        ("Bear_Pennant", 0.74, [0.7, 1.5], 0.15, ["5m", "15m", "1h", "4h"], 8, 0.15),
        ("Rectangle_Bullish", 0.72, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 10, 0.25),
        ("Rectangle_Bearish", 0.72, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 10, 0.25),
        ("Cup_and_Handle", 0.78, [0.6, 1.3], 0.15, ["1h", "4h", "1d", "1w"], 25, 0.3),
        ("Inverted_Cup_and_Handle", 0.78, [0.6, 1.3], 0.15, ["1h", "4h", "1d", "1w"], 25, 0.3),
        ("Ascending_Channel", 0.71, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, 0.2),
        ("Descending_Channel", 0.71, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, 0.2),
        ("Horizontal_Channel", 0.70, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, 0.3),
        ("Measured_Move_Up", 0.73, [0.7, 1.4], 0.15, ["15m", "1h", "4h"], 12, None),
        ("Measured_Move_Down", 0.73, [0.7, 1.4], 0.15, ["15m", "1h", "4h"], 12, None),
        ("Three_Drives_Pattern_Bullish", 0.77, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Three_Drives_Pattern_Bearish", 0.77, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, None),
        ("Scallop_Bullish", 0.72, [0.6, 1.3], 0.15, ["1h", "4h", "1d"], 20, 0.3),
        ("Scallop_Bearish", 0.72, [0.6, 1.3], 0.15, ["1h", "4h", "1d"], 20, 0.3),
        ("Flag_Formation_High_Tight", 0.76, [0.7, 1.5], 0.15, ["15m", "1h", "4h"], 10, 0.2),
        ("Pennant_Formation_High_Tight", 0.76, [0.7, 1.5], 0.15, ["15m", "1h", "4h"], 10, 0.15),
        ("Consolidation_Box", 0.70, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 12, 0.25),
        ("Trading_Range", 0.69, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, 0.3),
        ("Continuation_Diamond", 0.75, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 15, 0.2),
        ("Ladder_Bottom", 0.71, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 12, None),
        ("Ladder_Top", 0.71, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 12, None),
        ("Bull_Trap_Continuation", 0.74, [0.7, 1.4], 0.15, ["15m", "1h", "4h"], 10, None),
        ("Bear_Trap_Continuation", 0.74, [0.7, 1.4], 0.15, ["15m", "1h", "4h"], 10, None),
        ("Rectangle_Top_Continuation", 0.72, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 12, 0.25),
        ("Rectangle_Bottom_Continuation", 0.72, [0.7, 1.4], 0.15, ["1h", "4h", "1d"], 12, 0.25),
        ("Parallel_Channel_Up", 0.70, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, 0.2),
        ("Parallel_Channel_Down", 0.70, [0.7, 1.4], 0.15, ["15m", "1h", "4h", "1d"], 15, 0.2),
    ],
    
    # CANDLESTICK PATTERNS (33)
    "candlestick": [
        ("Doji", 0.70, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Hammer", 0.72, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Hanging_Man", 0.72, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Inverted_Hammer", 0.72, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Shooting_Star", 0.72, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Bullish_Engulfing", 0.74, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Bearish_Engulfing", 0.74, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Morning_Star", 0.76, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 3, None),
        ("Evening_Star", 0.76, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 3, None),
        ("Three_White_Soldiers", 0.75, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Three_Black_Crows", 0.75, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Piercing_Line", 0.73, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Dark_Cloud_Cover", 0.73, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Harami_Bullish", 0.74, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Harami_Bearish", 0.74, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Tweezer_Top", 0.72, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Tweezer_Bottom", 0.72, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 2, None),
        ("Spinning_Top", 0.68, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Marubozu_Bullish", 0.71, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Marubozu_Bearish", 0.71, [0.8, 1.3], 0.10, ["5m", "15m", "1h", "4h"], 1, None),
        ("Three_Inside_Up", 0.75, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Three_Inside_Down", 0.75, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Three_Outside_Up", 0.75, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Three_Outside_Down", 0.75, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Abandoned_Baby_Bullish", 0.77, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Abandoned_Baby_Bearish", 0.77, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Kicker_Bullish", 0.76, [0.8, 1.3], 0.10, ["5m", "15m", "1h"], 2, None),
        ("Kicker_Bearish", 0.76, [0.8, 1.3], 0.10, ["5m", "15m", "1h"], 2, None),
        ("Belt_Hold_Bullish", 0.69, [0.8, 1.3], 0.10, ["5m", "15m", "1h"], 1, None),
        ("Belt_Hold_Bearish", 0.69, [0.8, 1.3], 0.10, ["5m", "15m", "1h"], 1, None),
        ("Upside_Gap_Two_Crows", 0.74, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Downside_Tasuki_Gap", 0.73, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
        ("Upside_Tasuki_Gap", 0.73, [0.8, 1.3], 0.10, ["15m", "1h", "4h"], 3, None),
    ]
}


def generate_pattern_image(pattern_name, pattern_type, width=300, height=250):
    """Generate a simple, clean grayscale template image for a chart pattern."""
    
    fig, ax = plt.subplots(figsize=(width/100, height/100), dpi=100)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    
    # Set background to white
    fig.patch.set_facecolor('white')
    ax.set_facecolor('white')
    
    # Generate pattern-specific shapes
    name_lower = pattern_name.lower()
    
    # REVERSAL PATTERNS
    if "head_and_shoulders" in name_lower and "inverse" not in name_lower:
        # Head and Shoulders
        x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
        y = np.array([30, 50, 35, 60, 40, 50, 30, 40, 25])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [30, 25], 'k--', linewidth=1.5, alpha=0.6)  # Neckline
        
    elif "inverse_head_and_shoulders" in name_lower:
        # Inverse Head and Shoulders
        x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
        y = np.array([70, 50, 65, 40, 60, 50, 70, 60, 75])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [70, 75], 'k--', linewidth=1.5, alpha=0.6)  # Neckline
        
    elif "double_top" in name_lower:
        x = np.linspace(10, 90, 50)
        y = 30 + 30*np.sin((x-10)/15) * np.exp(-(x-50)**2/1000)
        y[20:25] = 60
        y[35:40] = 60
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [40, 40], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "double_bottom" in name_lower:
        x = np.linspace(10, 90, 50)
        y = 70 - 30*np.sin((x-10)/15) * np.exp(-(x-50)**2/1000)
        y[20:25] = 40
        y[35:40] = 40
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [60, 60], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "triple_top" in name_lower:
        x = np.array([10, 18, 25, 32, 40, 48, 55, 62, 70, 78, 90])
        y = np.array([30, 55, 40, 55, 38, 55, 40, 55, 35, 45, 25])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [38, 25], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "triple_bottom" in name_lower:
        x = np.array([10, 18, 25, 32, 40, 48, 55, 62, 70, 78, 90])
        y = np.array([70, 45, 60, 45, 62, 45, 60, 45, 65, 55, 75])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [62, 75], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "rounding_top" in name_lower or "saucer_top" in name_lower:
        x = np.linspace(10, 90, 60)
        y = 40 + 25 * np.cos((x-50)/25)
        ax.plot(x, y, 'k-', linewidth=2.5)
        
    elif "rounding_bottom" in name_lower or "saucer_bottom" in name_lower:
        x = np.linspace(10, 90, 60)
        y = 60 - 25 * np.cos((x-50)/25)
        ax.plot(x, y, 'k-', linewidth=2.5)
        
    elif "v_top" in name_lower:
        x = np.array([10, 50, 90])
        y = np.array([30, 75, 30])
        ax.plot(x, y, 'k-', linewidth=2.5)
        
    elif "v_bottom" in name_lower:
        x = np.array([10, 50, 90])
        y = np.array([70, 25, 70])
        ax.plot(x, y, 'k-', linewidth=2.5)
        
    elif "diamond" in name_lower:
        if "top" in name_lower:
            x = np.array([10, 30, 50, 70, 90])
            y = np.array([45, 70, 45, 70, 45])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 50], [45, 45], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([50, 90], [45, 45], 'k--', linewidth=1.5, alpha=0.6)
        else:
            x = np.array([10, 30, 50, 70, 90])
            y = np.array([55, 30, 55, 30, 55])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 50], [55, 55], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([50, 90], [55, 55], 'k--', linewidth=1.5, alpha=0.6)
            
    elif "broadening" in name_lower or "megaphone" in name_lower:
        if "top" in name_lower or "ascending" in name_lower:
            x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
            y = np.array([45, 55, 42, 60, 40, 65, 38, 68, 35])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 90], [45, 35], 'k--', linewidth=1, alpha=0.5)
            ax.plot([10, 90], [55, 68], 'k--', linewidth=1, alpha=0.5)
        else:
            x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
            y = np.array([55, 45, 58, 40, 60, 35, 62, 32, 65])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 90], [55, 65], 'k--', linewidth=1, alpha=0.5)
            ax.plot([10, 90], [45, 32], 'k--', linewidth=1, alpha=0.5)
            
    elif "island_reversal" in name_lower:
        if "top" in name_lower:
            x = np.array([10, 25, 30, 45, 50, 65, 90])
            y = np.array([30, 50, 50, 60, 60, 50, 30])
            ax.plot(x[:3], y[:3], 'k-', linewidth=2.5)
            ax.plot(x[2:5], y[2:5], 'k-', linewidth=2.5)
            ax.plot(x[4:], y[4:], 'k-', linewidth=2.5)
            ax.axvline(x=28, ymin=0.4, ymax=0.5, color='k', linestyle=':', linewidth=1.5)
            ax.axvline(x=52, ymin=0.5, ymax=0.4, color='k', linestyle=':', linewidth=1.5)
        else:
            x = np.array([10, 25, 30, 45, 50, 65, 90])
            y = np.array([70, 50, 50, 40, 40, 50, 70])
            ax.plot(x[:3], y[:3], 'k-', linewidth=2.5)
            ax.plot(x[2:5], y[2:5], 'k-', linewidth=2.5)
            ax.plot(x[4:], y[4:], 'k-', linewidth=2.5)
            ax.axvline(x=28, ymin=0.5, ymax=0.6, color='k', linestyle=':', linewidth=1.5)
            ax.axvline(x=52, ymin=0.4, ymax=0.5, color='k', linestyle=':', linewidth=1.5)
            
    # CONTINUATION PATTERNS
    elif "ascending_triangle" in name_lower:
        x = np.array([10, 25, 40, 55, 70, 85, 90])
        y = np.array([30, 55, 40, 55, 45, 55, 70])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [55, 55], 'k--', linewidth=1.5, alpha=0.6)
        ax.plot([10, 85], [30, 55], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "descending_triangle" in name_lower:
        x = np.array([10, 25, 40, 55, 70, 85, 90])
        y = np.array([70, 45, 60, 45, 55, 45, 30])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 90], [45, 45], 'k--', linewidth=1.5, alpha=0.6)
        ax.plot([10, 85], [70, 45], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "symmetrical_triangle" in name_lower:
        x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
        y = np.array([65, 40, 58, 42, 54, 46, 52, 48, 70])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 80], [65, 48], 'k--', linewidth=1.5, alpha=0.6)
        ax.plot([10, 80], [40, 48], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "rising_wedge" in name_lower:
        x = np.array([10, 25, 40, 55, 70, 85])
        y = np.array([30, 48, 42, 54, 48, 58])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 85], [30, 58], 'k--', linewidth=1.5, alpha=0.6)
        ax.plot([10, 85], [35, 63], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "falling_wedge" in name_lower:
        x = np.array([10, 25, 40, 55, 70, 85])
        y = np.array([70, 52, 58, 46, 52, 42])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([10, 85], [70, 42], 'k--', linewidth=1.5, alpha=0.6)
        ax.plot([10, 85], [65, 37], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "bull_flag" in name_lower or "flag_bullish" in name_lower:
        x = np.array([10, 20, 30, 40, 50, 55, 60, 65, 90])
        y = np.array([30, 55, 55, 53, 53, 51, 51, 49, 75])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([30, 65], [55, 49], 'k--', linewidth=1, alpha=0.5)
        ax.plot([30, 65], [53, 47], 'k--', linewidth=1, alpha=0.5)
        
    elif "bear_flag" in name_lower or "flag_bearish" in name_lower:
        x = np.array([10, 20, 30, 40, 50, 55, 60, 65, 90])
        y = np.array([70, 45, 45, 47, 47, 49, 49, 51, 25])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([30, 65], [45, 51], 'k--', linewidth=1, alpha=0.5)
        ax.plot([30, 65], [47, 53], 'k--', linewidth=1, alpha=0.5)
        
    elif "bull_pennant" in name_lower or "pennant_bullish" in name_lower:
        x = np.array([10, 20, 30, 38, 46, 54, 62, 70, 90])
        y = np.array([30, 55, 55, 52, 54, 51, 53, 51, 75])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([30, 70], [55, 51], 'k--', linewidth=1, alpha=0.5)
        ax.plot([30, 70], [50, 51], 'k--', linewidth=1, alpha=0.5)
        
    elif "bear_pennant" in name_lower or "pennant_bearish" in name_lower:
        x = np.array([10, 20, 30, 38, 46, 54, 62, 70, 90])
        y = np.array([70, 45, 45, 48, 46, 49, 47, 49, 25])
        ax.plot(x, y, 'k-', linewidth=2.5)
        ax.plot([30, 70], [45, 49], 'k--', linewidth=1, alpha=0.5)
        ax.plot([30, 70], [50, 49], 'k--', linewidth=1, alpha=0.5)
        
    elif "rectangle" in name_lower or "consolidation" in name_lower or "trading_range" in name_lower:
        if "bearish" in name_lower or "down" in name_lower:
            x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
            y = np.array([70, 60, 55, 60, 55, 60, 55, 60, 30])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([20, 80], [60, 60], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([20, 80], [55, 55], 'k--', linewidth=1.5, alpha=0.6)
        else:
            x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
            y = np.array([30, 40, 45, 40, 45, 40, 45, 40, 70])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([20, 80], [40, 40], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([20, 80], [45, 45], 'k--', linewidth=1.5, alpha=0.6)
        
    elif "cup_and_handle" in name_lower and "inverted" not in name_lower:
        x = np.linspace(10, 90, 80)
        y = np.zeros_like(x)
        y[:40] = 40 + 15 * (1 - np.cos(np.linspace(0, np.pi, 40)))
        y[40:55] = 70 - 5 * ((x[40:55] - 50) / 7.5)**2
        y[55:] = 70 + (x[55:] - 55) * 0.3
        ax.plot(x, y, 'k-', linewidth=2.5)
        
    elif "inverted_cup_and_handle" in name_lower:
        x = np.linspace(10, 90, 80)
        y = np.zeros_like(x)
        y[:40] = 60 - 15 * (1 - np.cos(np.linspace(0, np.pi, 40)))
        y[40:55] = 30 + 5 * ((x[40:55] - 50) / 7.5)**2
        y[55:] = 30 - (x[55:] - 55) * 0.3
        ax.plot(x, y, 'k-', linewidth=2.5)
        
    elif "channel" in name_lower or "parallel" in name_lower:
        if "ascending" in name_lower or "up" in name_lower:
            x = np.array([10, 30, 50, 70, 90])
            y = np.array([30, 40, 50, 60, 70])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 90], [35, 75], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([10, 90], [25, 65], 'k--', linewidth=1.5, alpha=0.6)
        elif "descending" in name_lower or "down" in name_lower:
            x = np.array([10, 30, 50, 70, 90])
            y = np.array([70, 60, 50, 40, 30])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 90], [75, 35], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([10, 90], [65, 25], 'k--', linewidth=1.5, alpha=0.6)
        else:  # horizontal
            x = np.array([10, 20, 30, 40, 50, 60, 70, 80, 90])
            y = np.array([50, 45, 50, 45, 50, 45, 50, 45, 50])
            ax.plot(x, y, 'k-', linewidth=2.5)
            ax.plot([10, 90], [50, 50], 'k--', linewidth=1.5, alpha=0.6)
            ax.plot([10, 90], [45, 45], 'k--', linewidth=1.5, alpha=0.6)
            
    # CANDLESTICK PATTERNS
    elif "doji" in name_lower:
        # Single candle with small body
        rect = Rectangle((45, 48), 10, 4, linewidth=1, edgecolor='black', facecolor='white')
        ax.add_patch(rect)
        ax.plot([50, 50], [30, 48], 'k-', linewidth=1.5)
        ax.plot([50, 50], [52, 70], 'k-', linewidth=1.5)
        
    elif "hammer" in name_lower and "inverted" not in name_lower:
        rect = Rectangle((45, 60), 10, 8, linewidth=1, edgecolor='black', facecolor='white')
        ax.add_patch(rect)
        ax.plot([50, 50], [30, 60], 'k-', linewidth=1.5)
        ax.plot([50, 50], [68, 72], 'k-', linewidth=1.5)
        
    elif "hanging_man" in name_lower:
        rect = Rectangle((45, 60), 10, 8, linewidth=1, edgecolor='black', facecolor='gray')
        ax.add_patch(rect)
        ax.plot([50, 50], [30, 60], 'k-', linewidth=1.5)
        ax.plot([50, 50], [68, 72], 'k-', linewidth=1.5)
        
    elif "inverted_hammer" in name_lower:
        rect = Rectangle((45, 32), 10, 8, linewidth=1, edgecolor='black', facecolor='white')
        ax.add_patch(rect)
        ax.plot([50, 50], [28, 32], 'k-', linewidth=1.5)
        ax.plot([50, 50], [40, 70], 'k-', linewidth=1.5)
        
    elif "shooting_star" in name_lower:
        rect = Rectangle((45, 32), 10, 8, linewidth=1, edgecolor='black', facecolor='gray')
        ax.add_patch(rect)
        ax.plot([50, 50], [28, 32], 'k-', linewidth=1.5)
        ax.plot([50, 50], [40, 70], 'k-', linewidth=1.5)
        
    elif "engulfing" in name_lower:
        if "bullish" in name_lower:
            rect1 = Rectangle((35, 48), 8, -10, linewidth=1, edgecolor='black', facecolor='gray')
            rect2 = Rectangle((57, 35), 8, 20, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([39, 39], [50, 55], 'k-', linewidth=1.5)
            ax.plot([39, 39], [38, 32], 'k-', linewidth=1.5)
            ax.plot([61, 61], [57, 65], 'k-', linewidth=1.5)
            ax.plot([61, 61], [35, 28], 'k-', linewidth=1.5)
        else:
            rect1 = Rectangle((35, 38), 8, 10, linewidth=1, edgecolor='black', facecolor='white')
            rect2 = Rectangle((57, 55), 8, -20, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([39, 39], [50, 55], 'k-', linewidth=1.5)
            ax.plot([39, 39], [38, 32], 'k-', linewidth=1.5)
            ax.plot([61, 61], [57, 65], 'k-', linewidth=1.5)
            ax.plot([61, 61], [35, 28], 'k-', linewidth=1.5)
            
    elif "morning_star" in name_lower:
        rect1 = Rectangle((25, 55), 8, -12, linewidth=1, edgecolor='black', facecolor='gray')
        rect2 = Rectangle((46, 42), 8, 3, linewidth=1, edgecolor='black', facecolor='white')
        rect3 = Rectangle((67, 45), 8, 15, linewidth=1, edgecolor='black', facecolor='white')
        ax.add_patch(rect1)
        ax.add_patch(rect2)
        ax.add_patch(rect3)
        ax.plot([29, 29], [57, 63], 'k-', linewidth=1.5)
        ax.plot([29, 29], [43, 38], 'k-', linewidth=1.5)
        ax.plot([50, 50], [46, 50], 'k-', linewidth=1.5)
        ax.plot([50, 50], [42, 38], 'k-', linewidth=1.5)
        ax.plot([71, 71], [62, 68], 'k-', linewidth=1.5)
        ax.plot([71, 71], [45, 40], 'k-', linewidth=1.5)
        
    elif "evening_star" in name_lower:
        rect1 = Rectangle((25, 45), 8, 12, linewidth=1, edgecolor='black', facecolor='white')
        rect2 = Rectangle((46, 55), 8, 3, linewidth=1, edgecolor='black', facecolor='gray')
        rect3 = Rectangle((67, 60), 8, -15, linewidth=1, edgecolor='black', facecolor='gray')
        ax.add_patch(rect1)
        ax.add_patch(rect2)
        ax.add_patch(rect3)
        ax.plot([29, 29], [59, 63], 'k-', linewidth=1.5)
        ax.plot([29, 29], [45, 40], 'k-', linewidth=1.5)
        ax.plot([50, 50], [59, 63], 'k-', linewidth=1.5)
        ax.plot([50, 50], [55, 50], 'k-', linewidth=1.5)
        ax.plot([71, 71], [62, 67], 'k-', linewidth=1.5)
        ax.plot([71, 71], [45, 38], 'k-', linewidth=1.5)
        
    elif "three_white_soldiers" in name_lower or "three_inside_up" in name_lower or "three_outside_up" in name_lower:
        for i, x_pos in enumerate([25, 45, 65]):
            height = 12 + i*2
            rect = Rectangle((x_pos, 40+i*5), 10, height, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect)
            ax.plot([x_pos+5, x_pos+5], [40+i*5+height, 40+i*5+height+6], 'k-', linewidth=1.5)
            ax.plot([x_pos+5, x_pos+5], [40+i*5, 40+i*5-4], 'k-', linewidth=1.5)
            
    elif "three_black_crows" in name_lower or "three_inside_down" in name_lower or "three_outside_down" in name_lower:
        for i, x_pos in enumerate([25, 45, 65]):
            height = 12 + i*2
            rect = Rectangle((x_pos, 60-i*5), 10, -height, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect)
            ax.plot([x_pos+5, x_pos+5], [60-i*5, 60-i*5+6], 'k-', linewidth=1.5)
            ax.plot([x_pos+5, x_pos+5], [60-i*5-height, 60-i*5-height-4], 'k-', linewidth=1.5)
            
    elif "piercing_line" in name_lower:
        rect1 = Rectangle((35, 60), 8, -15, linewidth=1, edgecolor='black', facecolor='gray')
        rect2 = Rectangle((57, 42), 8, 16, linewidth=1, edgecolor='black', facecolor='white')
        ax.add_patch(rect1)
        ax.add_patch(rect2)
        ax.plot([39, 39], [62, 68], 'k-', linewidth=1.5)
        ax.plot([39, 39], [45, 38], 'k-', linewidth=1.5)
        ax.plot([61, 61], [60, 66], 'k-', linewidth=1.5)
        ax.plot([61, 61], [42, 35], 'k-', linewidth=1.5)
        
    elif "dark_cloud_cover" in name_lower:
        rect1 = Rectangle((35, 40), 8, 15, linewidth=1, edgecolor='black', facecolor='white')
        rect2 = Rectangle((57, 58), 8, -16, linewidth=1, edgecolor='black', facecolor='gray')
        ax.add_patch(rect1)
        ax.add_patch(rect2)
        ax.plot([39, 39], [57, 63], 'k-', linewidth=1.5)
        ax.plot([39, 39], [40, 33], 'k-', linewidth=1.5)
        ax.plot([61, 61], [60, 66], 'k-', linewidth=1.5)
        ax.plot([61, 61], [42, 35], 'k-', linewidth=1.5)
        
    elif "harami" in name_lower:
        if "bullish" in name_lower:
            rect1 = Rectangle((32, 60), 12, -18, linewidth=1, edgecolor='black', facecolor='gray')
            rect2 = Rectangle((58, 48), 8, 6, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([38, 38], [62, 68], 'k-', linewidth=1.5)
            ax.plot([38, 38], [42, 36], 'k-', linewidth=1.5)
            ax.plot([62, 62], [56, 60], 'k-', linewidth=1.5)
            ax.plot([62, 62], [48, 44], 'k-', linewidth=1.5)
        else:
            rect1 = Rectangle((32, 40), 12, 18, linewidth=1, edgecolor='black', facecolor='white')
            rect2 = Rectangle((58, 52), 8, -6, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([38, 38], [60, 66], 'k-', linewidth=1.5)
            ax.plot([38, 38], [40, 34], 'k-', linewidth=1.5)
            ax.plot([62, 62], [54, 58], 'k-', linewidth=1.5)
            ax.plot([62, 62], [46, 42], 'k-', linewidth=1.5)
            
    elif "tweezer" in name_lower:
        if "bottom" in name_lower:
            rect1 = Rectangle((35, 45), 8, -10, linewidth=1, edgecolor='black', facecolor='gray')
            rect2 = Rectangle((57, 40), 8, 10, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([39, 39], [47, 52], 'k-', linewidth=1.5)
            ax.plot([39, 39], [35, 30], 'k-', linewidth=1.5)
            ax.plot([61, 61], [52, 57], 'k-', linewidth=1.5)
            ax.plot([61, 61], [40, 30], 'k-', linewidth=1.5)
        else:
            rect1 = Rectangle((35, 55), 8, 10, linewidth=1, edgecolor='black', facecolor='white')
            rect2 = Rectangle((57, 60), 8, -10, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([39, 39], [67, 72], 'k-', linewidth=1.5)
            ax.plot([39, 39], [55, 50], 'k-', linewidth=1.5)
            ax.plot([61, 61], [72, 67], 'k-', linewidth=1.5)
            ax.plot([61, 61], [50, 60], 'k-', linewidth=1.5)
            
    elif "spinning_top" in name_lower:
        rect = Rectangle((45, 48), 10, 4, linewidth=1, edgecolor='black', facecolor='lightgray')
        ax.add_patch(rect)
        ax.plot([50, 50], [32, 48], 'k-', linewidth=1.5)
        ax.plot([50, 50], [52, 68], 'k-', linewidth=1.5)
        
    elif "marubozu" in name_lower:
        if "bullish" in name_lower:
            rect = Rectangle((42, 35), 16, 30, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect)
        else:
            rect = Rectangle((42, 65), 16, -30, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect)
            
    elif "abandoned_baby" in name_lower:
        if "bullish" in name_lower:
            rect1 = Rectangle((22, 60), 10, -15, linewidth=1, edgecolor='black', facecolor='gray')
            rect2 = Rectangle((46, 42), 8, 2, linewidth=1, edgecolor='black', facecolor='white')
            rect3 = Rectangle((70, 45), 10, 18, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.add_patch(rect3)
            ax.plot([27, 27], [62, 68], 'k-', linewidth=1.5)
            ax.plot([27, 27], [45, 38], 'k-', linewidth=1.5)
            ax.plot([50, 50], [46, 52], 'k-', linewidth=1.5)
            ax.plot([50, 50], [42, 36], 'k-', linewidth=1.5)
            ax.plot([75, 75], [65, 72], 'k-', linewidth=1.5)
            ax.plot([75, 75], [45, 38], 'k-', linewidth=1.5)
        else:
            rect1 = Rectangle((22, 40), 10, 15, linewidth=1, edgecolor='black', facecolor='white')
            rect2 = Rectangle((46, 56), 8, 2, linewidth=1, edgecolor='black', facecolor='gray')
            rect3 = Rectangle((70, 63), 10, -18, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.add_patch(rect3)
            ax.plot([27, 27], [57, 63], 'k-', linewidth=1.5)
            ax.plot([27, 27], [40, 33], 'k-', linewidth=1.5)
            ax.plot([50, 50], [60, 66], 'k-', linewidth=1.5)
            ax.plot([50, 50], [56, 50], 'k-', linewidth=1.5)
            ax.plot([75, 75], [65, 72], 'k-', linewidth=1.5)
            ax.plot([75, 75], [45, 38], 'k-', linewidth=1.5)
            
    elif "kicker" in name_lower:
        if "bullish" in name_lower:
            rect1 = Rectangle((32, 58), 12, -16, linewidth=1, edgecolor='black', facecolor='gray')
            rect2 = Rectangle((58, 48), 12, 18, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([38, 38], [60, 66], 'k-', linewidth=1.5)
            ax.plot([38, 38], [42, 36], 'k-', linewidth=1.5)
            ax.plot([64, 64], [68, 74], 'k-', linewidth=1.5)
            ax.plot([64, 64], [48, 42], 'k-', linewidth=1.5)
        else:
            rect1 = Rectangle((32, 42), 12, 16, linewidth=1, edgecolor='black', facecolor='white')
            rect2 = Rectangle((58, 52), 12, -18, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect1)
            ax.add_patch(rect2)
            ax.plot([38, 38], [60, 66], 'k-', linewidth=1.5)
            ax.plot([38, 38], [42, 36], 'k-', linewidth=1.5)
            ax.plot([64, 64], [54, 60], 'k-', linewidth=1.5)
            ax.plot([64, 64], [34, 28], 'k-', linewidth=1.5)
            
    elif "belt_hold" in name_lower:
        if "bullish" in name_lower:
            rect = Rectangle((42, 38), 16, 24, linewidth=1, edgecolor='black', facecolor='white')
            ax.add_patch(rect)
            ax.plot([50, 50], [64, 70], 'k-', linewidth=1.5)
        else:
            rect = Rectangle((42, 62), 16, -24, linewidth=1, edgecolor='black', facecolor='gray')
            ax.add_patch(rect)
            ax.plot([50, 50], [38, 32], 'k-', linewidth=1.5)
            
    # DEFAULT PATTERNS - Generic shapes
    else:
        # Create a generic pattern visualization
        if "bullish" in name_lower or "up" in name_lower or "bottom" in name_lower:
            x = np.linspace(10, 90, 50)
            y = 30 + 35 * (1 / (1 + np.exp(-(x-50)/10)))
            ax.plot(x, y, 'k-', linewidth=2.5)
        elif "bearish" in name_lower or "down" in name_lower or "top" in name_lower:
            x = np.linspace(10, 90, 50)
            y = 70 - 35 * (1 / (1 + np.exp(-(x-50)/10)))
            ax.plot(x, y, 'k-', linewidth=2.5)
        else:
            # Neutral pattern
            x = np.linspace(10, 90, 50)
            y = 50 + 15 * np.sin((x-10)/10)
            ax.plot(x, y, 'k-', linewidth=2.5)
    
    # Save as grayscale PNG
    output_path = OUTPUT_DIR / f"{pattern_name.lower()}_ref.png"
    plt.savefig(output_path, dpi=100, bbox_inches='tight', pad_inches=0.1, 
                facecolor='white', edgecolor='none')
    plt.close()
    
    return output_path


def create_yaml(pattern_name, threshold, scale_range, scale_stride, timeframes, min_bars, aspect_tol):
    """Create YAML configuration for a pattern."""
    
    yaml_data = {
        'name': pattern_name.replace('_', ' '),
        'image': f"pattern_templates/{pattern_name.lower()}_ref.png",
        'threshold': threshold,
        'scale_range': scale_range,
        'scale_stride': scale_stride,
        'timeframe_hints': timeframes,
        'min_bars': min_bars
    }
    
    if aspect_tol is not None:
        yaml_data['aspect_tolerance'] = aspect_tol
    
    yaml_path = OUTPUT_DIR / f"{pattern_name.lower()}.yaml"
    
    with open(yaml_path, 'w') as f:
        yaml.dump(yaml_data, f, default_flow_style=False, sort_keys=False)
    
    return yaml_path


def main():
    """Generate all 108 patterns with YAMLs and images."""
    
    print("Generating all 108 chart patterns for QuantraVision...")
    print(f"Output directory: {OUTPUT_DIR}")
    
    total_count = 0
    
    for pattern_type, pattern_list in PATTERNS.items():
        print(f"\nGenerating {pattern_type.upper()} patterns ({len(pattern_list)} patterns)...")
        
        for pattern_data in pattern_list:
            name, threshold, scale_range, scale_stride, timeframes, min_bars, aspect_tol = pattern_data
            
            # Generate image
            img_path = generate_pattern_image(name, pattern_type)
            
            # Generate YAML
            yaml_path = create_yaml(name, threshold, scale_range, scale_stride, 
                                   timeframes, min_bars, aspect_tol)
            
            print(f"  ✓ {name}: YAML + Image created")
            total_count += 1
    
    print(f"\n{'='*60}")
    print(f"COMPLETE: Generated {total_count} patterns")
    print(f"  - Reversal: {len(PATTERNS['reversal'])}")
    print(f"  - Continuation: {len(PATTERNS['continuation'])}")
    print(f"  - Candlestick: {len(PATTERNS['candlestick'])}")
    print(f"{'='*60}")
    
    # Verify all files exist
    yaml_files = list(OUTPUT_DIR.glob("*.yaml"))
    png_files = list(OUTPUT_DIR.glob("*_ref.png"))
    
    print(f"\nVerification:")
    print(f"  YAML files: {len(yaml_files)}")
    print(f"  PNG files: {len(png_files)}")
    
    if total_count == 108:
        print("\n✓ SUCCESS: All 108 patterns generated successfully!")
    else:
        print(f"\n⚠ WARNING: Expected 108 patterns, generated {total_count}")


if __name__ == "__main__":
    main()
