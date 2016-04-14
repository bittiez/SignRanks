package US.bittiez.SignRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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

    //Set up these in config later
    private String signTitle = "[SignRanks]";
    private String changedGroup = "&2You have been &6promoted&2!";
    private String notEnoughMoney = "&2You do not have enough &6money &2to continue.";

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
                        String id = r.nextInt(10000) + "";
                        sign.setLine(SIGNLINES.TITLE, titlePrefix + signTitle);
                        sign.setLine(SIGNLINES.ID, ChatColor.MAGIC + id);
                    }
                }
            }
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





    private boolean isSignRankSign(Sign sign){
        if(sign.getLine(SIGNLINES.TITLE).equalsIgnoreCase(titlePrefix + signTitle))
            if(!sign.getLine(SIGNLINES.GROUP).isEmpty())
                if(sign.getLine(SIGNLINES.ID).startsWith(ChatColor.MAGIC + ""))
                    return true ;
                else
                    log.warning("Err 1");
            else
                log.warning("Err 3");
        else
            log.warning("Err 4");

        return false;
    }

    private void handleSignClick(Player who, Sign sign){
        if(who.hasPermission("SignRanks.use")){
            String group = sign.getLine(SIGNLINES.GROUP);
            Double cost = Double.parseDouble(sign.getLine(SIGNLINES.COST));
            String id = sign.getLine(SIGNLINES.ID);
            boolean proceed = false;
            if(cost > 0) {
                if (purchase(who, cost)) {
                    proceed = true;
                } else
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', notEnoughMoney));
            } else
                proceed = true;

            if(proceed){
                //permission.playerRemoveGroup(who, permission.getPrimaryGroup(who));
                permission.playerAddGroup(who, group);
                who.sendMessage(ChatColor.translateAlternateColorCodes('&', changedGroup));
            }
        } else {
            who.sendMessage(ChatColor.RED + "You do not have permission to use this sign!");
            log.info(who.getName() + " tried to use a SignRanks sign, but does not have the SignRanks.use permission.");
        }
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
