package com.csdprbsmc.essentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CsdprbsMcEssentials extends JavaPlugin implements Listener {

    private NamespacedKey compassKey;

    @Override
    public void onEnable() {
        compassKey = new NamespacedKey(this, "main_compass");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CsdprbsMc Essentials ativado!");
    }

    private ItemStack createCompass() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(
            "CSDPRBSMC MENU",
            NamedTextColor.YELLOW
        ).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));

        meta.getPersistentDataContainer().set(
            compassKey, PersistentDataType.BYTE, (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    private boolean isMainCompass(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer()
            .has(compassKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            player.getInventory().setItem(0, createCompass());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (isMainCompass(event.getItem())) {
            event.setCancelled(true);
            openMenu(event.getPlayer());
        }
    }

    private void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(
            null, 27,
            Component.text("CSDPRBSMC ESSENTIALS", NamedTextColor.YELLOW)
        );

        menu.setItem(10, button(Material.ENDER_PEARL, "TPA", NamedTextColor.BLACK));
        menu.setItem(11, button(Material.RED_BED, "HOME", NamedTextColor.YELLOW));
        menu.setItem(12, button(Material.COMPASS, "WARPS", NamedTextColor.BLACK));
        menu.setItem(13, button(Material.NAME_TAG, "CLANS", NamedTextColor.YELLOW));
        menu.setItem(14, button(Material.GOLD_INGOT, "RANKINGS", NamedTextColor.BLACK));
        menu.setItem(15, button(Material.EMERALD, "LOJA / ECONOMIA", NamedTextColor.YELLOW));

        player.openInventory(menu);
    }

    private ItemStack button(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name, color)
            .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));

        item.setItemMeta(meta);
        return item;
    }
                                }
