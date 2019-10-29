package com.github.tdd.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public String getNextLinkFromLinksToBeProcessed() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne("com.github.tdd.MyMapper.selectNextLink");
        }
    }

    @Override
    public void deleteLinkFromLinksToBeProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.tdd.MyMapper.deleteCurrentLink", link);
        }
    }

    @Override
    public void insertNewsToDatabase(String url, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.tdd.MyMapper.insertNews", new News(url, title, content));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.tdd.MyMapper.countLink", link);
            return count != 0;
        }
    }


    @Override
    public void insertLinkIntoLinksAlreadyProcessed(String link) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "LINKS_ALREADY_PROCESSED");
        map.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.tdd.MyMapper.insertLink", map);
        }
    }

    @Override
    public void insertLinkIntoLinksToBeProcessed(String href) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "LINKS_TO_BE_PROCESSED");
        map.put("link", href);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.tdd.MyMapper.insertLink", map);
        }
    }
}
