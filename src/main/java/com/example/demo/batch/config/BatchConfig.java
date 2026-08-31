package com.example.demo.batch.config;

import com.example.demo.repository.CategoryRepository;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.batch.processor.CategoryProcessor;
import com.example.demo.batch.reader.CategoryReader;
import com.example.demo.batch.writer.CategoryWriter;
import com.example.demo.domain.Category;

@Configuration
public class BatchConfig {
    
    private final CategoryRepository categoryRepository;

    BatchConfig(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Bean // ← このメソッドが返すJobをSpringの管理対象として登録する
        // ↓Spring BatchのJobを返す
    public Job productMigrationJob(
        JobRepository jobRepository,    // Jobの実行履歴や状態を管理する 
        Step categoryStep,              // 最初に実行するカテゴリ以降Step
        Step itemsStep){                // t食いに実行する商品移行Step

        // "migrationJob"という名前のJobを作成する
        return new JobBuilder("migrationJob", jobRepository)
               // 最初にcategoryStepを実行する
               .start(categoryStep)
               // categoryStepが正常終了したらitemsStepを実行する
               .next(itemsStep)
               // 設定した内容でJobを完成させる
               .build();

    }

    @Bean  // このメソッドが返すStepをSpringの管理対象として登録する
    public Step categoryStep(
        JobRepository jobRepository, // Stepの実行履歴や状態を管理する
        PlatformTransactionManager transactionManager,  // Chunk単位のトランザクションを管理する
        CategoryReader categoryReader,
        CategoryProcessor categoryProcessor,
        CategoryWriter categoryWriter
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
            // ここまでの設定でcateogryStepを完成させる
            .build();

    }

    @Bean
    public Step itemsStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ){
        return new StepBuilder("itemsStep", jobRepository)
            .<String,Integer>chunk(5000,transactionManager)
            .reader(null)
            .processor(null)
            .writer(null)
            .build();
    }
}
