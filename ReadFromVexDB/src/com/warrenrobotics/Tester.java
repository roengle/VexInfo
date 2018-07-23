package com.warrenrobotics;

public class Tester {
	public static void main(String[] args) throws Exception{
		//Set the spreadsheet ID
		//String spreadsheetId = "1pi_SqomCHotiRWv6dL-mqs-ER7nel2_t3sTlOwYAiqM";
		//Essentially grab all data and set it(constructor body calls get and set methods)
		//Set event link(s)
		String eventLink = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-3805.html"; //Worlds 2018
		String eventLink2 = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-4583.html"; //States - Bakersfield 2018
		@SuppressWarnings("unused")
		TeamAPI api = new TeamAPI(eventLink2);
	}
}
