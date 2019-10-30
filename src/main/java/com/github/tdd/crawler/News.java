package com.github.tdd.crawler;

import java.time.Instant;

public class News {
    int id;
    String url;
    String title;
    String content;
    Instant createdAt;
    Instant modifiedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public News() {
    }

    public News(News originNews) {
        this.url = originNews.url;
        this.title = originNews.title;
        this.content = originNews.content;
        this.createdAt = originNews.createdAt;
        this.modifiedAt = originNews.modifiedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }


    public News(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
