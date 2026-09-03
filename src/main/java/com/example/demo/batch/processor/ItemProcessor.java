package com.example.demo.batch.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;
import com.example.demo.enums.ErrorReason;
import com.example.demo.domain.Item;
import com.example.demo.domain.ItemProcessResult;
import com.example.demo.domain.Original;
import com.example.demo.repository.CategoryRepository;

/**
 * originalの商品データをitems登録用データまたはエラー情報へ変換するProcessor。
 *
 * category_nameの内容を検証し、エラー条件に該当する場合は
 * エラー理由を設定したItemProcessResultを返す。
 *
 * 正常データの場合はcategory_nameからcategory.idを取得し、
 * OriginalからItemへ必要な項目を変換して返す。
 */
@Component
public class ItemProcessor implements org.springframework.batch.item.ItemProcessor<Original, ItemProcessResult> {

    private final CategoryRepository categoryRepository;

    public ItemProcessor(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * originalの商品データを検証し、items登録用データまたはエラー情報へ変換する。
     *
     * @param original originalテーブルから読み込んだ商品データ
     * @return 正常時はItem、異常時はOriginalとエラー理由を保持するItemProcessResult
     */
    @Override
    public ItemProcessResult process(Original original) {

        if (original.getCategoryName() == null) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason(ErrorReason.CATEGORY_NULL);
            return itemProcessResult;
        }

        if (original.getCategoryName().isEmpty()) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason(ErrorReason.CATEGORY_NULL);
            return itemProcessResult;
        }

        String[] parts = original.getCategoryName().split("/");
        if (parts.length < 3) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason(ErrorReason.CATEGORY_NULL);
            return itemProcessResult;
        }

        String nameAll = Arrays.stream(parts).limit(3).collect(Collectors.joining("/"));

        if (!categoryIdMap.containsKey(nameAll)) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason(ErrorReason.CATEGORY_NULL);
            return itemProcessResult;
        }

        Integer id = categoryIdMap.get(nameAll);

        Item item = new Item();
        item.setName(original.getName());
        item.setCondition(original.getConditionId());
        item.setCategory(id);
        item.setBrand(original.getBrand());
        item.setPrice(original.getPrice());
        item.setShipping(original.getShipping());
        item.setDescription(original.getDescription());

        ItemProcessResult itemProcessResult = new ItemProcessResult();
        itemProcessResult.setItem(item);
        return itemProcessResult;
    }

    // category.name_allをキー、category.idを値として保持するMap
    // itemsStep開始時にDBから一括取得し、商品ごとのDB検索を省略する
    private Map<String, Integer> categoryIdMap = new HashMap<>();

    /**
     * itemsStep開始前にcategoryテーブルから
     * name_allとcategory.idの対応表を取得する。
     *
     * @param stepExecution 実行対象Stepの情報
     */
    @BeforeStep
    public void findCategoryId(StepExecution stepExecution) {
        // itemsStepの商品処理中にDB検索を繰り返さないよう、
        // category.idの対応表をStep開始前に一括取得する
        categoryIdMap = categoryRepository.findCategoryIdMap();

    }

}
