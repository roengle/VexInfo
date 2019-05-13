# VexInfo

VexInfo is made to allow for data of VEX EDR teams to be easily accesable. Currently, it can take a [RobotEvents](https://robotevents.com) URL of a VEX EDR tournament, and compile statistics for each team competing. 

# Motivation

The motivation for this project came from a problem that would come up whever scouting out tournaments was being discussed--a way to access statistics for the teams present. Seeing that there wasn't another project to take on this task, I took on the challenge of making my first program that would actually be useful.

## How it Works

 1) Given a [RobotEvents](https://robotevents.com) URL, the program acquires the events *SKU*, which looks like `RE-VRC-XX-XXXX`. 
 2) The date of the event is acquired using the [VexDB API](https://vexdb.io/the_data/). The program checks the current date of when 
    it is being run to the date of the event. The program _must_ be run at least 28 days before the event's scheduled date due to 
    restrictions in the API.
 3) The program creates services for the [Google Sheets API](https://developers.google.com/sheets/api/) and 
    the [Google Drive API](https://developers.google.com/drive/). These are used to send requests involving the Google Sheet.
 4) The program creates a Google Sheet using the [Google Sheets API](https://developers.google.com/sheets/api/).
 5) The program uses the [VexDB API](https://vexdb.io/the_data/) to get a list of teams at the event. These are referred to as team "numbers," which have a set of anywhere from two to five numbers with a letter after(IE: `90241A`).
 6) The program uses the [VexDB API](https://vexdb.io/the_data/) to get data for each individual team. This includes statistics from every
    other tournament that the team has competed at. For what data is included, see "Data" below. If the team has not competed at any 
    tournaments yet, a string "NOT_FOUND" will be substituted in. The team number along with all of its data is stored in its own `Team` 
    class, and all teams are kept track of. 
 7) After all of the team data has been calculated, the program uses the [Google Sheets API](https://developers.google.com/sheets/api/)
    to send a write request to the spreadsheet, inputting all of the data accordingly. 
 8) Using the [Google Sheets API](https://developers.google.com/sheets/api/), conditional formatting is applied to italicize cells with
    _NOT_FOUND_. Conditional formatting is also applied to give each column a color gradient scale. Red colors are worser data points 
    compared to data in the column, while greener colors are better data points compared to the data in the column.
 9) Using the [Google Drive API](https://developers.google.com/drive/), the Google Sheet has its ownership transferred to a separate 
    Google Drive account, which is speicified by an email address.

### Setting up

To set up this program for use, make sure to have Java 1.7.x or above. Clone into the repository. In the `src` folder, create a file named `Constants.java` and put the following code in.

```
public interface Constants {
	//OAuth2 Credentials
	final String GOOGLE_CLIENT_ID = "put-your-client-id-here";
	final String GOOGLE_CLIENT_SECRET = "put-your-client-secret-here";
	final String GOOGLE_REFRESH_TOKEN = "put-your-refresh-token-here";
}
```

Replace the according values above with your credentials(see "Getting Credentials"). To run the program, run the `Tester` program. I have some test values already in the program, but keep in mind this isn't a permanent solution. While the test program has the process start automatically, all that is needed is to instantiate a `TeamAPI` object like so, replacing the proper values:

```
TeamAPI api = new TeamAPI(put-event-link-here, put-new-ownership-user-email-here);
```

For example, obtaining statistics for 2019 Worlds and transferring it to an email address(note: this process takes quite a long time, 
due to how many calculations it must perform(about 500 teams!), and also the fact that it only operates on a single-thread):

```
TeamAPI api = new TeamAPI("https://www.robotevents.com/robot-competitions/vex-robotics-competition/RE-VRC-18-6082.html", robertibengle@gmail.com);
```

## APIs Used

* [VexDB API v1](https://vexdb.io/the_data) - API used to obtain all team data.
* [Google Sheets API v4](https://developers.google.com/sheets/api/) - API used to write data to sheet.
* [Google Drive API v3](https://developers.google.com/drive/) - API used to transfer ownership of sheet.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Versioning

[SemVer](https://semver.org/) is used for versioning. For the versions available, see the [tags on this repository](https://github.com/Wup123102/VexInfo/tags).

## Authors

* **Robert Engle** - *Creator* - [Wup123102](https://github.com/Wup123102)

## Worlds Events Sheets

* [Worlds 2017 MS Sheet](https://docs.google.com/spreadsheets/d/10zqWIbOEhHvc0FGhgJB6OBHfjgxY8x_m9Mpn1n8gigc/edit?usp=sharing)
* [Worlds 2017 HS Sheet](https://docs.google.com/spreadsheets/d/1C_q8Cf-2Y-Lq_ij00v7KjIU9Uwmjew1PVXVNJsmffpQ/edit?usp=sharing)
* [Worlds 2018 MS Sheet](https://docs.google.com/spreadsheets/d/1GRhYcvyfxWojKMFL27f82O6Jr2EqDcmOYKSgEU8Uu2M/edit?usp=sharing)
* [Worlds 2018 HS Sheet](https://docs.google.com/spreadsheets/d/1-Syt6zZK2tIxF6CWU4wcxrpAXvvUfb0fjPMlmVosJPE/edit?usp=sharing)
* [Worlds 2019 MS Sheet](https://docs.google.com/spreadsheets/d/1MJbDUgOZz4yt6syBj2W3u1SDRzM7p4fkdSSVdxXt_ek/edit?usp=sharing)
* [Worlds 2019 HS Sheet](https://docs.google.com/spreadsheets/d/1SDmY9aumDRKqvlrNy0t9aHNP1IFTuEFWtXckGaDZpzQ/edit?usp=sharing)

## License

This project is licensed under the GNU GPL v3.0 License - see the [LICENSE.md](LICENSE.MD) file for details.

## Acknowledgements

* My robotics advisor, Frank Menjivar, who always motivated me to strive for more, leading me to developing this project.
