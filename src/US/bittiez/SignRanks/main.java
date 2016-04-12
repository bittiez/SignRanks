package US.bittiez.SignRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
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
    private final Logger log = Logger.getLogger("SignRanks");
    private static Economy economy = null;

    //Set up these in config later
    private String signTitle = "[SignRanks]";

    @Override
    public void onEnable(){
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
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void main(SignChangeEvent sign){
        Player who = sign.getPlayer();
        String group, cost;
        if(who.hasPermission("SignRanks.create")) {
            if (!sign.isCancelled()) {
                if (sign.getLine(0).equalsIgnoreCase("[SignRanks]")) {
                    if (!(group = sign.getLine(1)).isEmpty()) {
                        cost = sign.getLine(2);
                        Random r = new Random();
                        String id = r.nextInt(1000) + "";
                        sign.setLine(0, ChatColor.DARK_BLUE + signTitle);
                        sign.setLine(3, ChatColor.MAGIC + id);
                    }
                }
            }
        }
    }

    private boolean purchase(Player who, int cost){
        if(economy != null) {
            if (economy.getBalance(who) >= cost) {
                EconomyResponse er = economy.withdrawPlayer(who, cost);
                if (er.transactionSuccess())
                    return true;
            }
        }
        return false;
    }
}
