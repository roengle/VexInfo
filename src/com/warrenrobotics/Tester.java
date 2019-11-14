package com.warrenrobotics;

public class Tester {
	public static void main(String[] args) throws Exception{
		//Time runtime of whole program
		long curTime = System.currentTimeMillis();
		//Set event link(s)
		String eventLink = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-18-5915.html"; //Random for test
		//Essentially grab all data and set it(constructor body calls create and write methods)
		@SuppressWarnings("unused")
		//If args isn't empty, use those. Otherwise, default values
		TeamAPI api = args.length != 0 ? new TeamAPI(args[0]) : new TeamAPI(eventLink);
		//Time taken
		long timeTaken = System.currentTimeMillis() - curTime; 
		//Print out message
		System.out.printf("Program runtime - %f seconds", ((double)timeTaken/1000));
		System.exit(0);
	}
}
