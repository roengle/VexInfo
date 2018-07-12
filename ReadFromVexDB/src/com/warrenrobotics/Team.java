package com.warrenrobotics;
	
//Used in Team class
import org.json.*;
import java.util.HashMap;
import java.util.Map;
//Use in TeamBuilder class
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
/**
 * This class allows for team statistics to be parsed and stored. 
 * 
 * Current statistics:
 * 		Average OPR
 * 		Average DPR
 * 		Average CCRWM
 * 		Average Max Score
 * 		Average Ranking
 * 		Average Autonomous Points
 * 		Average Skills Points
 * 		Average TRSP Points
 * 		Total events in season
 * 		Vrating rank(Custom ranking system developed by Team BNS)
 * 		Vrating(Custom ranking system developed by Team BNS)
 * 		Average Skills Score - Autonomous
 * 		Average Skills Score - Robot
 *		Average Skills Score - Combined
 * 
 * This class is responsible for calculating and storing statistics for a given VEX team.
 * 
 * The TeamBuilder class(line 690) is responsible for building a Team object.
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.1
 * @since 2018-02-21
 *
 */

public class Team {
	/*
	 Fields for team class
	*/
	//JSON Array Data
	private JSONArray tData_teams;
	private JSONArray tData_rankings;
	private JSONObject tData_events; //Is not an array since the only needed piece of data can be acquired from "size"
	private JSONArray tData_season_rankings;
	public JSONArray tData_skills;
	//Data - Teams
	public String number; //IE: 90241B
	public String teamName; //IE: Warren WarBots II 
	public String teamOrg;
	public String city;
	public String state;
	public String country;
	//Data - Rankings
	public double avgOPR;
	public double avgDPR;
	public double avgCCWM;
	public int avgMaxScore;
	public int avgRank;
	public int avgAP;
	public int avgSP;
	public int avgTRSP;
	//Data - Events
	public int numEvents;
	//Data - Season Rankings
	/* Vrating is a custom ranking method developed by Team BNS using a wide variety of different metrics to gauge a team
	   A higher vrating represents a better team
	*/
	public int vrating_rank;
	public double vrating;
	//Data - Skills
	public int avgSkillsScore_robot;
	public int avgSkillsScore_auton;
	public int avgSkillsScore_combined;
	
	//Use static initializer to prevent having to fill with values for every iteration
	public static Map<String, Boolean> fieldIndicators = new HashMap<>();
	static {
		//Initialize all to true, then when checking, set to false if needed
		fieldIndicators.put("opr", true);
		fieldIndicators.put("dpr", true);
		fieldIndicators.put("ccwm", true);
		fieldIndicators.put("max_score", true);
		fieldIndicators.put("rank", true);
		fieldIndicators.put("ap", true);
		fieldIndicators.put("sp", true);
		fieldIndicators.put("trsp", true);
		fieldIndicators.put("skills_auton", true);
		fieldIndicators.put("skills_robot", true);
		fieldIndicators.put("skills_combined", true);
		fieldIndicators.put("vrating_rank", true);
		fieldIndicators.put("vrating", true);
	}
	
	/**
	 * Constructs a Team object and runs all necessary calculations to compile statistics
	 * 
	 * @param name the name of the team(IE: 90241B)
	 * @param tData_rankings the JSONarray acquired from getting JSON array with key "result"
	 */
	private Team(TeamBuilder tb){
		//Set data
		this.number = tb.teamNumber;
		this.tData_teams = tb.tData_teams;
		this.tData_rankings = tb.tData_rankings;
		this.tData_events = tb.tData_events;
		this.tData_season_rankings = tb.tData_season_rankings;
		this.tData_skills = tb.tData_skills;
		//Perform calculations
		performCalculations_teams();
		performCalculations_rankings();
		performCalculations_events();
		performCalculations_season_rankings();
		performCalculations_skills();
	}
	
	/**
	 * Sets information about the team itself
	 */
	private void performCalculations_teams() {
		JSONObject result = tData_teams.getJSONObject(0);
		this.number = result.getString("number");
		this.teamName = result.getString("team_name");
		this.teamOrg = result.getString("organisation");
		this.city = result.getString("city");
		this.state = result.getString("region");
		this.country = result.getString("country");
	}
	
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
	
	/**
	 * Performs all calculations under the "skills" category
	 */
	private void performCalculations_skills() {
		calculateAvgSkillsScore_auton();
		calculateAvgSkillsScore_robot();
		calculateAvgSkillsScore_combined();
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
		//Initialize total for average
		double totalOPR = 0.0;
		//Break up array, and search for OPR in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			double opr = tData_rankings.getJSONObject(i).getDouble("opr");
			//Add to total
			totalOPR += opr;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("opr", true);
			//If nonzero, set to appropriate value
			this.avgOPR = (totalOPR + 0.0) / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("opr", false);
			//If zero, set to 0
			this.avgOPR = 0.0;
		}
	}
	
	/**
	 * Calculates the average DPR and sets the classes instance variable to it
	 */
	private void calculateAvgDPR() {
		//Initialize total for average
		double totalDPR = 0.0;
		//Break up array, and search for DPR in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			double dpr = tData_rankings.getJSONObject(i).getDouble("dpr");
			//Add to total
			totalDPR += dpr;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("dpr", true);
			//If nonzero, set to appropriate value
			this.avgDPR = (totalDPR + 0.0) / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("dpr", false);
			//If zero, set to 0
			this.avgDPR = 0.0;
		}
	}
	
	/**
	 * Calculates the average CCWM and sets the classes instance variable to it
	 */
	private void calculateAvgCCWM() {
		//Initialize total for average
		double totalCCWM = 0.0;
		//Break up array, and search for DPR in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			double ccwm = tData_rankings.getJSONObject(i).getDouble("ccwm");
			//Add to total
			totalCCWM += ccwm;
		}
		//Avoid ArithmeticException by checking if divisor is 0
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("ccwm", true);
			//If nonzero, set to appropriate value
			this.avgCCWM = (totalCCWM + 0.0) / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("ccwm", false);
			//If zero, set to 0
			this.avgCCWM = 0.0;
		}
	}
	
	/**
	 * Calculates the average max score and sets the classes instance variable to it
	 */
	private void calculateAvgMaxScore(){
		//Initialize total for average
		int totalScore = 0;
		//Break up array, and search for max score in each part
		for(int i = 0; i < tData_rankings.length(); i++){
			//Grab value
			int maxScore = tData_rankings.getJSONObject(i).getInt("max_score");
			//Add to total
			totalScore += maxScore;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("max_score", true);
			//If nonzero, set to appropriate value
			this.avgMaxScore = (int)totalScore / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("max_score", false);
			//If zero, set to 0
			this.avgMaxScore = 0;
		}
	}
	
	/**
	 * Parses through the teams ranks, and sets both the highest ranking and the 
	 * average ranking.
	 */
	private void calculateRanks(){
		//Initialize total for average
		int totalRank = 0;
		/* Break up array, and search though each ranking. For the average, follow
		 * the same procedure above. 
		 */
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int rank = tData_rankings.getJSONObject(i).getInt("rank");
			//Add to total
			totalRank += rank;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("rank", true);
			//If nonzero, set to appropriate value
			this.avgRank = (int)totalRank / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("rank", false);
			//If zero, set to 0
			this.avgRank = 0;
		}
	}
	
	/**
	 * Calculates the average autonomous points for a team
	 */
	private void calculateAvgAP() {
		//Initialize total for average
		int totalAP = 0;
		//Break up array, and search for APs in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int AP = tData_rankings.getJSONObject(i).getInt("ap");
			//Add to total
			totalAP += AP;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("ap", true);
			//If nonzero, set to appropriate value
			this.avgAP = (int)totalAP / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("ap", false);
			//If zero, set to 0
			this.avgAP = 0;
		}

	}
	
	/**
	 * Calculates the average skills points for a team
	 */
	private void calculateAvgSP() {
		//Initialize total for average
		int totalSP = 0;
		//Break up array, and search for SPs in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int SP = tData_rankings.getJSONObject(i).getInt("sp");
			//Add to total
			totalSP += SP;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("sp", true);
			//If nonzero, set to appropriate value
			this.avgSP = (int)totalSP / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("sp", false);
			//If zero, set to 0
			this.avgSP = 0;
		}

	}
	
	/**
	 * Calculates the average TRSPs (custom ranking method for SPs) for a team
	 */
	private void calculateAvgTRSP() {
		//Initialize total for average
		int totalTRSP = 0;
		//Break up array, and search for SPs in each part
		for(int i = 0; i < tData_rankings.length(); i++) {
			//Grab value
			int TRSP = tData_rankings.getJSONObject(i).getInt("trsp");
			//Add to total
			totalTRSP += TRSP;
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("trsp", true);
			//If nonzero, set to appropriate value
			this.avgTRSP = (int)totalTRSP / tData_rankings.length();
		}else {
			//Add indicator
			fieldIndicators.put("trsp", false);
			//If zero, set to 0
			this.avgTRSP = 0;
		}

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
	private void setvrating_rank() { 
		//Check if length of array is nonzero(if zero, trying to grab "vrating_rank" will throw JSONException)
		if(tData_season_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("vrating_rank", true);
			//If nonzero, set properly
			this.vrating_rank = tData_season_rankings.getJSONObject(0).getInt("vrating_rank"); 
		}else { //Is zero, so vrating rank won't even appear
			//Add indicator
			fieldIndicators.put("vrating_rank", false);
			//If zero, set to zero
			this.vrating_rank = 0;
		}
		
	}
	
	/**
	 * Set's the team's vrating
	 */
	private void setvrating() { 
		//Check if length of array is nonzero(if zero, trying to grab "vrating_rank" will throw JSONException)
		if(tData_season_rankings.length() != 0) {
			//Add indicator
			fieldIndicators.put("vrating", true);
			//If nonzero, set properly
			this.vrating = tData_season_rankings.getJSONObject(0).getDouble("vrating");
		}else { //Is zero, so vrating won't even appear
			//Add indicator
			fieldIndicators.put("vrating", false);
			//If zero, set to zero
			this.vrating = 0.0;
		}
		 
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									  SKILLS CALCULATIONS								//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	/*
	 * "type":0 - Autonomous
	 * "type":1 - Robot
	 * "type":2 - Combined
	 */
	
	/**
	 * Calculates the average skills score for autonomous mode
	 */
	private void calculateAvgSkillsScore_auton() {
		//Initialize total for average
		int totalScore = 0;
		//Break up array, search for skills score in each part
		for(int i = 0; i < tData_skills.length(); i++) {
			//Only check autonomous results(type = 0)
			if(tData_skills.getJSONObject(i).getInt("type") == 0){
				//Grab value
				int score = tData_skills.getJSONObject(i).getInt("score");
				//Add to total
				totalScore += score;
			}
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_skills.length() != 0) {
			//Add indicator
			fieldIndicators.put("skills_auton", true);
			//If nonzero, set to appropriate value
			this.avgSkillsScore_auton = (int)totalScore/tData_skills.length();
		}else {
			//Add indicator
			fieldIndicators.put("skills_auton", false);
			//If zero, set to zero
			this.avgSkillsScore_auton = 0;
		}
	}
	
	/**
	 * Calculates the average skills score for driver control mode
	 */
	private void calculateAvgSkillsScore_robot() {
		//Initialize total for average
		int totalScore = 0;
		//Break up array, search for skills score in each part
		for(int i = 0; i < tData_skills.length(); i++) {
			//Only check robot results(type = 1)
			if(tData_skills.getJSONObject(i).getInt("type") == 1){
				//Grab value
				int score = tData_skills.getJSONObject(i).getInt("score");
				//Add to total
				totalScore += score;
			}
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_skills.length() != 0) {
			//Add indicator
			fieldIndicators.put("skills_robot", true);
			//If nonzero, set to appropriate value
			this.avgSkillsScore_robot = (int)totalScore/tData_skills.length();
		}else {
			//Add indicator
			fieldIndicators.put("skills_robot", false);
			//If zero, set to zero
			this.avgSkillsScore_robot = 0;
		}
	}
	
	/**
	 * Calculates the average skills score for both autonomous and driver control modes
	 */
	private void calculateAvgSkillsScore_combined() {
		//Initialize total for average
		int totalScore = 0;
		//Break up array, search for skills score in each part
		for(int i = 0; i < tData_skills.length(); i++) {
			//Only check combined results(type = 2)
			if(tData_skills.getJSONObject(i).getInt("type") == 2){
				//Grab value
				int score = tData_skills.getJSONObject(i).getInt("score");
				//Add to total
				totalScore += score;
			}
		}
		//Compute average
		/*Avoid ArithmeticException by checking if divisor is 0*/
		if(tData_skills.length() != 0) {
			//Add indicator
			fieldIndicators.put("skills_combined", true);
			//If nonzero, set to appropriate value
			this.avgSkillsScore_combined = (int)totalScore/tData_skills.length();
		}else {
			//Add indicator
			fieldIndicators.put("skills_combined", false);
			//If zero, set to zero
			this.avgSkillsScore_combined = 0;
		}
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									   GETTER METHODS								    //
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Retrieves the current team name(IE: "90241B")
	 * 
	 * @return the team name
	 */
	public String getNumber() { return this.number; }
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
	
	/**
	 * Retrieves the average skills score for autonomous mode
	 * 
	 * @return the average skills score for autonomous
	 */
	public int getAvgSkillsScore_auton() { return this.avgSkillsScore_auton; }
	
	/**
	 * Retrieves the average skills score for driver control mode
	 * 
	 * @return the average skills score for driver control mode
	 */
	public int getAvgSkillsScore_robot() { return this.avgSkillsScore_robot; }
	
	/**
	 * Retrieves the average skills score for both autonomous and driver control mode
	 * 
	 * @return the average skills score for both autonomous and driver control mode
	 */
	public int getAvgSkillsScore_combined() { return this.avgSkillsScore_combined; }
	
	/**
	 * A toString method that simply returns the team name
	 */
	public String toString() { return "Team " + this.number; }
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//										  BUILDER CLASS								    //
	//																						//
	------------------------------------------------------------------------------------------
	*/
	/**
	 * A Builder method that is used to create a Team object. All fields in the class are 
	 * required, as I only used a Builder method to prevent having to use a separate class.
	 * 
	 * @author Robert Engle | WHS Robotics | Team 90241B
	 * @version 1.1
	 * @since 2018-10-8
	 */
	public static class TeamBuilder{
		//Name
		public String teamNumber;
		//Season(wont appear, only used for grabbing data)
		public String season;
		//JSON Array Data
		public JSONArray tData_teams;
		public JSONArray tData_rankings;
		public JSONObject tData_events; 
		public JSONArray tData_season_rankings;
		public JSONArray tData_skills;
		
		/**
		 * Constructs a TeamBuilder object. Is the only way to make a Team object. 
		 * 
		 * @param teamName the number of the team(IE: "90241B")
		 * @param season the season to get stats for(IE: "In The Zone")
		 * @throws JSONException
		 * @throws IOException
		 */
		public TeamBuilder(String teamNumber, String season) throws JSONException, IOException {
			this.teamNumber = teamNumber;
			this.season = season;
		}
		
		public TeamBuilder setTeamData() throws JSONException, IOException {
			//Construct link for lookup
			String str_teams = "https://api.vexdb.io/v1/get_teams?team=" + this.teamNumber;
			//Create JSON object
			JSONObject tObject_teams = readJsonFromUrl(str_teams);
			//Create respective array
			this.tData_teams = tObject_teams.getJSONArray("result");
			return this;
		}
		
		public TeamBuilder setRankingData() throws JSONException, IOException {
			//URL-Escape the season
			String formattedSeason = this.season.replace(" ", "%20");
			//Construct link for lookup
			String str_rankings = "https://api.vexdb.io/v1/get_rankings?team=" + this.teamNumber + "&season=" + formattedSeason;
			//Create JSON object
			JSONObject tObject_rankings = readJsonFromUrl(str_rankings);
			//Create respective array
			this.tData_rankings = tObject_rankings.getJSONArray("result");
			return this;
		}
		
		public TeamBuilder setEventData() throws JSONException, IOException {
			//URL-Escape the season
			String formattedSeason = this.season.replace(" ", "%20");
			//Construct link for lookup
			String str_events = "https://api.vexdb.io/v1/get_events?team=" + this.teamNumber + "&season=" + formattedSeason;
			//Create JSON object
			this.tData_events = readJsonFromUrl(str_events);
			return this;
		}
		
		public TeamBuilder setSeasonData() throws JSONException, IOException { 
			//URL-Escape the season
			String formattedSeason = this.season.replace(" ", "%20");
			//Construct link for lookup
			String str_season_rankings = "https://api.vexdb.io/v1/get_season_rankings?team=" + this.teamNumber + "&season=" + formattedSeason;
			//Create JSON object
			JSONObject tObject_season_rankings = readJsonFromUrl(str_season_rankings);
			//Create respective array
			this.tData_season_rankings = tObject_season_rankings.getJSONArray("result");
			return this;
		}
		
		public TeamBuilder setSkillsData() throws JSONException, IOException {
			//URL-Escape the season
			String formattedSeason = this.season.replace(" ", "%20");
			//Construct link for lookup
			String str_skills = "https://api.vexdb.io/v1/get_skills?team=" + this.teamNumber + "&season=" + formattedSeason;
			//Create JSON object
			JSONObject tObject_skills = readJsonFromUrl(str_skills);
			//Create respective array
			this.tData_skills = tObject_skills.getJSONArray("result");
			return this;
		}
		
		public Team build() {
			return new Team(this);
		}
		/**
		  * Creates a JSON object by reading a url that contains a JSON output, in this case 
		  * the URL is from the VexDB.io API
	      * 
		  * @param url the url that has the JSON output
		  * @return a JSONObject created from a JSON output from desired URL
		  * @throws IOException
		  * @throws JSONException
		  */
		public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
			InputStream is = new URL(url).openStream();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = readAll(rd);
				JSONObject json = new JSONObject(jsonText);
				return json;
			} finally {
				is.close();
			}
		}
			
		/**
		 * Reads all letters from a reader and returns a string of these. Is currently 
		 * a work-around for some bugs involving readers and outputs
		 * 
		 * @param rd The reader to extrapolate a string from
		 * @return the whole string outputted from the reader
		 * @throws IOException
		 */
		private static String readAll(Reader rd) throws IOException {
			StringBuilder sb = new StringBuilder();
		    int cp;
		    while ((cp = rd.read()) != -1) {
		      sb.append((char) cp);
		    }
		    return sb.toString();
		}
	}//end TeamBuilder class
}//end Team class
