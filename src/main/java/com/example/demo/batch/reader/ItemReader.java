package com.example.demo.batch.reader;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Original;

/**
 * originalテーブルから商品データを読み込むReader。
 *
 * 商品移行に必要な各項目をoriginalテーブルから取得し、
 * 1件ずつOriginalオブジェクトへ変換して後続のProcessorへ渡す。
 */
@Component
public class ItemReader {

    private final DataSource dataSource;

    public ItemReader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * originalテーブルの商品データを1件ずつ読み込むReaderを生成する。
     *
     * @return originalテーブルを読み込むJdbcCursorItemReader
     */
    public JdbcCursorItemReader<Original> itemReader() {
        return new JdbcCursorItemReaderBuilder<Original>()
                .name("itemReader")
                .dataSource(dataSource)
                .sql("SELECT id,name,condition_id,category_name,"
                        + "brand,price,shipping,description "
                        + "FROM original;")
                .rowMapper(ORIGINAL_ROW_MAPPER)
                .build();
    }

    // originalテーブルの検索結果1行をOriginalオブジェクトへ変換する
    private static final RowMapper<Original> ORIGINAL_ROW_MAPPER = (rs, i) -> {
        Original original = new Original();
        original.setId(rs.getInt("id"));
        original.setName(rs.getString("name"));
        original.setConditionId(rs.getInt("condition_id"));
        original.setCategoryName(rs.getString("category_name"));
        original.setBrand(rs.getString("brand"));
        original.setPrice(rs.getDouble("price"));
        original.setShipping(rs.getInt("shipping"));
        original.setDescription(rs.getString("description"));
        return original;
    };

}
