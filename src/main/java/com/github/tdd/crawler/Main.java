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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Main {
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:d:/software/IDEA/crawler/news", "root", "root");
        while (true) {
            //Links to be processed
            Deque<String> linkPool = new LinkedList<>(loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED"));
            if (linkPool.isEmpty()) {
                break;
            }
            String link = linkPool.removeFirst();
            deleteLinkFromDatabase(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            if (!isLinkProcessed(connection, link)) {
                if (isInterestingLink(link)) {
                    Document doc = getHtmlAndParse(link);
                    getUrlsFromDocumentAndStore(connection, doc);
                    storeNewsIntoDatabase(doc);
                    insertLinksIntoDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
                }
            }
        }
    }

    private static void deleteLinkFromDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void insertLinksIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void getUrlsFromDocumentAndStore(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            insertLinksIntoDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED (link) values (?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        }
        return results;
    }

    private static void storeNewsIntoDatabase(Document doc) {
        ArrayList<Element> titles = doc.select("article");
        if (!titles.isEmpty()) {
            String title = titles.get(0).child(0).text();
            System.out.println(title);
        }
    }

    private static Document getHtmlAndParse(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
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
