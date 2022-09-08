package main.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;

@Entity
@Table(name = "indexe")
public class Index {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO
            ,generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private Integer id;

    @Column (name = "site_id")
    private Integer siteId;

    @Column(name = "page_id")
    private Integer pageId;

    @Column(name = "lemma_id")
    private Integer lemmaId;

    private Float ranke;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public Integer getLemmaId() {
        return lemmaId;
    }

    public void setLemmaId(Integer lemmaId) {
        this.lemmaId = lemmaId;
    }

    public Float getRanke() {
        return ranke;
    }

    public void setRanke(Float rank) {
        this.ranke = rank;
    }

    public Integer getSiteId() { return siteId; }

    public void setSiteId(Integer siteId) { this.siteId = siteId; }
}
