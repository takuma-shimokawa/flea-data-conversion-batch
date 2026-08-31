package com.example.demo.batch.reader;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
public class CategoryReader {

    private final DataSource dataSource;

    public CategoryReader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcCursorItemReader<String> categoryReader(){
        return new JdbcCursorItemReaderBuilder<String>()
            .name("categoryReader")
            .dataSource(dataSource)
            .sql("SELECT DISTINCT category_name\r\n" + //
                                "FROM original\r\n" + //
                                "WHERE category_name IS NOT NULL\r\n" + //
                                "  AND category_name <> ''")
            .rowMapper((rs,rowNum)->{
                return rs.getString("category_name");
            })
            .build();

    }

    
    
    

    

}
