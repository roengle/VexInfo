package com.warrenrobotics;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tester {
	public static void main(String[] args) throws Exception{
		//Time runtime of whole program
		long curTime = System.currentTimeMillis();
		//Set event link(s)
		// future event 2020-03-07 to test 4-week lead time
		// String defaultLink = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-19-1124.html";
		String defaultLink = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-19-0201.html"; //Random for test
		//If args isn't empty, use those. Otherwise, default values
		String eventLink = args.length != 0 ? args[0] : defaultLink;

		System.out.printf("%s - Running Program%n", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));

		VexEvent event = VexLoader.loadEvent(eventLink, "");
		SheetsWriter sheetsWriter = new SheetsWriter();
		sheetsWriter.writeVexEvent(event);

		//Time taken
		long timeTaken = System.currentTimeMillis() - curTime; 
		//Print out message
		System.out.printf("Program runtime - %f seconds", ((double)timeTaken/1000));
		System.exit(0);
	}
}