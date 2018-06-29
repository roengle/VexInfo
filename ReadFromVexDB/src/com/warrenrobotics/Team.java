package com.warrenrobotics;
	
import org.json.*;
/**
 * This class allows for team statistics to be parsed and stored. Current
 * statistics include average OPR, average DPR, average CCWM, average max 
 * score, average rank(from all tournaments), and best rank(from one tournament).
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.1
 * @since 2018-02-21
 *
 */
public class Team {
	/*Instance variables*/
	//Name
	public String name;
	//JSON Array Data
	public JSONArray tData_rankings;
	public JSONObject tData_events;
	public JSONArray tData_season_rankings;
	public JSONArray tData_skills;
	//Data - Rankings
	public double avgOPR;
	public double avgDPR;
	public double avgCCWM;
	public int avgMaxScore;
	public int avgRank;
	public int bestRank;
	public int avgAP;
	public int avgSP;
	public int avgTRSP;
	//Data - Events
	private int numEvents;
	//Data - Season Rankings
	/* Vrating is a custom ranking method developed by Team BNS using a wide variety of different metrics to guage a team
	   A higher vrating represents a better team
	*/
	private int vrating_rank;
	private double vrating;
	//Data - Skills
	//TODO: Fill in skills fields
	/**
	 * Constructs a Team object and runs all necessary calculations to compile statisics
	 * 
	 * @param name the name of the team(IE: 90241B)
	 * @param tData_rankings the JSONarray acquired from getting JSON array with key "result"
	 */
	public Team(String name, JSONArray tData_rankings,JSONObject tData_events, JSONArray tData_season_rankings, JSONArray tData_skills){
		//Set data
		this.name = name;
		this.tData_rankings = tData_rankings;
		this.tData_events = tData_events;
		this.tData_season_rankings = tData_season_rankings;
		this.tData_skills = tData_skills;
		//Perform calculations
		performCalculations_rankings();
		performCalculations_events();
		performCalculations_season_rankings();
	}
	
	//Instance methods
	
	/**
	 * Performs all calculations under the "rankings" category
	 */
	private void performCalculations_rankings() {
		calculateAvgOPR();
		calculateAvgDPR();
		calculateAvgCCWM();
		calculateAvgMaxScore();
		calculateRanks();
		calculateAvgAP();
		calculateAvgSP();
		calculateAvgTRSP();
		
	}
	
	/**
	 * Performs all calculations under the "events" category
	 */
	private void performCalculations_events() {
		setNumEvents();
	}
	
	/**
	 * Performs all calculations under the "season_rankings" category
	 */
	private void performCalculations_season_rankings() {
		setvrating_rank();
		setvrating();
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									RANKING CALCULATIONS								//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Calculates the average OPR and sets the classes instance variable to it
	 */
	private void calculateAvgOPR() {
		double totalOPR = 0.0;
		//Break up array, and search for OPR in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			double opr = tData_rankings.getJSONObject(i).getDouble("opr");
			//Add to total
			totalOPR += opr;
		}
		
		this.avgOPR = (totalOPR + 0.0) / tData_rankings.length();
	}
	
	/**
	 * Calculates the average DPR and sets the classes instance variable to it
	 */
	private void calculateAvgDPR() {
		double totalDPR = 0.0;
		//Break up array, and search for DPR in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			double dpr = tData_rankings.getJSONObject(i).getDouble("dpr");
			//Add to total
			totalDPR += dpr;
		}
		//Compute average
		this.avgDPR = (totalDPR + 0.0) / tData_rankings.length();
	}
	
	/**
	 * Calculates the average CCWM and sets the classes instance variable to it
	 */
	private void calculateAvgCCWM() {
		double totalCCWM = 0.0;
		//Break up array, and search for DPR in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			double ccwm = tData_rankings.getJSONObject(i).getDouble("ccwm");
			//Add to total
			totalCCWM += ccwm;
		}
		//Compute average
		this.avgCCWM = (totalCCWM + 0.0) / tData_rankings.length();
	}
	
	/**
	 * Calculates the average max score and sets the classes instance variable to it
	 */
	private void calculateAvgMaxScore(){
		int totalScore = 0;
		//Break up array, and search for max score in each part
		for(int i = 0; i < tData_rankings.length(); i++){
			//Grab value
			int maxScore = tData_rankings.getJSONObject(i).getInt("max_score");
			//Add to total
			totalScore += maxScore;
		}
		//Compute average
		this.avgMaxScore = (int)totalScore / tData_rankings.length();
	}
	
	/**
	 * Parses through the teams ranks, and sets both the highest ranking and the 
	 * average ranking.
	 * 
	 * NOTE:Best ranking WON'T appear in spreadsheet
	 */
	private void calculateRanks(){
		int totalRank = 0;
		int bestSoFar;
		/* Break up array, and search though each ranking. For the average, follow
		 * the same procedure above. For best rank, simply set the first rank as the 
		 * best and do comparisons to find the LEAST value.
		 */
		//Set the best-so-far ranking as a reference, this will allow for the least
		//value to be set at the end(even if it is the first)
		bestSoFar = tData_rankings.getJSONObject(0).getInt("rank");
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int rank = tData_rankings.getJSONObject(i).getInt("rank");
			//Make comparison for ranking
			if(rank < bestSoFar) {
				bestSoFar = rank;
			}
			//Add to total
			totalRank += rank;
		}
		//Set best rank
		this.bestRank = bestSoFar;
		//Set average rank
		this.avgRank = (int)totalRank/tData_rankings.length();
	}
	/**
	 * Calculates the average autonomous points for a team
	 */
	private void calculateAvgAP() {
		int totalAP = 0;
		//Break up array, and search for APs in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int AP = tData_rankings.getJSONObject(i).getInt("ap");
			//Add to total
			totalAP += AP;
		}
		//Compute average by dividing total by length
		this.avgAP = (int)totalAP / tData_rankings.length();
	}
	
	/**
	 * Calculates the average skills points for a team
	 */
	private void calculateAvgSP() {
		int totalSP = 0;
		//Break up array, and search for SPs in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int SP = tData_rankings.getJSONObject(i).getInt("sp");
			//Add to total
			totalSP += SP;
		}
		//Compute average by dividing total by length
		this.avgSP = (int)totalSP / tData_rankings.length();
	}
	
	/**
	 * Calculates the average TRSPs (custom ranking method for SPs) for a team
	 */
	private void calculateAvgTRSP() {
		int totalTRSP = 0;
		//Break up array, and search for SPs in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int TRSP = tData_rankings.getJSONObject(i).getInt("trsp");
			//Add to total
			totalTRSP += TRSP;
		}
		//Compute average by dividing total by length
		this.avgSP = (int)totalTRSP / tData_rankings.length();
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//								   EVENTS CALCULATIONS								    //
	//																						//
	------------------------------------------------------------------------------------------
	*/
	/**
	 * Sets the total number of events a team has competed in within the season
	 */
	private void setNumEvents() { this.numEvents = tData_events.getInt("size"); }
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//								SEASON RANKING CALCULATIONS								//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	/**
	 * Sets the team's vrating rank
	 */
	private void setvrating_rank() { this.vrating_rank = tData_season_rankings.getJSONObject(0).getInt("vrating_rank"); }
	/**
	 * Set's the team's vrating
	 */
	private void setvrating() { this.vrating = tData_season_rankings.getJSONObject(0).getDouble("vrating"); }
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									  SKILLS CALCULATIONS								//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	//TODO: Fill in skills methods
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									   GETTER METHODS								    //
	//																						//
	------------------------------------------------------------------------------------------
	*/
	/**
	 * Retrieves the average OPR for select team(average of all matches in season)
	 * 
	 * @return the average OPR of the team
	 */
	public double getAvgOPR() { return avgOPR; }
	
	/**
	 * Retrieves the average DPR for select team(average of all matches in season)
	 * 
	 * @return the average DPR of the team
	 */
	public double getAvgDPR() { return avgDPR; }
	
	/**
	 * Retrieves the average DPR for select team(average of all matches in season)
	 * 
	 * @return the average CCWM of the team
	 */
	public double getAvgCCWM() { return avgCCWM; }
	
	/**
	 * Retrieves the average max score for select team(of all matches in season)
	 * 
	 * @return the average max score of the team
	 */
	public int getAvgMaxScore() { return avgMaxScore; }
	
	/**
	 * Retrieves the best rank that a team has achieved throughout the season
	 * 
	 * @return the best rank of the team
	 */
	public int getBestRank() { return bestRank; }
	
	/**
	 * Retrieves the average rank that a team has achieved throughout the season
	 * NOTE:Best ranking WON'T appear in spreadsheet
	 * 
	 * @return the average rank of the team
	 */
	public int getAvgRank() { return avgRank; }
	
	/**
	 * Retrieves average autonomous points for a team
	 * 
	 * @return a rounded-down integer of the average autonomous points
	 */
	public int getAvgAP() { return this.avgAP; }
	
	/**
	 * Retrieves average skills points for a team
	 * 
	 * @return a rounded-down integer of the average skills points
	 */
	public int getAvgSP() { return this.avgSP; }
	
	/**
	 * Retrieves average TRSP points for a team
	 * 
	 * @return a rounded-down integer of the average TRSP points
	 */
	public int getAvgTRSP() { return this.avgTRSP; }
	
	/**
	 * Retrieves the number of events a team has competed in during the season
	 * 
	 * @return the number of events
	 */
	public int getNumEvents() { return this.numEvents; }
	
	/**
	 * Retrieves the vrating rankings of a team in the season
	 * 
	 * @return the vrating ranking
	 */
	public int getvrating_rank() { return this.vrating_rank; }
	
	/**
	 * Retrieves the vrating of a team in the season
	 * 
	 * @return the vrating
	 */
	public double getvrating() { return this.vrating; }
}