package main.model;



import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "field")

public class Field {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO
            ,generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private Integer id;

    private String name;

    private String selector;

    private Float weight;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }
}
