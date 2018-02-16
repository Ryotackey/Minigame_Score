package red.man10.minigame_score;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Score_Process {

    private final Minigame_Score plugin;

    ExecutorService service = Executors.newFixedThreadPool(10);

    public Score_Process(Minigame_Score plugin) {
        this.plugin = plugin;
    }

    public void sendScore(CommandSender sender, ResultSet rs, Player p) {

        UUID puuid = p.getUniqueId();

        Future<ResultSet> countfuture = service.submit(new MySQLQuery("SELECT count(1) FROM minigame.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';", plugin));
        ResultSet count = null;

        Minigame_Score.ReturnColumn mn = minigame_name();

        try {
            count = countfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        int count1 = -1;

        try {
            while (count.next()) {
                count1 = count.getInt("count(1)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (count1 == 0) {

            new MySQLExcute("insert into minigame.minigame_score(name, uuid) values('" + p.getName() + "', '" + puuid + "');", plugin);

        }

        try {
            while (rs.next()) {

                sender.sendMessage("§e§l-----§f§l" + p.getDisplayName() + "§e§l-----");

                for (int i = 0; i < mn.count; i++) {
                    sender.sendMessage("§a" + mn.name[i] + "§f:" + String.valueOf(rs.getInt(mn.name[i])));
                }
                return;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return;

    }

    public Minigame_Score.ReturnColumn minigame_name() {

        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("Select COLUMN_NAME From INFORMATION_SCHEMA.COLUMNS where table_name='minigame_score' and DATA_TYPE='bigint';", plugin));
        Future<ResultSet> countfuture = service.submit(new MySQLQuery("select count(1) from information_schema.columns where table_name='minigame_score' and DATA_TYPE='bigint'", plugin));

        ResultSet rs = null;
        ResultSet count = null;

        try {
            count = countfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        String[] game_name;

        Minigame_Score.ReturnColumn rc = new Minigame_Score.ReturnColumn();

        int count1 = 0;

        try {
            while (count.next()) {
                count.first();

                count1 = count.getInt("count(1)");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        game_name = new String[count1];

        try {
            int i = 0;
            while (rs.next()) {

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

    public void record(Player p, String command, String amoount, String game) {

        Future<Boolean> insertfuture = service.submit(new MySQLExcute("insert into minigame.minigame_record(game, name, uuid, command, amount) values('" + game + "', '" + p.getName() + "', '" + p.getUniqueId() + "', '" + command + "', '" + amoount + "');", plugin));

        Boolean insert = null;

        try {
            insert = insertfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (insert == true) {
            Bukkit.getLogger().info("record complete");
            return;
        } else {
            Bukkit.getLogger().info("failed record");
            return;
        }

    }

    public void showScore(String[] args, CommandSender sender) {

        Player p = Bukkit.getPlayer(args[1]);
        UUID puuid = p.getUniqueId();

        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_score where uuid='" + puuid + "';", plugin));
        ResultSet rs = null;

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        sendScore(sender, rs, p);

        return;

    }

    public void deleteScore(String[] args) {

        Player p = Bukkit.getPlayer(args[1]);
        UUID puuid = p.getUniqueId();

        Future<Boolean> resultfuture = service.submit(new MySQLExcute("DELETE FROM `minigame`.`minigame_score` WHERE uuid='" + puuid + "';", plugin));
        Boolean result = null;

        try {
            result = resultfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (result == true) {
            p.sendMessage("§adelete complete");
            record(p, "delete", "ALL", "ALL");
        } else {
            p.sendMessage("§4failed delete");
        }

    }

    public void getItem(String[] args, CommandSender sender) {

        Player getterp = (Player) sender;
        UUID puuid = getterp.getUniqueId();

        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_score where uuid='" + puuid + "';", plugin));
        ResultSet rs = null;

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (rs == null) {

            sender.sendMessage("§4引数が違います");
            return;

        }


        int point = 0;

        try {
            while (rs.next()) {

                point = rs.getInt(args[1]);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (point >= 5) {

            Future<Boolean> resultfuture = service.submit(new MySQLExcute("update minigame.minigame_score set " + args[1] + "=" + args[1] + "-5 where uuid='" + puuid + "';", plugin));
            Future<Boolean> result1future = service.submit(new MySQLExcute("update minigame.minigame_history set " + args[1] + "_exchange = " + args[1] + "_exchange+1 where uuid='" + puuid + "';", plugin));

            Boolean result = null;
            Boolean result1 = null;

            try {
                result1 = result1future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            try {
                result = resultfuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (result == true && result1 == true) {

                ItemStack rewarditem = plugin.item.getConfig().getItemStack("item." + args[1]);
                getterp.getInventory().addItem(rewarditem);
                getterp.sendMessage("§a交換完了しました§f(§e" + args[1] + "§f:" + point + "§6⇒§f" + (point - 5) + ")");
                record(getterp, "getitem", "-5", args[1]);

            } else {
                getterp.sendMessage("§c交換できません！");
            }

        } else {
            getterp.sendMessage("§c交換には5ポイント必要です");
        }


    }

    public void getRank(String[] args, CommandSender sender) {

        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_histoy ORDER BY '" + args[1] + "' desc limit 10;", plugin));

        ResultSet rs = null;

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (rs == null) {

            sender.sendMessage("§4引数が違います");
            return;

        }

        int i = 0;

        try {
            while (rs.next()) {

                String rankname = rs.getString("name");
                int rank = rs.getInt(args[1]);

                if (i == 0) {
                    sender.sendMessage("§a§l========§b§l" + args[1] + "§e§lRanking§a§l========");
                }

                sender.sendMessage("§l" + (i + 1) + ".  §6§l" + rankname + " §c§l: §e§l" + rank);

                i++;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void setScore(String[] args, CommandSender sender) {

        Player p = Bukkit.getPlayer(args[1]);
        UUID puuid = p.getUniqueId();

        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_score;", plugin));

        ResultSet rs = null;

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (rs == null) {

            sender.sendMessage("§4引数が違います");
            return;

        }

        Future<ResultSet> countfuture = service.submit(new MySQLQuery("SELECT count(1) FROM minigame.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';", plugin));

        ResultSet count = null;

        try {
            count = countfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            while (rs.next()) ;
            {

                count.first();

                int count1 = count.getInt("count(1)");

                if (count1 == 0) {

                    Future<Boolean> insertfuture = service.submit(new MySQLExcute("insert into minigame.minigame_score(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', " + args[3] + ");", plugin));
                    Boolean insert = null;

                    try {
                        insert = insertfuture.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if (insert == true) {
                        sender.sendMessage("insert complete");
                        record(p, "set", args[3], args[2]);
                        return;
                    } else {
                        sender.sendMessage("failed insert");
                        return;
                    }

                }

                Future<Boolean> updatefuture = service.submit(new MySQLExcute("update minigame.minigame_score set " + args[2] + "=" + args[3] + " where uuid='" + puuid + "';", plugin));

                Boolean update = null;

                try {
                    update = updatefuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (update == true) {
                    sender.sendMessage("set complete");
                    record(p, "set", "set" + args[3], args[2]);
                    return;
                } else {
                    sender.sendMessage("failed set");
                    return;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    public void addScore(String[] args, CommandSender sender){

        Player p = Bukkit.getPlayer(args[1]);
        UUID puuid = p.getUniqueId();

        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_score;", plugin));

        ResultSet rs = null;

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (rs == null){

            sender.sendMessage("§4引数が違います");
            return;

        }

        Future<ResultSet> countfuture = service.submit(new MySQLQuery("SELECT count(1) FROM minigame.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';", plugin));

        ResultSet count = null;

        try {
            count = countfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            while (rs.next()) {

                count.first();

                int count1 = count.getInt("count(1)");

                if (count1 == 0) {

                    Future<Boolean> insertfuture = service.submit(new MySQLExcute("insert into minigame.minigame_score(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', " + args[3] +");", plugin));
                    new MySQLExcute("insert into minigamedb.minigame_history(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', " + args[3] + ");", plugin);

                    Boolean insert = null;

                    try {
                        insert = insertfuture.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if (insert == true) {
                        sender.sendMessage("insert complete");
                        record(p, "add", args[3], args[2]);
                        return;
                    } else {
                        sender.sendMessage("failed insert");
                        return;
                    }

                }

                Future<Boolean> updatefuture = service.submit(new MySQLExcute("update minigame.minigame_score set " + args[2] + "=" + args[2] + "+" + args[3] + " where uuid='" + puuid + "';", plugin));
                new MySQLExcute("update minigamedb.minigame_history set " + args[2] + "=" + args[2] + "+" + args[3] + " where uuid='" + puuid + "';", plugin);

                Boolean update = null;

                try {
                    update = updatefuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (update == true) {
                    sender.sendMessage("add complete");
                    record(p, "add", args[3], args[2]);
                    return;
                } else {
                    sender.sendMessage("failed add");
                    return;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void reduceScore(String[] args, CommandSender sender){

        Player p = Bukkit.getPlayer(args[1]);
        UUID puuid = p.getUniqueId();
        Future<ResultSet> rsfuture = service.submit(new MySQLQuery("SELECT * FROM minigame.minigame_score;", plugin));

        ResultSet rs = null;

        try {
            rs = rsfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (rs == null){

            sender.sendMessage("§4引数が違います");
            return;

        }


        Future<ResultSet> countfuture = service.submit(new MySQLQuery("SELECT count(1) FROM minigame.minigame_score WHERE uuid='" + String.valueOf(puuid) + "';", plugin));

        ResultSet count = null;

        try {
            count = countfuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            while (rs.next()) ;
            {

                count.first();

                int count1 = count.getInt("count(1)");

                if (count1 == 0) {

                    Future<Boolean> insertfuture = service.submit(new MySQLExcute("insert into minigame.minigame_score(name, uuid, " + args[2] + ") values('" + p.getName() + "', '" + puuid + "', -" + args[3] + ");", plugin));

                    Boolean insert = null;

                    try {
                        insert = insertfuture.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if (insert == true) {
                        sender.sendMessage("insert complete");
                        record(p, "reduce", args[3], args[2]);
                        return;
                    } else {
                        sender.sendMessage("failed insert");
                        return;
                    }

                }

                Future<Boolean> updatefuture = service.submit(new MySQLExcute("update minigame.minigame_score set " + args[2] + "=" + args[2] + "-" + args[3] + " where uuid='" + puuid + "';", plugin));

                Boolean update = null;

                try {
                    update = updatefuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (update == true) {
                    sender.sendMessage("reduce complete");
                    record(p, "reduce", "-" + args[3], args[2]);
                    return;
                } else {
                    sender.sendMessage("failed reduce");
                    return;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
