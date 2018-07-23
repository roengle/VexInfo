package com.warrenrobotics;

public class Tester {
	public static void main(String[] args) throws Exception{
		//Set the spreadsheet ID
		//String spreadsheetId = "1pi_SqomCHotiRWv6dL-mqs-ER7nel2_t3sTlOwYAiqM";
		//Essentially grab all data and set it(constructor body calls get and set methods)
		String eventLink = "https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-17-3805.html";
		@SuppressWarnings("unused")
		TeamAPI api = new TeamAPI(eventLink);
	}
}
