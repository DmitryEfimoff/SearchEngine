package main;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public class WebSearchAnswer {

    public WebSearchAnswer(){}

    private boolean result;
    private String error;
    private Integer count;
    private List<WebSearchData> data;

    public static class WebSearchData {

        public WebSearchData() {}

        private String site;
        private String siteName;
        private String uri;
        private String title;
        private String snippet;
        private Float relevance;

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSnippet() {
            return snippet;
        }

        public void setSnippet(String snippet) {
            this.snippet = snippet;
        }

        public Float getRelevance() {
            return relevance;
        }

        public void setRelevance(Float relevance) {
            this.relevance = relevance;
        }
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<WebSearchData> getData() {
        return data;
    }

    public void setData(List<WebSearchData> data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
