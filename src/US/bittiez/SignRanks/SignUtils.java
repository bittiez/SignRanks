package US.bittiez.SignRanks;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class SignUtils {
    public static void saveConfigFile(Plugin plugin, FileConfiguration config, String fileName) {
        try {
            config.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSignRankSign(Sign sign, ChatColor titlePrefix, String signTitle) {
        if (sign.getLine(SignLine.TITLE).equalsIgnoreCase(titlePrefix + signTitle))
            if (!sign.getLine(SignLine.GROUP).isEmpty())
                if (sign.getLine(SignLine.ID).startsWith(ChatColor.MAGIC + ""))
                    return true;
        return false;
    }

    public static FileConfiguration loadSignDataInto(File dataFolder, String fileName, FileConfiguration signDataConfig) {
        File signFile = new File(dataFolder, fileName);
        if (!signFile.exists()) {
            try {
                signFile.createNewFile();
                return YamlConfiguration.loadConfiguration(signFile);
            } catch (IOException e) {
                return new YamlConfiguration();
            }
        } else {
            return YamlConfiguration.loadConfiguration(signFile);
        }
    }
}
