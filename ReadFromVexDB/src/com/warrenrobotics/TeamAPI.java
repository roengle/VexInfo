package com.warrenrobotics;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddConditionalFormatRuleRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BooleanCondition;
import com.google.api.services.sheets.v4.model.BooleanRule;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.ConditionValue;
import com.google.api.services.sheets.v4.model.ConditionalFormatRule;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * <p>
 * Interacts with Google Sheets using the Google Sheets API v4 to automatically 
 * assign certain statistics to them.
 * </p>
 * <p>
 * This class is responsible for all interactions with Google Sheets.
 * </p>
 * <p>
 * To create a Google Sheet for an event, simply call the {@link TeamAPI} constructor, 
 * with the link of the event and the email to share the Sheet with.
 * </p>
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.2
 * @since 2018-02-21
 *
 */
public class TeamAPI {
	//Spreadsheet/user information
	private String spreadsheetId; 
	private String spreadsheetURL;
	//Event information
	private String season;
	private String eventName;
	private String eventDate;
	private String[] teamList;
	private String sku;
	//Constants
	public final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	//Logger
	private static Logger LOGGER = LogManager.getLogger(TeamAPI.class);
	/**
	 * Constructs a TeamAPI object to write data to a Google Sheet using the Google Sheets API v4
	 * and the Google Drive API v3
	 * 
	 * @param link the URL of the RobotEvents page
	 * @param usrEmail the email of the user who will be give ownership of the sheet
	 * @throws IOException for when an I/O error occurs
	 * @throws GeneralSecurityException
	 * @throws InterruptedException for when a working thread is interrupted
	 */
	public TeamAPI(String link, String usrEmail) throws IOException, GeneralSecurityException, InterruptedException{
		//Print date of start time
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		System.out.printf("%s - Running Program%n", dateFormat.format(date));
		//Process link into SKU, grab season, set event name, and set team list
		processLink(link, "");
		//Assign access tokens
		String accessToken_sheets = setAccessToken();
		String accessToken_drive = setAccessToken();
		//Assign credentials
		GoogleCredential credential_sheets = setCredential(accessToken_sheets);
		GoogleCredential credential_drive = setCredential(accessToken_drive);
		//Create sheet service with authenticated credential
		Sheets sheetsService = createSheetsService(credential_sheets);
		//Create drive service with authenticated credential
		Drive driveService = createDriveService(credential_drive);
		//Create spreadsheet
		executeCreateRequest(sheetsService);
		//Execute a write request
		executeWriteRequest(sheetsService);
		//Apply conditional formatting
		applyConditionalFormatting(sheetsService);
		//Transfer ownership
		transferOwnership(driveService, usrEmail);
	}
	
	/**
	 * <p>
	 * Constructs a TeamAPI object to write data to a Google Sheet using the Google Sheets API v4
	 * and the Google Drive API v3
	 * </p>
	 * <p>
	 * This version of the constructor allows a user to specify a season to get stats for, 
	 * and doesn't automatically get the season on the RobotEvents page. This is useful
	 * for getting statistics for tournaments really early into the season, as lots of 
	 * teams won't have any data.
	 * </p>
	 * 
	 * @param link the URL of the RobotEvents page
	 * @param usrEmail the email for the user who will be given ownership of the sheet
	 * @param season the specified season to get stats for
	 * @throws IOException for when an I/O error occurs
	 * @throws GeneralSecurityException
	 * @throws InterruptedException for when a working thread is interrupted
	 */
	public TeamAPI(String link, String usrEmail, String season) throws IOException, GeneralSecurityException, InterruptedException{
		//Print date of start time
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		System.out.printf("%s - Running Program%n", dateFormat.format(date));
		//Process link into SKU, grab season, set event name, and set team list
		processLink(link, season);
		//Assign access tokens
		String accessToken = setAccessToken();
		//Assign credentials
		GoogleCredential credential_sheets = setCredential(accessToken);
		GoogleCredential credential_drive = setCredential(accessToken);
		//Create sheet service with authenticated credential
		Sheets sheetsService = createSheetsService(credential_sheets);
		//Create drive service with authenticated credential
		Drive driveService = createDriveService(credential_drive);
		//Create spreadsheet
		executeCreateRequest(sheetsService);
		//Execute a write request
		executeWriteRequest(sheetsService);
		//Apply conditional formatting
		applyConditionalFormatting(sheetsService);
		//Transfer ownership
		transferOwnership(driveService, usrEmail);
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//								  AUTHENTICATION METHODS								//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Builds and returns an authenticated credential for the Sheets and Drive objects
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException for when an I/O error occurs
	 */
	private GoogleCredential setCredential(String accessToken) throws GeneralSecurityException, IOException {
		//Create new transport
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	    //Print message
		System.out.print("Building API Credential...");
		//Time how long it takes
		long curTime = System.currentTimeMillis();
		//Build authenticated credential
		GoogleCredential newCredential = new GoogleCredential.Builder()
				.setTransport(httpTransport)
				.setClientSecrets(Constants.GOOGLE_CLIENT_ID, Constants.GOOGLE_CLIENT_SECRET)
				.build()
				.setAccessToken(accessToken);
		//Get time difference
		int timeDif = (int)(System.currentTimeMillis() - curTime);
		//Print out time taken
		System.out.printf("(%d ms)%n", timeDif);
		//Return new credential
		return newCredential;
	}
	
	/**
	 * Creates a Sheets object that can be used to make a request for data
	 * 
	 * @return a Sheets object that can be used to grab and write data
	 * @throws IOException for when an I/O error occurs
	 * @throws GeneralSecurityException
	 */
	private Sheets createSheetsService(GoogleCredential cred) throws IOException, GeneralSecurityException {
		//Create new transport
		HttpTransport httpTransportSheets = GoogleNetHttpTransport.newTrustedTransport();
		//Print message
		System.out.print("Building Sheets Service...");
		//Time how long it takes
		long curTime = System.currentTimeMillis();
	    //Build a Sheets object
	    Sheets sheets = new Sheets.Builder(httpTransportSheets, jsonFactory, cred)
	        .setApplicationName("VexInfo.io - Sheets Usage")
	        .build();
	    //Get time difference
	    int timeDif = (int)(System.currentTimeMillis() - curTime);
	    //Print out time taken
	    System.out.printf("(%d ms)%n", timeDif);
	    //Return new Sheets object
	    return sheets;
	}
	
	/**
	 * Creates a Drive object that can be used to make a request for data
	 * 
	 * @return a Drive object that can be used to edit permissions
	 * @throws IOException for when an I/O error occurs
	 * @throws GeneralSecurityException
	 */
	private Drive createDriveService(GoogleCredential cred) throws IOException, GeneralSecurityException{
		//Create new transport
		HttpTransport httpTransportDrive = GoogleNetHttpTransport.newTrustedTransport();
		//Print message
		System.out.print("Building Drive Service...");
		//Time how long it takes
		long curTime = System.currentTimeMillis();
		//Build drive object and return it
		Drive drive = new Drive.Builder(httpTransportDrive, jsonFactory, cred)
				.setApplicationName("VexInfo.io - Drive Usage")
				.build();
		//Get time difference
	    int timeDif = (int)(System.currentTimeMillis() - curTime);
	    //Print out time taken
	    System.out.printf("(%d ms)%n", timeDif);
	    //Print out break
	    System.out.println("-----------------------------------------------------------");
	    //Return new Drive object 
	    return drive;
	}

	/**
	 * Retrieves an access token using the refresh token for the Sheets API
	 * 
	 * @throws IOException for when an I/O error occurs
	 * @throws GeneralSecurityException 
	 */
	public String setAccessToken() throws IOException, GeneralSecurityException{
		/*
		 * Note to users who plan to use this:
		 * 
		 * On Github, the Constants.java file will not show since I put it in 
		 * gitignore. In order to use this on your own, make a new file 
		 * Constants.java as an interface, and simply input
		 * the values "GOOGLE_CLIENT_ID" and "GOOGLE_CLIENT_SECRET", as well as 
		 * "GOOGLE_REFRESH_TOKEN".
		 */
		//Create a token response using refresh token and oauth credentials
		TokenResponse token_response = new GoogleRefreshTokenRequest(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), 
				Constants.GOOGLE_REFRESH_TOKEN, Constants.GOOGLE_CLIENT_ID, Constants.GOOGLE_CLIENT_SECRET)
				.execute();
		//Set the access token
		return token_response.getAccessToken();   
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									 SHEETS METHODS										//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Executes a create request to make a new spreadsheet
	 * 
	 * @param sheetsService the Sheets object with an authenticated credential
	 * @throws IOException for when an I/O error occurs
	 */
	public void executeCreateRequest(Sheets sheetsService) throws IOException {
		System.out.printf("Creating Google Sheet...");
		//Time how long algorithm takes
		long curTime = System.currentTimeMillis();
		//Create a request body and set appropriate title
		Spreadsheet requestBody = new Spreadsheet()
				.setProperties(new SpreadsheetProperties().set("title", "VexInfo.io - " + this.eventName));
		//Create a request to create a spreadsheet
		Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);
		//Execute the request and grab response
		Spreadsheet response = request.execute();
		//Set the proper spreadsheetId for the rest of the program
		this.spreadsheetId = response.getSpreadsheetId();
		//Set the URL of spreadsheet
		this.spreadsheetURL = response.getSpreadsheetUrl();
		//Get how long algorithm has taken
		long timeTaken = System.currentTimeMillis() - curTime;
		//Print success message(Format below)
		System.out.printf("(%d) ms%n%s%n", timeTaken, this.spreadsheetURL);
		//Print break
		System.out.println("-----------------------------------------------------------");
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
		//Build column #1 of the spreadsheet
		String[] names = new String[19];
		//Put default column #1 values
		putNames(names);
		//Build list
		List<List<Object>> topValues = Arrays.asList(Arrays.asList(names));
		//Configure body for request as ValueRange
		ValueRange topBody = new ValueRange().setValues(topValues);
		//Build request and execute
		@SuppressWarnings("unused")
		UpdateValuesResponse topResult = 
				sheetsService.spreadsheets().values().update(this.spreadsheetId, "Sheet1!A1:S1", topBody)
				.setValueInputOption("USER_ENTERED")
				.setIncludeValuesInResponse(false)
				.execute();
		//Print initialize message
		System.out.printf("Preparing (%d) Teams (Season:%s)%n", teamList.length, this.season);
		//Start with ArrayList, cast to List later
		List<List<Object>> values = new ArrayList<List<Object>>();
		//Loop through team list
		for(int i = 0; i < teamList.length; i++) {
			//Time how long each loop takes
			long sTime = System.currentTimeMillis();
			//Grab team name
			String n = teamList[i];
			//Parse team and calculate data
			Team t = new Team.TeamBuilder(n, this.season)
					.setTeamData()
					.setEventData()
					.setRankingData()
					.setSeasonData()
					.setSkillsData()
					.build();
			//Initialize array for inputting data
			String[] valuesArr = new String[19];
			//Build array with proper data
			buildValues(valuesArr, t);
			//Add to list
			values.add(Arrays.asList(valuesArr));
			//Time taken
			long timeTaken = System.currentTimeMillis() - sTime;
			//Print-out
			System.out.printf("Team %s inputted in (%d) ms\n", n.toString(), timeTaken);
		}
		//Time how long the write request takes
		long writeTime = System.currentTimeMillis();
		//Cast ArrayList to List
		List<List<Object>> inputValues = (List)values;
		//Configure body for input
		ValueRange body = new ValueRange()
				.setValues(inputValues);
		//Configure write-range
		String customRange = String.format("Sheet1!A2:S%d", (teamList.length + 2));
		//Execute write request
		sheetsService.spreadsheets().values()
			.update(this.spreadsheetId, customRange, body)
			.setValueInputOption("USER_ENTERED")
			.setIncludeValuesInResponse(false)
			.execute();
		//Time how long the write request takes
		long writeTimeTaken = System.currentTimeMillis() - writeTime;
		//Print out how long write time took
		System.out.printf("Write time: (%d) ms\n", writeTimeTaken);
		//Establish how long algorithm took to run(milliseconds)
		long runtime = System.currentTimeMillis() - startTime;
		//Convert to seconds
		double runtimeInSeconds = (double)runtime/1000;
		//Print success message
		System.out.printf("Success - %d Teams Updated in %.2f Seconds\n", teamList.length, runtimeInSeconds);
		//Print break
		System.out.println("-----------------------------------------------------------");
	}
	
	/**
	 * Applies conditional formatting to the sheet to highlight the sections with "NOT_FOUND" text.
	 * For all cells except ones for skills, this color will be red. For skills cells, this will be 
	 * orange. 
	 * 
	 * @param sheetsService the Sheets object with an authenticated credential
	 * @throws IOException for when an I/O error occurs
	 */
	public void applyConditionalFormatting(Sheets sheetsService) throws IOException {
		//Get start time to time how long it takes
		long startTime = System.currentTimeMillis();
		//Verbose message
		System.out.printf("Applying conditional formatting...");
		/*-------First conditional formatting(RED highlighting)---------------*/
		//Build ranges(use sheets GridRange, not Java.util)
		com.google.api.services.sheets.v4.model.GridRange ranges1_1 = new com.google.api.services.sheets.v4.model.GridRange();
		ranges1_1.setSheetId(0);
		ranges1_1.setStartColumnIndex(5);
		ranges1_1.setEndColumnIndex(14);
		ranges1_1.setStartRowIndex(1);
		com.google.api.services.sheets.v4.model.GridRange ranges1_2 = new com.google.api.services.sheets.v4.model.GridRange();
		ranges1_2.setSheetId(0);
		ranges1_2.setStartColumnIndex(17);
		ranges1_2.setEndColumnIndex(18);
		ranges1_2.setStartRowIndex(1);
		List<com.google.api.services.sheets.v4.model.GridRange> ranges1 = new ArrayList<>();
		ranges1.add(ranges1_1);
		ranges1.add(ranges1_2);
		
		//Build values
		List<ConditionValue> values1 = new ArrayList<>();
		values1.add(new ConditionValue().setUserEnteredValue("NOT_FOUND"));
		//Build condition
		BooleanCondition condition1 = new BooleanCondition();
		condition1.setType("TEXT_EQ");
		condition1.setValues(values1);
		
		//Build backGroundColor
		Color backgroundColor1 = new Color()
				.setRed((float)1.0);
		//Build format
		com.google.api.services.sheets.v4.model.CellFormat format1 = new com.google.api.services.sheets.v4.model.CellFormat()
				.setBackgroundColor(backgroundColor1);
		
		//Build booleanRule
		BooleanRule booleanRule1 = new BooleanRule();
		booleanRule1.setCondition(condition1);
		booleanRule1.setFormat(format1);
		
		//Build ConditionalFormatRule
		ConditionalFormatRule rule1 = new ConditionalFormatRule();
		rule1.setRanges(ranges1);
		rule1.setBooleanRule(booleanRule1);
		
		//Build AddConditionalFormatRule request
		AddConditionalFormatRuleRequest conditionalFormatRule1 = new AddConditionalFormatRuleRequest();
		conditionalFormatRule1.setRule(rule1);
		conditionalFormatRule1.setIndex(0);
		
		//Create list of requests
		List<Request> requests1 = new ArrayList<>();
		requests1.add(new Request().setAddConditionalFormatRule(conditionalFormatRule1));
		//Build request body
		BatchUpdateSpreadsheetRequest requestBody1 = new BatchUpdateSpreadsheetRequest()
				.setRequests(requests1);
		//Build the request
		Sheets.Spreadsheets.BatchUpdate request1 = sheetsService
				.spreadsheets().batchUpdate(this.spreadsheetId, requestBody1);
		//Execute request
		request1.execute();
		
		/*-------------Second conditional formatting(ORANGE highlighting)-----------------*/
		
		//Build ranges(use sheets GridRange, not Java.util)
		com.google.api.services.sheets.v4.model.GridRange ranges2_1 = new com.google.api.services.sheets.v4.model.GridRange();
		ranges2_1.setSheetId(0);
		ranges2_1.setStartColumnIndex(14);
		ranges2_1.setEndColumnIndex(17);
		ranges2_1.setStartRowIndex(1);
		List<com.google.api.services.sheets.v4.model.GridRange> ranges2 = new ArrayList<>();
		ranges2.add(ranges2_1);
		
		//Build values
		List<ConditionValue> values2 = new ArrayList<>();
		values2.add(new ConditionValue().setUserEnteredValue("NOT_FOUND"));
		//Build condition
		BooleanCondition condition2 = new BooleanCondition();
		condition2.setType("TEXT_EQ");
		condition2.setValues(values2);
		
		//Build backGroundColor
		Color backgroundColor2 = new Color()
				.setRed((float)1.0)
				.setGreen((float)0.71)
				.setBlue((float)0.24);
		//Build format
		com.google.api.services.sheets.v4.model.CellFormat format2 = new com.google.api.services.sheets.v4.model.CellFormat()
				.setBackgroundColor(backgroundColor2);
		
		//Build booleanRule
		BooleanRule booleanRule2 = new BooleanRule();
		booleanRule2.setCondition(condition2);
		booleanRule2.setFormat(format2);
		
		//Build ConditionalFormatRule
		ConditionalFormatRule rule2 = new ConditionalFormatRule();
		rule2.setRanges(ranges2);
		rule2.setBooleanRule(booleanRule2);
		
		//Build AddConditionalFormatRule request
		AddConditionalFormatRuleRequest conditionalFormatRule2 = new AddConditionalFormatRuleRequest();
		conditionalFormatRule2.setRule(rule2);
		conditionalFormatRule2.setIndex(0);
		
		//Create list of requests
		List<Request> requests2 = new ArrayList<>();
		requests2.add(new Request().setAddConditionalFormatRule(conditionalFormatRule2));
		//Build request body
		BatchUpdateSpreadsheetRequest requestBody2 = new BatchUpdateSpreadsheetRequest()
				.setRequests(requests2);
		//Build the request
		Sheets.Spreadsheets.BatchUpdate request2 = sheetsService
				.spreadsheets().batchUpdate(this.spreadsheetId, requestBody2);
		//Execute request
		request2.execute();
		
		//Get how long it took
		long runtime = System.currentTimeMillis() - startTime;
		System.out.printf("(%d ms)\n", runtime);
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									  DRIVE METHODS										//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Sets the permission ID for the current email on the Drive object
	 * 
	 * @param driveService the authenticated Drive object
	 * @throws IOException for when an I/O error occurs
	 */
	private String setPermissionId(Drive driveService) throws IOException {
		//Return get the permission ID using Drive.about.get
		return new JSONObject(driveService.about().get().setFields("user/permissionId").execute())
				.getJSONObject("user")
				.getString("permissionId");
	}
	
	/**
	 * Transfers the ownership of the Google Sheet to usrEmail, which is specified
	 * in the constructor of {@link TeamAPI}
	 * 
	 * @param driveService an authenticated Drive object
	 * @throws IOException for when an I/O error occurs
	 */
	private void transferOwnership(Drive driveService, String usrEmail) throws IOException {
		//Print message
		System.out.printf("Transferring ownership to %s...", usrEmail);
		//Time how long it takes
		long curTime = System.currentTimeMillis();
		//Use try-catch to catch if the program cannot transfer ownership
		try {
			//Build request body
			Permission body = new Permission()
					.setRole("owner")
					.setType("user")
					.setEmailAddress(usrEmail);
			//Execute Drive request to transfer ownership
			driveService.permissions().create(this.spreadsheetId, body)
					.setFileId(this.spreadsheetId)
					.setEmailMessage(String.format("VexInfo.io - %s%n%n%s", this.eventName, this.spreadsheetURL))
					.setSendNotificationEmail(true)
					.setSupportsTeamDrives(false)
					.setTransferOwnership(true)
					.setUseDomainAdminAccess(false)
					.execute();
			//Execute drive request to remove current email from sheet
			String permissionId = setPermissionId(driveService);
			driveService.permissions().delete(this.spreadsheetId,  permissionId)
					.setFileId(this.spreadsheetId)
					.setPermissionId(permissionId)
					.setSupportsTeamDrives(false)
					.setUseDomainAdminAccess(false)
					.execute();
			//Time taken
			long timeTaken = System.currentTimeMillis() - curTime;
			//Print message
			System.out.printf("(%d ms)%n", timeTaken);
		}catch(com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
			System.err.println("\nTRANSFER REQUEST ERROR");
			System.err.println(e.getMessage() + "\n");
		}
	}
	
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									PROCESSING METHODS									//
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * <p>
	 * Processes a RobotEvents.com link to be able to get an events SKU, 
	 * the season for that event, the name of the event, and a team list
	 * for the event.
	 * </p>
	 * 
	 * <p>
	 * Either gets season for the current tournament(tied to the RobotEvents link), or
	 * allows the user to specify their own season. If season is empty, it will used the 
	 * season tied to the RobotEvents link. If not empty, it will use the season specified.
	 * </p>
	 * <p>
	 * <b>Note:</b> Team lists can only be generated <i>4 weeks</i> before the start date
	 * of the tournament. Trying to do so will cause the program to stop. This is because
	 * of restrictions in the VexDB database.
	 * </p>
	 * 
	 * @param s the URL of the robot events link
	 * @param season Any valid season within the VexDB query list.(can also be "" to get current season)
	 * @throws JSONException for when JSON API encounters error
	 * @throws IOException for when an I/O error occurs
	 */
	private void processLink(String s, String season) throws JSONException, IOException {
		//Create URL from link
		URL link = new URL(s);
		//Get file path of url
		String[] filePath = link.getPath().split("/");
		//Get and set event code
		this.sku = filePath[filePath.length - 1].replace(".html", "");
		//Get JSON data from API
		JSONObject eventJson = Team.TeamBuilder
				.readJsonFromUrl("https://api.vexdb.io/v1/get_events?sku=" + this.sku)
				.getJSONArray("result")
				.getJSONObject(0);
		//Set event season
		if(season.equals("")) {
			this.season = eventJson.getString("season");
		} else {
			this.season = season;
		}
		//Set event name
		this.eventName = eventJson.getString("name");
		//Print event name
		System.out.printf("Event Name: %s%n", this.eventName);
		//Print out event code
		System.out.printf("Event Code: %s\n", this.sku);
		//Print season for stats
		System.out.printf("Season: %s\n", this.season);
		//Print out venue name
		System.out.printf("Venue: %s\n", eventJson.getString("loc_venue"));
		//Print out address
		System.out.printf("Address: %s\n", eventJson.getString("loc_address1"));
		//Print out city/state
		System.out.printf("\t%s, %s %s\n", eventJson.getString("loc_city"), eventJson.getString("loc_region"),
				eventJson.getString("loc_postcode"));
		//Print out county
		System.out.printf("Country: %s\n", eventJson.getString("loc_country"));
		//Set event date(only grab start day, ignore time)
		//Format: YYYY-MM-DD
		this.eventDate = eventJson.getString("start").split("T")[0];
		//Check date to see if it first 4-week restriction
		checkDate();
		//Build JSON array from SKU
		JSONArray result = Team.TeamBuilder
				.readJsonFromUrl("https://api.vexdb.io/v1/get_teams?sku=" + this.sku)
				.getJSONArray("result");
		//Initialize team list
		String[] teams = new String[result.length()];
		//Fill the team list
		for(int i = 0; i < result.length(); i++) {
			teams[i] = result.getJSONObject(i).getString("number");
		}
		//Set team list
		this.teamList = teams;
	}
	
	/**
	 * Checks the current date and compares it to the event date(more specifically,
	 * it compares it to the date exactly <u>28 days(4 weeks)</u> before the event date). 
	 * <p>
	 * If the current date isn't within <u>4 weeks(28 days)</u> of the event date(or the event hasn't already happened), then 
	 * the program will log it as an error and exit with an error code.
	 * </p>
	 * <p>
	 * If the current date is within <u>4 weeks</u> of the event date(or the event has already happened), then
	 * the program will continue.
	 * </p>
	 */
	public void checkDate(){
		//Print break
		System.out.println("-----------------------------------------------------------");
		//New Calendar instance
		Calendar c = Calendar.getInstance();
		//Set leniency to true, so program could subtract 4 weeks(28 days)
		//from event date, without the calendar object throwing an exception
		c.setLenient(true);
		//Set to start of current day
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		//Put into date object
		Date today = c.getTime();
		//Get event date in an array of strings(Format: YYYY-MM-DD)
		String[] eventTimeInfoStr = this.eventDate.split("-");
		//Initialize and convert string array to int array
		int[] eventTimeInfo = new int[3];
		//Loop through array to fill with integers
		for(int i = 0; i < eventTimeInfo.length; i++) {
			eventTimeInfo[i] = Integer.parseInt(eventTimeInfoStr[i]);
		}
		//Set calendar to event date
		c.set(eventTimeInfo[0], eventTimeInfo[1], eventTimeInfo[2]);
		//Get the actual date
		Date eventDateActual = c.getTime();
		//Set event date to exactly 4 weeks(28 days) before its date,
		//since that is what the program is checking for
		c.set(eventTimeInfo[0], (eventTimeInfo[1] - 1), (eventTimeInfo[2] - 28));
		//Check date with this specified date
		Date dateSpecified = c.getTime();
		//If current Date is greater than 4 weeks before the event
		if(today.before(dateSpecified)) { //Date restriction not met
			//Get difference of dates in milliseconds
			long difInMs = dateSpecified.getTime() - today.getTime();
			//Convert milliseconds to days
			int dayDifference = (int)TimeUnit.MILLISECONDS.toDays(difInMs);
			//Create a DateFormat object to get desired format for date
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			//Print out dates
			System.out.printf("Today:%s\n", df.format(today));
			System.out.printf("Actual:%s\n", df.format(eventDateActual));
			System.out.printf("Specified:%s\n", df.format(dateSpecified));
			//Log the issue
			LOGGER.error(String.format("Requirement not met. Wait (%d) days.", dayDifference));
			//Print out messages
			System.out.println("DATE CHECK:FALSE");
			System.err.println("CANNOT GET DATA FROM API UNTIL 4-WEEK RESTRICTION MET");
			System.err.printf("WAIT (%d) DAYS%nEXITING PROGRAM(1)%n", dayDifference);
			//Stop program
			System.exit(1);
		} else { //Date restriction met
			//Format date Strings
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			System.out.printf("Today's Date: %s%n", df.format(today));
			System.out.printf("Event's Date(Actual): %s%n", df.format(eventDateActual));
			System.out.printf("Event's Date(4 Weeks Prior): %s%n", df.format(dateSpecified));
			System.out.println("DATE CHECK:TRUE");
			//Print break
			System.out.println("-----------------------------------------------------------");
			//Program continues running
		}
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
	 * 
	 * @param arr the array to write the statistics to
	 * @param t the team who the statistics are for
	 */
	
	@SuppressWarnings("static-access")
	private void buildValues(String[] arr, Team t) {
		//Make NOT FOUND string
		final String ntFnd = "NOT_FOUND";
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
		arr[5] = t.fieldIndicators.get("opr") ? Double.toString(t.getAvgOPR()) : ntFnd;
		//6.
		arr[6] = t.fieldIndicators.get("dpr") ? Double.toString(t.getAvgDPR()) : ntFnd;
		//7.
		arr[7] = t.fieldIndicators.get("ccwm") ? Double.toString(t.getAvgCCWM()) : ntFnd;
		//8.
		arr[8] = t.fieldIndicators.get("ap") ? Double.toString(t.getAvgAP()) : ntFnd;
		//9.
		arr[9] = t.fieldIndicators.get("sp") ? Integer.toString(t.getAvgSP()) : ntFnd;
		//10.
		arr[10] = t.fieldIndicators.get("trsp") ? Integer.toString(t.getAvgTRSP()) : ntFnd;
		//11.
		arr[11] = t.fieldIndicators.get("vrating_rank") ? Integer.toString(t.getvrating_rank()) : ntFnd;
		//12.
		arr[12] = t.fieldIndicators.get("vrating") ? Double.toString(t.getvrating()) : ntFnd;
		//13.
		arr[13] = t.fieldIndicators.get("rank") ? Integer.toString(t.getAvgRank()) : ntFnd;
		//14.
		arr[14] = t.fieldIndicators.get("skills_auton") ? Integer.toString(t.getAvgSkillsScore_auton()) : ntFnd;
		//15.
		arr[15] = t.fieldIndicators.get("skills_robot") ? Integer.toString(t.getAvgSkillsScore_robot()) : ntFnd;
		//16.
		arr[16] = t.fieldIndicators.get("skills_combined") ? Integer.toString(t.getAvgSkillsScore_combined()) : ntFnd;
		//17.
		arr[17] = t.fieldIndicators.get("max_score") ? Integer.toString(t.getAvgMaxScore()) : ntFnd;
		//18.
		arr[18] = Integer.toString(t.getNumEvents());
	}
	
	/**
	 * Defaults the first column of the spreadsheet to proper values.
	 * 
	 * @param a the array of strings that will be used for the first column
	 */
	private void putNames(String[] a) {
		//Represents column #1 of the spreadsheet
		a[0] = "Team";
		a[1] = "Team Name";
		a[2] = "Organization";
		a[3] = "Location";
		a[4] = "VexDB Link";
		a[5] = "Average OPR";
		a[6] = "Average DPR";
		a[7] = "Average CCWM";
		a[8] = "Average AP's";
		a[9] = "Average SP's";
		a[10] = "Average TSRP's";
		a[11] = "Vrating Rank";
		a[12] = "Vrating";
		a[13] = "Average Rank";
		a[14] = "Average Skills Score(Auton)";
		a[15] = "Average Skills Score(Robot)";
		a[16] = "Average Skills Score(Combined)";
		a[17] = "Average Max Score";
		a[18] = String.format("Total Events:(%s)", this.season);
	}
	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									   GETTER METHODS								    //
	//																						//
	------------------------------------------------------------------------------------------
	*/
	
	/**
	 * Retrieves the current team list provided by proccessResponseIntoTeamList()
	 * 
	 * @return the team list as an array of Strings
	 */
	public List<String> getTeamList() { return Arrays.asList(this.teamList); }
	
	/**
	 * Returns the event name and spreadsheet ID of the current {@link TeamAPI} instance
	 */
	@Override
	public String toString() {
		return String.format("VexInfo.io - %s (%s)", this.eventName, this.spreadsheetId);
	}
}