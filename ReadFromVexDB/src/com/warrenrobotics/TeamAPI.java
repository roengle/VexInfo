package com.warrenrobotics;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Interacts with Google Sheets using the Google Sheets API v4 to automatically 
 * assign certain statistics to them.<br><br>
 * 
 * 
 * This class is responsible for all interactions with Google Sheets.
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.2
 * @since 2018-02-21
 *
 */
public class TeamAPI {
	//Instance variables
	private String spreadsheetId; 
	public String season;
	private String accessToken;
	private ValueRange response; //Currently depreciated
	private String[] teamList;
	private String sku;
	
	/**
	 * Constructs a TeamAPI object to interpret data from a Google Sheets using the Google Sheets API v4
	 * 
	 * @param spreadsheetId the id of the spreadsheet(commonly found in the link of the spreadsheet)
	 * @param link the URL of the RobotEvents page
	 */
	public TeamAPI(String spreadsheetId, String link) throws IOException, GeneralSecurityException, InterruptedException{
		//Set spreadsheet id
		this.spreadsheetId = spreadsheetId;
		//Process link into SKU, grab season, and set team list
		processLink(link);
		//Assign access token
		setAccessToken();
		//Create sheet service and request data using it
		Sheets sheetsService = createSheetsService();
		//Execute a write request
		executeWriteRequest(sheetsService);
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//								  AUTHENTICATION METHODS								//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Creates a sheets service that can be used to make a request for data
	 * 
	 * @return a Sheets object that can be used to grab and write data
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private Sheets createSheetsService() throws IOException, GeneralSecurityException {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	    GoogleCredential credential = new GoogleCredential.Builder()
	    		.setTransport(httpTransport)
	    		.setJsonFactory(jsonFactory)
	    		.setClientSecrets(Constants.GOOGLE_CLIENT_ID, Constants.GOOGLE_CLIENT_SECRET)
	    		.build();
	    credential.setAccessToken(this.accessToken).setRefreshToken(Constants.GOOGLE_REFRESH_TOKEN);
	    
	    return new Sheets.Builder(httpTransport, jsonFactory, credential)
	        .setApplicationName("https://www.googleapis.com/auth/spreadsheets")
	        .build();
	}

	/**
	 * Retrieves an access token using the refresh token
	 * 
	 * @return an access token that can be used to create a proper credential
	 */
	public void setAccessToken() throws IOException, GeneralSecurityException, TokenResponseException{
		/*
		 * Note to users who plan to use this:
		 * 
		 * On Github, the Constants.java file will not show since I put it in 
		 * git ignore, due to it having sensitive data. In order to use this on
		 * your own, make a new file Constants.java as an interface, and simply input
		 * the values "GOOGLE_CLIENT_ID" and "GOOGLE_CLIENT_SECRET", as well as 
		 * "GOOGLE_REFRESH_TOKEN".
		 */
		//Create a token response using refresh token and oauth credentials
		TokenResponse response = new GoogleRefreshTokenRequest(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), 
				Constants.GOOGLE_REFRESH_TOKEN, Constants.GOOGLE_CLIENT_ID, Constants.GOOGLE_CLIENT_SECRET)
				.execute();
		//Set the access token
		this.accessToken = response.getAccessToken();   
	}
	
	//IMPLEMENT LATER - GETTING TOKENS FROM AUTHORIZATION CODE USING POST
	/*
	public void setTokens() throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("https://accounts.google.com/o/oauth2/token");
		List<NameValuePair> pairs = new ArrayList<>();
		pairs.add(new BasicNameValuePair("code", Constants.GOOGLE_OAUTH2_AUTHCODE));
	    pairs.add(new BasicNameValuePair("client_id", Constants.GOOGLE_CLIENT_ID));
	    pairs.add(new BasicNameValuePair("client_secret", Constants.GOOGLE_CLIENT_SECRET));
	    pairs.add(new BasicNameValuePair("redirect_uri", "https://developers.google.com/oauthplayground"));
	    pairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
	    post.setEntity(new UrlEncodedFormEntity(pairs));
	    org.apache.http.HttpResponse response = client.execute(post);
	    String responseBody = EntityUtils.toString(response.getEntity());
	    System.out.println(responseBody);
	}
	*/
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									 SHEETS METHODS										//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Executes a get request for all data in the spreadsheet
	 * 
	 * CURRENTLY DEPRECIATED UNTIL FURTHER NOTICE. 
	 * MAY BE USED IN THE EVENT THAT A USER WOULD WANT TO MAKE A SPREADSHEET WITH ONLY SPECIFIC TEAMS
	 * 
	 * @param sheetsService the Sheets object with an authenticated credential
	 * @throws IOException for when an I/O error occurs
	 */
	public void executeGetRequest(Sheets sheetsService) throws IOException{
		//Setup a request for getting spreadsheet data
		Sheets.Spreadsheets.Values.Get request =
		    sheetsService.spreadsheets().values().get(this.spreadsheetId, "Sheet1");
		    request.setValueRenderOption("FORMATTED_VALUE");
		    request.setDateTimeRenderOption("SERIAL_NUMBER");
		//Get a response as a ValueRange(which can converted to JSON Objects)
		this.response = request.execute();
	}
	
	/**
	 * Executes a write request to write data to a spreadsheet.
	 * 
	 * @param sheetsService the Sheets object with an authenticated credential
	 * @throws IOException for when an I/O error occurs
	 * @throws InterruptedException for when a thread is being occupied and interrupted
	 */
	public void executeWriteRequest(Sheets sheetsService) throws IOException, InterruptedException{
		//Debugging for how long algorithm takes to run with certain data sets
		long startTime = System.currentTimeMillis();
		//Initialize ApilRateLimiter object
		//ApilRateLimiter apiRateLimiter = new ApilRateLimiter(Constants.SHEETS_QUOTA_PER_SECOND);
		//Loop through team list
		for(int i = 0; i < teamList.length; i++) {
			//Time how long each loop takes
			long sTime = System.currentTimeMillis();
			//Start values as null
			List<List<Object>> values = null;
			String range = null;
			String printMsg = null;
			//ONE-TEAM V. TWO-TEAM SETTINGS
			if((i + 1) == teamList.length) {//At end, grabbing second team will throw out of bounds exception
				//ONE-TEAM SETTING
				//Grab team name
				String n = teamList[i];
				//Parse team and calculate data
				Team t = new Team.TeamBuilder(n, this.season)
						.setEventData()
						.setRankingData()
						.setSeasonData()
						.setSkillsData()
						.build();
				//Initialize array for inputting data
				String[] valuesArr = new String[19];
				//Build array with proper data
				buildValues(valuesArr, t);
				//Configure body for input
				values = Arrays.asList(Arrays.asList(valuesArr));
				//Configure range as Sheet1!F#:S# where # is a number based on the current team(i+2)
				range = "Sheet1!A" + (i + 2) + ":S" + (i + 2);
				//Setup print message
				printMsg = "COLUMN#" + (i + 2) + " STATS UPDATED: " + t.number + " (";
			}else {//Can still grab two teams without exception
				//TWO-TEAM SETTING
				//Grab first team name
				String n1 = teamList[i];
				//Parse team 1 and calculate data
				Team t1 = new Team.TeamBuilder(n1, this.season)
						.setEventData()
						.setRankingData()
						.setSeasonData()
						.setSkillsData()
						.build();
				//Initialize array for inputting data
				String[] valuesArr1 = new String[19];
				//Build array with proper data
				buildValues(valuesArr1, t1);
				//Grab second team name
				String n2 = teamList[i + 1];
				//Parse team 2 and calculate data
				Team t2 = new Team.TeamBuilder(n2, this.season)
						.setEventData()
						.setRankingData()
						.setSeasonData()
						.setSkillsData()
						.build();
				//Initialize array for inputting data
				String[] valuesArr2 = new String[18];
				//Build array with proper data
				buildValues(valuesArr2, t2);
				//Configure body for input
				values = Arrays.asList(Arrays.asList(valuesArr1), Arrays.asList(valuesArr2));
				//Configure range as Sheet1!F#:S# where # is a number based on the current team and next team(i+3)
				range = "Sheet1!A" + (i + 2) + ":S" + (i + 3);
				//Setup print message
				printMsg = "COLUMN#" + (i + 2) + "," + (i + 3) + " STATS UPDATED: " + t1.number + "," + t2.number + "(";
				i++;
			}
			//Configure body as a ValueRange object
			ValueRange body = new ValueRange().setValues(values);
			//Reserve quota(currently disabled, quota was updated by google)
			//apiRateLimiter.reserve(Constants.SHEETS_QUOTA_PER_SECOND);
			//Send write request and receive response
			@SuppressWarnings("unused")
			UpdateValuesResponse result = 
					sheetsService.spreadsheets().values().update(this.spreadsheetId, range, body)
					.setValueInputOption("USER_ENTERED")
					.setIncludeValuesInResponse(false)
					.execute();
			//Grab how long it took
			long timeTaken = System.currentTimeMillis() - sTime;
			//Print out success message
			System.out.print(printMsg + timeTaken + " ms)\n");
		}
		//Establish how long algorithm took to run(milliseconds)
		long runtime = System.currentTimeMillis() - startTime;
		//Convert to seconds
		double runtimeInSeconds = (double)runtime/1000;
		System.out.println("SUCCESS - " + teamList.length + " TEAMS UPDATED IN " + runtimeInSeconds + " SECONDS");
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									PROCESSING METHODS									//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Processes the response into an array of strings containing team names(IE: ["90241A", "90241B"])
	 * 
	 * CURRENTLY DEPRECIATED UNTIL FURTHER NOTICE. 
	 * MAY BE USED IN THE EVENT THAT A USER WOULD WANT TO MAKE A SPREADSHEET WITH ONLY SPECIFIC TEAMS
	 * 
	 * @return the team names as an array of strings
	 */
	private void processResponseIntoTeamList() {
		//Build string from ValueRange
		String responseStr = this.response.toString();
		//Build a json object from the string
		JSONObject json = new JSONObject(responseStr);
		//Build a json array from the "values" section
		JSONArray values = json.getJSONArray("values");
		//Make an array of strings for team names. Is length - 1 since the first iteration of values doesn't contain a team
		String[] teams = new String[values.length() - 1];
		//Start at index 1 to ignore top of row(which shows "Team")
		for(int i = 1; i < values.length(); i++) {
			//Must input at i - 1 since array starts at 0 still
			teams[i - 1] = values.getJSONArray(i).getString(0);
		}
		//Set the class team list
		this.teamList = teams;
	}
	
	/**
	 * Processes a RobotEvents.com link to be able to get an events SKU, 
	 * the season for that event, and a team list for that event.
	 * 
	 * Note: Team lists can only be generated 4 weeks before the start date
	 * of the tournament
	 * 
	 * @param s the URL of the robot events link
	 * @throws JSONException for when JSON API encounters error
	 * @throws IOException for when an I/O error occurs
	 */
	private void processLink(String s) throws JSONException, IOException {
		//Create URL from link
		URL link = new URL(s);
		//Get file path of url
		String[] filePath = link.getPath().split("/");
		//Get and set event code
		this.sku = filePath[filePath.length - 1].replaceAll(".html", "");
		//Get and set season from API
		this.season = Team.TeamBuilder.readJsonFromUrl("https://api.vexdb.io/v1/get_events?sku=" + this.sku)
				.getJSONArray("result")
				.getJSONObject(0)
				.getString("season");
		//Build JSON array from SKU
		JSONArray result = Team.TeamBuilder
				.readJsonFromUrl("https://api.vexdb.io/v1/get_teams?sku=" + this.sku)
				.getJSONArray("result");
		//Build team list
		String[] teams = new String[result.length()];
		for(int i = 0; i < result.length(); i++) {
			teams[i] = result.getJSONObject(i).getString("number");
		}
		this.teamList = teams;
	}
	
	/**
	 * Builds values in an array representing certain statistics of the team.
	 * <ul>
	 * 		<b>Writes in this specific order:</b>(19 fields total)
	 * 		<ol start=0>
	 * 			<li>number</li>
	 * 			<li>teamName</li>
	 * 			<li>teamOrg</li>
	 * 			<li>teamLocation</li>
	 * 			<li>teamLink</li>
	 * 			<li>avgOPR</li>
	 * 			<li>avgDPR</li>
	 * 			<li>avgCCWM</li>
	 * 			<li>avgAP</li>
	 * 			<li>avgSP</li>
	 * 			<li>avgTSRP</li>
	 * 			<li>vratingRank</li>
	 * 			<li>vrating</li>
	 * 			<li>avgRank</li>
	 * 			<li>avgSkills_auton</li>
	 * 			<li>avgSkills_robot</li>
	 * 			<li>avgSkills_combined</li>
	 * 			<li>avgMaxScore</li>
	 * 			<li>totalEvents</li>
	 * 		</ol>
	 * </ul>
	 * @param arr the array to write the statistics to
	 * @param t the team who the statistics are for
	 */
	
	@SuppressWarnings("static-access")
	private void buildValues(String[] arr, Team t) {
		//TODO: Iteravely build a list instead of setting individual values
		//0.
		arr[0] = t.getNumber();
		//1.
		arr[1] = t.getTeamName();
		//2.
		arr[2] = t.getTeamOrg();
		//3.
		arr[3] = t.getTeamLocation();
		//4.
		arr[4] = t.getTeamLink();
		//5.
		if(t.fieldIndicators.get("opr") == true) {
			arr[5] = Double.toString(t.getAvgOPR());
		}else {
			arr[5] = "NOT_FOUND";
		}
		//6.
		if(t.fieldIndicators.get("dpr") == true) {
			arr[6] = Double.toString(t.getAvgDPR());
		}else {
			arr[6] = "NOT_FOUND";
		}
		//7.
		if(t.fieldIndicators.get("ccwm") == true) {
			arr[7] = Double.toString(t.getAvgCCWM());
		}else {
			arr[7] = "NOT_FOUND";
		}
		//8.
		if(t.fieldIndicators.get("ap") == true) {
			arr[8] = Integer.toString(t.getAvgAP());
		}else {
			arr[8] = "NOT_FOUND";
		}
		//9.
		if(t.fieldIndicators.get("ap") == true) {
			arr[9] = Integer.toString(t.getAvgSP());
		}else {
			arr[9] = "NOT_FOUND";
		}
		//10.
		if(t.fieldIndicators.get("trsp") == true) {
			arr[10] = Integer.toString(t.getAvgTRSP());
		}else {
			arr[10] = "NOT_FOUND";
		}
		//11.
		if(t.fieldIndicators.get("vrating_rank") == true) {
			arr[11] = Integer.toString(t.getvrating_rank());
		}else {
			arr[11] = "NOT_FOUND";
		}
		//12.
		if(t.fieldIndicators.get("vrating") == true) {
			arr[12] = Double.toString(t.getvrating());
		}else {
			arr[12] = "NOT_FOUND";
		}
		//13.
		if(t.fieldIndicators.get("rank") == true) {
			arr[13] = Integer.toString(t.getAvgRank());
		}else {
			arr[13] = "NOT_FOUND";
		}
		//14.
		if(t.fieldIndicators.get("skills_auton") == true) {
			arr[14] = Integer.toString(t.getAvgSkillsScore_auton());
		}else {
			arr[14] = "NOT_FOUND";
		}
		//15.
		if(t.fieldIndicators.get("skills_robot") == true) {
			arr[15] = Integer.toString(t.getAvgSkillsScore_robot());
		}else {
			arr[15] = "NOT_FOUND";
		}
		//16.
		if(t.fieldIndicators.get("skills_combined") == true) {
			arr[16] = Integer.toString(t.getAvgSkillsScore_combined());
		}else {
			arr[16] = "NOT_FOUND";
		}
		//17.
		if(t.fieldIndicators.get("max_score") == true) {
			arr[17] = Integer.toString(t.getAvgMaxScore());
		}else {
			arr[17] = "NOT_FOUND";
		}
		//18.
		arr[18] = Integer.toString(t.getNumEvents());
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									   GETTER METHODS								    //
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Retrieves the current access token provided by setAccessToken()
	 * 
	 * @return an access token allowing access to a sheet
	 */
	public String getAccessToken() { return this.accessToken; }
	
	/**
	 * Retrieves the current team list provided by proccessResponseIntoTeamList()
	 * 
	 * @return the team list as an array of Strings
	 */
	public String[] getTeamList() { return this.teamList; }
}