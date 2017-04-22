package US.bittiez.SignRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class main extends JavaPlugin {
    private static Logger log = Logger.getLogger("SignRanks");
    private static Economy economy = null;
    private static Permission permission = null;
    public static ChatColor titlePrefix = ChatColor.DARK_BLUE;
    private FileConfiguration mainConfig = getConfig();
    private FileConfiguration signData = new YamlConfiguration();
    private SignEventListener signEventListener;

    //Options set in the config
    public static String signTitle, changedGroup, notEnoughMoney, failedToChangePermission;

    @Override
    public void onEnable() {
        log = getLogger();
        vaultOnEnable();
        if (permissionOnEnable()) {
            loadConfig();
            signData = SignUtils.loadSignDataInto(getDataFolder(), SignEventListener.signDataConfigFile);
            signEventListener = new SignEventListener(this, signData, economy, permission, log);
            getServer().getPluginManager().registerEvents(signEventListener, this);
        } else {
            log.warning("SignRanks has been disabled. Could not find a permissions plugin.");
            this.setEnabled(false);
        }
    }

    private boolean permissionOnEnable() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        if (permission == null) {
            return false;
        }
        return true;
    }

    private void vaultOnEnable() {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault == null) {
            log.warning("Vault was not detected! SignRanks will still work but will not be able to charge players to use signs.");
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                log.warning("Could not find an economy plugin! SignRanks will still work but will not be able to charge players to use signs");
            } else
                economy = rsp.getProvider();
        }
    }

    public boolean onCommand(CommandSender who, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("signranks")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (who.hasPermission("SignRanks.reload")) {
                        loadConfig();
                        signData = SignUtils.loadSignDataInto(getDataFolder(), SignEventListener.signDataConfigFile);
                        who.sendMessage(ChatColor.GOLD + "Reloaded Sign Ranks config and data files!");
                        return true;
                    } else {
                        log.warning(who.getName() + " tried to use /SignRanks reload without the SignRanks.reload permission!");
                        return true;
                    }
                }
            } else
                return true;
        }
        return false;
    }

    private void loadConfig() {
        mainConfig.options().copyDefaults();
        saveDefaultConfig();

        signTitle = mainConfig.getString("signTitle");
        changedGroup = mainConfig.getString("changedGroup");
        notEnoughMoney = mainConfig.getString("notEnoughMoney");
        failedToChangePermission = mainConfig.getString("failedToChangePermission");
    }
}
