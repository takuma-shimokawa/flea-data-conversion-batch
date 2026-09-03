package com.example.demo.batch.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Category;
import com.example.demo.repository.CategoryRepository;

/**
 * カテゴリデータを階層順にcategoryテーブルへ登録するWriter。
 *
 * 親カテゴリから子カテゴリの順に処理し、
 * DBで採番されたカテゴリIDを次の階層のparentIdとして設定する。
 *
 * 登録済みカテゴリはフルパスとIDをキャッシュし、
 * 同一カテゴリに対する不要なDB検索を抑制する。
 */
@Component
public class CategoryWriter implements ItemWriter<List<Category>> {

    private final CategoryRepository categoryRepository;

    /**
     * CategoryWriterを生成する。
     *
     * @param categoryRepository カテゴリの検索・登録を行うRepository
     */
    CategoryWriter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Chunk内のカテゴリデータを親階層から順番に登録する。
     *
     * 既存カテゴリはキャッシュまたはDBからIDを取得し、
     * 未登録カテゴリの場合は新規登録して採番されたIDを取得する。
     * 取得したIDは次の階層のparentIdとして使用する。
     *
     * @param chunk Processorから渡されたカテゴリ階層の一覧
     * @throws Exception カテゴリ登録処理中にエラーが発生した場合
     */
    @Override
    public void write(Chunk<? extends List<Category>> chunk) throws Exception {

        // Chunk内のカテゴリ一覧を1件ずつ取得する
        for (List<Category> categories : chunk) {
            // 直前に処理したカテゴリのIDを保持する
            // 最上位カテゴリには親がいないため、最初はnull
            Integer previousCategoryId = null;

            // 現在処理しているカテゴリまでのパスを作るため
            // 各階層のカテゴリ名を順番に保持する
            List<String> categoryPathNames = new ArrayList<>();

            // Men → Tops → T-shirts のように
            // 親カテゴリから子カテゴリへ順番に処理する
            for (Category category : categories) {

                // 直前に取得したカテゴリIDを
                // 今処理しているカテゴリのparentIdとして設定する
                // 最上位カテゴリの場合はpreviousCategoryIdがnullなのでparentIdもnullになる
                category.setParentId(previousCategoryId);

                // 現在のカテゴリ名をパス作成用Listへ追加する
                categoryPathNames.add(category.getName());
                // Men ・ Men/Tops ・ Men/Tops/T-shirts のようなフルパスを作成する
                String fullPath = String.join("/", categoryPathNames);

                // 既に同じフルパスのカテゴリIDをキャッシュしているか確認する
                if (categoryIdCache.containsKey(fullPath)) {

                    // キャッシュ済みの場合はDB検索を行わず、
                    // 保存済みのカテゴリIDを次の階層用に使用する
                    previousCategoryId = categoryIdCache.get(fullPath);
                } else {
                    // キャッシュに存在しない場合は、
                    // parentId + name の組み合わせでDB上の既存カテゴリを検索する
                    List<Integer> existingCategoryIds = categoryRepository.findIdByParentIdAndName(category);
                    if (existingCategoryIds.isEmpty()) {
                        // DBにも存在しない場合は新規登録し、DBで自動採番されたカテゴリIDを取得する
                        previousCategoryId = categoryRepository.insertAndGetId(category);
                    } else {
                        // 既にDBに存在する場合は、検索結果から既存のカテゴリIDを取得する
                        previousCategoryId = existingCategoryIds.get(0);
                    }
                    // DB検索またはINSERTで確定したカテゴリIDを、フルパスと紐づけてキャッシュへ保存する
                    // 次回同じカテゴリが来た場合はDB検索を省略できる
                    categoryIdCache.put(fullPath, previousCategoryId);
                }
            }
        }

    }

    // カテゴリのフルパスとcategory.idを紐づけて保持するキャッシュ
    // 例 Men → 1 ・ Men/Tops → 5 ・ Men/Tops/T-shirts → 12
    private final Map<String, Integer> categoryIdCache = new HashMap<>();

}
