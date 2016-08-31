/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.service;

import gr.aueb.demo.PropertyRegistryBean;
import gr.aueb.structures.Wikipedia;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

/**
 *
 * @author pmeladianos
 */
public class WikipediaService {

    public static List<Wikipedia> createWikipediaArticles() {
        ArrayList<Wikipedia> list = new ArrayList<Wikipedia>();
        list.add(new Wikipedia("0"));
        return list;
    }

    public static List<Wikipedia> getWikipediaArticles() {
        String query = getWikipediaServiceQuery();
        String response = callWIKIAPI(query);
        Document doc = Jsoup.parse(response, "", Parser.xmlParser());
        List<Wikipedia> items=new ArrayList<Wikipedia>();
        for (Element e : doc.select("p")) {
            String title = e.attr("title");
            items.add(new Wikipedia(title));
        }
        System.out.println("WIKI query" + query);

        System.out.println("WIKI hits" + items.size());
        return items;
    }

    private static String getWikipediaServiceQuery() {
        String q = "";
        String tags = "";
        for (Object key : PropertyRegistryBean.getTechicalKeywords().keySet()) {
            String s = key.toString();
            tags += s + "%20";
        }
        q = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + tags.substring(0, tags.length() - 1) + "&format=xml";
        return q;
    }

    private static String callWIKIAPI(String query) {
        String output = "";
        try {
            URL url = new URL(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            // Question q = new Gson().fromJson(in, Question.class);
            String line;
            StringBuffer content = new StringBuffer();
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            output = content.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

}
