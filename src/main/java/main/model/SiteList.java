package main.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sites")
public class SiteList {

    public SiteList() {

    }

    private static List<String> urls;

    public static List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public boolean contains(String url) {
        if (urls.contains(url)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SiteList{" +
                "urls=" + urls +
                '}';
    }
}
