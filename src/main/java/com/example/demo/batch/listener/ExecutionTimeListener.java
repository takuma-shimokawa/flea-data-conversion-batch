package com.example.demo.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Job全体および各Stepの実行時間を計測し、ログへ出力するListener
 * 
 * JobExecutionListenerでJob全体の開始・終了を監視し、
 * StepExecutionListenerで各Stepの開始・終了を監視
 */
@Component
public class ExecutionTimeListener implements JobExecutionListener,StepExecutionListener {

    // ExecutionTimeListener用のログ出力
    Logger log = LoggerFactory.getLogger(ExecutionTimeListener.class);

    // Job開始時刻を保持する
    long jobStart;

    /**
     * Job開始前に呼び出される
     * 
     * Job全体の実行時間を計測するため、開始時刻をミリ秒で保持する
     * @param jobExecution 実行するJobの情報
     */
    @Override
    public void beforeJob(JobExecution jobExecution){
        // Job開始時点の現在時刻を保存
        jobStart = System.currentTimeMillis();
    }

    /**
     * Job終了後に呼び出される
     * 
     * Job終了時刻と開始時刻の差からJob全体の実行時間を計算してログへ出力する
     * @param jobExecution 実行終了したJobの情報
     */
    @Override
    public void afterJob(JobExecution jobExecution){
        // Job終了時点の現在時刻を取得
        long jobEnd = System.currentTimeMillis();
        // 終了時刻 - 開始時刻でJob全体の実行時間を計算
        long executionTime = jobEnd - jobStart; 
        // Job全体の実行時間をミリ秒でログ出力
        log.info("Job実行時間 : {}ms", executionTime);
    }

    // 現在実行中のStepの開始時刻を保持
    long stepStart;

    /**
     * Step開始前に呼び出される
     * 
     * 各Stepの実行時間を計測するため、Step開始時刻をミリ秒で保持
     * @param stepExecution 実行するStepの情報
     */
    @Override
    public void beforeStep(StepExecution stepExecution){
        // Stepの開始時点の現在時刻を保存
        stepStart = System.currentTimeMillis();
    }

    /**
     * Step終了後に呼び出される
     * 
     * Step終了時刻と開始時刻の差から実行時間を計算し、Step名と合わせてろぐへ出力する
     * @param stepExecution 実行終了したStepの情報
     * @return Stepの終了状態
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution){
        // Step終了時点の現在時刻を取得
        long stepEnd = System.currentTimeMillis();
        // 終了時刻 -　開始時刻でStepの実行時間を計算
        long stepExecutionTime = stepEnd - stepStart;
        // どのStepに何ミリ秒かかったかをログ出力
        log.info("Step実行時間 : {} {}ms", stepExecution.getStepName(),stepExecutionTime);
        // Step本来の終了状態をそのまま返す
        return stepExecution.getExitStatus();
    }

}
