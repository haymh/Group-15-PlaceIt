package com.example.placeit;

// when there is contradiction in schedule, throw this exception
public class ContradictoryScheduleException extends Exception{
	public ContradictoryScheduleException(String problem){
		super(problem);
	}
}
