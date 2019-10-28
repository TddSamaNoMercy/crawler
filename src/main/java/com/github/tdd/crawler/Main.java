package com.github.tdd.crawler;

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
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        //Links to be processed
        Deque<String> linkPool = new LinkedList<>();
        linkPool.add("https://sina.cn");
        //processed links
        Set<String> processedLinks = new HashSet<>();
        while (!linkPool.isEmpty()) {
            String link = linkPool.removeFirst();
            if (!processedLinks.contains(link)) {
                if (isInterestingLink(link)) {
                    Document doc = getHtmlAndParse(link);
                    doc.select("a").stream().map(aTag -> aTag.attr("herf")).forEach(linkPool::add);
                    storeNewsIntoDatabase(doc);
                    processedLinks.add(link);
                }
            }
        }
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
            System.out.println(link);
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
        return isNewsPage(link) || isIndexPage(link)
                && !isLoginPage(link);
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
