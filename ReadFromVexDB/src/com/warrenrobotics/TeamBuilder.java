package com.warrenrobotics;

import org.json.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
/**
 * This part of the program is able to take team names(IE: 90241B) and parse stats from 
 * selected team, for example average OPR. 
 * 
 * This class is responsible for creating team objects by retrieving data from VexDB
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.1
 * @since 2018-02-21
 *
 */
public class TeamBuilder {
	/**
	  * Parses a team and its stats into the teams list
	  * 
	  * @param teamName the name the of team(IE: 90241B)
	  * @throws IOException
	  */
	 public static Team parseTeam(String teamName) throws IOException{
		 //Construct links for lookup
		 String str_rankings = "https://api.vexdb.io/v1/get_rankings?team=" + teamName + "&season=In%20The%20Zone";
		 String str_events = "https://api.vexdb.io/v1/get_events?team=" + teamName + "&season=In%20The%20Zone";
		 String str_season_rankings = "https://api.vexdb.io/v1/get_season_rankings?team=" + teamName + "&season=In%20The%20Zone";
		 String str_skills = "https://api.vexdb.io/v1/get_skills?team=" + teamName + "&season=In%20The%20Zone";
		 //Create JSON objects
		 JSONObject tData_rankings = readJsonFromUrl(str_rankings);
		 JSONObject tData_events = readJsonFromUrl(str_events);
		 JSONObject tData_season_rankings = readJsonFromUrl(str_season_rankings);
		 JSONObject tData_skills = readJsonFromUrl(str_skills);
		 //Create respective results array
		 JSONArray tArray_rankings = tData_rankings.getJSONArray("result");
		 JSONArray tArray_season_rankings = tData_season_rankings.getJSONArray("result");
		 JSONArray tArray_skills = tData_skills.getJSONArray("result");
		 //Return new team (events does not need to be in an array since the "size" value is all that is needed)
		 return new Team(teamName, tArray_rankings, tData_events, tArray_season_rankings, tArray_skills);
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
}