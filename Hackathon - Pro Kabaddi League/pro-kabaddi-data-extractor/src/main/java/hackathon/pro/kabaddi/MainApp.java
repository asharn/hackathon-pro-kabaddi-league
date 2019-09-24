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
import org.json.simple.parser.ParseException;

/**
 * Main Application to read data from JSON file through website URL
 *
 */
public class MainApp {
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
			StringBuilder matchData = new StringBuilder("[");
			StringBuilder teamData = new StringBuilder("[");
			groups.forEach(group -> {
				final JSONObject teams = (JSONObject) ((JSONObject) group).get("teams");
				final JSONArray team = (JSONArray) teams.get("team");
				team.forEach(teamItr -> {
					final Long teamId = (Long) ((JSONObject) teamItr).get("team_id");
					try {
						if (!teamIds.contains(teamId)) {
							fetchAndSaveTeamData(teamId, teamData);
							fetchAndSaveMatchData(teamItr, matchData);
						}
						teamIds.add(teamId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			});
			teamData.deleteCharAt(teamData.lastIndexOf(","));
			teamData.append("]");
			saveAsJsonFile(teamData.toString(), "stats_team.json");

			matchData.deleteCharAt(matchData.lastIndexOf(","));
			matchData.append("]");
			saveAsJsonFile(matchData.toString(), "stats_match.json");
			fetchAndSaveSquadRelatedData();
			fetchAndSavePlayerData();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		System.out.println("All required data saved successfully.");
	}

	private static void fetchAndSavePlayerData() {

		/**
		 * Fetch player related information.
		 */
		StringBuilder playerData = new StringBuilder("[");
		playerIds.forEach(playerId -> {
			try {
				playerData.append(readUrl(
						KabaddiContants.BASE_URL + playerStatsUrl.replace("{{player_Id}}", String.valueOf(playerId))));
				playerData.append(",");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		playerData.deleteCharAt(playerData.lastIndexOf(","));
		playerData.append("]");
		saveAsJsonFile(playerData.toString(), "stats_player.json");

	}

	private static void fetchAndSaveMatchData(Object teamItr, StringBuilder matchData) {
		final JSONObject matchResult = (JSONObject) ((JSONObject) teamItr).get("match_result");
		final JSONArray matchs = (JSONArray) matchResult.get("match");
		matchs.forEach(match -> {
			final Long matchId = (Long) ((JSONObject) match).get("id");
			try {
				matchData.append(readUrl(
						KabaddiContants.BASE_URL + matchFileUrl.replace("{{MATCH_ID}}", String.valueOf(matchId))));
				matchData.append(",");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	private static void fetchAndSaveTeamData(final Long teamId, StringBuilder teamData) {
		try {
			teamData.append(
					readUrl(KabaddiContants.BASE_URL + teamStatsUrl.replace("{{team_Id}}", String.valueOf(teamId))));
			teamData.append(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void fetchAndSaveSquadRelatedData() {
		/**
		 * Fetch all season tracking related information except all and current going on
		 * because data is not available for them.
		 */
		StringBuilder squadData = new StringBuilder("[");
		seasonIds.forEach(seriesId -> teamIds.forEach(teamId -> {
			try {
				final String squadJsonData = readUrl(
						KabaddiContants.BASE_URL + teamSquadUrl.replace("{{series_id}}", String.valueOf(seriesId) + "_")
								.replace("{{team_id}}", String.valueOf(teamId)));
				squadData.append(squadJsonData);
				squadData.append(",");
				collectPlayerIds(squadJsonData);
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		})

		);
		squadData.deleteCharAt(squadData.lastIndexOf(","));
		squadData.append("]");
		saveAsJsonFile(squadData.toString(), "stats_squad.json");
	}

	private static void collectPlayerIds(String squadData) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject squadParsedData = (JSONObject) jsonParser.parse(squadData);
		JSONObject squads = (JSONObject) squadParsedData.get("squads");
		JSONObject squad = (JSONObject) squads.get("squad");
		JSONArray players = (JSONArray) squad.get("players");
		players.forEach(player -> playerIds.add((Long) ((JSONObject) player).get("player_id")));

	}

	private static void fetchAndSaveSeasonTrackerData() {
		/**
		 * Fetch all season tracking related information except all and current going on
		 * because data is not available for them.
		 */
		StringBuilder seasonData = new StringBuilder("[");
		seasonIds.stream().filter(season -> (season != 0L && season != 49L)).forEach(season -> {
			try {
				seasonData.append(readUrl(
						KabaddiContants.BASE_URL + seasonStatsUrl.replace("{{series_Id}}", String.valueOf(season))));
				seasonData.append(",");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		seasonData.deleteCharAt(seasonData.lastIndexOf(","));
		seasonData.append("]");
		saveAsJsonFile(seasonData.toString(), "season_tracker.json");
	}

	private static void saveAsJsonFile(String seasonData, String fileName) {
		try (final FileOutputStream outputStreamSeason = new FileOutputStream(
				KabaddiContants.DATA_DIR_PATH + fileName)) {
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