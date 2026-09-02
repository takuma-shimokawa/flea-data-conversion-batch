package com.example.demo.batch.writer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.example.demo.domain.ItemProcessResult;
import com.example.demo.domain.Original;

/**
 * ItemProcessorでエラーと判定された商品データをCSVへ出力するWriter
 * FlatFileItemWriterを利用し、itemStep開始時にエラーCSVを作成して、
 * エラーデータを1件ずつ出力する
 */
@Component
@StepScope
public class ErrorCsvWriter extends FlatFileItemWriter<ItemProcessResult> {

    public ErrorCsvWriter() {

        // バッチ実行毎に異なるCSVファイル名を作成するため、現在日時を取得
        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // 出力するCSVファイルを設定
        setResource(
                new FileSystemResource(
                        "error_" + dateTime + ".csv"));

        // Spring Batch上でWriterを識別する名前委を設定
        setName("errorCsvWriter");

        // CSVの1行目にヘッダーを出力
        setHeaderCallback(writer -> writer.write("id,name,category_name,error_reason"));

        // ItemProcessorResult 1件をCSVの1行へ変換する処理
        setLineAggregator(itemProcessResult -> {
            Original original = itemProcessResult.getOriginal();
            return csvEscape(original.getId())
                    + ","
                    + csvEscape(original.getName())
                    + ","
                    + csvEscape(original.getCategoryName())
                    + ","
                    + csvEscape(itemProcessResult.getErrorReason());
        });

    }

    /**
     * CSV内でカンマやダブルクォートなどを含む値を正しく出力できる形式へ変換する
     * @param value CSVへ出力する値
     * @return　CSV出力用の文字列
     */
    private String csvEscape(Object value){
        if(value == null){
            return "";
        }

        String text = String.valueOf(value);

        // ダブルクォートはCSVのルールに従って2つ重ねる
        text = text.replace("\"", "\"");

        // 値全体をダブルクォートで囲む
        return "\"" + text + "\"";
    }

}
