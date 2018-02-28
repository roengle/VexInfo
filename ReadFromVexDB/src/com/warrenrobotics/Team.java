package com.warrenrobotics;
	
import org.json.*;
/**
 * This class allows for team statistics to be parsed and stored. Current
 * statistics include average OPR, average DPR, average CCWM, average max 
 * score, average rank(from all tournaments), and best rank(from one tournament).
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.0
 * @since 2018-02-21
 *
 */
public class Team {
	//Instance variables
	public String name;
	public JSONArray tData;
	private double avgOPR;
	private double avgDPR;
	private double avgCCWM;
	private int avgMaxScore;
	private int avgRank;
	private int bestRank;
	
	/**
	 * Constructs a Team object and runs all necessary calculations
	 * 
	 * @param name the name of the team(IE: 90241B)
	 * @param tData the JSONarray acquired from getting JSON array with key "result"
	 */
	public Team(String name, JSONArray tData){
		this.name = name;
		this.tData = tData;
		calculateAvgOPR();
		calculateAvgDPR();
		calculateAvgCCWM();
		calculateAvgMaxScore();
		calculateRanks();
	}
	
	//Instance methods
	/**
	 * Calculates the average OPR and sets the classes instance variable to it
	 */
	void calculateAvgOPR() {
		double totalOPR = 0.0;
		//Break up array, and search for OPR in each part
		for(int i = 0; i < tData.length(); i++) {
			//Grab value
			double opr = tData.getJSONObject(i).getDouble("opr");
			//Add to total
			totalOPR += opr;
		}
		
		avgOPR = (totalOPR + 0.0) / tData.length();
	}
	
	/**
	 * Calculates the average DPR and sets the classes instance variable to it
	 */
	void calculateAvgDPR() {
		double totalDPR = 0.0;
		//Break up array, and search for DPR in each part
		for(int i = 0; i < tData.length(); i++) {
			//Grab value
			double dpr = tData.getJSONObject(i).getDouble("dpr");
			//Add to total
			totalDPR += dpr;
		}
		//Compute average
		avgDPR = (totalDPR + 0.0) / tData.length();
	}
	
	/**
	 * Calculates the average CCWM and sets the classes instance variable to it
	 */
	void calculateAvgCCWM() {
		double totalCCWM = 0.0;
		//Break up array, and search for DPR in each part
		for(int i = 0; i < tData.length(); i++) {
			//Grab value
			double ccwm = tData.getJSONObject(i).getDouble("ccwm");
			//Add to total
			totalCCWM += ccwm;
		}
		//Compute average
		avgCCWM = (totalCCWM + 0.0) / tData.length();
	}
	
	/**
	 * Calculates the average max score and sets the classes instance variable to it
	 */
	void calculateAvgMaxScore(){
		int totalScore = 0;
		//Break up array, and search for average max score in each part
		for(int i = 0; i < tData.length(); i++){
			//Grab value
			int maxScore = tData.getJSONObject(i).getInt("max_score");
			//Add to total
			totalScore += maxScore;
		}
		//Compute average
		avgMaxScore = (int)totalScore / tData.length();
	}
	
	/**
	 * Parses through the teams ranks, and sets both the highest ranking and the 
	 * average ranking.
	 */
	void calculateRanks(){
		int totalRank = 0;
		int bestSoFar;
		/* Break up array, and search though each ranking. For the average, follow
		 * the same procedure above. For best rank, simply set the first rank as the 
		 * best and do comparisons to find the LEAST value.
		 */
		//Set the best-so-far ranking as a reference, this will allow for the least
		//value to be set at the end(even if it is the first)
		bestSoFar = tData.getJSONObject(0).getInt("rank");
		for(int i = 0; i < tData.length(); i++) {
			//Grab value
			int rank = tData.getJSONObject(i).getInt("rank");
			//Make comparison for ranking
			if(rank < bestSoFar) {
				bestSoFar = rank;
			}
			//Add to total
			totalRank += rank;
		}
		//Set best rank
		bestRank = bestSoFar;
		//Set average rank
		avgRank = (int)totalRank/tData.length();
	}
	/**
	 * Retrieves the average OPR for select team(average of all matches in season)
	 * 
	 * @return the average OPR of the team
	 */
	double getAvgOPR() { return avgOPR; }
	
	/**
	 * Retrieves the average DPR for select team(average of all matches in season)
	 * 
	 * @return the average DPR of the team
	 */
	double getAvgDPR() { return avgDPR; }
	
	/**
	 * Retrieves the average DPR for select team(average of all matches in season)
	 * 
	 * @return the average CCWM of the team
	 */
	double getAvgCCWM() { return avgCCWM; }
	
	/**
	 * Retrieves the average max score for select team(of all matches in season)
	 * 
	 * @return the average max score of the team
	 */
	int getAvgMaxScore() { return avgMaxScore; }
	
	/**
	 * Retrieves the best rank that a team has achieved throughout the season
	 * 
	 * @return the best rank of the team
	 */
	int getBestRank() { return bestRank; }
	
	/**
	 * Retrieves the average rank that a team has achieved throughout the season
	 * 
	 * @return the average rank of the team
	 */
	
	int getAvgRank() { return avgRank; }
}