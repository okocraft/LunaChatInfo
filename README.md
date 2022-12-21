# LunaChatInfo
LunaChatの現在のデフォルトチャンネルをPlaceholderAPIに伝達するためだけのプラグイン

## 導入方法
まずは[ここ](https://github.com/okocraft/LunaChatInfo/releases/tag/v1.0.0)からダウンロードします。
配布しているjarはSpigotサーバーにもBungeecordサーバーにも使えます。

### Spigotサーバー側
PlaceholderAPI、LunaChat、LunaChatInfoをインストールします。

これで導入を終われば、Spigotサーバーのみでの運用となります。

### Bungeecordサーバー側
LunaChatInfoとLunaChatをインストールし、LunaChatをBungeecordで利用できるように適切に設定します。

## 機能
Spigotサーバー側のPlaceholderAPIに以下のプレースホルダを追加します。
* `%lunachatinfo%` or `%lunachatinfo_defaultchannel%` 実行プレイヤーの現在のデフォルトチャンネルを返します。
* `%lunachatinfo_defaultchannel_<player-name>%` 指定したプレイヤーのデフォルトチャンネルを返します。

Bungeecordサーバー側に適切に導入することで、BungeecordにインストールされたLunaChatのデフォルトチャンネルを返すようになります。

将来的にデフォルトチャンネル以外の値をBungeecordからSpigotに伝達する必要が生じた場合、機能を追加するかもしれません。 
追加要望はissueを作成してくれれば対応するかもしれません。

## 開発者向け
レポジトリをクローンして、`./gradlew build` または `./gradlew.bat build` をコンソールから実行すると
* `bukkit/build/libs` フォルダの中には bukkit/spigot/paper だけで使えるjar
* `bungeecord/build/libs` フォルダの中には bungeecord/waterfall だけで使えるjar
* `./build/libs` には両方で使えるjar

が生成されます。PR歓迎です。