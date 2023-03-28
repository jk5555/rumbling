package com.kun.rumbling.dao;

import com.kun.rumbling.domain.ChatMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Repository
public class ChatMessageDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getToken() {
        String sql = "select token from chatgpt_token where enable = 1 limit 1";
        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);
        if (CollectionUtils.isEmpty(queryForList)) {
            return StringUtils.EMPTY;
        }
        return String.valueOf(queryForList.get(0).get("token"));
    }


    public int insert(ChatMessage message) {
        String sql = "INSERT INTO chatgpt_message (`user`,batch_num,message) VALUES (?,?,?);";
        return jdbcTemplate.update(sql, message.getUser(), message.getBatchNum(), message.getMessage());
    }




}
