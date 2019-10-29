package com.github.tdd.crawler;

import java.sql.SQLException;

public interface CrawlerDao {
    void insertNewsToDatabase(String link, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    String getNextLinkFromLinksToBeProcessed() throws SQLException;

    void deleteLinkFromLinksToBeProcessed(String link) throws SQLException;

    void insertLinkIntoLinksAlreadyProcessed(String link) throws SQLException;

    void insertLinkIntoLinksToBeProcessed(String href) throws SQLException;
}
