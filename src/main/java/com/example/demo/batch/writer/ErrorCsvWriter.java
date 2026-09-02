package com.example.demo.batch.writer;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.example.demo.batch.config.ErrorCsvProperties;
import com.example.demo.domain.ItemProcessResult;
import com.example.demo.domain.Original;

/**
 * ItemProcessorでエラーと判定された商品データをCSVへ出力するWriter。
 *
 * <p>FlatFileItemWriterを利用し、itemsStep開始時にエラーCSVを作成して、
 * エラーデータを1件ずつ出力する。</p>
 */
@Component
@StepScope
public class ErrorCsvWriter extends FlatFileItemWriter<ItemProcessResult> {

    /**
     * application.ymlのCSV出力設定を使用してErrorCsvWriterを生成する。
     *
     * @param properties エラーCSVの出力パス・ファイル名を保持する設定クラス
     */
    public ErrorCsvWriter(ErrorCsvProperties properties) {

        // バッチ実行ごとに異なるCSVファイル名を作成するため、現在日時を取得
        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // application.ymlで設定したファイル名の%s部分を実行日時へ置き換える
        String fileName = String.format(properties.getFileName(), dateTime);

        // application.ymlの出力パスとファイル名を組み合わせる
        Path outputFile = Path.of(properties.getOutputPath(), fileName);

        // 出力するCSVファイルを設定
        setResource(new FileSystemResource(outputFile));

        // Spring Batch上でWriterを識別する名前を設定
        setName("errorCsvWriter");

        // CSVの1行目にヘッダーを出力
        setHeaderCallback(
                writer -> writer.write("id,name,category_name,error_reason"));

        // ItemProcessResult 1件をCSVの1行へ変換する
        setLineAggregator(itemProcessResult -> {
            Original original = itemProcessResult.getOriginal();

            return csvEscape(original.getId())
                    + ","
                    + csvEscape(original.getName())
                    + ","
                    + csvEscape(original.getCategoryName())
                    + ","
                    + csvEscape(itemProcessResult.getErrorReason().getCode());
        });
    }

    /**
     * CSV内でカンマやダブルクォートなどを含む値を
     * 正しく出力できる形式へ変換する。
     *
     * @param value CSVへ出力する値
     * @return CSV出力用の文字列
     */
    private String csvEscape(Object value) {

        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        // ダブルクォートはCSVのルールに従って2つ重ねる
        text = text.replace("\"", "\"\"");

        // 値全体をダブルクォートで囲む
        return "\"" + text + "\"";
    }
}
