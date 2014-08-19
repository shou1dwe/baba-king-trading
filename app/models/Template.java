package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by user on 8/18/2014.
 */

@Entity
public class Template extends Model{

    @Id
    public int tempId;

    public String name;

    public Template(int tempId, String name) {
        this.tempId = tempId;
        this.name = name;
    }

    public static Finder<Integer, Template> find = new Finder<Integer, Template>(
            Integer.class, Template.class
    );

}
