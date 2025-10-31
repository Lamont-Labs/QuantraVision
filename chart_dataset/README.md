# Chart Dataset Collection Guide

## Purpose
This dataset is used to train and validate chart-agnostic pattern recognition. We need screenshots from multiple platforms, chart types, and themes to ensure the system works everywhere.

## Target: 100+ Screenshots

### Platform Distribution (20 screenshots each)
- **TradingView** (primary target - most popular)
- **MetaTrader 4/5** (forex/CFD traders)
- **Robinhood** (retail mobile traders)
- **TD Ameritrade/thinkorswim** (desktop platform)
- **Webull** (mobile/web platform)

### Chart Type Distribution (per platform)
- **Candlestick charts**: 12 screenshots
  - 6 dark theme
  - 6 light theme
- **Line charts**: 4 screenshots
  - 2 dark theme
  - 2 light theme
- **Bar charts**: 4 screenshots
  - 2 dark theme
  - 2 light theme

### What to Capture

#### Required Patterns (capture at least 3-5 of these)
- Head and Shoulders
- Double Top/Bottom
- Triangle (ascending/descending/symmetrical)
- Channel (up/down)
- Flag/Pennant
- Wedge

#### Capture Checklist
For each screenshot:
- [ ] Chart takes up majority of screen
- [ ] Pattern is clearly visible
- [ ] At least 20-30 candles/data points visible
- [ ] Axes and gridlines visible (helps with viewport isolation)
- [ ] No overlapping pop-ups or dialogs
- [ ] Screenshot is at least 1080x720 resolution

#### Theme Variations
**Dark Theme:**
- Background: Dark (black, dark blue, dark gray)
- Candles: Green/red or custom colors

**Light Theme:**
- Background: White or light gray
- Candles: Green/red or custom colors

#### Zoom Level Variations
- **Zoomed out**: 50+ candles visible
- **Medium**: 20-30 candles visible
- **Zoomed in**: 10-15 candles visible

Mix these in your dataset!

---

## How to Capture Screenshots

### TradingView (Start Here - Priority Platform)
1. Go to tradingview.com
2. Open any popular ticker (BTC/USD, SPY, AAPL, etc.)
3. Switch between candlestick/line/bar using chart type selector
4. Toggle dark/light theme (bottom toolbar)
5. Look for patterns or create them using replay mode
6. Take screenshot (F12 or Snipping Tool)
7. Save as: `tradingview/candlestick/dark/btc_headshoulders_001.png`

### MetaTrader 4/5
1. Open MetaTrader demo account (free download)
2. Open chart for EUR/USD, GBP/USD, etc.
3. Switch chart types (candlestick, line, bar)
4. Right-click → Properties → Colors to change theme
5. Take screenshot
6. Save as: `metatrader/candlestick/dark/eurusd_triangle_001.png`

### Robinhood
1. Open Robinhood app or web
2. View stock charts (SPY, AAPL, TSLA, etc.)
3. Screenshots from mobile or web browser
4. Save as: `robinhood/line/dark/spy_channel_001.png`

### TD Ameritrade (thinkorswim)
1. Download thinkorswim demo (free)
2. Open charts for various tickers
3. Change chart type and color scheme in settings
4. Take screenshots
5. Save as: `td_ameritrade/candlestick/light/aapl_doubletop_001.png`

### Webull
1. Download Webull app or use web version
2. Open various stock charts
3. Switch between chart types
4. Take screenshots
5. Save as: `webull/candlestick/dark/tsla_flag_001.png`

---

## Naming Convention

```
{platform}/{chart_type}/{theme}/{ticker}_{pattern}_{number}.png

Examples:
- tradingview/candlestick/dark/btc_headshoulders_001.png
- metatrader/line/light/eurusd_triangle_002.png
- robinhood/candlestick/dark/spy_doubletop_001.png
```

---

## Quick Start (Minimum Viable Dataset)

**Phase 1 - Get Started (30 screenshots in 1-2 hours):**
1. TradingView only (easiest access)
2. Candlestick charts only
3. 15 dark theme + 15 light theme
4. Mix of 3-5 different patterns
5. Variety of zoom levels

**Phase 2 - Expand (add 40 more):**
1. Add MetaTrader screenshots
2. Add line chart variations
3. Add Robinhood mobile screenshots

**Phase 3 - Complete (add final 30):**
1. Add TD Ameritrade/Webull
2. Add bar charts
3. Add edge cases (very zoomed in/out, custom colors)

---

## Validation Checklist

Before we can proceed to building the extraction pipeline:
- [ ] At least 30 screenshots collected
- [ ] Multiple platforms represented
- [ ] Dark and light themes both present
- [ ] Clear, visible patterns in screenshots
- [ ] No duplicate/redundant screenshots
- [ ] Proper naming convention followed

---

## Next Steps After Collection

Once you have 30+ screenshots:
1. Run validation script to check image quality
2. Annotate ground truth (mark pattern locations)
3. Begin building viewport isolation module
4. Test OHLC extraction accuracy

---

## Tips for Fast Collection

- Use TradingView replay mode to quickly find patterns
- Take multiple screenshots of same pattern (different zoom levels)
- Don't worry about perfect patterns - we need variety
- Mobile screenshots are valid too (Robinhood, Webull apps)
- You can use demo/paper trading accounts (no real money needed)
