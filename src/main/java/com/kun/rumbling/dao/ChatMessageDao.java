package com.kun.rumbling.dao;

import com.kun.rumbling.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChatMessageDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public int insert(ChatMessage message) {
        String sql = "INSERT INTO chatgpt_message (`user`,batch_num,message) VALUES (?,?,?);";
        return jdbcTemplate.update(sql, message.getUser(), message.getBatchNum(), message.getMessage());
    }




}
