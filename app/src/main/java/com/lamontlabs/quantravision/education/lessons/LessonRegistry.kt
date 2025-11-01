package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson

object LessonRegistry {
    
    fun getAllLessons(): List<Lesson> = listOf(
        lesson01Intro,
        lesson02HeadShoulders,
        lesson03DoubleTopBottom,
        lesson04Triangles,
        lesson05FlagsPennants,
        lesson06Wedges,
        lesson07Rectangles,
        lesson08CupHandle,
        lesson09RoundingBottomTop,
        lesson10Diamond,
        lesson11CandlestickBasics,
        lesson12DojiSpinningTops,
        lesson13HammerHangingMan,
        lesson14EngulfingPatterns,
        lesson15MorningEveningStar,
        lesson16ThreeWhiteBlackCrows,
        lesson17SupportResistance,
        lesson18TrendlinesChannels,
        lesson19FibonacciRetracements,
        lesson20VolumeAnalysis,
        lesson21RiskManagement,
        lesson22PositionSizing,
        lesson23TradePsychology,
        lesson24BacktestingStrategies,
        lesson25TradingPlan
    )
}
