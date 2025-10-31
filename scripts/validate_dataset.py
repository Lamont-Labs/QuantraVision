#!/usr/bin/env python3
"""
Chart Dataset Validation Script
Checks collected screenshots for quality and completeness
"""

import os
from pathlib import Path
from PIL import Image
import sys

DATASET_ROOT = Path("chart_dataset")

# Quality requirements
MIN_WIDTH = 800
MIN_HEIGHT = 600
MIN_SCREENSHOTS = 30

def validate_image_quality(image_path):
    """Check if image meets quality requirements"""
    issues = []
    
    try:
        with Image.open(image_path) as img:
            width, height = img.size
            
            if width < MIN_WIDTH:
                issues.append(f"Width {width}px too small (min {MIN_WIDTH}px)")
            if height < MIN_HEIGHT:
                issues.append(f"Height {height}px too small (min {MIN_HEIGHT}px)")
            
            # Check file size (should be at least 50KB for meaningful chart)
            file_size = os.path.getsize(image_path)
            if file_size < 50000:
                issues.append(f"File size {file_size} bytes too small (may be blank)")
                
    except Exception as e:
        issues.append(f"Could not open image: {str(e)}")
    
    return issues

def scan_dataset():
    """Scan dataset and report statistics"""
    print("=" * 60)
    print("Chart Dataset Validation Report")
    print("=" * 60)
    
    if not DATASET_ROOT.exists():
        print(f"❌ Dataset directory not found: {DATASET_ROOT}")
        print("   Run from project root directory")
        return False
    
    # Count screenshots
    all_images = list(DATASET_ROOT.glob("**/*.png")) + list(DATASET_ROOT.glob("**/*.jpg"))
    total_count = len(all_images)
    
    print(f"\n📊 Total screenshots: {total_count} / {MIN_SCREENSHOTS} minimum")
    
    if total_count == 0:
        print("\n⚠️  No screenshots found!")
        print("   Please collect screenshots following chart_dataset/README.md")
        return False
    
    # Count by platform
    print("\n📱 By Platform:")
    platforms = ["tradingview", "metatrader", "robinhood", "td_ameritrade", "webull"]
    for platform in platforms:
        count = len(list(DATASET_ROOT.glob(f"{platform}/**/*.png")))
        status = "✅" if count > 0 else "⬜"
        print(f"   {status} {platform:15} : {count:3} screenshots")
    
    # Count by chart type
    print("\n📈 By Chart Type:")
    chart_types = ["candlestick", "line", "bar"]
    for chart_type in chart_types:
        count = len(list(DATASET_ROOT.glob(f"**/{chart_type}/**/*.png")))
        status = "✅" if count > 0 else "⬜"
        print(f"   {status} {chart_type:15} : {count:3} screenshots")
    
    # Count by theme
    print("\n🎨 By Theme:")
    themes = ["dark", "light"]
    for theme in themes:
        count = len(list(DATASET_ROOT.glob(f"**/{theme}/**/*.png")))
        status = "✅" if count > 0 else "⬜"
        print(f"   {status} {theme:15} : {count:3} screenshots")
    
    # Quality check
    print("\n🔍 Quality Check:")
    quality_issues = []
    for img_path in all_images:
        issues = validate_image_quality(img_path)
        if issues:
            quality_issues.append((img_path, issues))
    
    if quality_issues:
        print(f"   ⚠️  {len(quality_issues)} images with quality issues:")
        for img_path, issues in quality_issues[:5]:  # Show first 5
            print(f"      - {img_path.name}:")
            for issue in issues:
                print(f"        • {issue}")
        if len(quality_issues) > 5:
            print(f"      ... and {len(quality_issues) - 5} more")
    else:
        print("   ✅ All images meet quality requirements")
    
    # Readiness assessment
    print("\n" + "=" * 60)
    if total_count >= MIN_SCREENSHOTS and len(quality_issues) == 0:
        print("✅ READY: Dataset is sufficient to begin development!")
        print("   Next step: Start building viewport isolation module")
        return True
    elif total_count >= MIN_SCREENSHOTS:
        print("⚠️  PARTIAL: Enough screenshots but quality issues exist")
        print(f"   Fix {len(quality_issues)} quality issues before proceeding")
        return False
    else:
        needed = MIN_SCREENSHOTS - total_count
        print(f"❌ NOT READY: Need {needed} more screenshots")
        print("   Focus on TradingView candlestick charts (easiest to collect)")
        return False

def main():
    ready = scan_dataset()
    sys.exit(0 if ready else 1)

if __name__ == "__main__":
    main()
