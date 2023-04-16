package com.kun.rumbling.event;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class CreateTableRunner  implements CommandLineRunner {
    private static final Logger LOOGGER = LoggerFactory.getLogger(CreateTableRunner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 应用启动时自动检测表是否存在，不存在就自动建表
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        LOOGGER.info("start create table if not exists...");
        ClassPathResource resource = new ClassPathResource("table.sql");
        if (!resource.exists()) {
            LOOGGER.error("==========================================>  table.sql not found!");
            return;
        }
        String sqlContent = resource.getContentAsString(Charset.defaultCharset());
        String[] sqlArray = StringUtils.split(sqlContent, ";");
        for (String sql : sqlArray) {
            if (StringUtils.isNotBlank(sql)) {
                LOOGGER.info("start execute sql [{}]", sql);
                jdbcTemplate.execute(sql);
            }
        }
        LOOGGER.info("create table script run success!");

    }

}
