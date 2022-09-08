package main.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "lemma")

public class Lemma {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO
            ,generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private Integer id;

    @Column (name = "site_id")
    private Integer siteId;

    private String lemma;

    private Float frequency;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public Float getFrequency() {
        return frequency;
    }

    public void setFrequency(Float frequency) {
        this.frequency = frequency;
    }

    public Integer getSiteId() { return siteId; }

    public void setSiteId(Integer siteId) { this.siteId = siteId; }
}
