package com.example.demo.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

/**
 * バッチ処理の進捗件数をログ出力するListener
 * 
 * Chunk終了後に現在までの書き込み件数を取得し、
 * 10,000件処理するごとに進捗をログへ出力する
 */
@Component
public class ProgressListener implements ChunkListener {

    // ProgressListener用のログ出力
    Logger log = LoggerFactory.getLogger(ProgressListener.class);

    /**
     * Chunkの処理が正常終了した後に呼び出される
     * 
     * StepExcutionから現在までの書き込み件数を取得し、
     * 10,000件ごとに進捗をログへ出力する
     * @param context 現在実行中のChunk・Stepに関する情報
     */
    @Override
    public void afterChunk(ChunkContext context){

        // 現在実行中のStepの実行情報を取得
        StepExecution stepExecution = context.getStepContext().getStepExecution();

        // 現在までにWriterへ書き込まれた件数を取得
        long writeCount = stepExecution.getWriteCount();

        // 10,000件処理するごとに現在の進捗件数をログ出力
        if(writeCount > 0 && writeCount % 10000 == 0){
            log.info("現在処理件数 : {}件", writeCount);
        }

    }

}
