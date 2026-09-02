package com.example.demo.domain;

import com.example.demo.enums.ErrorReason;

/**
 * ItemProcessorの処理結果を保持するクラス。
 *
 * 正常データの場合はItemを保持し、
 * エラーデータの場合はOriginalとErrorReasonを保持する。
 */
public class ItemProcessResult {

    // 正常データの場合にitemsテーブルへ登録する商品情報
    private Item item;

    // エラーデータの場合にCSVへ出力する元の商品情報
    private Original original;

    // 商品データ移行時のエラー理由
    private ErrorReason errorReason;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Original getOriginal() {
        return original;
    }

    public void setOriginal(Original original) {
        this.original = original;
    }

    public ErrorReason getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(ErrorReason errorReason) {
        this.errorReason = errorReason;
    }
}
