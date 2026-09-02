package com.example.demo.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.ymlに定義したエラーCSVの出力設定を保持するクラス。
 */
@Component
@ConfigurationProperties(prefix = "batch.error-csv")
public class ErrorCsvProperties {

    private String outputPath;
    private String fileName;

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
