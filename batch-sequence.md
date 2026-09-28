# 商品データ移行バッチ シーケンス図

```mermaid
sequenceDiagram
    participant Job
    participant CategoryReader
    participant CategoryProcessor
    participant CategoryWriter
    participant ItemReader
    participant ItemProcessor
    participant ItemWriter
    participant DB

    Job->>CategoryReader: カテゴリデータを読み込む
    CategoryReader->>CategoryProcessor: カテゴリ情報を渡す
    CategoryProcessor->>CategoryWriter: 階層化したカテゴリを渡す
    CategoryWriter->>DB: categoryテーブルへ登録

    Job->>ItemReader: 商品データを読み込む
    ItemReader->>ItemProcessor: 商品情報を渡す
    ItemProcessor->>ItemWriter: 変換した商品データを渡す
    ItemWriter->>DB: itemsテーブルへ登録
