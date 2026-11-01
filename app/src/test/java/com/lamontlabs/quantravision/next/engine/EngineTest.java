package com.lamontlabs.quantravision.next.engine;
import org.junit.Test;
public final class EngineTest {
    @Test public void darkChart(){EngineChecks.darkChart();}
    @Test public void lightChart(){EngineChecks.lightChart();}
    @Test public void darkNeutralWicks(){EngineChecks.darkNeutralWicks();}
    @Test public void lightNeutralWicks(){EngineChecks.lightNeutralWicks();}
    @Test public void blankRejected(){EngineChecks.blankRejected();}
    @Test public void tooSmallRejected(){EngineChecks.tooSmallRejected();}
    @Test public void badDimensions(){EngineChecks.badDimensions();}
    @Test public void transparentRejected(){EngineChecks.transparentRejected();}
    @Test public void unsupportedPalette(){EngineChecks.unsupportedPalette();}
    @Test public void noiseRejected(){EngineChecks.noiseRejected();}
    @Test public void volumeRejected(){EngineChecks.volumeRejected();}
    @Test public void measuredBodies(){EngineChecks.measuredBodies();}
    @Test public void boundsRemainInImage(){EngineChecks.boundsRemainInImage();}
    @Test public void localizesKnownPeaks(){EngineChecks.localizesKnownPeaks();}
    @Test public void cancellationBeforePixels(){EngineChecks.cancellationBeforePixels();}
    @Test public void cancellationDuringPixels(){EngineChecks.cancellationDuringPixels();}
    @Test public void doubleBottom(){EngineChecks.doubleBottom();}
    @Test public void headShoulders(){EngineChecks.headShoulders();}
    @Test public void inverseHeadShoulders(){EngineChecks.inverseHeadShoulders();}
    @Test public void trendIsNotDoubleTop(){EngineChecks.trendIsNotDoubleTop();}
    @Test public void doji(){EngineChecks.doji();}
    @Test public void engulfing(){EngineChecks.engulfing();}
    @Test public void invalidCandleRejected(){EngineChecks.invalidCandleRejected();}
    @Test public void observationsImmutable(){EngineChecks.observationsImmutable();}
    @Test public void lowerWick(){EngineChecks.lowerWick();}
    @Test public void upperWick(){EngineChecks.upperWick();}
    @Test public void bearishEngulfing(){EngineChecks.bearishEngulfing();}
    @Test public void ascendingTriangle(){EngineChecks.ascendingTriangle();}
    @Test public void descendingTriangle(){EngineChecks.descendingTriangle();}
    @Test public void symmetricalTriangle(){EngineChecks.symmetricalTriangle();}
    @Test public void scaledGrayScreen(){EngineChecks.scaledGrayScreen();}
    @Test public void hollowDark(){EngineChecks.hollowDark();}
    @Test public void hollowLight(){EngineChecks.hollowLight();}
    @Test public void shortWideCandles(){EngineChecks.shortWideCandles();}
    @Test public void neutralOutlinedBody(){EngineChecks.neutralOutlinedBody();}
    @Test public void labelClustersRejected(){EngineChecks.labelClustersRejected();}
    @Test public void olderVisibleCandle(){EngineChecks.olderVisibleCandle();}
    @Test public void traceDoubleTop(){EngineChecks.traceDoubleTop();}
    @Test public void traceBlankRejected(){EngineChecks.traceBlankRejected();}
    @Test public void traceNoiseRejected(){EngineChecks.traceNoiseRejected();}
    @Test public void traceCancellation(){EngineChecks.traceCancellation();}
    @Test public void traceObservationsImmutable(){EngineChecks.traceObservationsImmutable();}
}
