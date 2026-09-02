package com.example.demo.batch.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Category;

/**
 * original.category_nameを階層ごとのCategoryへ変換するProcessor。
 *
 * カテゴリ名を「/」で分割し、最大3階層までCategoryを作成する。
 * 処理対象の最下層カテゴリには、カテゴリのフルパスをnameAllとして設定する。
 */
@Component
public class CategoryProcessor implements ItemProcessor<String, List<Category>> {

    /**
     * category_nameを階層ごとのCategoryに変換する。
     *
     * @param categoryName originalテーブルから取得したcategory_name
     * @return 親カテゴリから子カテゴリの順に格納したCategoryの一覧
     * @throws Exception 変換処理中にエラーが発生した場合
     */
    @Override
    public List<Category> process(String categoryName) throws Exception {

        // category_nameを「/」で分割する
        String[] parts = categoryName.split("/");

        // 今回処理する階層数を保持する
        int levelCount;

        // 4階層以上ある場合でも要件により3階層目までを使用する
        if (parts.length >= 3) {
            levelCount = 3;
        } else {
            // 3階層未満の場合は、存在する階層数をそのまま使用する
            levelCount = parts.length;
        }

        // 分割した各階層のCategoryをまとめるためのList
        List<Category> categoryList = new ArrayList<>();
        // 1階層目から、処理対象の最下層まで順番にCategoryを作成する
        for (int i = 0; i < levelCount; i++) {
            // categoryテーブル１行分のCategoryを作成する
            Category category = new Category();
            // 現在処理している階層のカテゴリ名を設定する
            category.setName(parts[i]);

            // 現在の改装が最下層の場合だけnameAllを設定する
            if (i == levelCount - 1) {
                // 先頭から処理対象の最下層までを「/」で連結する
                // 4階層いじょうでも3階層までに切り捨てられる
                String nameAll = String.join("/", Arrays.copyOfRange(parts, 0, levelCount));
                // 作成したフルパスをCateogryのnameAllに設定する
                category.setNameAll(nameAll);
            }

            // 完成したCategoryをListへ追加する
            categoryList.add(category);
        }
        // 作成した各階層のCategoryをまとめてWriterへ渡す
        return categoryList;
    }

}
