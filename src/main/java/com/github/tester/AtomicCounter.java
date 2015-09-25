package com.github.tester;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter {
	private AtomicInteger value;
	
    public AtomicCounter(int start) {
        value = new AtomicInteger(start);
    }
    
	public int getValue() {
		return value.get();
	}
	
	public int getNextValue() {
		return value.incrementAndGet();
	}
	
	public int getPrevousValue() {
		return value.decrementAndGet();
	}
}
