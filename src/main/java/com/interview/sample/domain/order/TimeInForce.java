package com.interview.sample.domain.order;

public enum TimeInForce {

    /**
     * The order remains working until the business date has rolled.
     */
    DAY,

    /**
     * The order remains working until cancelled.
     */
    GOOD_TILL_CANCEL,

    /**
     * The order matches immediately with resting order,
     * then the remaining quantity is cancelled.
     */
    IMMEDIATE_OR_CANCEL,

    /**
     * The order either matches fully with resting order
     * or the order is cancelled.
     */
    FILL_OR_KILL,

    /**
     * The order remains working until a specified business date.
     */
    GOOD_TILL_DATE,
    /**
     * The order remains in the book until it can be fully matched at the specified price.
     */
    ALL_OR_NONE
}