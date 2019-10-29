package com.github.tdd.crawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDao implements CrawlerDao {

    Connection connection;

    @Override
    public String getNextLinkFromLinksToBeProcessed() throws SQLException {
        return getNextLink("SELECT LINK FROM LINKS_TO_BE_PROCESSED LIMIT 1");
    }

    @Override
    public void deleteLinkFromLinksToBeProcessed(String link) throws SQLException {
        updateDatabase(link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?");
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:d:/software/IDEA/crawler/news", "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertLinkIntoLinksToBeProcessed(String href) throws SQLException {
        updateDatabase(href, "insert into LINKS_TO_BE_PROCESSED(LINK)\n" +
                "values (?)");
    }

    @Override
    public void insertLinkIntoLinksAlreadyProcessed(String link) throws SQLException {
        updateDatabase(link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
    }

    public String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    @Override
    public void insertNewsToDatabase(String link, String title, String content) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO NEWS (URL,TITLE,CONTENT,CREATED_AT,MODIFIED_AT) VALUES (?,?,?,NOW(),NOW())")) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
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
}
