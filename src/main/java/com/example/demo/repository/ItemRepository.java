package com.example.demo.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.Item;

@Repository
public class ItemRepository {

    private final NamedParameterJdbcTemplate template;

    public ItemRepository(NamedParameterJdbcTemplate template){
        this.template = template;
    }

    public void insertItem(Item item){
        String sql = """
                INSERT INTO items 
                (name,condition,category,brand,
                price,shipping,description) 
                VALUES(:name,:condition,:category,:brand,
                :price,:shipping,:description)
                """;

        MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("name", item.getName())
                    .addValue("condition", item.getCondition())
                    .addValue("category", item.getCategory())
                    .addValue("brand", item.getBrand())
                    .addValue("price", item.getPrice())
                    .addValue("shipping", item.getShipping())
                    .addValue("description", item.getDescription());

        template.update(sql, ps);
    }

}
