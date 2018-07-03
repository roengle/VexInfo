package com.warrenrobotics;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.SQLAdminScopes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.json.*;

/**
 * Interacts with Google Sheets using the Google Sheets API v4 to automatically take 
 * team names and assign certain statistics to them.
 * 
 * This class is responsible for all interactions with Google Sheets.
 * 
 * @author Robert Engle | WHS Robotics | Team 90241B
 * @version 1.1
 * @since 2018-02-21
 *
 */
public class TeamAPI {
	//Instance variables
	private String spreadsheetId; 
	private String range;
	private String valueRenderOption;
	private String dateTimeRenderOption;
	private String refreshToken;
	private String accessToken;
	private String scope;
	private ValueRange response;
	
	/**
	 * Constructs a TeamAPI object to interpret data from a Google Sheets using the Google Sheets API v4
	 * 
	 * @param spreadsheetId the id of the spreadsheet(commonly found in the link of the spreadsheet)
	 * @param valueRenderOption how values are represented in output. Default is FORMATTED_VALUE
	 * @param accessToken the oauth2 access token obtained from the OAuth 2.0 Playground
	 * @param scope the scope of data allowed access for the end user. Since we are planning on editing and reading spreadsheets, 
	 * 		  we will use https://www.googleapis.com/auth/spreadsheets
	 */
	public TeamAPI(String spreadsheetId, String valueRenderOption, String refreshToken, String scope) throws IOException, GeneralSecurityException{
		this.spreadsheetId = spreadsheetId;
		this.range = "Sheet1";
		this.valueRenderOption = valueRenderOption;
		this.dateTimeRenderOption = "SERIAL_NUMBER";
		this.refreshToken = refreshToken;
		this.scope = scope;
		//Assign access token
		setAccessToken();
		//Create sheet service and request data using it
		Sheets sheetsService = createSheetsService();
		Sheets.Spreadsheets.Values.Get request =
		    sheetsService.spreadsheets().values().get(this.spreadsheetId, this.range);
		    request.setValueRenderOption(this.valueRenderOption);
		    request.setDateTimeRenderOption(this.dateTimeRenderOption);
		this.response = request.execute();
		//Process into team list
		String[] teamList = processResponseIntoTeamList();
		//Loop through each name, grab statistics, and 
	}
	
	/**
	 * Creates a sheets service that can be used to make a request for data
	 * 
	 * @return a Sheets object that can be used to grab data
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private Sheets createSheetsService() throws IOException, GeneralSecurityException {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	    GoogleCredential credential = new GoogleCredential().setAccessToken(this.accessToken);
	    
	    return new Sheets.Builder(httpTransport, jsonFactory, credential)
	        .setApplicationName(this.scope)
	        .build();
	}
	
	/**
	 * Retrieves an access token using the refresh token
	 * 
	 * @return an access token that can be used to create a proper credential
	 */
	public void setAccessToken() throws IOException, GeneralSecurityException{
		//Initialize OAuth Credential strings
		String clientId;
		String clientSecret;
		/*
		 * Note to uses who plan to use this:
		 * 
		 * Create a txt file named "OAuth2Credentials.txt" (or anything you like and change the filepath below)
		 * Inside the text file, on the first line put only the OAuth2 client ID,
		 * and on the second line put only the OAuth2 client secret.
		 */
		String filePath = "src\\com\\warrenrobotics\\OAuth2Credentials.txt";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		//Set client id to first line
		clientId = br.readLine();
		//Set client secret to second line
		clientSecret = br.readLine();
		//Don't leak resources
		br.close();
		//Create a token response using refresh token and oauth credentials
		TokenResponse response = new GoogleRefreshTokenRequest(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), 
				this.refreshToken, clientId, clientSecret).execute();
		//Set the access token as the response
		this.accessToken = response.getAccessToken();   
	}
	
	/**
	 * Processes the response into an array of strings containing team names(IE: ["90241A", "90241B"])
	 * 
	 * @return the team names as an array of strings
	 */
	private String[] processResponseIntoTeamList() {
		String responseStr = this.response.toString();
		JSONObject json = new JSONObject(responseStr);
		JSONArray values = json.getJSONArray("values");
		String[] teams = new String[values.length()];
		//Start at index 1 to ignore top of row(which shows "Team")
		for(int i = 1; i < values.length(); i++) {
			teams[i] = values.getJSONArray(i).getString(0);
		}
		return teams;
	}
	
	/**
	 * Returns the ValueRange response as a String
	 * 
	 * @return a string of the request response
	 */
	public String getResponse() { return this.response.toString(); }
	/**
	 * Retrieves the current access token provided by setAccessToken()
	 * 
	 * @return an access token allowing access to a sheet
	 */
	public String getAccessToken() { return this.accessToken; }
	
}