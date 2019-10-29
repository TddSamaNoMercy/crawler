package com.github.tdd.crawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler {
    private CrawlerDao dao = new JdbcCrawlerDao();

    public void run() throws SQLException, IOException {
        String link;
        while ((link = dao.getNextLink("select link from LINKS_TO_BE_PROCESSED limit 1")) != null) {
            dao.updateDatabase(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            if (!dao.isLinkProcessed(link)) {
                if (isInterestingLink(link)) {
                    Document doc = getHtmlAndParse(link);
                    getUrlsFromDocumentAndStore(doc);
                    storeNewsIntoDatabase(doc, link);
                    dao.updateDatabase(link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
                }
            }
        }
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    private void getUrlsFromDocumentAndStore(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (isInterestingLink(href)) {
                dao.updateDatabase(href, "insert into LINKS_TO_BE_PROCESSED(LINK)\n" +
                        "values (?)");
            }
        }
    }

    private void storeNewsIntoDatabase(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                String content = articleTag.select("p").stream()
                        .map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewToDatabase(link, title, content);

            }
        }
    }

    private Document getHtmlAndParse(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return (!isBadData(link) && isNewsPage(link) && !isLoginPage(link))
                || isIndexPage(link);
    }

    private static boolean isBadData(String link) {
        return link.contains("\\");


    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }
}
