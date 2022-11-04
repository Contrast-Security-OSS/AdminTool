### 概要
TeamServerのセキュリティ制御や例外などをGUIで一括設定できるツールです。  
TeamServer上でもセキュリティ制御や例外の設定を行うことはできますが、施す設定が大量にある場合は時間を要してしまいます。  
本ツールを使うことにより、大量の設定を操作する手間を軽減させることができます。  
使用方法の詳細については [Release](https://github.com/Contrast-Security-OSS/AdminTool/releases) からダウンロードできるzipファイルに同梱のマニュアルpdfをご確認ください。


### 動作環境
Windows8.1、Windows10  
いずれも、jre1.8.0_202

### ソースからビルドする場合

ビルドからではなく、すぐにお使いいただく場合は[リリースについて](#リリースについて)を参照ください。


#### 環境にあわせてbuild.gradleの以下箇所を弄ってください。

- Windows 64bitの場合（java 64bitでEclipseなど動かしている場合はこのままで良いです）

  ```gradle
  compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
  //compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  //compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
  ```
- Windows 32bitの場合（exeを作るために32bit版のビルドをする場合）

  ```gradle
  //compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
  compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  //compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
  ```

#### コマンドプロンプト、ターミナルでビルドする場合

- Windows
  ```powershell
  gradlew clean jar
  ```
build\libsの下にjarが作成されます。

#### Eclipseでビルド、実行できるようにする場合

- Windows
  ```powershell
  gradlew cleanEclipse eclipse
  ```
Eclipseでプロジェクトをリフレッシュすると、あとは実行でcom.contrastsecurity.csvdltool.Mainで、ツールが起動します。

#### Windows配布用のexe化について

- launch4jを使っています。
- launch4j.xmlを読み込むと、ある程度設定が入っていて、あとはjar（ビルドによって作成された）やexeのパスを修正するぐらいです。
- jreがインストールされていない環境でも、jreフォルダを同梱することで環境に依存せずjavaを実行できるような設定になっています。  
  jreをDLして解凍したフォルダを **jre** というフォルダ名として置いておくと、優先して使用するような設定に既になっています。
- 32bit版Javaにしている理由ですが、今はもうないかもしれないですが、32bit版のwindowsの場合も想定してという感じです。

#### Mac配布用のapp化について

- javapackagerを使っています。
- jreを同梱させるため、実施するMacに1.8.0_202のJREフォルダを任意の場所に配置しておいてください。
- jarpackage.sh内の3〜7行目を適宜、修正してください。
- jarpackage.shを実行します。
  ```bash
  ./jarpackage.sh
  ```
  build/libs/bundle下にappフォルダが作られます。

#### exe, appへの署名について

まず、証明書ファイル(pfx)と証明書パスワードを入手してください。  
署名についは以下の手順で実行してください。  
- Windows  
  - エイリアスの確認
    ```powershell
    keytool -list -v -storetype pkcs12 -keystore C:\Users\turbou\Desktop\AdminTool_work\XXXXX.pfx
    # 証明書パスワードを入力
    ```
  - 署名  
    launch4jのsign4jを使用します。
    ```powershell
    cd C:\Program Files (x86)\launch4j\sign4j
    sign4j.exe java -jar jsign-2.0.jar --alias 1 --keystore C:\Users\turbou\Desktop\AdminTool_work\XXXXX.pfx --storepass [パスワード] C:\Users\turbou\Desktop\AdminTool_work\common\AdminTool_1.0.2.exe
    ```
  - 署名の確認  
    署名の確認については、exeを右クリック->プロパティ で確認できます。

### 起動後の使い方について

#### 基本設定に関して
- 設定画面で、TeamServerのURL、ユーザ名、サービスキーを個人のものに変更してください。
- 組織情報の追加ボタンで組織を追加してください。
  組織は複数登録が可能です。セキュリティ制御や例外の設定を行う対象の組織にチェックを入れてください。
  ※ 必要に応じて、プロキシ設定も行っておいてください。
#### セキュリティ制御の操作について
セキュリティ制御は組織に対して行う設定となります。  
- セキュリティ制御のインポートを行う際は、インポートデータとなるjson形式のファイルを用意してください。  
  インポートボタンを押して、jsonファイルを選択してください。インポートが完了すると成功数、失敗数を表示するダイアログが表示されます。  
  インポートデータとなるjsonファイルは「スケルトンJSON出力」でスケルトンファイルを生成することができます。  
  ルールを個別に設定する場合は「ルール一覧」で設定可能なルールを表示することができます。
- エクスポートはバックアップを取得する際に使用します。
- TeamServerに登録されているセキュリティ制御を削除する場合は「削除対象を表示」を実行します。  
  削除対象を絞り込みたい場合はボタン右のテキストボックスに入力してください。(アスタリスクを使うことで前方一致、後方一致、部分一致の指定が可能です)  
  実行すると削除対象の確認ダイアログが表示されます。対象にチェックが入っていることを確認して「削除実行」によって、セキュリティ制御を削除することができます。
- 「インポート済みチェック」を実行することで、比較元のjsonファイルとTeamServerに登録されているセキュリティ制御の差異を確認することができます。
#### 例外の操作について
例外はアプリケーションに対して行う設定となります。  
- 設定を閉じて、アプリの読み込みをして、アプリケーション一覧をロードします。


使用方法の詳細については [Release](https://github.com/Contrast-Security-OSS/AdminTool/releases) からダウンロードできるzipファイルに同梱のマニュアルpdfをご確認ください。

### リリースについて
[Release](https://github.com/Contrast-Security-OSS/AdminTool/releases) で以下2種類のバイナリを提供しています。ビルド不要でダウンロード後すぐにお使いいただけます。
- Windows
  - AdminTool_X.X.X.zip  
    初回ダウンロードの場合はこちらをダウンロードして解凍して、お使いください。  
    jreフォルダ（1.8.0_202）が同梱されているため、exeの起動ですぐにツールを使用できます。
  - AdminTool_X.X.X.exe  
    既にzipをダウンロード済みの場合はexeのダウンロードと入れ替えのみでツールを使用できます。

以上
