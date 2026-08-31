package com.example.demo.batch.reader;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Original;

@Component
public class ItemReader {

    private final DataSource dataSource;

    public ItemReader(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public JdbcCursorItemReader<Original> itemReader(){
        return new JdbcCursorItemReaderBuilder<Original>()
            .name("itemReader")
            .dataSource(dataSource)
            .sql("SELECT id,name,condition_id,category_name,"
                 + "brand,price,shipping,description "
                 + "FROM original;"
            )
            .build();
    }

    private static final RowMapper<Original> ORIGINAL_ROW_MAPPER = (rs, i) -> {
        Original original = new Original();
        original.setId(rs.getInt("id"));
        original.setName(rs.getString("name"));
        original.setConditionId(rs.getInt("conditionId"));
        original.setCategoryName(rs.getString("categoryName"));
        original.setBrand(rs.getString("brand"));
        original.setPrice(rs.getDouble("price"));
        original.setShipping(rs.getInt("shipping"));
        original.setDescription(rs.getString("description"));
        return original;
    };
    

}
