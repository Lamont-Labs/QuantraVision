# QuantraVision Pattern Catalog

**Total Patterns: 109**
- **Reversal Patterns**: 41
- **Continuation Patterns**: 35  
- **Candlestick Patterns**: 33

All patterns include comprehensive YAML configuration with:
- Pattern name
- Template image path
- Match confidence threshold (0.65-0.85)
- Scale range for multi-scale detection
- Scale stride for pyramid search
- Aspect tolerance (where applicable)
- Timeframe hints
- Minimum bars required

---

## Reversal Patterns (41)

1. Head_and_Shoulders
2. Inverse_Head_and_Shoulders
3. Double_Top
4. Double_Bottom
5. Triple_Top
6. Triple_Bottom
7. Rounding_Top
8. Rounding_Bottom
9. V_Top
10. V_Bottom
11. Diamond_Top
12. Diamond_Bottom
13. Broadening_Top
14. Broadening_Bottom
15. Island_Reversal_Top
16. Island_Reversal_Bottom
17. Adam_Eve_Double_Top
18. Eve_Adam_Double_Top
19. Adam_Adam_Double_Top
20. Eve_Eve_Double_Top
21. Adam_Eve_Double_Bottom
22. Eve_Adam_Double_Bottom
23. Adam_Adam_Double_Bottom
24. Eve_Eve_Double_Bottom
25. Saucer_Top
26. Saucer_Bottom
27. Spike_and_Channel_Reversal
28. Bump_and_Run_Reversal_Top
29. Bump_and_Run_Reversal_Bottom
30. Complex_Head_and_Shoulders
31. Complex_Inverse_Head_and_Shoulders
32. Megaphone_Top
33. Megaphone_Bottom
34. Key_Reversal_Up
35. Key_Reversal_Down
36. Pipe_Top
37. Pipe_Bottom
38. Two_Bar_Reversal_Up
39. Two_Bar_Reversal_Down
40. Horn_Top
41. Horn_Bottom

## Continuation Patterns (35)

1. Ascending_Triangle
2. Descending_Triangle
3. Symmetrical_Triangle
4. Rising_Wedge
5. Falling_Wedge
6. Bull_Flag
7. Bear_Flag
8. Bull_Pennant
9. Bear_Pennant
10. Rectangle_Bullish
11. Rectangle_Bearish
12. Cup_and_Handle
13. Inverted_Cup_and_Handle
14. Ascending_Channel
15. Descending_Channel
16. Horizontal_Channel
17. Measured_Move_Up
18. Measured_Move_Down
19. Three_Drives_Pattern_Bullish
20. Three_Drives_Pattern_Bearish
21. Scallop_Bullish
22. Scallop_Bearish
23. Flag_Formation_High_Tight
24. Pennant_Formation_High_Tight
25. Consolidation_Box
26. Trading_Range
27. Continuation_Diamond
28. Ladder_Bottom
29. Ladder_Top
30. Bull_Trap_Continuation
31. Bear_Trap_Continuation
32. Rectangle_Top_Continuation
33. Rectangle_Bottom_Continuation
34. Parallel_Channel_Up
35. Parallel_Channel_Down

## Candlestick Patterns (33)

1. Doji
2. Hammer
3. Hanging_Man
4. Inverted_Hammer
5. Shooting_Star
6. Bullish_Engulfing
7. Bearish_Engulfing
8. Morning_Star
9. Evening_Star
10. Three_White_Soldiers
11. Three_Black_Crows
12. Piercing_Line
13. Dark_Cloud_Cover
14. Harami_Bullish
15. Harami_Bearish
16. Tweezer_Top
17. Tweezer_Bottom
18. Spinning_Top
19. Marubozu_Bullish
20. Marubozu_Bearish
21. Three_Inside_Up
22. Three_Inside_Down
23. Three_Outside_Up
24. Three_Outside_Down
25. Abandoned_Baby_Bullish
26. Abandoned_Baby_Bearish
27. Kicker_Bullish
28. Kicker_Bearish
29. Belt_Hold_Bullish
30. Belt_Hold_Bearish
31. Upside_Gap_Two_Crows
32. Downside_Tasuki_Gap
33. Upside_Tasuki_Gap

---

## File Structure

Each pattern has two files:
- `[pattern_name].yaml` - Configuration with detection parameters
- `[pattern_name]_ref.png` - Grayscale template image (200-400px)

## YAML Schema

```yaml
name: Pattern Display Name
image: pattern_templates/[pattern_name]_ref.png
threshold: 0.70-0.85
scale_range:
- min_scale (0.6-0.8)
- max_scale (1.3-1.6)
scale_stride: 0.10-0.20
timeframe_hints:
- 5m
- 15m
- 1h
- 4h
- 1d
min_bars: 1-30
aspect_tolerance: 0.15-0.30 (optional)
```

## Template Images

All template images are:
- Grayscale PNG format
- 200-400 pixels in size
- Clean, simple visualizations
- Optimized for template matching
- Generated using matplotlib

---

Generated: October 30, 2025
QuantraVision Pattern Detection System
