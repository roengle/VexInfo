package com.warrenrobotics;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * This class is responsible for all keeping track of a VexEvent.
 * </p>
 * <p>
 * A {@link VexEvent} contains a tournaments season, event name, 
 * event date, a list of teams, and the RobotEvents SKU, which serves
 * as a unique identifier for APIs.
 * </p>
 * @author Robert Engle
 * @version 0.3.1-beta.1
 * @since 2018-02-21
 *
 */
public class VexEvent {
    private String season;
    private String eventName;
    private LocalDate eventDate;
    private List<Team> teamList;
    private String sku;

    /**
     * Constructs a VexEvent given season, event name, event date, a team list, and a robotevents SKU.
     * @param season
     * @param eventName
     * @param eventDate
     * @param teamList
     * @param sku
     */
    public VexEvent(String season, String eventName, LocalDate eventDate, List<Team> teamList, String sku) {
        this.season = season;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.teamList = teamList;
        this.sku = sku;
    }

    /**
     * Returns the season for the VexEvent.
     * 
     * @return the season name for the event
     */
    public String getSeason() {
        return season;
    }

    /**
     * Returns the title of the tournament.
     * 
     * @return the name of the VexEvent
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Returns the date of the tournament.
     * 
     * @return the local date of the event
     */
    public LocalDate getEventDate() {
        return eventDate;
    }

    /**
     * Returns a list of {@link Team} objects that are competing at the tournament.
     * 
     * @return the team list for the event
     */
    public List<Team> getTeamList() {
        return teamList;
    }

    /**
     * Returns the SKU identifier of the tournament
     * 
     * @return the sku of the event
     */
    public String getSku() {
        return sku;
    }
}