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
public class QuestionItems {
    private List<StackOverflow> items;

    public QuestionItems(List<StackOverflow> items) {
        this.items = items;
    }

    public List<StackOverflow> getItems() {
        return items;
    }

    public void setItems(List<StackOverflow> items) {
        this.items = items;
    }
    
    
}
