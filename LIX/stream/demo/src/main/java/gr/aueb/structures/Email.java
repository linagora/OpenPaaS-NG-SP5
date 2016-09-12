/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.structures;

import org.jsoup.Jsoup;

/**
 *
 * @author pmeladianos
 */
public class Email {
    String id;
    String from;
    String sentDate;
    String subject;
    String content;

    public Email(String id, String from, String sentDate, String subject, String content) {
        this.id = id;
        this.from = from;
        this.sentDate = sentDate;
        this.subject = subject;
        
        this.content = content;//Jsoup.parse(content).text();;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Jsoup.parse(content).text();;
    }
    
    
}
