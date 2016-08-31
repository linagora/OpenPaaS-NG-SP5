/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.structures;

/**
 *
 * @author pmeladianos
 */
public class Wikipedia {
    String title;
    String link;
    public Wikipedia(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        
        return "https://en.wikipedia.org/wiki/"+this.title.replaceAll(" ", "_");
    }


    
    
}
