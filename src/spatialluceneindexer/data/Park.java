/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spatialluceneindexer.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 *
 * @author spousty
 * 
 * Basically just a POJO to bind to the JSON
 */
public class Park {
    
    //need this because the JSON actually contains a capitol letter
    @JsonProperty("Name")
    private String Name = "";
    
    private ArrayList<Double> pos = null;

    public String getname() {
        return Name;
    }

    public void setname(String name) {
        this.Name = name;
    }

    public ArrayList<Double> getPos() {
        return pos;
    }

    public void setPos(ArrayList<Double> pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "Park{" + "name=" + Name + ", pos=" + pos + '}';
    }
    
    
    
}
