package red.man10.minigame_score;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.*;

public final class Minigame_Score extends JavaPlugin {

    CustomConfig item;
    CustomConfig config;

    MySOLManager mysql;

    Score_Process process = new Score_Process(this);

    public static class ReturnColumn{

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(10);

        switch (args.length) {

            case 0:
                if (!(sender instanceof Player)) return false;
                Player senderp = (Player) sender;
                UUID senderpuuid = senderp.getUniqueId();

                Future<ResultSet> myrsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_score where uuid='" + senderpuuid + "';", this));

                ResultSet myrs = null;

                try {
                    myrs = myrsfuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                process.sendScore(sender, myrs, senderp);
                return true;

            case 1:
                if (args[0].equalsIgnoreCase("help")) {
                    if (sender.hasPermission("minigamescore")) {

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

                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("minigamescore")) {

                        reloadConfig();
                        item.reloadConfig();
                        sender.sendMessage("§aReload complete");

                    }
                }
                break;

            case 2:
                if (args[0].equalsIgnoreCase("show")) {
                    if (sender.hasPermission("minigamescore")) {

                        process.showScore(args, sender);
                        return true;

                    } else {
                        sender.sendMessage("§4§l権限がありません");
                        return false;
                    }

                }

                if (args[0].equalsIgnoreCase("delete")) {
                    if (sender.hasPermission("minigamescore")) {

                        process.deleteScore(args);
                        return true;

                    } else {
                        sender.sendMessage("§4権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("additem")) {
                    if (!(sender instanceof Player)) return false;
                    Player havep = (Player) sender;
                    if (sender.hasPermission("minigamescore")) {

                        item.getConfig().set("item." + args[1], havep.getInventory().getItemInMainHand());
                        item.saveConfig();

                        havep.sendMessage("§aitemadd complete");

                    } else {
                        sender.sendMessage("&4&l権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("getitem")) {
                    if (sender.hasPermission("minigamescore")) {
                        if (!(sender instanceof Player)) return false;
                        process.getItem(args, sender);
                    }
                }

                if (args[0].equalsIgnoreCase("rank")) {

                    process.getRank(args, sender);

                    return true;

                }
                break;

            case 4:
                if (args[0].equalsIgnoreCase("set")) {
                    if (sender.hasPermission("minigamescore")) {

                        process.setScore(args, sender);

                        return true;

                    } else {
                        sender.sendMessage("§4§l権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("add")) {
                    if (sender.hasPermission("minigamescore")) {

                        process.addScore(args, sender);

                        return true;
                    } else {
                        sender.sendMessage("§4§l権限がありません");
                    }
                }

                if (args[0].equalsIgnoreCase("reduce")) {
                    if (sender.hasPermission("minigamescore")) {

                        process.reduceScore(args, sender);

                        return true;
                    } else {
                        sender.sendMessage("§4§l権限がありません");
                    }
                }
                break;

        }
        return false;
    }



}

class MySQLQuery implements Callable<ResultSet> {

    private String sql;
    private final Minigame_Score plugin;

    public MySQLQuery(String sql, Minigame_Score plugin){
        this.sql = sql;
        this.plugin = plugin;
    }

    @Override
    public ResultSet call(){

        ResultSet rs = plugin.mysql.query(sql);

        return rs;
    }

}

class MySQLExcute implements Callable<Boolean> {

    private String sql;
    private final Minigame_Score plugin;

    public MySQLExcute(String sql, Minigame_Score plugin){
        this.sql = sql;
        this.plugin = plugin;
    }

    @Override
    public Boolean call(){

        Boolean result = plugin.mysql.execute(sql);

        return result;
    }

}

