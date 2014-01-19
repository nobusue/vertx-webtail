vertx-webtail
=============

Simple "tail -f" using websocket on Vert.x

Vert.xを利用したシンプルなtailコマンドのサンプルです。

timerで一定時間おきにファイルサイズをチェックし、ファイルサイズが増加していたら差分をイベントバスに送信し、WebSocketサーバー経由でブラウザにリアルタイム表示します。

一応、ファイルの読み込み部分はファイルサイズが大きくなっても耐えられるよう、NIO2のSeekableByteChannelとByteBufferを利用してランダムアクセスを行っています。

このサンプルは、2014/1/17開催の[新春恒例GGX2013ロンドン報告＆LT大会！ - JGGUG G*ワークショップZ Jan 2014](http://jggug.doorkeeper.jp/events/8205)で発表した内容に、多少手を加えたものです。
発表資料は[こちら](http://www.slideshare.net/nobusue/gws-20140117-lt)です。

# 前提条件
* JDK7以上
* Vert.x 2.0以上(2.1M2で動作確認済み)
* WebSocket対応ブラウザ


# 利用方法

## vert.xインストール
http://vertx.io/install.html を参照してください。

Bashが使える環境でしたら、[GVM](http://gvmtool.net/)を利用するのがオススメです。

できればvertxコマンドにパスを通しておく方が何かと楽です。

## vertx-webtail(このモジュール)取得
適当なフォルダでgit cloneなりしてください。vert.x以外への依存関係はないので、配置先など特に制限はありません。

## 監視対象ファイル設定
WebTail.conf(JSON)を編集して、

* tail対象のファイル名
* WebSocketのポート

を指定してください。

## 起動
`$ vertx run WebTail.groovy -conf WebTail.conf
`

サーバーが起動したら、WebSocket対応ブラウザ(Chrome/Safari等)で http://localhost:<websocket_port/ を開いてください。

１秒おきに監視対象のファイルの内容の更新差分がブラウザに表示されるはずです。例えば以下のコマンドなどで試してください。

`$ for i in {1..5}; do date >> /tmp/dummy.log; sleep 1; done
`

`$ cat /dev/null > /tmp/dummy.log
`

# 参考情報

* [Vert.x Core Manual: Periodic Timers](http://vertx.io/core_manual_groovy.html#periodic-timers)
* [Vert.x Core Manual: WebSockets](http://vertx.io/core_manual_groovy.html#websockets)
* [Using Vert.x from the command line](http://vertx.io/manual.html#using-vertx-from-the-command-line)
* [java.nio.channels.SeekableByteChannel](http://docs.oracle.com/javase/jp/7/api/java/nio/channels/SeekableByteChannel.html)
* [ITPro Java技術最前線「New I/Oで高速な入出力」第3回　バッファを使ってみよう ](http://itpro.nikkeibp.co.jp/article/COLUMN/20060417/235453/)

# ToDo

* エラーハンドリング全般
* フィルタ（メッセージ変換）でJSON化など->ElasticSearch連携とか
* セキュリティ確保の仕組み
