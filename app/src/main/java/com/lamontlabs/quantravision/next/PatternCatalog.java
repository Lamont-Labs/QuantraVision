package com.lamontlabs.quantravision.next;

import java.util.*;

/** Educational metadata for the twelve rules implemented by ChartEngine. */
final class PatternCatalog {
    static final class Pattern {
        final String id,title,family,summary,criteria,limits,source;
        final int color;
        Pattern(String id,String title,String family,String summary,String criteria,String limits,String slug,int color){this.id=id;this.title=title;this.family=family;this.summary=summary;this.criteria=criteria;this.limits=limits;this.source="https://chartschool.stockcharts.com/table-of-contents/chart-analysis/"+slug;this.color=color;}
    }
    static final List<Pattern> ALL=Collections.unmodifiableList(Arrays.asList(
        p("double_top","Double top","Structure","Two peaks. One intervening pullback.","Comparable swing highs separated by at least five candle positions, with a clear pullback between them.","A matching outline does not establish a broken neckline, volume confirmation or future direction.","chart-patterns/double-top-reversal"),
        p("double_bottom","Double bottom","Structure","Two troughs with a recovery between.","Comparable swing lows separated by at least five candle positions, with a clear recovery between them.","The screenshot cannot verify support prices, a confirmed breakout or trading performance.","chart-patterns/double-bottom-reversal"),
        p("head_shoulders","Head & shoulders","Structure","A central peak above two shoulders.","Five alternating swings with a higher central peak, comparable shoulder heights measured from the intervening neckline, which may slope.","The rule checks image geometry. It does not verify a neckline break or the preceding market trend.","chart-patterns/head-and-shoulders-top"),
        p("inverse_head_shoulders","Inverse head & shoulders","Structure","A central trough below two shoulders.","Five alternating swings with a lower central trough and comparable shoulder depths measured from the intervening neckline, which may slope.","Market context, volume and a confirmed reversal remain unknown.","chart-patterns/head-and-shoulders-bottom"),
        p("ascending_triangle","Ascending triangle","Structure","Level highs meet rising lows.","At least two swing highs and lows. The upper boundary is approximately level and the lower boundary rises.","The engine fits visible boundaries; it does not predict breakout direction.","chart-patterns/ascending-triangle"),
        p("descending_triangle","Descending triangle","Structure","Falling highs meet level lows.","At least two swing highs and lows. The lower boundary is approximately level and the upper boundary falls.","No volume, market price or breakout confirmation is available.","chart-patterns/descending-triangle"),
        p("symmetrical_triangle","Symmetrical triangle","Structure","Two boundaries converge.","Falling swing highs and rising swing lows form a narrowing range without crossing.","A narrowing outline alone does not predict which boundary will break.","chart-patterns/symmetrical-triangle"),
        p("doji","Doji","Candle","A small body within a longer range.","The body occupies at most 15% of the measured candle range, which must be at least 12 image pixels.","Resolution changes the apparent shape. Trend context is required for interpretation.","candlestick-charts/candlestick-pattern-dictionary"),
        p("hammer","Long lower wick","Candle","A small body near the range high.","Lower wick at least twice the body height; the upper wick is small relative to the body.","This shape is not called a confirmed hammer reversal without a preceding downtrend and confirmation.","candlestick-charts/candlestick-pattern-dictionary"),
        p("shooting_star","Long upper wick","Candle","A small body near the range low.","Upper wick at least twice the body height; the lower wick is small relative to the body.","A long upper wick alone is not a confirmed shooting-star reversal.","candlestick-charts/candlestick-pattern-dictionary"),
        p("bullish_engulfing","Bullish engulfing body","Candle","An upward body contains a downward body.","Opposite body colors; the current upward body contains the preceding body and is more than 15% taller.","Direction comes from supported body colors. Gaps, volume and trend context are not inferred.","candlestick-charts/candlestick-pattern-dictionary"),
        p("bearish_engulfing","Bearish engulfing body","Candle","A downward body contains an upward body.","Opposite body colors; the current downward body contains the preceding body and is more than 15% taller.","This checks visible bodies, not a complete market-data trading signal.","candlestick-charts/candlestick-pattern-dictionary")
    ));
    private static Pattern p(String id,String title,String family,String summary,String criteria,String limits,String slug){return new Pattern(id,title,family,summary,criteria,limits,slug,"Candle".equals(family)?Ui.TEAL:Ui.LIME);}
    static Pattern get(String id){for(Pattern p:ALL)if(p.id.equals(id))return p;return null;}
    static String key(String matchId){int last=matchId.lastIndexOf('_'),before=last<0?-1:matchId.lastIndexOf('_',last-1);return before<0?matchId:matchId.substring(0,before);}
}
