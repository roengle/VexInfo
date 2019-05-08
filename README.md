# VexInfo

VexInfo is made to allow for data of VEX EDR teams to be easily accesable. Currently, it can take a [RobotEvents](https://robotevents.com) 
URL of a VEX EDR tournament, and compile statistics for each team competing. 

## How it Works

 1) Given a [RobotEvents](https://robotevents.com) URL, the program acquires the events *SKU*, which looks like `RE-VRC-XX-XXXX`. 
 2) The date of the event is acquired using the [VexDB API](https://vexdb.io/the_data/). The program checks the current date of when 
    it is being run to the date of the event. The program _must_ be run at least 28 days before the event's scheduled date due to 
    restrictions in the API.
 3) The program creates services for the [Google Sheets API](https://developers.google.com/sheets/api/) and 
    the [Google Drive API](https://developers.google.com/drive/). These are used to send requests involving the Google Sheet.
 4) The program creates a Google Sheet using the [Google Sheets API](https://developers.google.com/sheets/api/).
 5) The program uses the [VexDB API](https://vexdb.io/the_data/) to get a list of teams at the event. These are referred to as team "numbers,"
    which have a set of anywhere from two to five numbers with a letter after(IE: `90241A`).
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

## Built With
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors
* **Robert Engle** - *Creator* - [Wup123102](https://github.com/Wup123102)

## License

This project is licensed under the GNU GPL v3.0 License - see the [LICENSE.md](LICENSE.MD) file for details.

## Acknowledgements

* My robotics advisor, Frank Menjivar, who always motivated me to strive for me, leading me to developing this project.
