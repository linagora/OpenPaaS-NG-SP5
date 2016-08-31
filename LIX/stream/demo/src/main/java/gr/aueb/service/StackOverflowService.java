/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.service;

import com.google.gson.Gson;
import gr.aueb.demo.PropertyRegistryBean;
import gr.aueb.structures.QuestionItems;
import gr.aueb.structures.StackOverflow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pmeladianos
 */
public class StackOverflowService {

    public static List<StackOverflow> createStackOverflowQuestions() {
        ArrayList<StackOverflow> list = new ArrayList<StackOverflow>();
        ArrayList<String> tags = new ArrayList<String>();
        tags.add("sample");
        tags.add("answer");
        list.add(new StackOverflow(tags, "1470617822", "38818943", "http://stackoverflow.com/questions/38818943/about-transfer-a-file-to-multiple-receiver-using-java-udp", "About transfer a file to multiple receiver using java UDP"));
        return list;
    }

    public static List<StackOverflow> getStackOverflowQuestions() {
        String query = getStackOverflowServiceQuery();
        String response = callSOAPI(query);
        Gson gson = new Gson();
        QuestionItems so = gson.fromJson(response,QuestionItems.class);
        if (so!=null){
            System.out.println("SO query" + query);

            System.out.println("SO hits" + so.getItems().size());
            return so.getItems();
        }
        else return new ArrayList<StackOverflow>();
    }

    private static String getStackOverflowServiceQuery() {
        String tags = "";
        for (Object key : PropertyRegistryBean.getTechicalKeywords().keySet()) {
            String s = key.toString();
            tags += s + ";";
        }
        String query = "http://api.stackexchange.com/2.2/search?order=desc&sort=activity&tagged=" + tags.substring(0, tags.length() - 1) + "&site=stackoverflow&filter=!BHMIbze0EPheMk572h0ktETsgnphhV";
        return query;
    }

    private static String callSOAPI(String query) {
        String output = "";
        try {
            URL url = new URL(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(url.openStream())));
            // Question q = new Gson().fromJson(in, Question.class);
            String line;
            StringBuffer content = new StringBuffer();
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            output=content.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

}
