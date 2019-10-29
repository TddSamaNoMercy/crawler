package com.github.tdd.crawler;

import java.sql.SQLException;

public interface CrawlerDao {
    void updateDatabase(String link, String sql) throws SQLException;

    String getNextLink(String sql) throws SQLException;

    void insertNewToDatabase(String link, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;
}
