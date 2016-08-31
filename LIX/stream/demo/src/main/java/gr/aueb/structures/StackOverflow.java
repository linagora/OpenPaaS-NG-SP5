/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.structures;

import java.util.List;

/**
 *
 * @author pmeladianos
 */
public class StackOverflow {
    ///2.2/questions?order=desc&sort=activity&tagged=java;sockets&site=stackoverflow&filter=!BHMIbze0EPheMk572h0ktETsgnphhV
    /*{
      "tags": [
        "java",
        "sockets",
        "udp",
        "file-transfer"
      ],
      "last_activity_date": 1470617822,
      "question_id": 38818943,
      "link": "http://stackoverflow.com/questions/38818943/about-transfer-a-file-to-multiple-receiver-using-java-udp",
      "title": "About transfer a file to multiple receiver using java UDP"
    }*/
    private List<String> tags;
    private String last_activity_date;
    private String question_id;
    private String link;
    private String title;

    public StackOverflow(List<String> tags, String last_activity_date, String question_id, String link, String title) {
        this.tags = tags;
        this.last_activity_date = last_activity_date;
        this.question_id = question_id;
        this.link = link;
        this.title = title;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLast_activity_date() {
        return last_activity_date;
    }

    public void setLast_activity_date(String last_activity_date) {
        this.last_activity_date = last_activity_date;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    
}
