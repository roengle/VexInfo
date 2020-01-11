package com.warrenrobotics;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Interacts with Google Sheets using the Google Sheets API v4 to automatically
 * assign certain statistics to them.
 * </p>
 * <p>
 * This class is responsible for all interactions with Google Sheets.
 * </p>
 * <p>
 * To create a Google Sheet for an event, pass a {@link VexEvent} to
 * {@link #writeVexEvent}.
 * </p>
 * @author Robert Engle, Ivan Wick
 * @version 0.3.1-beta.1
 * @since 2018-02-21
 *
 */
public class SheetsWriter {
    private Credential credential;
    private Sheets sheetsService;
    //Constants
    public final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    public SheetsWriter() throws GeneralSecurityException, IOException{
        this.credential = createCredential();
        this.sheetsService = createSheetsService(credential);
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
    private Credential createCredential() throws GeneralSecurityException, IOException {
        //Print message
        System.out.print("Building API Credential...\n");
        //Time how long it takes
        long curTime = System.currentTimeMillis();
        //Get a credential object using credentials
        InputStream in = this.getClass().getResourceAsStream("credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), clientSecrets, scopes)
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        //Get time difference
        int timeDif = (int)(System.currentTimeMillis() - curTime);
        //Print out time taken
        System.out.printf("(%d ms)%n", timeDif);
        return credential;
    }

    /**
     * Creates a Sheets object that can be used to make a request for data
     *
     * @return a Sheets object that can be used to grab and write data
     * @throws IOException for when an I/O error occurs
     * @throws GeneralSecurityException
     */
    private Sheets createSheetsService(Credential cred) throws IOException, GeneralSecurityException {
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

    public String writeVexEvent(VexEvent event) throws IOException, InterruptedException {
        //Create spreadsheet
        String spreadsheetId = executeCreateRequest(sheetsService, event);
        //Execute a write request
        executeWriteRequest(sheetsService, spreadsheetId, event);
        //Apply text-centering and dimension resizing
        applyDimensionAutoResize(sheetsService, spreadsheetId);
        //Apply conditional formatting
        applyConditionalFormatting(sheetsService, spreadsheetId);

        return spreadsheetId;
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
     * @return spreadsheetId of newly created sheet
     * @throws IOException for when an I/O error occurs
     */
    private String executeCreateRequest(Sheets sheetsService, VexEvent event) throws IOException {
        System.out.printf("Creating Google Sheet...");
        //Time how long algorithm takes
        long curTime = System.currentTimeMillis();
        //Create a request body and set appropriate title
        Spreadsheet requestBody = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().set("title", "VexInfo.io - " + event.getEventName()));
        //Create a request to create a spreadsheet
        Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);
        //Execute the request and grab response
        Spreadsheet response = request.execute();
        //Set the proper spreadsheetId for the rest of the program
        String spreadsheetId = response.getSpreadsheetId();
        //Set the URL of spreadsheet
        String spreadsheetURL = response.getSpreadsheetUrl();
        //Get how long algorithm has taken
        long timeTaken = System.currentTimeMillis() - curTime;
        //Print success message(Format below)
        System.out.printf("(%d) ms%n%s%n", timeTaken, spreadsheetURL);
        //Print break
        System.out.println("-----------------------------------------------------------");

        return spreadsheetId;
    }

    /**
     * Executes a write request to write data to a spreadsheet.
     *
     * @param sheetsService the Sheets object with an authenticated credential
     * @throws IOException for when an I/O error occurs
     * @throws InterruptedException for when a thread is being occupied and interrupted
     */
    private void executeWriteRequest(Sheets sheetsService, String spreadsheetId, VexEvent event) throws IOException, InterruptedException{
        //Debugging for how long algorithm takes to run with certain data sets
        long startTime = System.currentTimeMillis();
        //Build column #1 of the spreadsheet
        String[] names = new String[20];
        //Put default column #1 values
        putNames(names);
        //Build list
        List<List<Object>> topValues = Arrays.asList(Arrays.asList((Object[])names));
        //Configure body for request as ValueRange
        ValueRange topBody = new ValueRange().setValues(topValues);
        //Build request and execute
        @SuppressWarnings("unused")
        UpdateValuesResponse topResult =
                sheetsService.spreadsheets().values().update(spreadsheetId, "Sheet1!A1:T1", topBody)
                        .setValueInputOption("USER_ENTERED")
                        .setIncludeValuesInResponse(false)
                        .execute();
        int teamCount = event.getTeamList().size();
        //Print initialize message
        System.out.printf("Writing (%d) Teams (Season:%s)%n", teamCount, event.getSeason());
        //Start with ArrayList, cast to List later
        List<List<Object>> values = new ArrayList<List<Object>>();
        //Loop through team list
        for(Team t : event.getTeamList()) {
            //Initialize array for inputting data
            String[] valuesArr = new String[20];
            //Build array with proper data
            buildValues(valuesArr, t);
            //Add to list
            values.add(Arrays.asList((Object[])valuesArr));
        }
        //Time how long the write request takes
        long writeTime = System.currentTimeMillis();
        //Cast ArrayList to List
        List<List<Object>> inputValues = (List)values;
        //Configure body for input
        ValueRange body = new ValueRange()
                .setValues(inputValues);
        //Configure write-range
        String customRange = String.format("Sheet1!A2:T%d", (teamCount + 2));
        //Execute write request
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, customRange, body)
                .setValueInputOption("USER_ENTERED")
                .setIncludeValuesInResponse(false)
                .execute();
        System.out.print("Write Time...");
        //Time how long the write request takes
        long writeTimeTaken = System.currentTimeMillis() - writeTime;
        //Print out how long write time took
        System.out.printf("(%d) ms\n", writeTimeTaken);
        //Establish how long algorithm took to run(milliseconds)
        long runtime = System.currentTimeMillis() - startTime;
        //Convert to seconds
        double runtimeInSeconds = (double)runtime/1000;
        //Print success message
        System.out.printf("Success - (%d) Teams Updated in (%.2f) Seconds\n", teamCount, runtimeInSeconds);
        //Print break
        System.out.println("-----------------------------------------------------------");
    }

    /**
     * Applies automatic dimension resizing for the sheet. This allows for the data to not be "squished" together.
     * The text is automatically centered, as the automatic dimension resizing requires this for proper column dimension.
     *
     * @param sheetsService the Sheets object with an authenticated credential
     * @throws IOException for when an I/O error occurs
     */
    private void applyDimensionAutoResize(Sheets sheetsService, String spreadsheetId) throws IOException {
        //Get start time to time how long it takes
        long startTime = System.currentTimeMillis();
        //Verbose message
        System.out.print("Applying dimension resizing...");

        /*-----------------------Build repeatCellRequest-----------------------*/
        //Build GridRange for repeatCellRequest. This handles the range which the request will act on
        GridRange repeatCellRange = new GridRange();
        repeatCellRange.setSheetId(0);
        repeatCellRange.setStartColumnIndex(0);
        repeatCellRange.setEndColumnIndex(20);
        repeatCellRange.setStartRowIndex(0);

        //Build CellFormat for userEnteredFormat
        CellFormat userEnteredCellFormat = new CellFormat();
        userEnteredCellFormat.setHorizontalAlignment("CENTER");
        userEnteredCellFormat.setVerticalAlignment("MIDDLE");
        //Build CellData. This handles what will happen to the cells in the range
        CellData repeatCellData = new CellData();
        repeatCellData.setUserEnteredFormat(userEnteredCellFormat);

        //Build fields
        String fields = "userEnteredFormat(horizontalAlignment,verticalAlignment)";

        //Build repeatCellRequest using the range, cell format, and fields
        RepeatCellRequest repeatCellRequest = new RepeatCellRequest();
        repeatCellRequest.setRange(repeatCellRange);
        repeatCellRequest.setCell(repeatCellData);
        repeatCellRequest.setFields(fields);
        /*-------------------Build autoResizeDimensionsRequest-----------------*/
        //Build DimensionRange for autoResizeDimensionRequest. This handles where the request will act on
        DimensionRange dimensionRange = new DimensionRange();
        dimensionRange.setDimension("COLUMNS");
        dimensionRange.setStartIndex(0);
        dimensionRange.setEndIndex(20);
        dimensionRange.setSheetId(0);

        AutoResizeDimensionsRequest autoResizeDimensionsRequest = new AutoResizeDimensionsRequest();
        autoResizeDimensionsRequest.setDimensions(dimensionRange);
        /*----------------------------Build requests---------------------------*/
        //Make a list of requests and add our previous requests to it
        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setRepeatCell(repeatCellRequest));
        requests.add(new Request().setAutoResizeDimensions(autoResizeDimensionsRequest));
        /*--------------------Build BatchUpdateSpreadsheetRequest--------------*/
        BatchUpdateSpreadsheetRequest mainRequestBody = new BatchUpdateSpreadsheetRequest();
        mainRequestBody.setRequests(requests);
        /*-----------------------------Execute Request-------------------------*/
        sheetsService.spreadsheets()
                .batchUpdate(spreadsheetId, mainRequestBody)
                .execute();
        /*---------------------------------Runtime-----------------------------*/
        long runtime = System.currentTimeMillis() - startTime;
        System.out.printf("(%d ms)\n", runtime);
    }
    
    /**
     * Applies conditional formatting to the sheet.
     * <ul>
     * 	The following conditional formatting rules are applied:
     * 	<li>
     * 		Italicizes the text in cells that show NOT_FOUND
     * 	</li>
     * 	<li>
     * 		Applies a color gradient to numerical data points in the sheet. A greener color represents a
     * 		better value in comparison to the rest, while a more red color represents a worse value in
     * 		comparison to the others.
     * 	</li>
     * </ul>
     *
     * @param sheetsService the Sheets object with an authenticated credential
     * @throws IOException for when an I/O error occurs
     */
    private void applyConditionalFormatting(Sheets sheetsService, String spreadsheetId) throws IOException {
        //Get start time to time how long it takes
        long startTime = System.currentTimeMillis();
        //Verbose message
        System.out.printf("Applying conditional formatting...");

        /*-------First conditional formatting(Italic on NOT_FOUND)---------------*/

        //Build ranges(use sheets GridRange, not Java.util)
        com.google.api.services.sheets.v4.model.GridRange ranges1_1 = new com.google.api.services.sheets.v4.model.GridRange();
        ranges1_1.setSheetId(0);
        ranges1_1.setStartColumnIndex(4);
        ranges1_1.setEndColumnIndex(18);
        ranges1_1.setStartRowIndex(1);
        List<com.google.api.services.sheets.v4.model.GridRange> ranges1 = new ArrayList<>();
        ranges1.add(ranges1_1);

        //Build values
        List<ConditionValue> values1 = new ArrayList<>();
        values1.add(new ConditionValue().setUserEnteredValue("NOT_FOUND"));
        //Build condition
        BooleanCondition condition1 = new BooleanCondition();
        condition1.setType("TEXT_EQ");
        condition1.setValues(values1);

        //Build format(use italic for NOT_FOUND)
        CellFormat format1 = new CellFormat()
                .setTextFormat(new TextFormat().setItalic(true));

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
        //Build and execute request
        sheetsService.spreadsheets()
                .batchUpdate(spreadsheetId, requestBody1)
                .execute();

        /*------------------------------Apply Gradients-----------------------------------*/

        /* Build ranges */
        /**
         * Using a loop like this creates a bunch of conditional formats, but allows each column to have its own gradient.
         */
        //Declare start and end columns
        int startColumn = 4;
        int endColumn = 18;
        //Loop through respective columns
        for(int i = startColumn; i < endColumn; i++) {
            //Initialize list for ranges
            List<GridRange> gradientRanges = new ArrayList<>();
            //Add respective range
            gradientRanges.add(new GridRange().setSheetId(0).setStartColumnIndex(i).setEndColumnIndex((i + 1)));
            /* Build Colors */

            //Create color for minpoint
            Color minColor = new Color()
                    .setRed(0.796875F);
            //Create color for midpoint
            Color midColor = new Color()
                    .setRed(0.94140625F)
                    .setGreen(0.7578125F)
                    .setBlue(0.1953125F);
            //Create color for maxpoint
            Color maxColor = new Color()
                    .setGreen(1.0F);

            /* Build InterpolationPoint(s)*/

            //Create and build InterpolationPoint for minpoint(reverse from max to min, since lower is better for dpr and vrating rank)
            InterpolationPoint min = (i != 6 && i != 11) ?
                    new InterpolationPoint()
                            .setColor(minColor)
                            .setType("MIN")
                    :
                    new InterpolationPoint()
                            .setColor(maxColor)
                            .setType("MIN");
            //Create and build InterpolationPoint for midpoint
            InterpolationPoint mid = new InterpolationPoint()
                    .setColor(midColor)
                    .setType("PERCENT")
                    .setValue("50");
            //Create and build InterpolationPoint for maxpoint(reverse from max to min, since lower is better for dpr and vrating rank)
            InterpolationPoint max = (i != 6 && i != 11) ?
                    new InterpolationPoint()
                            .setColor(maxColor)
                            .setType("MAX")
                    :
                    new InterpolationPoint()
                            .setColor(minColor)
                            .setType("MAX");

            /* Build gradientRule */

            GradientRule gradientRule = new GradientRule()
                    .setMinpoint(min)
                    .setMidpoint(mid)
                    .setMaxpoint(max);

            /* Build ConditionalFormatRule */

            ConditionalFormatRule gradient_conditionalFormatRule = new ConditionalFormatRule()
                    .setRanges(gradientRanges)
                    .setGradientRule(gradientRule);

            /* Build AddConditionalFormatRuleRequest */

            AddConditionalFormatRuleRequest gradient_addConditionalFormatRuleRequest = new AddConditionalFormatRuleRequest()
                    .setRule(gradient_conditionalFormatRule)
                    .setIndex(0);

            /* Create list of requests */

            //Make list of requests
            List<Request> gradient_requests = new ArrayList<>();
            //Add our conditionalFormatRuleRequest
            gradient_requests.add(new Request().setAddConditionalFormatRule(gradient_addConditionalFormatRuleRequest));

            /* Build BatchUpdateSpreadsheetRequest */

            //Build request body
            BatchUpdateSpreadsheetRequest gradient_mainRequestBody = new BatchUpdateSpreadsheetRequest()
                    .setRequests(gradient_requests);

            /* Build and execute request*/
            sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetId, gradient_mainRequestBody)
                    .execute();
        }

        /*----------------------------------Runtime----------------------------*/
        long runtime = System.currentTimeMillis() - startTime;
        System.out.printf("(%d ms)\n", runtime);
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
     * 			<li>avgOPR</li>
     * 			<li>avgDPR</li>
     * 			<li>avgCCWM</li>
     * 			<li>avgWP</li>
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
     * 			<li>awards</li>
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
        //Fill the target array with corresponding team information
        arr[0] = t.getNumber();
        arr[1] = t.getTeamName();
        arr[2] = t.getTeamOrg();
        arr[3] = t.getTeamLocation();
        arr[4] = t.fieldIndicators.get("opr") ? Double.toString(t.getAvgOPR()) : ntFnd;
        arr[5] = t.fieldIndicators.get("dpr") ? Double.toString(t.getAvgDPR()) : ntFnd;
        arr[6] = t.fieldIndicators.get("ccwm") ? Double.toString(t.getAvgCCWM()) : ntFnd;
        arr[7] = t.fieldIndicators.get("wp") ? Double.toString(t.getAvgWP()) : ntFnd;
        arr[8] = t.fieldIndicators.get("ap") ? Double.toString(t.getAvgAP()) : ntFnd;
        arr[9] = t.fieldIndicators.get("sp") ? Integer.toString(t.getAvgSP()) : ntFnd;
        arr[10] = t.fieldIndicators.get("trsp") ? Integer.toString(t.getAvgTRSP()) : ntFnd;
        arr[11] = t.fieldIndicators.get("vrating_rank") ? Integer.toString(t.getvrating_rank()) : ntFnd;
        arr[12] = t.fieldIndicators.get("vrating") ? Double.toString(t.getvrating()) : ntFnd;
        arr[13] = t.fieldIndicators.get("rank") ? Integer.toString(t.getAvgRank()) : ntFnd;
        arr[14] = t.fieldIndicators.get("skills_auton") ? Integer.toString(t.getAvgSkillsScore_auton()) : ntFnd;
        arr[15] = t.fieldIndicators.get("skills_robot") ? Integer.toString(t.getAvgSkillsScore_robot()) : ntFnd;
        arr[16] = t.fieldIndicators.get("skills_combined") ? Integer.toString(t.getAvgSkillsScore_combined()) : ntFnd;
        arr[17] = t.fieldIndicators.get("max_score") ? Integer.toString(t.getAvgMaxScore()) : ntFnd;
        //Subtract one since getNumEvents() includes the current event
        arr[18] = Integer.toString((t.getNumEvents() - 1));
        //Build string for displaying awards
        //Initialize output string
        String awardStr = "";
        //Iterate through award name count pair map
        for (Map.Entry<String, Integer> entry : t.getAwardNameCountPair().entrySet()) {
        	String awardName = entry.getKey();
        	Integer awardNum = entry.getValue();
        	awardStr += awardName + " x" + awardNum + "\n";
        }
        if(awardStr.length() != 0) {
        	awardStr = awardStr.substring(0, awardStr.length() - 1);
        }
        arr[19] = awardStr;
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
        a[4] = "Average OPR";
        a[5] = "Average DPR";
        a[6] = "Average CCWM";
        a[7] = "Average WP";
        a[8] = "Average AP's";
        a[9] = "Average SP's";
        a[10] = "Average TRSP's";
        a[11] = "Vrating Rank";
        a[12] = "Vrating";
        a[13] = "Average Rank";
        a[14] = "Average Skills Score(Auton)";
        a[15] = "Average Skills Score(Robot)";
        a[16] = "Average Skills Score(Combined)";
        a[17] = "Average Max Score";
        a[18] = "Previous Events";
        a[19] = "Awards";
    }
}