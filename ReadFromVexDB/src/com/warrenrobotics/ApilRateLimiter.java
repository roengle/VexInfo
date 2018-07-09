package com.warrenrobotics;
/*
 * Credit to Tony BenBrahim on StackOverflow
 * 
 * https://stackoverflow.com/questions/24983679/gmail-api-users-messages-get-throws-user-rate-limit-exceeded?noredirect=1&lq=1
 */
public class ApilRateLimiter {
	//timeSliceEnd is the "per second" part of the measurement
	private long timeSliceEnd;
    private final int quotaPerSecond;
    private int quotaRemaining;

    public ApilRateLimiter(final int quotaPerSecond) {
        this.quotaPerSecond = quotaPerSecond;
        this.quotaRemaining = quotaPerSecond;
        this.timeSliceEnd = System.currentTimeMillis() + 1_000L;
    }

    public void reserve(final int quotaReserved) throws InterruptedException {
        if (quotaReserved > quotaPerSecond) {
            throw new IllegalArgumentException(
                "reservation would never be successful as quota requested is greater than quota per second");
        }           
        final long currentTime = System.currentTimeMillis();
        //If past the "per second"
        if (currentTime >= timeSliceEnd) {
            this.timeSliceEnd = currentTime + 1_000L;
            this.quotaRemaining = quotaPerSecond - quotaReserved;
        } else if (quotaReserved <= quotaRemaining) {
            quotaRemaining -= quotaReserved;
        } else {
            Thread.sleep(timeSliceEnd - currentTime);
            reserve(quotaReserved);
        }
    }
}
