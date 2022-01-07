package net.ryzech.smpcore.commands.everyone;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ryzech.smpcore.SmpCorePlugin;
import net.ryzech.smpcore.util.SmpCoreApi;
import net.ess3.api.MaxMoneyException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;


public class economy implements CommandExecutor {

    private final SmpCorePlugin plugin;

    public economy(SmpCorePlugin plugin) {
        this.plugin = plugin;

        Objects.requireNonNull(plugin.getCommand("deposit")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("withdraw")).setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run as a player.");
        } else {
            if (command.getName().equalsIgnoreCase("deposit")) {
                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();
                Integer diamonds;
                if (args.length == 0) {
                    diamonds = SmpCoreApi.getAmount(player, Material.DIAMOND);
                } else {
                    diamonds = Integer.valueOf(args[0]);
                }
                try {
                    if(diamonds == 0) {
                        player.sendMessage(Component.text("Sorry, but you don't have enough diamonds in your inventory to deposit.", NamedTextColor.RED));
                    } else if (diamonds < 0) {
                        player.sendMessage(Component.text("Sorry, but you can't deposit a negative amount.", NamedTextColor.RED));
                    } else if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), diamonds)) {
                        Economy.add(uuid, BigDecimal.valueOf(diamonds));
                        player.getInventory().removeItem(new ItemStack(Material.DIAMOND, diamonds));
                        player.sendMessage(Component.text()
                                .append(Component.text("You deposited ", NamedTextColor.GOLD))
                                .append(Component.text( diamonds + "♢", NamedTextColor.AQUA))
                                .append(Component.text(" your balance is now ", NamedTextColor.GOLD))
                                .append(Component.text( Economy.getMoneyExact(uuid)+ "♢.", NamedTextColor.AQUA)));
                    } else {
                        player.sendMessage(Component.text("Sorry, but you don't have enough diamonds in your inventory to deposit.", NamedTextColor.RED));
                    }

                } catch (UserDoesNotExistException e) {
                    e.printStackTrace();
                } catch (NoLoanPermittedException e) {
                    e.printStackTrace();
                } catch (MaxMoneyException e) {
                    e.printStackTrace();
                }
            }
            if (command.getName().equalsIgnoreCase("withdraw")) {
                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();

                try {
                    BigDecimal balance = Economy.getMoneyExact(uuid);
                    Integer actualBalance = balance.intValueExact();
                    if(args.length == 0) {
                        player.sendMessage(Component.text("Please select an amount, /withdraw <amount>.", NamedTextColor.GOLD));
                    } else if (Integer.parseInt(args[0]) > actualBalance) {
                        player.sendMessage(Component.text("Sorry, but you don't have enough diamonds in your account to withdraw.", NamedTextColor.RED));
                    } else {
                        Economy.subtract(uuid, BigDecimal.valueOf(Long.parseLong(args[0])));
                        SmpCoreApi.giveItemMaterial(player, Material.DIAMOND, Integer.parseInt(args[0]));
                        player.sendMessage(Component.text()
                                .append(Component.text("You withdrew ", NamedTextColor.GOLD)
                                        .append(Component.text(args[0] + "♢", NamedTextColor.AQUA))
                                        .append(Component.text(" from your account."))));
                    }

                } catch (NoLoanPermittedException e) {
                    e.printStackTrace();
                } catch (UserDoesNotExistException e) {
                    e.printStackTrace();
                } catch (MaxMoneyException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}