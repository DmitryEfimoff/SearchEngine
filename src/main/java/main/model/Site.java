package main.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "site")
public class Site {

    public Site() {

    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO
            ,generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private Integer id;

    private String status;

    @Column(name = "status_time")
    private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    private String url;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
