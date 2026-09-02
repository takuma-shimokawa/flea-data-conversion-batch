package com.example.demo.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.Item;

/**
 * itemsテーブルへの商品登録処理を行うRepository。
 */
@Repository
public class ItemRepository {

    private final NamedParameterJdbcTemplate template;

    public ItemRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Itemの内容をitemsテーブルへ1件登録する。
     *
     * @param item 登録する商品データ
     */
    public void insertItem(Item item) {
        // itemsテーブルへ商品データを登録するSQL
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

        // 設定した商品データをitemsテーブルへ登録する
        template.update(sql, ps);
    }

}
