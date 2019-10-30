package com.github.tdd.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    private static void mockData(SqlSessionFactory sqlSessionFactory, int count) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNews = session.selectList("com.github.tdd.MockMapper.selectNews");
            Random r = new Random();
            try {
                while (count-- > 0) {
                    int index = r.nextInt(currentNews.size());
                    News newsToBeInserted = new News(currentNews.get(index));
                    Instant currentTime = newsToBeInserted.getModifiedAt();
                    currentTime = currentTime.minusSeconds(r.nextInt(3600 * 24 * 365));
                    newsToBeInserted.setCreatedAt(currentTime);
                    newsToBeInserted.setModifiedAt(currentTime);
                    session.insert("com.github.tdd.MockMapper.insertNews", newsToBeInserted);
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mockData(sqlSessionFactory, 200_000);
    }
}
