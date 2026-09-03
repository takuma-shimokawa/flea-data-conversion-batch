package com.example.demo.batch.reader;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.stereotype.Component;

/**
 * originalテーブルからカテゴリ名を取得するReader。
 *
 * category_nameがNULLまたは空文字のデータを除外し、
 * 重複しないカテゴリ名を1件ずつ読み込む。
 */
@Component
public class CategoryReader {

    private final DataSource dataSource;

    public CategoryReader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * originalテーブルから重複を除いたcategory_nameを読み込むReaderを生成する。
     *
     * @return category_nameを1件ずつ読み込むJdbcCursorItemReader
     */
    public JdbcCursorItemReader<String> categoryReader() {
        return new JdbcCursorItemReaderBuilder<String>()
                // ReaderをSpring Batch上で識別する名前を設定
                .name("categoryReader")
                // 読み込み元DBへの接続情報を設定
                .dataSource(dataSource)
                // NULL・空文字を除外し、重複しないcategory_nameを取得
                .sql("SELECT DISTINCT category_name\r\n" + //
                        "FROM original\r\n" + //
                        "WHERE category_name IS NOT NULL\r\n" + 
                        "  AND category_name <> ''")
                // SQLの検索結果からcategory_nameをStringとして取り出す
                .rowMapper((rs, rowNum) -> {
                    return rs.getString("category_name");
                })
                .build();
    }
}
