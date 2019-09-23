package hackathon.pro.kabaddi;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Main Application to read data from JSON file through website URL
 *
 */
public class MainApp {
	private static final String DATA_DIR_PATH = "/home/akarn/Downloads/Artificial-Intelligence-master/Hackathon - Pro Kabaddi League/pro-kabaddi-data-extractor/data/";
	private static String teamStatsUrl;
	private static String matchFileUrl;
	private static String seasonStatsUrl;
	private static String playerStatsUrl;
	private static String teamSquadUrl;
	private static Set<Long> seasonIds;
	private static Set<Long> playerIds;
	private static Set<Long> teamIds;

	public static void main(String[] args) { // JSON parser object to parse read file
		JSONParser jsonParser = new JSONParser();
		try {
			init();

			String standing = readUrl(KabaddiContants.BASE_URL + KabaddiContants.ALL_STATNDING);

			// Read JSON file as clientConfigObject
			JSONObject clientConfigObject = (JSONObject) jsonParser.parse(standing);
			// Get config object within list
			JSONObject standings = (JSONObject) clientConfigObject.get("standings");
			// Get kabaddiPaths object within list
			JSONArray groups = (JSONArray) standings.get("groups");

			fetchAndSaveSeasonTrackerData();
			groups.forEach(group -> {
				final JSONObject teams = (JSONObject) ((JSONObject) group).get("teams");
				final JSONArray team = (JSONArray) teams.get("team");
				team.forEach(teamItr -> {
					final Long teamId = (Long) ((JSONObject) teamItr).get("team_id");
					teamIds.add(teamId);
					try {
						fetchAndSaveTeamData(teamId);
						//fetchAndSaveMatchData(teamItr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			});
			
			fetchAndSaveSquadRelatedData();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		System.out.println("All required data saved successfully.");
	}

	private static void fetchAndSaveMatchData(Object teamItr) {
		final JSONObject matchResult = (JSONObject) ((JSONObject) teamItr).get("match_result");
		final JSONArray matchs = (JSONArray) matchResult.get("match");
		matchs.forEach(match -> {
			final Long matchId = (Long) ((JSONObject) match).get("id");
			try {
				final String matchData = readUrl(
						KabaddiContants.BASE_URL + matchFileUrl.replace("{{MATCH_ID}}", String.valueOf(matchId)));
				saveAsJsonFile(matchData, matchId + "_match.json");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static void fetchAndSaveTeamData(final Long teamId) {
		try {
			final String teamData = readUrl(
					KabaddiContants.BASE_URL + teamStatsUrl.replace("{{team_Id}}", String.valueOf(teamId)));
			saveAsJsonFile(teamData, teamId + "_team.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void fetchAndSaveSquadRelatedData() {
		/**
		 * Fetch all season tracking related information except all and current going on
		 * because data is not available for them.
		 */
		seasonIds.forEach(seriesId -> teamIds.forEach(teamId -> {
			try {
				/*teamSquadUrl = teamSquadUrl.replace("{{series_id}}", String.valueOf(seriesId)+"_");
				teamSquadUrl = teamSquadUrl.replace("{{team_id}}", String.valueOf(teamId));*/
				String squadData = readUrl(KabaddiContants.BASE_URL
						+ teamSquadUrl.replace("{{series_id}}", String.valueOf(seriesId) + "_")).replace("{{team_id}}", String.valueOf(teamId));
				saveAsJsonFile(squadData, seriesId + "_" + teamId + "_tracker.json");
			} catch (IOException e) {
				e.printStackTrace();
			}
		})

		);
	}
	
	
	private static void fetchAndSaveSeasonTrackerData() {
		/**
		 * Fetch all season tracking related information except all and current going on
		 * because data is not available for them.
		 */
		seasonIds.stream().filter(season -> (season != 0L && season != 49L)).forEach(season -> {
			String seasonData;
			try {
				seasonData = readUrl(
						KabaddiContants.BASE_URL + seasonStatsUrl.replace("{{series_Id}}", String.valueOf(season)));
				saveAsJsonFile(seasonData, "1_" + season + "_tracker.json");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static void saveAsJsonFile(String seasonData, String fileName) {
		try (final FileOutputStream outputStreamSeason = new FileOutputStream(DATA_DIR_PATH + fileName)) {
			outputStreamSeason.write(seasonData.getBytes());
			System.out.println(fileName + " file save successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void init() {
		playerIds = new HashSet<>();
		teamIds = new HashSet<>();
		seasonIds = JsoupDataExtractor.fetchSeasonIds();
		initDifferentURL();
	}

	private static void initDifferentURL() {
		JSONParser jsonParser = new JSONParser();
		try {
			String clientConfig = readUrl(KabaddiContants.BASE_URL + KabaddiContants.CLIENT_CONIFG);

			// Read JSON file as clientConfigObject
			JSONObject clientConfigObject = (JSONObject) jsonParser.parse(clientConfig);

			JSONObject config = (JSONObject) clientConfigObject.get("config");

			JSONObject kabaddiPaths = (JSONObject) config.get("kabaddiPaths");

			// "teamstats": "/sifeeds/kabaddi/static/json/{{team_Id}}_team.json"
			teamStatsUrl = (String) kabaddiPaths.get("teamstats");

			// "matchFile":"/sifeeds/kabaddi/live/json/from_venue/{{MATCH_ID}}_match.json"
			matchFileUrl = (String) kabaddiPaths.get("matchFile");

			// "seasonstats": "/sifeeds/kabaddi/live/json/1_{{series_Id}}_tracker.json"
			seasonStatsUrl = (String) kabaddiPaths.get("seasonstats");

			// "playerstats": "/sifeeds/kabaddi/static/json/{{player_Id}}_player.json"
			playerStatsUrl = (String) kabaddiPaths.get("playerstats");

			// "/sifeeds/kabaddi/static/json/{{series_id}}{{team_id}}_squad.json"
			teamSquadUrl = (String) kabaddiPaths.get("teamsquad");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String readUrl(String urlString) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()))) {
			StringBuilder stringBuilder = new StringBuilder();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				stringBuilder.append(chars, 0, read);

			return stringBuilder.toString();
		}
	}
}