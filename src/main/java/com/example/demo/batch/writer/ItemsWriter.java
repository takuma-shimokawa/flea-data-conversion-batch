package com.example.demo.batch.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Item;
import com.example.demo.domain.ItemProcessResult;
import com.example.demo.repository.ItemRepository;

/**
 * 正常に処理された商品データをitemsテーブルへ登録するWriter
 * 
 * ItemProcessorから渡されたItemProcessorResultのうち、
 * 正常データとして振り分けられたものを受け取り
 * ItemRepositoryを通してitemsテーブルへ登録する
 */
@Component
public class ItemsWriter implements ItemWriter<ItemProcessResult> {

    // itemsテーブルへの登録処理を行うRepository
    private final ItemRepository itemRepository;

    /**
     * ItemRepositoryをコンストラクタインジェクションする
     * @param itemRepository　itemsテーブルへのDB操作を行うRepository
     */
    public ItemsWriter(ItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    /**
     * Chunke単位で受け取った正常な商品データをitemsテーブルへ登録する
     * 
     * ItemProcessorResultからItemを取り出し、ItemRepositoryへ1件ずつ登録を依頼する
     * 
     * @param chunk ItemProcessorResultが格納されたChunk
     * @throws Exception 書き込み処理中にエラーが発生した場合
     */
    @Override
    public void write(Chunk<? extends ItemProcessResult> chunk) throws Exception{

        // Chunk内のItemProcessorResultを1件ずつ取得する
        for(ItemProcessResult itemProcessResult : chunk){
            // Processorで作成された正常データのItemを取得する
            Item item = itemProcessResult.getItem();
            // Itemをitemsテーブルへ登録する
            itemRepository.insertItem(item);
        }

    }
}
