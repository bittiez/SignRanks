package US.bittiez.SignRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class Payments {
    public static boolean refund(Player who, double cost, Economy economy) {
        if (economy != null) {
            EconomyResponse er = economy.depositPlayer(who, cost);
            if (er.transactionSuccess())
                return true;
        }
        return false;
    }

    public static boolean purchase(Player who, double cost, Economy economy) {
        if (economy != null) {
            if (economy.getBalance(who) >= cost) {
                EconomyResponse er = economy.withdrawPlayer(who, cost);
                if (er.transactionSuccess())
                    return true;
            }
        }
        return false;
    }
}
