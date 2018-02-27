package com.warrenrobotics;

import org.json.*;

import java.net.URL;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
/**
 * This part of the program is able to take team names(IE: 90241B) and parse stats from 
 * selected team, such as average OPR, 
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.0
 * @since 2018-02-21
 *
 */
public class TeamAPI {

	static Map<String, Team> teamMappings = new HashMap<>();
	static List<String> teamNames = new ArrayList<>();
	
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
	  * Parses a team and its stats into the teams list
	  * 
	  * @param teamName the name the of team(IE: 90241B)
	  * @throws IOException
	  */
	 public static void parseTeam(String teamName) throws IOException{
		 //Construct link for lookup
		 StringBuilder sb = new StringBuilder();
		 sb.append("https://api.vexdb.io/v1/get_rankings?team=" + teamName + "&season=In%20The%20Zone");
		 String link = sb.toString();
		 //Crease JSONObject
		 JSONObject teamJson = readJsonFromUrl(link);
		 //Create respective results array
		 JSONArray teamResults = teamJson.getJSONArray("result");
		 //Put the team into hashmap
		 teamMappings.put(teamName,new Team(teamName, teamResults));
	 }
	 
	 /**
	  * Prints the statistics for a select team. Statistics include average OPR, average DPR,
	  * average CCWM, average max score, best ranking(in one tournament), and average ranking
	  * (all tournaments)
	  * 
	  * @param teamName the name of the team(IE: 90241B)
	  */
	 public static void printTeamStats(String teamName) {
		 //Assign team
		 Team t1 = teamMappings.get(teamName);
		 //Print out team name
		 System.out.println(t1.name);
		 //Print out statistics, each with a tab in front
		 System.out.println("\tAVG OPR:" + t1.getAvgOPR());
		 System.out.println("\tAVG DPR:" + t1.getAvgDPR());
		 System.out.println("\tAVG CCWM:" + t1.getAvgCCWM());
		 System.out.println("\tAVG Max Score:" + t1.getAvgMaxScore());
		 System.out.println("\tBest Rank(season):" + t1.getBestRank());
		 System.out.println("\tAverage Rank(season):" + t1.getAvgRank());
	 }
	 
	 /**
	  * Takes a CSV list and extracts the team names. Note that team names should be in the first row.
	  * 
	  * TODO:Eventually make a method to read actual excel files, and soon make a method to be able to work with
	  * google sheets
	  * 
	  * @param filePath the path of the CSV file(IE: "C:/Sheet.csv")
	  */
	 public static void extractTeamListFromCSV(String filePath){
		 try {
			 //Construct buffered reader for comma-separated list
			 BufferedReader br = new BufferedReader(new FileReader(filePath));
			 //Skip first line
			 br.readLine();
			 //Set up input String
			 String input;
			 //Read until at end of file, assigning input string to line read from BufferedReader
			 while((input = br.readLine()) != null){
				 //Split with commas
				 String[] inputSplit = input.split(",");
				 //Since only getting team names is important, get the first element
				 teamNames.add(inputSplit[0]);
			 }
			 //Loop through all the team names and parse into program
			 for(int i = 0; i < teamNames.size(); i++){
				 parseTeam(teamNames.get(i));
			 }
			 //Close reader
			 br.close();
		 } catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
	  
	public static void main(String[] args) throws Exception{
		extractTeamListFromCSV("C:/Sheet.csv");
		for(int i = 0; i < teamNames.size(); i ++) {
			printTeamStats(teamNames.get(i));
		}
	}

}