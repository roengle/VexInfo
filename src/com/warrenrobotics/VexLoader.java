package com.warrenrobotics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class is responsible for loading a {@link VexEvent} using
 * a RobotEvents URL.
 * </p>
 * @author Robert Engle, Ivan Wick
 * @version 0.3.1-beta.1
 * @since 2018-02-21
 *
 */
public class VexLoader {

    //Logger
    private static Logger LOGGER = LogManager.getLogger(VexLoader.class);

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
    public static VexEvent loadEvent(String s, String season) throws JSONException, IOException {
        String eventName;

        //Create URL from link
        URL link = new URL(s);

        String sku = extractSKU(link);

        //Get JSON data from API
        JSONObject eventJson = Team.TeamBuilder
                .readJsonFromUrl("https://api.vexdb.io/v1/get_events?sku=" + sku)
                .getJSONArray("result")
                .getJSONObject(0);
        //Set event season
        season = season.equals("") ? eventJson.getString("season") : season ;
        //Set event name
        eventName = eventJson.getString("name");
        //Print event name
        System.out.printf("Event Name: %s%n", eventName);
        //Print out event code
        System.out.printf("Event Code: %s\n", sku);
        //Print season for stats
        System.out.printf("Season: %s\n", season);
        //Print out venue name
        System.out.printf("Venue: %s\n", eventJson.getString("loc_venue"));
        //Print out address
        System.out.printf("Address: %s\n", eventJson.getString("loc_address1"));
        //Print out city/state
        System.out.printf("\t %s, %s %s\n", eventJson.getString("loc_city"), eventJson.getString("loc_region"),
                eventJson.getString("loc_postcode"));
        //Print out county
        System.out.printf("Country: %s\n", eventJson.getString("loc_country"));
        //Set event date(only grab start day, ignore time)
        LocalDate eventDate = parseLocalDate(eventJson.getString("start"));
        //Check date to see if it first 4-week restriction
        checkDate(eventDate);
        //Build JSON array from SKU
        JSONArray result = Team.TeamBuilder
                .readJsonFromUrl("https://api.vexdb.io/v1/get_teams?sku=" + sku)
                .getJSONArray("result");
        //Initialize team list
        String[] teamNames = new String[result.length()];
        //Fill the team list
        for(int i = 0; i < result.length(); i++) {
            teamNames[i] = result.getJSONObject(i).getString("number");
        }
        //Print out estimated runtime
        //TODO: Fix me. Although the time spend logging in does add to this
        double estimatedRuntime = (2.0 + (0.3 * teamNames.length) + 0.6 + 9.0 + 3.0);
        System.out.printf("Estimated Runtime(Broken) - (%.2f) seconds\n", estimatedRuntime);
        //Print break
        System.out.println("-----------------------------------------------------------");

        List<Team> teamList = loadTeams(teamNames, season);

        return new VexEvent(season, eventName, eventDate, teamList, sku);
    }

  	/*
	------------------------------------------------------------------------------------------
	//																						//
	//									PROCESSING METHODS									//
	//																						//
	------------------------------------------------------------------------------------------
	*/

    /**
     * Returns a {@link LocalDate} object given a String of the date in ISO 8601 format.
     * 
     * @param dateStr a String of a date in ISO 8601 format.
     * @return a {@link LocalDate} object for the date
     */
    private static LocalDate parseLocalDate(String dateStr) {
        // https://stackoverflow.com/questions/25938560/parse-iso8601-date-string-to-date-with-utc-timezone
        // https://stackoverflow.com/questions/9474121/i-want-to-get-year-month-day-etc-from-java-date-to-compare-with-gregorian-cal
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor accessor = timeFormatter.parse(dateStr);
        // Looks like VexDB is posting full DateTimes but in UTC timezone (+00:00)
        // Assume this is unintentional and they mean local time
        LocalDate localDate = Instant.from(accessor).atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate;
    }

    /**
     * Extract the SKU from a RobotEvents URL.
     * <p>
     * The following is an example of a RobotEvents URL:
     * </p>
     * <p style="text-indent: 40px">
     * https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-19-8971.html
     * </p>
     * <p>
     * <b>RE-VRX-XX-XXXX</b> indicates an events SKU. In the example above, the SKU is RE-VRX-19-8971.
     * </p>
     * @param link
     * @return
     */
    private static String extractSKU(URL link) {
        //Get file path of url
        String[] filePath = link.getPath().split("/");
        //Get and set event code
        return filePath[filePath.length - 1].replace(".html", "");
    }

    /**
     * Checks the date of the events with the current date.
     * 
     * <p>
     * The event date must be no more than 4 weeks after the current date. This is
     * due to a current limitation in the API for gathering team data.
     * </p>
     * 
     * @param eventDateActual the {@link LocalDate} of the event date
     */
    private static void checkDate(LocalDate eventDateActual) {
    	//Get today's date
        LocalDate today = LocalDate.now();
        
        //Set specified date to exactly 4 weeks(28 days) before event date,
        //since that is what the program is checking for
        LocalDate dateSpecified = eventDateActual.minusWeeks(4);

        //If current Date is greater than 4 weeks before the event
        if(today.isBefore(dateSpecified)) { //Date restriction not met

            long dayDifference = today.until(dateSpecified, ChronoUnit.DAYS);

            //Print out dates
            System.out.printf("Today:%s\n", today);
            System.out.printf("Actual:%s\n", eventDateActual);
            System.out.printf("Specified:%s\n", dateSpecified);
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
            System.out.printf("Today's Date: %s%n", today);
            System.out.printf("Event's Date(Actual): %s%n", eventDateActual);
            System.out.printf("Event's Date(4 Weeks Prior): %s%n", dateSpecified);
            System.out.println("DATE CHECK:TRUE");
            //Print break
            System.out.println("-----------------------------------------------------------");
            //Program continues running
        }

    }

    /**
     * Returns a list of {@link Team} objects given a String array of team names and the season
     * to get team statistics for
     * 
     * @param teamNames a String array containing the team names
     * @param season the season to get team statistics for
     * @return a list of {@link Team} objects
     * @throws IOException for when an I/O error occurs
     */
    public static List<Team> loadTeams(String[] teamNames, String season) throws IOException {
        List<Team> teamList = new ArrayList<>(teamNames.length);
        //Loop through team list
        for(int i = 0; i < teamNames.length; i++) {
            //Time how long each loop takes
            long sTime = System.currentTimeMillis();
            //Grab team name
            String n = teamNames[i];
            //Parse team and calculate data
            Team t = new Team.TeamBuilder(n, season)
                    .setTeamData()
                    .setEventData()
                    .setRankingData()
                    .setSeasonData()
                    .setAwardsData()
                    .setSkillsData()
                    .build();

            teamList.add(t);

            //Time taken
            long timeTaken = System.currentTimeMillis() - sTime;
            //Print-out
            System.out.print(String.format("\t%-10s(%dms)\n", n, timeTaken).replace(" ", "."));
        }

        return teamList;
    }
}