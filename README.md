# Decrescendo
多粒度コードクローン検出ツール

## ビルド&実行方法

JDK8+をインストールし`JAVA_HOME`を設定済みの環境で、以下のコマンドを実行

```
$ gradlew installApp
```

`build/install`にDecrescendoディレクトリが生成される。
`build/install/Decrescendo/bin`内のバッチファイルを実行する。

## 設定
作業ディレクトリ上に以下の内容を記述したプロパティファイルを`decrescendo.properties`という名前で用意する

```
targetPath=D:\\work\\apache\\exp-tomcat\\tomcat
outputPath=results_test.db
language=java
file=true
method=true
codeFragment=true
fileMinTokens=50
methodMinTokens=50
codeFragmentMinTokens=50
gapRate=0.3
suffix=true
smithWaterman=false
```

- targetPath: 検出対象のパス
- outputPath: 検出結果を格納するDBのパス
- language: 検出対象の言語(javaのみ)
- file: ファイル単位のクローン検出を行うか
- method: メソッド単位のクローン検出を行うか
- codeFragment: コード片単位のクローン検出を行うか
- fileMinTokens: ファイル単位のクローン検出における最小クローン長
- methodMinTokens: メソッド単位のクローン検出における最小クローン長
- codeFragmentMinTokens: コード片単位のクローン検出における最小クローン長
- gapRate: SmithWatermanの最大ギャップ率
- suffix: サフィックスツリーによるコード片単位のクローン検出をするか
- smithWaterman: SmithWatermanによるコード片単位のクローン検出をするか