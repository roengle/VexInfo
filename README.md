# VexInfo

VexInfo is made to allow for data of VEX EDR teams to be easily accessible. Currently, it can take a [RobotEvents](https://robotevents.com) URL of a VEX EDR tournament, and compile statistics for each team competing. 

# Motivation

The motivation for this project came from a problem that would come up whenever scouting out tournaments was being discussed--a way to access statistics for the teams present. Seeing that there wasn't another project to take on this task, I took on the challenge of making my first program that would actually be useful.

## How it Works

 1) Given a [RobotEvents](https://robotevents.com) URL, the program acquires the events *SKU*, which looks like `RE-VRC-XX-XXXX`. 
 2) The date of the event is acquired using the [VexDB API](https://vexdb.io/the_data/). The program checks the current date of when 
    it is being run to the date of the event. The program _must_ be run at least 28 days before the event's scheduled date due to 
    restrictions in the API.
 3) The program creates services for the [Google Sheets API](https://developers.google.com/sheets/api/). These are used to send requests involving the 	Google 	Sheet. At this point, the program prompts the user to sign in. This ensures the program essentially does all the work on the user's 
 	Google account, with the Spreadsheets scope.
 4) The program creates a Google Sheet using the [Google Sheets API](https://developers.google.com/sheets/api/).
 5) The program uses the [VexDB API](https://vexdb.io/the_data/) to get a list of teams at the event. These are referred to as team "numbers," which have a 	set of anywhere from two to five numbers with a letter after(IE: `90241A`).
 6) The program uses the [VexDB API](https://vexdb.io/the_data/) to get data for each individual team. This includes statistics from every
    other tournament that the team has competed at. For what data is included, see "Data" below. If the team has not competed at any 
    tournaments yet, a string "NOT_FOUND" will be substituted in. The team number along with all of its data is stored in its own `Team` 
    class, and all teams are kept track of. 
 7) After all of the team data has been calculated, the program uses the [Google Sheets API](https://developers.google.com/sheets/api/)
    to send a write request to the spreadsheet, inputting all of the data accordingly. 
 8) Using the [Google Sheets API](https://developers.google.com/sheets/api/), conditional formatting is applied to italicize cells with
    _NOT_FOUND_. Conditional formatting is also applied to give each column a color gradient scale. Red colors are worser data points 
    compared to data in the column, while greener colors are better data points compared to the data in the column.

### Data

The following is a list of the data that is included in each Google Sheet:

* Average OPR: The Offensive Power Ranking (OPR) is a value that represents how offensive a team is. A higher value represents a stronger team. The average 	OPR is the average of a team's OPR from all of the tournaments they competed at.
* Average DPR: The Defensive Power Ranking (DPR) is a value that represents how defensive a team is. A lower value represents a stronger team. The average 	DPR is the average of a team's DPR from all of the tournaments they competed at.
* Average CCWM: The Calculated Contribution to Winning Margin (CCWM) is a value computed using both OPR and DPR to represent how a team has contributed to an 	alliance's winning point margin. A higher value represents a stronger team. A team with negative CCWM should generally never be chosen. The average CCWM 	is the average of a team's CCWM from all of the tournaments they competed at. 
* Average AP's: Autonomous Points (AP's) are the number of autonomous points that a team has earned through winning autonomous rounds. The amount of 	autonomous points rewarded for an autonomous round win depend on the season. The average AP is the average of a team's AP's from all the tournaments they 	competed at.
* Average SP's: Strength of Schedule Points (SP's) are awarded in the amount of the score of the losing alliance.
* Average TRSP's: Truly Representative Schedule Points (TRSP's) is a custom SP value that cannot be farmed and is only effected by difficulty of schedule.
	See [this](vexforum.com/t/sp-ranking-system-ideas/22614) forum post for more information.
* Vrating: vRating is a custom ranking method developed by Team BNS which takes into account a wide variety of different metrics to guage a team's 	performance. A higher value represents a stronger team.
* Vrating Rank: The vRating ranking of the team for the given program and season, compared to other teams. A lower value (higher ranking) represents a 	stronger team.
* Average Rank: The rank of a team is it's rank at a specific tournament. The average rank is the average of a team's ranks from all the tournaments they 	competed at.
* Average Skills Score (Autonomous): The autonomous skills score is the score of a team's autonomous run in a skills match. The average skills score for 	autonomous is the average of a team's autonomous skills score across all skills matches in all tournaments they competed at.
* Average Skills Score (Robot): The robot skills score is the score of a team's driver-controlled run in a skills match. The average skills score for robot 	is the average of a team's robot skills score across all skills matches in all tournaments they competed at.
* Average Skills Score (Combined): The combined skills score is the combined score of a team's autonomous and robot run in a skills match. The average 	combined skills score is the average of a team's combined skills score across all skills matches in all tournaments they competed at.
* Average Max Score: The max score is the maximum score that a team has achieved during a single tournament. The average max score is the average of a team's 	max score's from all the tournaments they competed at.
* Previous Events: The number of previous events that the team has competed at in the current season.

### Setting up

To run the program, proper OAuth credentials must be placed into the same directory as the `TeamAPI.java` file. This will be in the form of a JSON file. Make sure the file is named "credentials.json".

## APIs Used

* [VexDB API v1](https://vexdb.io/the_data) - API used to obtain all team data.
* [Google Sheets API v4](https://developers.google.com/sheets/api/) - API used to write data to sheet.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Versioning

[SemVer](https://semver.org/) is used for versioning. For the versions available, see the [tags on this repository](https://github.com/Wup123102/VexInfo/tags).

## Authors

* **Robert Engle** - *Creator & Lead Developer* - [Wup123102](https://github.com/Wup123102)

* **Ivan Wick** - *Contributor* - [ivanwick](https://github.com/ivanwick)

## Worlds Events Sheets

* [Worlds 2017 MS Sheet](https://docs.google.com/spreadsheets/d/10zqWIbOEhHvc0FGhgJB6OBHfjgxY8x_m9Mpn1n8gigc/edit?usp=sharing)
* [Worlds 2017 HS Sheet](https://docs.google.com/spreadsheets/d/1C_q8Cf-2Y-Lq_ij00v7KjIU9Uwmjew1PVXVNJsmffpQ/edit?usp=sharing)
* [Worlds 2018 MS Sheet](https://docs.google.com/spreadsheets/d/1GRhYcvyfxWojKMFL27f82O6Jr2EqDcmOYKSgEU8Uu2M/edit?usp=sharing)
* [Worlds 2018 HS Sheet](https://docs.google.com/spreadsheets/d/1-Syt6zZK2tIxF6CWU4wcxrpAXvvUfb0fjPMlmVosJPE/edit?usp=sharing)
* [Worlds 2019 MS Sheet](https://docs.google.com/spreadsheets/d/1MJbDUgOZz4yt6syBj2W3u1SDRzM7p4fkdSSVdxXt_ek/edit?usp=sharing)
* [Worlds 2019 HS Sheet](https://docs.google.com/spreadsheets/d/1SDmY9aumDRKqvlrNy0t9aHNP1IFTuEFWtXckGaDZpzQ/edit?usp=sharing)

