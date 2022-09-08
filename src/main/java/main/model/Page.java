package main.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "pages")
public class Page {

    public Page(String url) {
        this.path = url;
    }

    public Page() {
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO
            ,generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private Integer id;

    @Column (name = "site_id")
    private Integer siteId;

    private String path;

    private Integer code;

    private String content;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSiteId() { return siteId; }

    public void setSiteId(Integer siteId) { this.siteId = siteId; }

}
