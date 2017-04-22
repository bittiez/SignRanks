package US.bittiez.SignRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class SignEventListener implements Listener {
    public static String signDataConfigFile = "signData.yml";
    private FileConfiguration signData;
    private Plugin plugin;
    private Economy economy;
    private Permission permission;
    private Logger log;

    public SignEventListener(Plugin plugin, FileConfiguration signData, Economy economy, Permission permission, Logger log) {
        this.plugin = plugin;

        this.signData = signData;
        this.economy = economy;
        this.permission = permission;
        this.log = log;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent sign) {
        if (!sign.isCancelled()) {
            Player who = sign.getPlayer();
            String group, cost;
            if (who.hasPermission("SignRanks.create") && sign.getLine(SignLine.TITLE).equalsIgnoreCase(main.signTitle)) {
                if (!(group = sign.getLine(SignLine.GROUP)).isEmpty()) {
                    cost = sign.getLine(SignLine.COST);
                    Random r = new Random();
                    String id = r.nextInt(100000) + "";
                    while (signData.contains("signs." + id))
                        id = r.nextInt(100000) + "";
                    sign.setLine(SignLine.TITLE, main.titlePrefix + main.signTitle);
                    sign.setLine(SignLine.ID, ChatColor.MAGIC + id);

                    signData.set("signs." + id, new String[] {group, cost});
                    signData.set("signs." + id + ".commands", new String[]{""});
                    signData.set("signs." + id + ".consoleCommands", new String[]{""});
                    SignUtils.saveConfigFile(plugin, signData, signDataConfigFile);
                    who.sendMessage(String.format("SignRanks sign created! [Sign ID: %s] [To add commands you will need this ID, and need to edit the config file.]", id));
                }

            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block != null)
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (SignUtils.isSignRankSign(sign, main.titlePrefix, main.signTitle)) {
                    if (!event.getPlayer().hasPermission("SignRanks.break")) {
                        handleSignClick(event.getPlayer(), sign);
                        event.setCancelled(true);
                    } else {
                        String id = sign.getLine(SignLine.ID);
                        id = ChatColor.stripColor(id);
                        if (signData.contains("signs." + id)) {
                            signData.set("signs." + id, null);
                            SignUtils.saveConfigFile(plugin, signData, signDataConfigFile);
                        }
                        sign.getBlock().breakNaturally();
                    }
                }
            }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null)
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (SignUtils.isSignRankSign(sign, main.titlePrefix, main.signTitle)) {
                    handleSignClick(event.getPlayer(), sign);
                }
            }
    }

    public void handleSignClick(Player who, Sign sign) {
        if (who.hasPermission("SignRanks.use")) {
            String group = sign.getLine(SignLine.GROUP);
            Double cost = Double.parseDouble(sign.getLine(SignLine.COST));
            String id = sign.getLine(SignLine.ID);
            id = ChatColor.stripColor(id);
            boolean proceed = false;
            String prevGroup = permission.getPrimaryGroup(who);
            if (prevGroup != group) {
                if (cost > 0) {
                    if (Payments.purchase(who, cost, economy)) {
                        proceed = true;
                    } else
                        who.sendMessage(ChatColor.translateAlternateColorCodes('&', main.notEnoughMoney));
                } else
                    proceed = true;
            }

            if (signData.get("signs." + id) == null) {
                proceed = false;
                log.warning(who.getName() + " tried to use a SignRanks sign that is not a valid sign!");
            }


            if (proceed && !prevGroup.equals(group)) {
                if (permission.playerAddGroup(null, who, group)) {
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', main.changedGroup));
                    permission.playerRemoveGroup(null, who, prevGroup);

                    List<String> commands = signData.getStringList("signs." + id + ".commands");
                    if (commands.size() > 0) {
                        for (String s : commands) {
                            if (s.startsWith("/"))
                                s = s.substring(1);

                            who.performCommand(s);
                        }
                    }

                    List<String> consoleCommands = signData.getStringList("signs." + id + ".consoleCommands");
                    if (consoleCommands.size() > 0) {
                        for (String s : consoleCommands) {
                            if (s.startsWith("/"))
                                s = s.substring(1);

                            s = s.replaceAll("(\\[USERNAME\\])", who.getName());

                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                        }
                    }
                } else {
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', main.failedToChangePermission));
                    log.warning(who.getName() + " tried to use a SignRanks sign to change their group, there was an error changing their permission." +
                            String.format("[Who: %1] [Sign Location: %2] [Attempted Group: %3] [Sign ID: %4]", who.getName(), sign.getLocation().toString(), group, id));
                    if (cost > 0)
                        Payments.refund(who, cost, economy);
                }
            }
        } else {
            who.sendMessage(ChatColor.RED + "You do not have permission to use this sign!");
            log.info(who.getName() + " tried to use a SignRanks sign, but does not have the SignRanks.use permission.");
        }
    }
}
