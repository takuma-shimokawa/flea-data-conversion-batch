package com.example.demo.batch.config;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.batch.listener.ExecutionTimeListener;
import com.example.demo.batch.listener.ProgressListener;
import com.example.demo.batch.processor.CategoryProcessor;
import com.example.demo.batch.processor.ItemProcessor;
import com.example.demo.batch.reader.CategoryReader;
import com.example.demo.batch.reader.ItemReader;
import com.example.demo.batch.writer.CategoryWriter;
import com.example.demo.batch.writer.ErrorCsvWriter;
import com.example.demo.batch.writer.ItemsWriter;
import com.example.demo.domain.Category;
import com.example.demo.domain.ItemProcessResult;
import com.example.demo.domain.Original;

@Configuration
public class BatchConfig {

    @Bean // ← このメソッドが返すJobをSpringの管理対象として登録する
        // ↓Spring BatchのJobを返す
    public Job productMigrationJob(
        JobRepository jobRepository,    // Jobの実行履歴や状態を管理する 
        Step categoryStep,              // 最初に実行するカテゴリ以降Step
        Step itemsStep,                 // 次に実行する商品移行Step
        ExecutionTimeListener executionTimeListener
        ){                
        // "migrationJob"という名前のJobを作成する
        return new JobBuilder("migrationJob", jobRepository)
               // 最初にcategoryStepを実行する
               .start(categoryStep)
               // categoryStepが正常終了したらitemsStepを実行する
               .next(itemsStep)
               // Job全体の開始・終了を監視し、Job全体の実行時間をログ出力するListenerを設定
               .listener(executionTimeListener)
               // 設定した内容でJobを完成させる
               .build();

    }

    @Bean  // このメソッドが返すStepをSpringの管理対象として登録する
    public Step categoryStep(
        JobRepository jobRepository, // Stepの実行履歴や状態を管理する
        PlatformTransactionManager transactionManager,  // Chunk単位のトランザクションを管理する
        CategoryReader categoryReader,
        CategoryProcessor categoryProcessor,
        CategoryWriter categoryWriter,
        ProgressListener progressListener,
        ExecutionTimeListener executionTimeListener
        ){

        // "categoryStep"という名前のStepを作成する
        return new StepBuilder("categoryStep", jobRepository)
            // chunk方式で処理する。5000件ごとに処理・コミット
            // <String,Integer>は仮置き。1つ目：Readerが返す型。2つ目：Processorが返す型
            .<String,List<Category>>chunk(5000,transactionManager)
            // originalからcategory_nameを読み込むReader
            .reader(categoryReader.categoryReader())
            // cateogry_nameを分解・加工するProcessor
            .processor(categoryProcessor)
            // categoryテーブルへ登録するWriter
            .writer(categoryWriter)
            // Chunk処理の進捗を10,000件ごとにログ出力するListenerを設定
            .listener(progressListener)
            // Stepの開始・終了を監視し、Step全体の実行時間をログ出力するListenerを設定
            .listener(executionTimeListener)
            // ここまでの設定でcateogryStepを完成させる
            .build();

    }

    /**
     * 商品データをoriginalテーブルからitemsテーブルへ移行するStepを作成する
     * 
     * Readerでoriginalの商品データを読み込み、
     * Processorで正常・エラー判定やItemへの変換を行い、
     * Writerで正常データとエラーデータをそれぞれの出力先へ振り分ける
     * @param jobRepository Spring BatchのJob・Step実行状態を管理するRepository
     * @param transactionManager　Chunk単位のトランザクションを管理するためのもの
     * @return　商品データ移行処理を行うitemsStep
     */
    @Bean
    public Step itemsStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader itemReader,
        ItemProcessor itemProcessor,
        ErrorCsvWriter errorCsvWriter,
        ClassifierCompositeItemWriter<ItemProcessResult> errorCheck,
        ProgressListener progressListener,
        ExecutionTimeListener executionTimeListener
    ){
        return new StepBuilder("itemsStep", jobRepository)

            // 5000件を1Chunkとして処理する。
            // Chunk単位でトランザクションが管理される
            .<Original,ItemProcessResult>chunk(5000,transactionManager)
            // originalテーブルから商品データを読み込みReaderを設定する
            .reader(itemReader.itemReader())
            // Readerから受け取ったデータに対して、
            // カテゴリーエラー判定やItemへの変換を行うProcessorを設定
            .processor(itemProcessor)
            // Processorの処理結果を受け取り、
            // 正常データとエラーデータをそれぞれのWriterへ振り分けるWriterを設定
            .writer(errorCheck)
            // ErrorCsvWriterのファイルopen / closeをStepに管理させるためstream登録
            .stream(errorCsvWriter)
            // Chunk処理の進捗を10,000件ごとにログ出力するListenerを設定
            .listener(progressListener)
            // Stepの開始・終了を監視し、Step全体の実行時間をログ出力するListenerを設定
            .listener(executionTimeListener)
            // 上記の設定内容をもとにitemsStepを作成する
            .build();
    }

    /**
     * ItemProcessorから渡された処理結果を確認し、
     * 正常データとエラーデータで書き込み先を振り分けるWriterを作成
     * 
     * 正常データはItemsWriterへ渡してitemsテーブルへ登録し、
     * エラーデータはErrorCsvWriterへ渡してCSV出力する
     * @param itemsWriter　正常データをitemsテーブルへ登録するWriter
     * @param errorCsvWriter　エラーデータをCSVへ出力するWriter
     * @return　正常・エラーの振り分けルールを設定したWriter
     */
    @Bean
    public ClassifierCompositeItemWriter<ItemProcessResult> errorCheck(ItemsWriter itemsWriter,ErrorCsvWriter errorCsvWriter){

        // 正常データかエラーデータかを見て、書き込み先を決めるWriter本体を作成
        ClassifierCompositeItemWriter<ItemProcessResult> checkItem = new ClassifierCompositeItemWriter<>();

        // Processorから渡されたItemProcessResultを1件ずつ確認し、
        // Itemが存在するかどうかで書き込み先を決定する
        checkItem.setClassifier(itemProcessResult ->{
            // Itemが存在する場合は正常データなのでitemsテーブルへ登録するItemWriterへ渡す
            if(itemProcessResult.getItem() != null){
                return itemsWriter;
            // Itemが存在しない場合はエラーデータなので、CSVへ出力するErrorCsvWriterへわた    
            }else{
                return errorCsvWriter;
            }
        });

        // 振り分けルールを設定したWriterを返し、itemsStepから使用できるようにする
        return checkItem;

    }
}
