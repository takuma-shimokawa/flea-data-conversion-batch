package com.example.demo.enums;

/**
 * 商品データ移行時のエラー理由を管理するenum。
 *
 * <p>
 * ItemProcessorで判定したエラー理由を固定値として管理し、
 * 文字列の直接記述やタイプミスを防止する。
 * </p>
 */
public enum ErrorReason {

    CATEGORY_NULL("CATEGORY_NULL"),
    CATEGORY_EMPTY("CATEGORY_EMPTY"),
    CATEGORY_LEVEL_INSUFFICIENT("CATEGORY_LEVEL不足"),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND");

    private final String code;

    /**
     * エラー理由を生成する。
     *
     * @param code CSVへ出力するエラー理由
     */
    ErrorReason(String code) {
        this.code = code;
    }

    /**
     * CSVへ出力するエラー理由を取得する。
     *
     * @return エラー理由
     */
    public String getCode() {
        return code;
    }

}
