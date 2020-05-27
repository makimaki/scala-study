message-api
====

各種チャネル毎の送受信フォーマットとmcseのメッセージフォーマットの変換を行う仲介者です。

### 事前作業

shared のものとは別に, message-api のみで使用する依存サービスの Docker コンテナを立てます。

```
$ docker-compose -f message-api/dev/docker-compose.yml up -d
```

### Web 以外のインテグレーションの動作確認をする場合

トップレベルの README に従って投入したクライアントマスタのクライアント `00000000-0000-0000-0000-000000000000` のプロパティ `integrations` を下記のように設定します。

```json
{
  "values": [
    {
      "entityValue": {
        "properties": {
          "channel": {
            "stringValue": "${channel}"
          },
          "id": {
            "stringValue": "aaaaa"
          }
        }
      }
    }
  ]
}
```

`${channel}` には、下記のいずれかを入力します。

- LINE@: `line-at`
- Dialog One (LINE): `line-dialog-one`


また、以下のエンティティを作成します。プロパティ部分はインテグレーションの種別に拠ります。

- クライアント設定
  - 名前空間: `00000000-0000-0000-0000-000000000000`
  - 種類: `Config`
  - キー: カスタム名 `default`
  - プロパティ:
    - LINE@: `lineAtIntegrations` 
    - Dialog One (LINE): `lineDialogOneIntegrations`
（埋め込みエンティティの配列。設定サンプルは以下）

#### LINE@

```json
{
  "values": [
    {
      "entityValue": {
        "properties": {
          "followEventMessage": {
            "stringValue": "https://www.example.com/login?lineId={LINE_ID}"
          },
          "channelSecret": {
            "stringValue": "ttttt"
          },
          "channelAccessToken": {
            "stringValue": "kkkkk"
          },
          "id": {
            "stringValue": "aaaaa"
          }
        }
      }
    }
  ]
}
```

#### Dialog One (LINE)

```json
{
  "values": [
    {
      "entityValue": {
        "properties": {
          "accountId": {
            "stringValue": "ppppp"
          },
          "apiKey": {
            "stringValue": "ttttt"
          },
          "webhookKey": {
            "stringValue": "kkkkk"
          },
          "id": {
            "stringValue": "aaaaa"
          }
        }
      }
    }
  ]
}
```

### 起動

```sh
sbt 'project message-api' 'run 9000'
```

### テスト

```sh
sbt 'project message-api' test
sbt 'project message-api' it:test
```

### 実行

```sh
### Web インテグレーション ###

$ curl -XPOST -w '%{http_code}\n' -H 'Content-Type:application/json' -d '{"time": "2016-10-26T08:01:08.933Z", "client_id": "00000000-0000-0000-0000-000000000000", "session_id": "testsession", "browser_id": "testbrowser", "message": "スカートください", "reason": "manual"}' http://localhost:9000

### LINE インテグレーション ###

# 環境変数 SIGNATURE_VALIDATION_ENABLED=false でシグニチャバリデーションは無効にしてある想定

# follow イベント
$ curl -XPOST -w '%{http_code}\n' -H 'Content-Type:application/json' -d '{"events":[{"type":"follow","timestamp":123,"source":{"type":"user","userId":"u00-9999"},"replyToken":"KKKKK"}]}' http://localhost:9000/00000000-0000-0000-0000-000000000000/integrations/aaaaa

# message イベント
$ curl -XPOST -w '%{http_code}\n' -H 'Content-Type:application/json' -d '{"events":[{"type":"message","timestamp":123,"source":{"type":"user","userId":"u00-9999"},"replyToken":"KKKKK","message":{"id":"MMMMMM","type":"text","text":"質問2"}}]}' http://localhost:9000/00000000-0000-0000-0000-000000000000/integrations/aaaaa

# postback イベント
$ curl -XPOST -w '%{http_code}\n' -H 'Content-Type:application/json' -d '{"events":[{"type":"postback","timestamp":123,"source":{"type":"user","userId":"u00-9999"},"replyToken":"KKKKK","postback":{"data":"postback_FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"}}]}' http://localhost:9000/00000000-0000-0000-0000-000000000000/integrations/aaaaa

### Dialog One (LINE) インテグレーション ###

# ※ LINE と同じ
```

### message-broker

リプライではなく、スグレス側から能動的なプッシュ通知を実施する際にメッセージキューから各種チャンネルへの仲介を行ないます。

### message-broker の起動について

動作確認については、開発環境にデプロイして行う。

※ sbt runMainについては、確認できていないため非推奨
