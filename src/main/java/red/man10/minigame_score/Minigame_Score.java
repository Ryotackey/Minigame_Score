package red.man10.minigame_score;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class Minigame_Score extends JavaPlugin {

    CustomConfig item;
    CustomConfig config;

    public MySOLManager mysql;

    public class ReturnColumn{

        String[] name;
        int count;

    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        item = new CustomConfig(this, "item.yml");
        item.saveDefaultConfig();

        config = new CustomConfig(this, "config.yml");
        config.saveDefaultConfig();

        mysql = new MySOLManager(this, "Test");

        getCommand("mgscore").setExecutor(this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        switch (args.length){

            case 0:
                if (!(sender instanceof Player)) return false;
                Player senderp = (Player) sender;
                UUID senderpuuid = senderp.getUniqueId();

                ResultSet myrs = mysql.query("SELECT * FROM minigamedb.minigame_score where uuid=" + senderpuuid +"';");

                sendScore(sender, myrs, senderp);
                return true;

            case 1:
                if (args[0].equalsIgnoreCase("help")) {
                    if (sender.hasPermission("minigamescore")){

                        sender.sendMessage("§6§l========§a§l<§b§lMinigame §e§lScore§a§l>§6§l========");
                        sender.sendMessage("・/mgscore show [名前] : [名前]の人のスコア一覧を見る");
                        sender.sendMessage("・/mgscore delete [名前] : [名前]の人のスコアを削除する");
                        sender.sendMessage("・/mgscore additem <ミニゲーム名> : <ミニゲーム名>の報酬アイテム設定(手に持ってるもの)");
                        sender.sendMessage("・/mgscore getitem <ミニゲーム名> : <ミニゲーム名>の報酬アイテムをスコアと引き換えに手に入れる");
                        sender.sendMessage("・/mgscore add [名前] <ミニゲーム名> : [名前]の人の<ミニゲーム名>のスコアを1加算");
                        sender.sendMessage("・/mgscore reduce [名前] <ミニゲーム名> : [名前]の人の<ミニゲーム名>のスコアを1減算");
                        sender.sendMessage("・/mgscore set [名前] <ミニゲーム名> {数値} : [名前]の人の<ミニゲーム名>のスコアを{数値}にする");
                        sender.sendMessage("Created by Ryotackey");

                    }
                }

                if(args[0].equalsIgnoreCase("reload")){
                    if (sender.hasPermission("minigamescore")){

                        reloadConfig();
                        item.reloadConfig();
                        sender.sendMessage("§aReload complete");

                    }
                }

            case 2:
                if (args[0].equalsIgnoreCase("show")) {
                    if (sender.hasPermission("minigamescore")) {

                        Player p = Bukkit.getPlayer(args[1]);
                        UUID puuid = p.getUniqueId();

                        ResultSet rs = mysql.query("SELECT * FROM minigamedb.minigame_score where uuid='" + puuid + "';");
                        sendScore(sender, rs, p);

                        return true;
                    }else {
                        sender.sendMessage("§4§l権限がありません");
                    }

                }

                if (args[0].equalsIgnoreCase("delete")){
                    if (sender.hasPermission("minigamescore")){

                        Player p = Bukkit.getPlayer(args[1]);
                        UUID puuid = p.getUniqueId();

                        boolean result = mysql.execute("DELETE FROM `minigamedb`.`minigame_score` WHERE uuid='" + puuid + "';");

                        if (result == true){
                            p.sendMessage("§adelete complete");
                        }else {
                            p.sendMessage("§4failed delete");
                        }

                        return true;

                    }else {
                        sender.sendMessage("§4権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("additem")){
                    if (!(sender instanceof Player)) return false;
                    Player havep = (Player) sender;
                    if (sender.hasPermission("minigamescore")){

                        item.getConfig().set("item." + args[1], havep.getInventory().getItemInMainHand());
                        item.saveConfig();

                        havep.sendMessage("§aitemadd complete");

                    }else {
                        sender.sendMessage("&4&l権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("getitem")){
                    if (sender.hasPermission("minigamescore")){
                        if (!(sender instanceof Player)) return false;
                        Player getterp = (Player) sender;
                        UUID puuid = getterp.getUniqueId();

                        ResultSet rs = mysql.query("SELECT * FROM minigamedb.minigame_score where uuid='" + puuid + "';");

                        if (rs == null){

                            sender.sendMessage("&4引数が違います");
                            return false;

                        }


                        int point = 0;

                        try {
                            while (rs.next()){

                                point = rs.getInt(args[1]);

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if (point >= 5) {

                            boolean result = mysql.execute("update minigamedb.minigame_score set " + args[1] + "=" + args[1] + "-5 where uuid='" + puuid + "';");
                            boolean result1 = mysql.execute("update minigamedb.minigame_history set " + args[1] + "_exchange = " + args[1] + "_exchange+1 where uuid='" + puuid + "';");
                            if (result == true && result1 == true) {

                                ItemStack rewarditem = item.getConfig().getItemStack("item." + args[1]);
                                getterp.getInventory().addItem(rewarditem);
                                getterp.sendMessage("§a交換完了しました§f(§e" + args[1] + "§f:" + point + "§6⇒§f" + (point-5) + ")");

                            }else {
                                getterp.sendMessage("§c交換できません！");
                            }

                        }else {
                            getterp.sendMessage("§c交換には5ポイント必要です");
                        }

                    }
                }

                if (args[0].equalsIgnoreCase("rank")){

                    ResultSet rs = mysql.query("SELECT * FROM minigamedb.minigame_score ORDER BY " + args[1] + " desc limit 10;");

                    if (rs == null){

                        sender.sendMessage("&4引数が違います");
                        return false;

                    }

                    int i = 0;

                    try {
                        while (rs.next()){

                            String rankname = rs.getString("name");
                            int rank = rs.getInt(args[1]);

                            if (i == 0){
                                sender.sendMessage("§a§l========§b§l" + args[1] + "§e§lRanking§a§l========");
                            }

                            sender.sendMessage("§l" + (i+1) + ".  §6§l" + rankname + " §c§l: §e§l" + rank);

                            i++;

                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return true;

                }

            case 4:
                if (args[0].equalsIgnoreCase("set")){
                    if (sender.hasPermission("minigamescore")) {
                        Player p = Bukkit.getPlayer(args[1]);
                        UUID puuid = p.getUniqueId();

                        ResultSet rs = mysql.query("SELECT * FROM minigamedb.minigame_score;");

                        if (rs == null){

                            sender.sendMessage("&4引数が違います");
                            return false;

                        }


                        ResultSet count = mysql.query("SELECT count(1) FROM minigamedb.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';");



                        try {
                            while (rs.next()) ;
                            {

                                count.first();

                                int count1 = count.getInt("count(1)");

                                if (count1 == 0) {

                                    boolean insert = mysql.execute("insert into minigamedb.minigame_score(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', " + args[3] + ");");

                                    if (insert == true) {
                                        Bukkit.broadcastMessage("§ainsert complete");
                                        return true;
                                    } else {
                                        Bukkit.broadcastMessage("§cfailed insert");
                                        return false;
                                    }

                                }

                                boolean update = mysql.execute("update minigamedb.minigame_score set " + args[2] + "=" + args[3] + " where uuid='" + puuid + "';");

                                if (update == true) {
                                    Bukkit.broadcastMessage("§aset complete");
                                    return true;
                                } else {
                                    Bukkit.broadcastMessage("§cfailed set");
                                    return false;
                                }

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }else {
                        sender.sendMessage("§4§l権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("add")){
                    if (sender.hasPermission("minigamescore")) {
                        Player p = Bukkit.getPlayer(args[1]);
                        UUID puuid = p.getUniqueId();

                        ResultSet rs = mysql.query("SELECT * FROM minigamedb.minigame_score;");

                        if (rs == null){

                            sender.sendMessage("&4引数が違います");
                            return false;

                        }


                        ResultSet count = mysql.query("SELECT count(1) FROM minigamedb.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';");

                        try {
                            while (rs.next()) {

                                count.first();

                                int count1 = count.getInt("count(1)");

                                if (count1 == 0) {

                                    boolean insert = mysql.execute("insert into minigamedb.minigame_score(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', " + args[3] +");");
                                    boolean insert1 = mysql.execute("insert into minigamedb.minigame_history(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', " + args[3] + ");");

                                    if (insert == true) {
                                        Bukkit.broadcastMessage("§ainsert complete");
                                        return true;
                                    } else {
                                        Bukkit.broadcastMessage("§cfailed insert");
                                        return false;
                                    }

                                }

                                boolean update = mysql.execute("update minigamedb.minigame_score set " + args[2] + "=" + args[2] + "+" + args[3] + " where uuid='" + puuid + "';");
                                boolean update1 = mysql.execute("update minigamedb.minigame_history set " + args[2] + "=" + args[2] + "+" + args[3] + " where uuid='" + puuid + "';");

                                if (update == true) {
                                    Bukkit.broadcastMessage("§aadd complete");
                                    return true;
                                } else {
                                    Bukkit.broadcastMessage("§cfailed add");
                                    return false;
                                }

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }else {
                        sender.sendMessage("§4§l権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("reduce")) {
                    if (sender.hasPermission("minigamescore")) {
                        Player p = Bukkit.getPlayer(args[1]);
                        UUID puuid = p.getUniqueId();

                        ResultSet rs = mysql.query("SELECT * FROM minigamedb.minigame_score;");

                        if (rs == null){

                            sender.sendMessage("&4引数が違います");
                            return false;

                        }


                        ResultSet count = mysql.query("SELECT count(1) FROM minigamedb.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';");

                        try {
                            while (rs.next()) ;
                            {

                                count.first();

                                int count1 = count.getInt("count(1)");

                                if (count1 == 0) {

                                    boolean insert = mysql.execute("insert into minigamedb.minigame_score(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', -" + args[3] + ");");

                                    if (insert == true) {
                                        Bukkit.broadcastMessage("§ainsert complete");
                                        return true;
                                    } else {
                                        Bukkit.broadcastMessage("§cfailed insert");
                                        return false;
                                    }

                                }

                                boolean update = mysql.execute("update minigamedb.minigame_score set " + args[2] + "=" + args[2] + "-" + args[3] + " where uuid='" + puuid + "';");

                                if (update == true) {
                                    Bukkit.broadcastMessage("§areduce complete");
                                    return true;
                                } else {
                                    Bukkit.broadcastMessage("§cfailed reduce");
                                    return false;
                                }

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }else {
                        sender.sendMessage("§4§l権限がありません");
                    }
                }


        }

        return true;
    }

    public void sendScore(CommandSender sender, ResultSet rs, Player p){

        UUID puuid = p.getUniqueId();

        ResultSet count = mysql.query("SELECT count(1) FROM minigamedb.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';");

        ReturnColumn mn = minigame_name();

        int count1 = -1;

        try {
            while (count.next()) {
                count1 = count.getInt("count(1)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (count1 == 0){

            mysql.execute("insert into minigamedb.minigame_score(name, uuid) values('" + p.getName() + "', '" + puuid +"');");

        }

        try {
            while (rs.next()){

                sender.sendMessage("§e§l-----§f§l" + p.getDisplayName() + "§e§l-----");

                for (int i = 0; i < mn.count; i++) {
                    sender.sendMessage("§a" + mn.name[i]  +"§f:" + String.valueOf(rs.getInt(mn.name[i])));
                }
                return;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return;

    }

    public ReturnColumn minigame_name(){

        ResultSet rs = mysql.query("Select COLUMN_NAME From INFORMATION_SCHEMA.COLUMNS where table_name='minigame_score' and DATA_TYPE='bigint';");
        ResultSet count = mysql.query("select count(1) from information_schema.columns where table_name='minigame_score' and DATA_TYPE='bigint'");

        String[] game_name;

        ReturnColumn rc = new ReturnColumn();

        int count1 = 0;

        try {
            while (count.next()){
                count.first();

                count1 = count.getInt("count(1)");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        game_name = new String[count1];

        try {
            int i = 0;
            while (rs.next()){

                game_name[i] = rs.getString("COLUMN_NAME");

                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rc.name = game_name;
        rc.count = count1;

        return rc;
    }

}

