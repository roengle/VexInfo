package com.warrenrobotics;

public class Tester {
	public static void main(String[] args) throws Exception{
		//Time runtime of whole program
		long curTime = System.currentTimeMillis();
		//Set event link(s)
		String eventLink = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-3805.html"; //Worlds 2018
		String eventLink2 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-4583.html"; //Central California States 2018
		String eventLink3 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-4125.html"; //McBride HS #1 2017-2018
		String eventLink4 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-4185.html"; //VRC Howl in the Hills
		String eventLink5 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-18-5915.html"; //El Camino Turning Point
		String eventLink6 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-18-5481.html"; //First for 2018-2019
		String eventLink7 = "https://www.robotevents.com/robot-competitions/vex-iq-challenge/RE-VIQC-18-7854.html"; //Tester for IQ
		String eventLink8 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-18-7675.html"; //2019 CA State HS San Diego
		String eventLink9 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-18-6082.html"; //Worlds 2019
		//Set email for user to transfer ownership to
		String usrEmail = "iragequitwup123zz@gmail.com";
		//Essentially grab all data and set it(constructor body calls create and write methods)
		@SuppressWarnings("unused")
		//If args isn't empty, use those. Otherwise, default values
		TeamAPI api = args.length != 0 ? new TeamAPI(args[0], args[1]) : new TeamAPI(eventLink5, usrEmail);
		//Time taken
		long timeTaken = System.currentTimeMillis() - curTime; 
		//Print out message
		System.out.printf("Program runtime - %f seconds", ((double)timeTaken/1000));
		System.exit(0);
	}
}
