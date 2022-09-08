package main;

import main.model.Page;

import java.util.Vector;
import java.util.concurrent.ForkJoinPool;

public class SearchEngine extends Thread {

    public SearchEngine() {

    }

    public SearchEngine(String url, Integer siteId) {

        this.url = url;
        this.siteId = siteId;

    }

    private Integer siteId;

    private String url;

    private Vector<Page> pages = new Vector<>();

    public Vector<Page> getPages() {
        return pages;
    }

    public void setPages(Vector<Page> pages) {
        this.pages = pages;
    }

    @Override
    public void run() {

        Vector<String> foundPages = new Vector<>();
        foundPages.add("/");

//        Page firstPage = new Page(url);

        System.out.println("Базовый индекс для поиска: ");
        String baseIndex = url.substring((url.charAt(4) == 's') ? 8 : 7, url.length() - 1);
        System.out.println(baseIndex);

        pages = new ForkJoinPool().invoke(new PagesSearcher(url, baseIndex, foundPages));

    }

    public Integer getSiteId() { return siteId; }

    public String getUrl() { return url; }
}
