package com.example.demo.batch.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Item;
import com.example.demo.domain.ItemProcessResult;
import com.example.demo.domain.Original;
import com.example.demo.repository.CategoryRepository;

@Component
public class ItemProcessor implements org.springframework.batch.item.ItemProcessor<Original, ItemProcessResult> {

    private final CategoryRepository categoryRepository;

    public ItemProcessor(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository; 
    }

    @Override
    public ItemProcessResult process(Original original) {

        if (original.getCategoryName() == null) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason("CATEGORY_NULL");
            return itemProcessResult;
        }

        if (original.getCategoryName().isEmpty()) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason("CATEGORY_EMPTY");
            return itemProcessResult;
        }

        String[] parts = original.getCategoryName().split("/");
        if (parts.length < 3) {
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason("CATEGORY_LEVEL不足");
            return itemProcessResult;
        }

        String nameAll = Arrays.stream(parts).limit(3).collect(Collectors.joining("/"));
    
        if(!categoryIdMap.containsKey(nameAll)){
            ItemProcessResult itemProcessResult = new ItemProcessResult();
            itemProcessResult.setOriginal(original);
            itemProcessResult.setErrorReason("CATEGORY_NOT_FOUND");
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

    Map<String,Integer> categoryIdMap = new HashMap<>();

    @BeforeStep
    public void findCategoryId(StepExecution stepExecution){
        categoryIdMap = categoryRepository.findCategoryIdMap();

    }


}
