package main;

import java.util.Date;
import java.util.List;

public class WebStatisticsAnswer {

    public WebStatisticsAnswer(){}

    private boolean result;

    private WebStatistics statistics;

    public static class WebStatistics {

        public WebStatistics(){}

        private WebTotal total;

        private List<WebDetailed> detailed;

        public static class WebTotal {

            public WebTotal(){}

            private Integer sites;
            private Integer pages;
            private Integer lemmas;
            private boolean isIndexing;

            public Integer getSites() {
                return sites;
            }

            public void setSites(Integer sites) {
                this.sites = sites;
            }

            public Integer getPages() {
                return pages;
            }

            public void setPages(Integer pages) {
                this.pages = pages;
            }

            public Integer getLemmas() {
                return lemmas;
            }

            public void setLemmas(Integer lemmas) {
                this.lemmas = lemmas;
            }

            public boolean isIndexing() {
                return isIndexing;
            }

            public void setIndexing(boolean indexing) {
                isIndexing = indexing;
            }
        }

        public static class WebDetailed {

            public WebDetailed(){}

            private String url;
            private String name;
            private String status;
            private Date statusTime;
            private String error;
            private Integer pages;
            private Integer lemmas;

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

            public Date getStatusTime() {
                return statusTime;
            }

            public void setStatusTime(Date statusTime) {
                this.statusTime = statusTime;
            }

            public String getError() {
                return error;
            }

            public void setError(String error) {
                this.error = error;
            }

            public Integer getPages() {
                return pages;
            }

            public void setPages(Integer pages) {
                this.pages = pages;
            }

            public Integer getLemmas() {
                return lemmas;
            }

            public void setLemmas(Integer lemmas) {
                this.lemmas = lemmas;
            }
        }

        public WebTotal getTotal() {
            return total;
        }

        public void setTotal(WebTotal total) {
            this.total = total;
        }

        public List<WebDetailed> getDetailed() {
            return detailed;
        }

        public void setDetailed(List<WebDetailed> detailed) {
            this.detailed = detailed;
        }
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public WebStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(WebStatistics statistics) {
        this.statistics = statistics;
    }
}
