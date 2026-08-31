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

@Component
public class CategoryWriter implements ItemWriter<List<Category>> {

    private final CategoryRepository categoryRepository;

    CategoryWriter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void write(Chunk<? extends List<Category>> chunk) throws Exception {
        
        // Chunk内のカテゴリ一覧を1件ずつ取得する
        for (List<Category> categories : chunk) {
            // 直前に処理したカテゴリのIDを保持する
            // 最上位カテゴリには親がいないため、最初はnull
            Integer firstId = null;

            // 現在処理しているカテゴリまでのパスを作るため
            // 各階層のカテゴリ名を順番に保持する
            List<String> categoryIdFullpath = new ArrayList<>();
            
            // Men → Tops → T-shirts のように
            // 親カテゴリから子カテゴリへ順番に処理する
            for (Category category : categories) {

                // 直前に取得したカテゴリIDを
                // 今処理しているカテゴリのparentIdとして設定する
                // 最上位カテゴリの場合はfirstIdがnullなのでparentIdもnullになる
                category.setParentId(firstId);

                // 現在のカテゴリ名をパス作成用Listへ追加する
                categoryIdFullpath.add(category.getName());
                // Men ・ Men/Tops ・ Men/Tops/T-shirts　のようなフルパスを作成する
                String fullPath = String.join("/", categoryIdFullpath);

                // 既に同じフルパスのカテゴリIDをキャッシュしているか確認する
                if (categoryIdMap.containsKey(fullPath)) {

                    // キャッシュ済みの場合はDB検索を行わず、
                    // 保存済みのカテゴリIDを次の階層用に使用する
                    firstId = categoryIdMap.get(fullPath);
                } else {
                    // キャッシュに存在しない場合は、
                    // parentId + name の組み合わせでDB上の既存カテゴリを検索する
                    List<Integer> categoryId = categoryRepository.findIdByParentIdAndName(category);
                    if (categoryId.isEmpty()) {
                        // DBにも存在しない場合は新規登録し、DBで自動採番されたカテゴリIDを取得する
                        firstId = categoryRepository.insertAndGetId(category);
                    } else {
                        // 既にDBに存在する場合は、検索結果から既存のカテゴリIDを取得する
                        firstId = categoryId.get(0);
                    }
                    // DB検索またはINSERTで確定したカテゴリIDを、フルパスと紐づけてキャッシュへ保存する
                    // 次回同じカテゴリが来た場合はDB検索を省略できる
                    categoryIdMap.put(fullPath, firstId);
                }
            }
        }

    }
    // カテゴリのフルパスとcategory.idを紐づけて保持するキャッシュ
    // 例　Men → 1 ・ Men/Tops → 5 ・　Men/Tops/T-shirts → 12
    Map<String, Integer> categoryIdMap = new HashMap<>();

}
