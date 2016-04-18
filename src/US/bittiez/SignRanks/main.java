package US.bittiez.SignRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by bittiez on 11/04/16.
 */
/**
Sign setup:
 [SignRanks]
 Group
 Cost
*/

public class main extends JavaPlugin implements Listener{
    private static Logger log = Logger.getLogger("SignRanks");
    private static Economy economy = null;
    private static Permission permission = null;
    private static ChatColor titlePrefix = ChatColor.DARK_BLUE;
    private FileConfiguration mainConfig = getConfig();
    private FileConfiguration signData;

    //Options set in the config
    private String signTitle, changedGroup, notEnoughMoney, failedToChangePermission;

    @Override
    public void onEnable(){
        log = getLogger();
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if(vault == null){
            log.warning("Vault was not detected! SignRanks will still work but will not be able to charge players to use signs.");
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                log.warning("Could not find an economy plugin! SignRanks will still work but will not be able to charge players to use signs");
            } else
                economy = rsp.getProvider();
        }

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        if(permission == null){
            log.warning("Could not find a permission plugin! SignRanks will not load without a permission plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        loadConfig();
        loadSignData();
    }

    public boolean onCommand(CommandSender who, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("signranks")){
            if(who instanceof Player)
                if(args[0].equalsIgnoreCase("reload")){
                    if(who.hasPermission("SignRanks.reload")){
                        loadConfig();
                        loadSignData();
                        who.sendMessage(ChatColor.GOLD + "Reloaded Sign Ranks config and data files!");
                        return true;
                    } else {
                        log.warning(who.getName() + " tried to use /SignRanks reload without the SignRanks.reload permission!");
                        return true;
                    }
                }
        }
        return false;
    }

    @EventHandler
    public void main(SignChangeEvent sign){
        Player who = sign.getPlayer();
        String group, cost;
        if(who.hasPermission("SignRanks.create")) {
            if (!sign.isCancelled()) {
                if (sign.getLine(SIGNLINES.TITLE).equalsIgnoreCase(signTitle)) {
                    if (!(group = sign.getLine(SIGNLINES.GROUP)).isEmpty()) {
                        cost = sign.getLine(SIGNLINES.COST);
                        Random r = new Random();
                        String id = r.nextInt(100000) + "";
                        sign.setLine(SIGNLINES.TITLE, titlePrefix + signTitle);
                        sign.setLine(SIGNLINES.ID, ChatColor.MAGIC + id);

                        String[] signInfo = {
                                group, cost
                        };

                        signData.set("signs." + id, signInfo);
                        signData.set("signs." + id + ".commands", new String[] {""});
                        saveSignData();
                    }
                }
            }
        }
    }

    private void saveSignData(){
        try {
            signData.save(new File(this.getDataFolder(), "signData.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void main(BlockBreakEvent event){
        Block block = event.getBlock();
        if(block != null)
            if(block.getState() instanceof Sign){
                Sign sign = (Sign)block.getState();
                if(isSignRankSign(sign)){
                    if(!event.getPlayer().hasPermission("SignRanks.break")) {
                        handleSignClick(event.getPlayer(), sign);
                        event.setCancelled(true);
                    }
                }
            }
    }

    @EventHandler
    public void main(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if(block != null)
            if(block.getState() instanceof Sign){
                Sign sign = (Sign)block.getState();
                if(isSignRankSign(sign)){
                    handleSignClick(event.getPlayer(), sign);
                }
            }
    }



    private void loadSignData(){
        File signFile = new File(this.getDataFolder(), "signData.yml");
        if(!signFile.exists()) {
            try {
                signFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(signFile.exists())
            signData = YamlConfiguration.loadConfiguration(signFile);
        else
            getServer().getPluginManager().disablePlugin(this);
    }

    private void loadConfig(){
        mainConfig.options().copyDefaults();
        saveDefaultConfig();

        signTitle = mainConfig.getString("signTitle");
        changedGroup = mainConfig.getString("changedGroup");
        notEnoughMoney = mainConfig.getString("notEnoughMoney");
        failedToChangePermission = mainConfig.getString("failedToChangePermission");
    }

    private boolean isSignRankSign(Sign sign){
        if(sign.getLine(SIGNLINES.TITLE).equalsIgnoreCase(titlePrefix + signTitle))
            if(!sign.getLine(SIGNLINES.GROUP).isEmpty())
                if(sign.getLine(SIGNLINES.ID).startsWith(ChatColor.MAGIC + ""))
                    return true ;
        return false;
    }

    private void handleSignClick(Player who, Sign sign){
        if(who.hasPermission("SignRanks.use")){
            String group = sign.getLine(SIGNLINES.GROUP);
            Double cost = Double.parseDouble(sign.getLine(SIGNLINES.COST));
            String id = sign.getLine(SIGNLINES.ID);
            id = ChatColor.stripColor(id);
            boolean proceed = false;
            if(cost > 0) {
                if (purchase(who, cost)) {
                    proceed = true;
                } else
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', notEnoughMoney));
            } else
                proceed = true;

            if(proceed){
                String prevGroup = permission.getPrimaryGroup(who);
                if(permission.playerAddGroup(null, who, group)) {
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', changedGroup));
                    permission.playerRemoveGroup(null, who, prevGroup);

                    List<String> commands = signData.getStringList("signs." + id + ".commands");
                    if(commands.size() > 0){
                        for(String s : commands){
                            if(s.startsWith("/"))
                                s = s.substring(1);

                            who.performCommand(s);
                        }
                    }
                }
                else {
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', failedToChangePermission));
                    log.warning(who.getName() + " tried to use a SignRanks sign to change their group, there was an error changing their permission." +
                            String.format("[Who: %1] [Sign Location: %2] [Attempted Group: %3] [Sign ID: %4]", who.getName(), sign.getLocation().toString(), group, id));
                    if(cost > 0)
                        refund(who, cost);
                }
            }
        } else {
            who.sendMessage(ChatColor.RED + "You do not have permission to use this sign!");
            log.info(who.getName() + " tried to use a SignRanks sign, but does not have the SignRanks.use permission.");
        }
    }

    private boolean refund(Player who, double cost){
        if(economy != null)
        {
            EconomyResponse er = economy.depositPlayer(who, cost);
            if(er.transactionSuccess())
                return true;
        }
        return false;
    }

    private boolean purchase(Player who, double cost){
        if(economy != null) {
            if (economy.getBalance(who) >= cost) {
                EconomyResponse er = economy.withdrawPlayer(who, cost);
                if (er.transactionSuccess())
                    return true;
            }
        }
        return false;
    }
    private boolean purchase(Player who, int cost){
        return purchase(who, (double)cost);
    }
}
