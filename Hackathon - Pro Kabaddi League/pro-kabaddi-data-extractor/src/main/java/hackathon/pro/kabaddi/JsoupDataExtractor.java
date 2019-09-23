package hackathon.pro.kabaddi;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JsoupDataExtractor {

	// URL url = new URL("https://www.prokabaddi.com/stats");
	public static Set<Long> fetchSeasonIds() {
		Set<Long> sessionIds = new HashSet<>();

		Document doc;
		try {

			doc = Jsoup.connect("https://www.prokabaddi.com/stats").get();
			// Element link = doc.select("a").first();
			// String relHref = link.attr("href"); // == "/"
			// String absHref = link.attr("abs:href"); // "http://jsoup.org/"
			// System.out.println(Jsoup.connect("https://www.prokabaddi.com/stats").get().html());
			// doc = Jsoup.parse(url,10000);
			// TreeBuilder treeBuilder = doc.parser().getTreeBuilder();
			Elements content = doc.getAllElements();
			// System.out.println(content);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * <select class="si-selectBox" id="season_selectBox"> <option
		 * data-series-id="0">All Seasons</option> <option data-series-id="49">Season
		 * 7</option> <option data-series-id="26">Season 6</option> <option
		 * data-series-id="8">Season 5</option> <option data-series-id="4">Season
		 * 4</option> <option data-series-id="3">Season 3</option> <option
		 * data-series-id="2">Season 2</option> <option data-series-id="1">Season
		 * 1</option> </select>
		 */
		sessionIds.add(1L);
		sessionIds.add(2L);
		sessionIds.add(3L);
		sessionIds.add(4L);
		sessionIds.add(8L);
		sessionIds.add(26L);
		sessionIds.add(49L);
		sessionIds.add(0L);

		return sessionIds;
	}

	public static void main(String[] args) {
		fetchSeasonIds();
	}
}
