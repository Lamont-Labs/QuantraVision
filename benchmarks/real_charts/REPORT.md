# External chart development evaluation

Small selected development set: 9 source charts, correlated full-image and crop cases, and publisher-provided pattern names. Labels and regions were transcribed by the implementation agent; there was no independent adjudication. The engine has been adjusted using these sources. This is not a held-out test or a general accuracy estimate. OHLC and line images use explicit experimental trace mode only in the cropped cases. The baseline has no trace reader. Extra detections on positive images are unscored because annotations are not exhaustive. Doji, long upper wick and both engulfing rules have no external positive targets. Three negative cases do not establish a general false-positive rate. No future-return or trading-performance claim is made.

| Case | Format / reader | Outcome | Baseline |
|---|---|---|---|
| [Ford Double Top · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/double-top-reversal) | OHLC bars / candles | missed | missed |
| [Ford Double Top · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/double-top-reversal) | OHLC bars / trace | missed | unsupported_mode |
| [Pfe Double Bottom · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/double-bottom-reversal) | OHLC bars / candles | missed | missed |
| [Pfe Double Bottom · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/double-bottom-reversal) | OHLC bars / trace | missed | unsupported_mode |
| [Adm Head Shoulders · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/head-and-shoulders-top) | line / candles | missed | missed |
| [Adm Head Shoulders · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/head-and-shoulders-top) | line / trace | missed | unsupported_mode |
| [Frx Inverse Head Shoulders · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/head-and-shoulders-bottom) | OHLC bars / candles | missed | missed |
| [Frx Inverse Head Shoulders · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/head-and-shoulders-bottom) | OHLC bars / trace | missed | unsupported_mode |
| [Prtl Ascending Triangle · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/ascending-triangle) | OHLC bars / candles | missed | missed |
| [Prtl Ascending Triangle · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/ascending-triangle) | OHLC bars / trace | matched | unsupported_mode |
| [Nue Descending Triangle · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/descending-triangle) | OHLC bars / candles | missed | missed |
| [Nue Descending Triangle · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/descending-triangle) | OHLC bars / trace | matched | unsupported_mode |
| [Cnc Symmetrical Triangle · full](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/symmetrical-triangle) | OHLC bars / candles | missed | missed |
| [Cnc Symmetrical Triangle · plot](https://chartschool.stockcharts.com/table-of-contents/chart-analysis/chart-patterns/symmetrical-triangle) | OHLC bars / trace | missed | unsupported_mode |
| [ONCS lower-wick example · full](https://commons.wikimedia.org/wiki/File:Hammer_candlestick_pattern_example.png) | filled candles / candles | matched | missed |
| [ONCS lower-wick example · plot](https://commons.wikimedia.org/wiki/File:Hammer_candlestick_pattern_example.png) | filled candles / candles | matched | missed |
| [NIFTY · whole chart readability](https://commons.wikimedia.org/wiki/File:Qtstalker_candlestick_chart.png) | hollow candles / candles | readable | unreadable |
| [NIFTY monotonic segment · candles](https://commons.wikimedia.org/wiki/File:Qtstalker_candlestick_chart.png) | hollow candles / candles | negative_pass | negative_pass |
| [NIFTY monotonic segment · trace](https://commons.wikimedia.org/wiki/File:Qtstalker_candlestick_chart.png) | hollow candles / trace | negative_pass | unsupported_mode |
| [Application chrome · not a chart](https://commons.wikimedia.org/wiki/File:Qtstalker_candlestick_chart.png) | hollow candles / candles | negative_pass | negative_pass |

## Recorded counts

```json
{
  "sourceCharts": 9,
  "cases": 20,
  "positiveTargets": 16,
  "matchedTargets": 4,
  "missedTargets": 12,
  "negativePatternChecks": 3,
  "negativeReadChecks": 1,
  "forbiddenPatternFlags": 0,
  "falseReadableInputs": 0,
  "baselineMatchedTargets": 0,
  "baselineUnsupportedTargets": 7
}
```

## Reproduce

`python3 scripts/evaluate-real-charts.py --check`

Requires Python 3 + Pillow and JDK 17. Hash-verified publisher inputs are fetched into an ignored local cache, not redistributed. Full/cropped cases are correlated. A matching key must overlap the frozen target region; merely finding the same rule elsewhere does not score a hit. Source bytes, crops, labels and regions are recorded in manifest.json. The baseline is the exact shipped e07dee4 engine and lacks trace mode.
