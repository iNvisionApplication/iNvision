package com.invision.web.Invision.enums;


public enum LoanPeriod {
    ONE_WEEK(7),
    TWO_WEEKS(14),
    THREE_WEEKS(21),
    FOUR_WEEKS(28);

    private final int days;

    LoanPeriod(int days) { this.days = days; }
    public int getDays() { return days; }
}
