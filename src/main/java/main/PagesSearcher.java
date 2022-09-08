package main;

import main.model.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.RecursiveTask;

public class PagesSearcher extends RecursiveTask<Vector<Page>> {

    public PagesSearcher(String basePage, String baseIndex, Vector<String> foundPages) {

        this.basePage = basePage;
        this.foundPages = foundPages;
        this.baseIndex = baseIndex;

    }

    private Vector<Page> allPages = new Vector<>();
    private Vector<String> foundPages;

    private String basePage;

    String baseIndex;

    Connection.Response response = null;


    @Override
    protected Vector<Page> compute() {

        Page page = new Page();
        page.setPath(basePage);

//        String baseIndex = "playback.ru";

        String subLinkRegex = "\\/[A-z0-9]*\\.html?";

        String skillboxRegex = ".*" + baseIndex.substring(0,baseIndex.length() - 3) + "\\.ru" + ".*";
        String regexPdf = ".*\\.[A-z]{3,4}.*";
        String regexHtm = ".*\\.html?";
        String regexMailTo = "mailto\\:.*";
        String regexMail = ".*\\@.*";
        String regexPound = ".*\\#.*";

        Vector<String> subLinks = new Vector<>();


        try {
            response = Jsoup.connect(basePage)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com").timeout(20000)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int statusCode = response.statusCode();

        page.setCode(statusCode);

        if (statusCode != 200) {
            page.setContent("");
            allPages.add(page);
            return allPages;
        }

        String body = response.body();

        page.setContent(body);

        page.setPath(basePage.substring(basePage.indexOf(baseIndex) + baseIndex.length()));

        allPages.add(page);


        try {

            Document doc = Jsoup.connect(basePage)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com").maxBodySize(0).get();



            Elements docTable = doc.select("a");

            for (Element table : docTable) {
                String sublink = table.attr("href");
//                System.out.println("+ " + sublink);
                if (sublink.matches(subLinkRegex)) {
                    sublink = "http://" + baseIndex + sublink;
                }
                if (!sublink.matches(skillboxRegex) || (sublink.matches(regexPdf)&&!sublink.matches(regexHtm))) { continue; }
                if (sublink.matches(regexMailTo) || sublink.matches(regexMail)) { continue; }
                if (sublink.matches(regexPound)) { continue; }

                if (foundPages.contains(sublink.substring(sublink.indexOf(baseIndex) + baseIndex.length()))) {
                    continue;
                }

                foundPages.add(sublink.substring(sublink.indexOf(baseIndex) + baseIndex.length()));

                subLinks.add(sublink);
//                System.out.println("- " + sublink);


            }



        } catch (Exception ex) {
            ex.printStackTrace();
            return allPages;
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


// Threads START

        Vector<PagesSearcher> taskList = new Vector<>();

        for (String subLink : subLinks) {

            System.out.println(subLink);


            PagesSearcher task = new PagesSearcher(subLink, baseIndex, foundPages);
            task.fork();
            taskList.add(task);
        }

        if (!taskList.isEmpty()) {
            for (PagesSearcher task : taskList) {
                if (!task.join().equals(null)) {
                    for (Page sublink : task.join()) {
                        if (!sublink.equals(null)) {
                            allPages.add(sublink);
                        }
                    }
                }
            }
        }

// Threads END


        return allPages;
    }

}
