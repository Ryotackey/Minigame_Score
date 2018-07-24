package red.man10.minigame_score

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.kotlin.CustomConfig

class Minigame_Score : JavaPlugin() {

    var config = CustomConfig(this)
    var item = CustomConfig(this, "item.yml")

    override fun onEnable() {
        // Plugin startup logic
        item.saveDefaultConfig()
        config.saveDefaultConfig()

        getCommand("mgscore").executor = this

        val create = CreateTable(this)
        create.start()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<String>?): Boolean {

        if (args == null) return false
        
        if(sender == null) return false

        when (args.size) {

            0 -> {
                if (sender !is Player) return false
                val senderp: Player = sender
                
                val ss = SendScore(this, senderp, sender)
                ss.start()
                
                return true
            }

            1 -> {
                if (args[0].equals("help", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {

                        sender.sendMessage("§6§l========§a§l<§b§lMinigame §e§lScore§a§l>§6§l========")
                        sender.sendMessage("・/mgscore show [名前] : [名前]の人のスコア一覧を見る")
                        sender.sendMessage("・/mgscore delete [名前] : [名前]の人のスコアを削除する")
                        sender.sendMessage("・/mgscore additem <ミニゲーム名> : <ミニゲーム名>の報酬アイテム設定(手に持ってるもの)")
                        sender.sendMessage("・/mgscore getitem <ミニゲーム名> : <ミニゲーム名>の報酬アイテムをスコアと引き換えに手に入れる")
                        sender.sendMessage("・/mgscore add [名前] <ミニゲーム名> : [名前]の人の<ミニゲーム名>のスコアを1加算")
                        sender.sendMessage("・/mgscore reduce [名前] <ミニゲーム名> : [名前]の人の<ミニゲーム名>のスコアを1減算")
                        sender.sendMessage("・/mgscore set [名前] <ミニゲーム名> {数値} : [名前]の人の<ミニゲーム名>のスコアを{数値}にする")
                        sender.sendMessage("Created by Ryotackey")

                    }
                }

                if (args[0].equals("reload", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {
                        item.reloadConfig()
                        config.reloadConfig()
                        sender.sendMessage("§aReload complete")
                    }
                }
            }

            2 -> {
                if (args[0].equals("show", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {

                        
                        val ofp = Bukkit.getOfflinePlayer(args[1])
                        
                        if (ofp == null){
                            sender.sendMessage("§cそのPlayerは存在しません")
                            return true
                        }

                        val p = ofp.player

                        val ss = SendScore(this, p, sender)
                        ss.start()
                        
                        return true

                    } else {
                        sender.sendMessage("§c権限がありません")
                        return true
                    }

                }

                if (args[0].equals("delete", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {

                        val ofp = Bukkit.getOfflinePlayer(args[1])

                        if (ofp == null){
                            sender.sendMessage("§cそのPlayerは存在しません")
                            return true
                        }

                        val p = ofp.player

                        val ds = DeleteScore(this, p, sender)
                        ds.start()

                        return true

                    } else {
                        sender.sendMessage("§c権限がありません")
                    }
                }

                if (args[0].equals("additem", ignoreCase = true)) {
                    if (sender !is Player) return false
                    val havep = sender as Player?
                    if (sender.hasPermission("minigamescore")) {

                        item.getConfig()!!.set("item." + args[1], havep!!.inventory.itemInMainHand)
                        item.saveConfig()

                        havep.sendMessage("§aitemadd complete")

                    } else {
                        sender.sendMessage("§c権限がありません")
                    }
                }

                if (args[0].equals("getitem", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {
                        if (sender !is Player) return false
                        val gi = GetItem(this, sender, args[1])
                        gi.start()
                    }
                }

                if (args[0].equals("rank", ignoreCase = true)) {

                    val gr = GetRank(this, sender, args[1])
                    gr.start()

                    return true

                }
            }

            4 -> {
                if (args[0].equals("set", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {

                        val ofp = Bukkit.getOfflinePlayer(args[1])

                        if (ofp == null){
                            sender.sendMessage("§cそのPlayerは存在しません")
                            return true
                        }

                        val p = ofp.player

                        var point: Int

                        try {
                            point = args[3].toInt()
                        }catch (e: NumberFormatException){
                            sender.sendMessage("§c数字を指定して下さい")
                            return true
                        }

                        val ss = SetScore(this, sender, args[2], p, point)
                        ss.start()

                        return true

                    } else {
                        sender.sendMessage("§c権限がありません")
                    }
                }

                if (args[0].equals("add", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {

                        val ofp = Bukkit.getOfflinePlayer(args[1])

                        if (ofp == null){
                            sender.sendMessage("§cそのPlayerは存在しません")
                            return true
                        }

                        val p = ofp.player

                        var point: Int

                        try {
                            point = args[3].toInt()
                        }catch (e: NumberFormatException){
                            sender.sendMessage("§c数字を指定して下さい")
                            return true
                        }

                        val add = AddScore(this, sender, args[2], p, point)
                        add.start()
                        return true
                    } else {
                        sender.sendMessage("§c権限がありません")
                    }
                }

                if (args[0].equals("reduce", ignoreCase = true)) {
                    if (sender.hasPermission("minigamescore")) {

                        val ofp = Bukkit.getOfflinePlayer(args[1])

                        if (ofp == null){
                            sender.sendMessage("§cそのPlayerは存在しません")
                            return true
                        }

                        val p = ofp.player

                        var point: Int

                        try {
                            point = args[3].toInt()
                        }catch (e: NumberFormatException){
                            sender.sendMessage("§c数字を指定して下さい")
                            return true
                        }

                        val reduce = ReduceScore(this, sender, args[2], p, point)

                        reduce.start()
                        return true
                    } else {
                        sender.sendMessage("§c権限がありません")
                    }
                }
            }
        }
        return true
    }
}

class SendScore(private val plugin: Minigame_Score, val p: Player, val sender: CommandSender) : Thread() {

    override fun run() {

        val mysql = MySQLManager(plugin, "Minigame_Score")

        val count = mysql.query("SELECT count(1) FROM minigame_score WHERE uuid='" + p.uniqueId.toString() + "';")

        if (count == null) {
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        val rs = mysql.query("SELECT * FROM minigame_score where uuid='${p.uniqueId}';")

        if (rs == null) {
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        val minigame_name = mutableListOf<String>()

        val name = mysql.query("Select COLUMN_NAME From INFORMATION_SCHEMA.COLUMNS where table_name='minigame_score' and DATA_TYPE='bigint';")

        if (name == null){
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        while (name.next()){
            minigame_name.add(name.getString("COLUMN_NAME"))
        }

        count.first()

        val countnum = count.getInt("count(1)")

        if (countnum == 0) {

            mysql.execute("insert into minigame_score(name, uuid) values('" + p.name + "', '" + p.uniqueId + "');")

        }

        while (rs.next()) {
            sender.sendMessage("§e§l-----§f§l" + p.displayName + "§e§l-----")
            for (i in minigame_name) {
                sender.sendMessage("§a" + i + "§f:" + rs.getInt(i).toString())
            }
        }

        count.close()
        name.close()
        rs.close()
        mysql.close()
        return

    }

}

class DeleteScore(val plugin: Minigame_Score, val p: Player, val sender: CommandSender): Thread(){

    override fun run() {
        val mysql = MySQLManager(plugin, "Minigame_Score")

        val result = mysql.execute("DELETE FROM `minigame_score` WHERE uuid='${p.uniqueId}';")

        if (result) {
            sender.sendMessage("§adelete complete")
            val record = Record(plugin, p, sender, "delete", "ALL", "ALL")
            record.start()
        } else {
            sender.sendMessage("§cfailed delete")
        }

    }

}

class Record(val plugin: Minigame_Score, val p: Player, val sender: CommandSender, val command: String, val amount: String, val game: String): Thread(){

    override fun run() {
        val mysql = MySQLManager(plugin, "Minigame_Score")

        val result = mysql.execute("insert into minigame_record(game, name, uuid, command, amount) values('" + game + "', '" + p.name + "', '" + p.uniqueId + "', '" + command + "', '" + amount + "');")

        if (result) {
            sender.sendMessage("§arecord complete")
        } else {
            sender.sendMessage("§cfailed record")
        }

    }

}

class GetItem(val plugin: Minigame_Score, val p: Player, val game: String): Thread(){

    override fun run() {

        val mysql = MySQLManager(plugin, "Minigame_Score")
        val rs = mysql.query("SELECT * FROM minigame_score where uuid='${p.uniqueId}';")

        if (rs == null) {
            p.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        rs.first()

        val point = rs.getInt(game)

        if (point < 5){
            p.sendMessage("§c交換には5ポイント必要です")
            return
        }

        val result = mysql.execute("update minigame_score set " + game + "=" + game + "-5 where uuid='" + p.uniqueId + "';")

        if (!result){
            p.sendMessage("§4データベースに接続できません")
            return
        }

        val rewarditem = plugin.item.getConfig()!!.getItemStack("item.$game")
        p.inventory.addItem(rewarditem)
        p.sendMessage("§a交換完了しました§f(§e" + game + "§f:" + point + "§6⇒§f" + (point - 5) + ")")

        val record = Record(plugin, p, p, "getitem", "-5", game)
        record.start()

        rs.close()
        mysql.close()

    }

}

class GetRank(val plugin: Minigame_Score, val sender: CommandSender, val game: String): Thread(){

    override fun run() {
        val mysql = MySQLManager(plugin, "Minigame_Score")

        val rs = mysql.query("SELECT * FROM minigame_score ORDER BY $game desc limit 10;")

        if (rs == null){
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        var i = 0

        while (rs.next()) {

            val rankname = rs.getString("name")
            val rank = rs.getInt(game)

            if (i == 0) {
                sender.sendMessage("§a§l========§b§l$game§e§lRanking§a§l========")
            }

            sender.sendMessage("§l" + (i + 1) + ".  §6§l" + rankname + " §c§l: §e§l" + rank)

            i++
        }
        rs.close()
        mysql.close()

    }

}

class SetScore(val plugin: Minigame_Score, val sender: CommandSender, val game: String, val p: Player, val amount: Int): Thread(){

    override fun run() {

        val mysql = MySQLManager(plugin, "Minigame_Score")

        val count = mysql.query("SELECT count(1) FROM minigame_score WHERE uuid='" + p.uniqueId + "';")

        if (count == null) {
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        count.first()

        val count1 = count.getInt("count(1)")

        val rs = mysql.query("SELECT * FROM minigame_score;")

        if (rs == null) {

            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return

        }

        if (count1 == 0){

            val result = mysql.execute("insert into minigame_score(name, uuid, " + game + ") values('" + p.name + "', '" + p.uniqueId + "', " + amount + ");")

            if (result) {
                sender.sendMessage("insert complete")
                val record = Record(plugin, p, sender, "set", amount.toString(), game)
                record.start()
                return
            } else {
                sender.sendMessage("failed insert")
                return
            }

        }else{

            val result = mysql.execute("update minigame_score set " + game + "=" + amount + " where uuid='" + p.uniqueId + "';")

            if (result) {
                sender.sendMessage("update complete")
                val record = Record(plugin, p, sender, "set", amount.toString(), game)
                record.start()
                return
            } else {
                sender.sendMessage("failed update")
                return
            }

        }

    }

}

class AddScore(val plugin: Minigame_Score, val sender: CommandSender, val game: String, val p: Player, val amount: Int): Thread(){

    override fun run() {

        val mysql = MySQLManager(plugin, "Minigame_Score")

        val count = mysql.query("SELECT count(1) FROM minigame_score WHERE uuid='" + p.uniqueId + "';")

        if (count == null) {
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        count.first()

        val count1 = count.getInt("count(1)")

        val rs = mysql.query("SELECT * FROM minigame_score;")

        if (rs == null) {

            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return

        }

        if (count1 == 0) {

            val result = mysql.execute("insert into minigame_score(name, uuid, " + game + ") values('" + p.name + "', '" + p.uniqueId + "', " + amount + ");")

            if (result) {
                sender.sendMessage("insert complete")
                val record = Record(plugin, p, sender, "add", amount.toString(), game)
                record.start()
                return
            } else {
                sender.sendMessage("failed insert")
                return
            }

        }else{

            val result = mysql.execute("update minigame_score set " + game + "=" + game + "+" + amount + " where uuid='" + p.uniqueId + "';")

            if (result) {
                sender.sendMessage("update complete")
                val record = Record(plugin, p, sender, "add", amount.toString(), game)
                record.start()
            } else {
                sender.sendMessage("update failed")
                return
            }
        }
    }
}

class ReduceScore(val plugin: Minigame_Score, val sender: CommandSender, val game: String, val p: Player, val amount: Int): Thread(){

    override fun run() {

        val mysql = MySQLManager(plugin, "Minigame_Score")

        val count = mysql.query("SELECT count(1) FROM minigame_score WHERE uuid='" + p.uniqueId + "';")

        if (count == null) {
            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return
        }

        count.first()

        val count1 = count.getInt("count(1)")

        val rs = mysql.query("SELECT * FROM minigame_score;")

        if (rs == null) {

            sender.sendMessage("§4データが存在しないか、データベースに接続できません")
            return

        }

        if (count1 == 0) {

            val result = mysql.execute("insert into minigame_score(name, uuid, " + game + ") values('" + p.name + "', '" + p.uniqueId + "', " + amount*-1 + ");")

            if (result) {
                sender.sendMessage("insert complete")
                val record = Record(plugin, p, sender, "reduce", amount.toString(), game)
                record.start()
                return
            } else {
                sender.sendMessage("failed insert")
                return
            }

        }else{

            val result = mysql.execute("update minigame_score set " + game + "=" + game + "+" + amount*-1 + " where uuid='" + p.uniqueId + "';")

            if (result) {
                sender.sendMessage("update complete")
                val record = Record(plugin, p, sender, "reduce", amount.toString(), game)
                record.start()
            } else {
                sender.sendMessage("update failed")
                return
            }
        }
    }
}

class CreateTable(val plugin: Minigame_Score): Thread(){

    override fun run() {

        val mysql = MySQLManager(plugin, "Minigame_Score")

        mysql.execute("CREATE TABLE `minigame_history` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(50) DEFAULT NULL,\n" +
                "  `uuid` varchar(50) DEFAULT NULL,\n" +
                "  `bed_wars` bigint(20) DEFAULT '0',\n" +
                "  `man10_escape` bigint(20) DEFAULT '0',\n" +
                "  `tnt_run` bigint(20) DEFAULT '0',\n" +
                "  `skywars` bigint(20) DEFAULT '0',\n" +
                "  `blockhunt` bigint(20) DEFAULT '0',\n" +
                "  `build_battle` bigint(20) DEFAULT '0',\n" +
                "  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  `time` int(20) DEFAULT NULL,\n" +
                "  `man10_escape_exchange` bigint(20) DEFAULT '0',\n" +
                "  `bed_wars_exchange` bigint(20) DEFAULT '0',\n" +
                "  `tnt_run_exchange` bigint(20) DEFAULT '0',\n" +
                "  `skywars_exchange` bigint(20) DEFAULT '0',\n" +
                "  `blockhunt_exchange` bigint(20) DEFAULT '0',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1;")

        mysql.execute("CREATE TABLE `minigame_record` (\n" +
                "  `id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                "  `game` varchar(50) DEFAULT NULL,\n" +
                "  `name` varchar(50) DEFAULT NULL,\n" +
                "  `uuid` varchar(50) DEFAULT NULL,\n" +
                "  `command` varchar(50) DEFAULT NULL,\n" +
                "  `amount` varchar(50) DEFAULT NULL,\n" +
                "  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                "\n")

        mysql.execute("CREATE TABLE `minigame_score` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(50) DEFAULT NULL,\n" +
                "  `uuid` varchar(50) DEFAULT NULL,\n" +
                "  `bed_wars` bigint(20) DEFAULT '0',\n" +
                "  `man10_escape` bigint(20) DEFAULT '0',\n" +
                "  `tnt_run` bigint(20) DEFAULT '0',\n" +
                "  `skywars` bigint(20) DEFAULT '0',\n" +
                "  `blockhunt` bigint(20) DEFAULT '0',\n" +
                "  `build_battle` bigint(20) DEFAULT '0',\n" +
                "  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  `time` int(20) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=343 DEFAULT CHARSET=latin1;\n")

        return

    }

}
