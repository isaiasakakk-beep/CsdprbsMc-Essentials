
package com.csdprbsmc.essentials;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;
import java.lang.reflect.Method;

public final class CsdprbsMcEssentials extends JavaPlugin
        implements Listener, CommandExecutor {

    private final Map<UUID, UUID> tpa = new HashMap<>();
    private final Map<UUID, Long> tpaCreated = new HashMap<>();

    private NamespacedKey compassKey;
    private Object bedrockMenus;

    private static final String MENU = "§6§lCSDPRBSMC";
    private static final String SHOP = "§e§lLOJA";
    private static final String CLANS = "§e§lCLANS";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        compassKey = new NamespacedKey(this, "main_compass");
        initializeBedrockMenus();

        Bukkit.getPluginManager().registerEvents(this, this);

        String[] commands = {
                "ess", "tpa", "tpaccept", "tpdeny",
                "sethome", "home", "delhome",
                "setwarp", "warp", "delwarp",
                "clan", "bal", "pay", "shop",
                "topmoney", "topkills", "setmoney"
        };

        for (String name : commands) {
            PluginCommand command = getCommand(name);
            if (command != null) {
                command.setExecutor(this);
            }
        }

        getLogger().info("CsdprbsMc Essentials iniciado!");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    private void initializeBedrockMenus() {
        if (!Bukkit.getPluginManager().isPluginEnabled("floodgate")) return;
        try {
            Class<?> type = Class.forName("com.csdprbsmc.essentials.BedrockMenus");
            bedrockMenus = type.getConstructor().newInstance();
            getLogger().info("Integração de formulários Bedrock ativada.");
        } catch (Throwable error) {
            bedrockMenus = null;
            getLogger().warning("Floodgate detectado, mas os formulários não puderam ser ativados: " + error.getClass().getSimpleName());
        }
    }

    private Object invokeBedrock(String method, Class<?>[] types, Object... args) {
        if (bedrockMenus == null) return null;
        try {
            Method target = bedrockMenus.getClass().getMethod(method, types);
            return target.invoke(bedrockMenus, args);
        } catch (Throwable error) {
            getLogger().warning("Falha na integração Bedrock (" + method + "): " + error.getClass().getSimpleName());
            return null;
        }
    }

    private void handleBedrockChoice(Player p, int choice) {
        switch (choice) {
            case 0 -> msg(p, "Use /tpa <jogador>");
            case 1 -> msg(p, "Use /home <nome> ou /sethome <nome>");
            case 2 -> msg(p, "Use /warp <nome>");
            case 3 -> invokeBedrock("openClan", new Class<?>[]{Player.class}, p);
            case 4 -> { showTop(p, "kills"); showTop(p, "money"); }
            case 5 -> invokeBedrock("openShop", new Class<?>[]{Player.class, double.class, Consumer.class},
                    p, money(p), (Consumer<Integer>) index -> buyBedrockIndex(p, index));
            default -> { }
        }
    }

    private void buyBedrockIndex(Player p, int index) {
        String[] names = {"§fFerro", "§6Ouro", "§bDiamante", "§aEsmeralda", "§6Madeira", "§ePão", "§5Obsidiana"};
        if (index >= 0 && index < names.length) buy(p, names[index]);
    }

    private void msg(Player p, String text) {
        p.sendMessage("§6[CsdprbsMc] §f" + text);
    }

    private String id(Player p) {
        return p.getUniqueId().toString();
    }

    private double money(Player p) {
        return getConfig().getDouble("players." + id(p) + ".money", 100);
    }

    private void money(Player p, double amount) {
        getConfig().set("players." + id(p) + ".money", amount);
        saveConfig();
    }

    private void addMoney(Player p, double amount) {
        money(p, money(p) + amount);
    }

    private void giveCompass(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (isCompass(item)) {
                return;
            }
        }

        ItemStack compass = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName("§e§lCSDPRBSMC MENU");
        meta.setLore(List.of("§7Clique para abrir o menu"));
        meta.getPersistentDataContainer().set(
                compassKey, PersistentDataType.BYTE, (byte) 1
        );

        compass.setItemMeta(meta);
        int slot = p.getInventory().firstEmpty();
        if (slot == -1) {
            msg(p, "Seu inventário está cheio. Libere um espaço para receber a bússola do menu.");
            return;
        }
        p.getInventory().setItem(slot, compass);
    }

    private boolean isCompass(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null &&
                meta.getPersistentDataContainer().has(
                        compassKey, PersistentDataType.BYTE
                );
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        UUID leaving = event.getPlayer().getUniqueId();
        tpa.entrySet().removeIf(entry -> entry.getKey().equals(leaving) || entry.getValue().equals(leaving));
        tpaCreated.keySet().removeIf(target -> !tpa.containsKey(target));
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        Bukkit.getScheduler().runTask(this, () -> {
            giveCompass(p);

            getConfig().set(
                    "players." + id(p) + ".name",
                    p.getName()
            );

            if (!getConfig().contains("players." + id(p) + ".money")) {
                money(p, 100);
            }

            saveConfig();
        });
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (isCompass(event.getItem())) {
            event.setCancelled(true);
            openMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void drop(PlayerDropItemEvent event) {
        if (isCompass(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void clickCompass(InventoryClickEvent event) {
        if (isCompass(event.getCurrentItem()) || isCompass(event.getCursor()) ||
                (event.getHotbarButton() >= 0 && isCompass(event.getWhoClicked().getInventory().getItem(event.getHotbarButton())))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void dragCompass(InventoryDragEvent event) {
        if (isCompass(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            String path = "players." + id(killer) + ".kills";

            getConfig().set(path, getConfig().getInt(path) + 1);
            addMoney(killer, 10);

            msg(killer, "Você ganhou §a$10 §fe uma kill!");
        }

        event.getDrops().removeIf(this::isCompass);
    }

    private void openMenu(Player p) {
        Object bedrock = invokeBedrock("isBedrock", new Class<?>[]{Player.class}, p);
        if (Boolean.TRUE.equals(bedrock)) {
            invokeBedrock("openMenu", new Class<?>[]{Player.class, Consumer.class},
                    p, (Consumer<Integer>) choice -> handleBedrockChoice(p, choice));
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, MENU);

        inv.setItem(10, button(Material.ENDER_PEARL, "§0§lTPA"));
        inv.setItem(11, button(Material.RED_BED, "§e§lHOME"));
        inv.setItem(12, button(Material.ENDER_EYE, "§0§lWARPS"));
        inv.setItem(13, button(Material.NAME_TAG, "§e§lCLANS"));
        inv.setItem(14, button(Material.GOLD_INGOT, "§0§lRANKINGS"));
        inv.setItem(15, button(Material.EMERALD, "§e§lLOJA/ECONOMIA"));

        p.openInventory(inv);
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void menuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) {
            return;
        }

        String title = event.getView().getTitle();

        if (!title.equals(MENU) &&
                !title.equals(SHOP) &&
                !title.equals(CLANS)) {
            return;
        }

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        String name = item.getItemMeta().getDisplayName();

        if (title.equals(MENU)) {
            switch (name) {
                case "§0§lTPA" -> {
                    p.closeInventory();
                    msg(p, "Use §e/tpa <jogador>");
                }
                case "§e§lHOME" -> {
                    p.closeInventory();
                    msg(p, "Use §e/home <nome> §fou §e/sethome <nome>");
                }
                case "§0§lWARPS" -> {
                    p.closeInventory();
                    msg(p, "Use §e/warp <nome>");
                }
                case "§e§lCLANS" -> openClanMenu(p);
                case "§0§lRANKINGS" -> {
                    p.closeInventory();
                    showTop(p, "kills");
                    showTop(p, "money");
                }
                case "§e§lLOJA/ECONOMIA" -> openShop(p);
            }
        } else if (title.equals(SHOP)) {
            buy(p, name);
        } else if (title.equals(CLANS)) {
            p.closeInventory();
            msg(p, "Use §e/clan ajuda §fpara ver os comandos.");
        }
    }

    private void openClanMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, CLANS);
        inv.setItem(13, button(Material.NAME_TAG, "§e§lCOMANDOS DE CLAN"));
        p.openInventory(inv);
    }

    private void openShop(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, SHOP);

        inv.setItem(10, shopItem(Material.IRON_INGOT, "§fFerro", 20));
        inv.setItem(11, shopItem(Material.GOLD_INGOT, "§6Ouro", 35));
        inv.setItem(12, shopItem(Material.DIAMOND, "§bDiamante", 150));
        inv.setItem(13, shopItem(Material.EMERALD, "§aEsmeralda", 100));
        inv.setItem(14, shopItem(Material.OAK_LOG, "§6Madeira", 10));
        inv.setItem(15, shopItem(Material.BREAD, "§ePão", 8));
        inv.setItem(16, shopItem(Material.OBSIDIAN, "§5Obsidiana", 80));

        p.openInventory(inv);
    }

    private ItemStack shopItem(Material material, String name, double price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(List.of(
                "§7Preço: §a$" + price,
                "§eClique para comprar"
        ));

        item.setItemMeta(meta);
        return item;
    }

    private void buy(Player p, String name) {
        Map<String, Object[]> items = new HashMap<>();

        items.put("§fFerro", new Object[]{Material.IRON_INGOT, 20.0});
        items.put("§6Ouro", new Object[]{Material.GOLD_INGOT, 35.0});
        items.put("§bDiamante", new Object[]{Material.DIAMOND, 150.0});
        items.put("§aEsmeralda", new Object[]{Material.EMERALD, 100.0});
        items.put("§6Madeira", new Object[]{Material.OAK_LOG, 10.0});
        items.put("§ePão", new Object[]{Material.BREAD, 8.0});
        items.put("§5Obsidiana", new Object[]{Material.OBSIDIAN, 80.0});

        Object[] data = items.get(name);
        if (data == null) {
            return;
        }

        Material material = (Material) data[0];
        double price = (double) data[1];

        if (money(p) < price) {
            msg(p, "Você não tem dinheiro suficiente.");
            return;
        }

        if (p.getInventory().firstEmpty() == -1) {
            msg(p, "Seu inventário está cheio.");
            return;
        }

        money(p, money(p) - price);
        p.getInventory().addItem(new ItemStack(material));

        msg(p, "Compra realizada!");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        Player p = (sender instanceof Player player) ? player : null;

        if (p == null && !command.getName().equalsIgnoreCase("setmoney")) {
            sender.sendMessage("Este comando é para jogadores.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "ess" -> openMenu(p);

            case "bal" -> msg(p, "Seu saldo: §a$" + money(p));

            case "tpa" -> {
                if (args.length != 1) {
                    msg(p, "Use /tpa <jogador>");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);

                if (target == null || target.equals(p)) {
                    msg(p, "Jogador inválido.");
                    return true;
                }

                tpa.put(target.getUniqueId(), p.getUniqueId());
                tpaCreated.put(target.getUniqueId(), System.currentTimeMillis());

                msg(p, "Pedido enviado para " + target.getName() + ". Expira em 60 segundos.");
                msg(target, p.getName() + " pediu teleporte. Use /tpaccept ou /tpdeny.");
            }

            case "tpaccept" -> {
                UUID requesterId = tpa.remove(p.getUniqueId());
                Long created = tpaCreated.remove(p.getUniqueId());

                if (requesterId == null || created == null) {
                    msg(p, "Você não tem pedidos.");
                    return true;
                }

                if (System.currentTimeMillis() - created > 60_000L) {
                    msg(p, "Esse pedido de teleporte expirou.");
                    return true;
                }

                Player requester = Bukkit.getPlayer(requesterId);

                if (requester == null) {
                    msg(p, "O jogador saiu do servidor.");
                    return true;
                }

                requester.teleport(p.getLocation());
                msg(p, "Pedido aceito.");
                msg(requester, "Seu pedido foi aceito.");
            }

            case "tpdeny" -> {
                tpa.remove(p.getUniqueId());
                tpaCreated.remove(p.getUniqueId());
                msg(p, "Pedido recusado.");
            }

            case "sethome" -> {
                if (args.length != 1) {
                    msg(p, "Use /sethome <nome>");
                    return true;
                }

                String path = "players." + id(p) + ".homes";
                ConfigurationSection homes =
                        getConfig().getConfigurationSection(path);

                if (homes == null) {
                    homes = getConfig().createSection(path);
                }

                if (!homes.contains(args[0]) &&
                        homes.getKeys(false).size() >= 5) {
                    msg(p, "Você já tem 5 homes.");
                    return true;
                }

                getConfig().set(path + "." + args[0], p.getLocation());
                saveConfig();
                msg(p, "Home salva!");
            }

            case "home" -> {
                if (args.length != 1) {
                    msg(p, "Use /home <nome>");
                    return true;
                }

                Location loc = getConfig().getLocation(
                        "players." + id(p) + ".homes." + args[0]
                );

                if (loc == null) {
                    msg(p, "Home não encontrada.");
                    return true;
                }

                p.teleport(loc);
                msg(p, "Teleportado!");
            }

            case "delhome" -> {
                if (args.length != 1) {
                    msg(p, "Use /delhome <nome>");
                    return true;
                }

                getConfig().set(
                        "players." + id(p) + ".homes." + args[0],
                        null
                );

                saveConfig();
                msg(p, "Home apagada.");
            }

            case "setwarp" -> {
                if (!p.hasPermission("csdprbsmc.admin")) {
                    return true;
                }

                if (args.length != 1) {
                    msg(p, "Use /setwarp <nome>");
                    return true;
                }

                getConfig().set("warps." + args[0], p.getLocation());
                saveConfig();
                msg(p, "Warp criada.");
            }

            case "warp" -> {
                if (args.length != 1) {
                    msg(p, "Use /warp <nome>");
                    return true;
                }

                Location loc = getConfig().getLocation("warps." + args[0]);

                if (loc == null) {
                    msg(p, "Warp não encontrada.");
                    return true;
                }

                p.teleport(loc);
                msg(p, "Teleportado!");
            }

            case "delwarp" -> {
                if (!p.hasPermission("csdprbsmc.admin")) {
                    return true;
                }

                if (args.length != 1) {
                    msg(p, "Use /delwarp <nome>");
                    return true;
                }

                getConfig().set("warps." + args[0], null);
                saveConfig();
                msg(p, "Warp apagada.");
            }

            case "pay" -> {
                if (args.length != 2) {
                    msg(p, "Use /pay <jogador> <valor>");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);

                if (target == null || target.equals(p)) {
                    msg(p, "Jogador inválido.");
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[1]);

                    if (!Double.isFinite(amount) ||
                            amount <= 0 ||
                            money(p) < amount) {
                        msg(p, "Valor inválido ou saldo insuficiente.");
                        return true;
                    }

                    money(p, money(p) - amount);
                    addMoney(target, amount);

                    msg(p, "Você enviou $" + amount);
                    msg(target, "Você recebeu $" + amount);
                } catch (NumberFormatException ex) {
                    msg(p, "Valor inválido.");
                }
            }

            case "shop" -> openShop(p);

            case "topmoney" -> showTop(p, "money");

            case "topkills" -> showTop(p, "kills");

            case "setmoney" -> {
                if (!sender.hasPermission("csdprbsmc.admin")) {
                    sender.sendMessage("Você não tem permissão.");
                    return true;
                }

                if (args.length != 2) {
                    sender.sendMessage("Use /setmoney <jogador> <valor>");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                try {
                    double amount = Double.parseDouble(args[1]);

                    if (!Double.isFinite(amount) || amount < 0) {
                        sender.sendMessage("Valor inválido.");
                        return true;
                    }

                    getConfig().set(
                            "players." + target.getUniqueId() + ".money",
                            amount
                    );

                    getConfig().set(
                            "players." + target.getUniqueId() + ".name",
                            args[0]
                    );

                    saveConfig();
                    sender.sendMessage("Saldo atualizado.");
                } catch (NumberFormatException ex) {
                    sender.sendMessage("Valor inválido.");
                }
            }

            case "clan" -> clanCommand(p, args);
        }

        return true;
    }

    private void showTop(Player p, String type) {
        ConfigurationSection section =
                getConfig().getConfigurationSection("players");

        if (section == null) {
            msg(p, "Ainda não há dados para o ranking.");
            return;
        }

        Map<String, Double> entries = new HashMap<>();

        for (String uuid : section.getKeys(false)) {
            String name = getConfig().getString(
                    "players." + uuid + ".name",
                    "Desconhecido"
            );

            double value = getConfig().getDouble(
                    "players." + uuid + "." + type
            );

            entries.put(name, value);
        }

        List<Map.Entry<String, Double>> sorted =
                new ArrayList<>(entries.entrySet());

        sorted.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        msg(p, "§e§lTOP 10 " + type.toUpperCase());

        for (int i = 0; i < Math.min(10, sorted.size()); i++) {
            Map.Entry<String, Double> entry = sorted.get(i);

            msg(p, "§6#" + (i + 1) + " §f" +
                    entry.getKey() + " : " + entry.getValue());
        }
    }

    private void clanCommand(Player p, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("ajuda")) {
            msg(p, "/clan criar <nome>");
            msg(p, "/clan convidar <jogador>");
            msg(p, "/clan aceitar");
            msg(p, "/clan sair");
            msg(p, "/clan info");
            msg(p, "/clan expulsar <jogador>");
            msg(p, "/clan transferir <jogador>");
            msg(p, "/clan dissolver");
            return;
        }

        String ownClan = getConfig().getString(
                "players." + id(p) + ".clan"
        );

        switch (args[0].toLowerCase()) {
            case "criar" -> {
                if (args.length != 2 || ownClan != null) {
                    msg(p, "Você já tem clan ou usou o comando errado.");
                    return;
                }

                String clan = args[1].toLowerCase(Locale.ROOT);
                if (!clan.matches("[a-z0-9_]{3,16}")) {
                    msg(p, "Nome inválido. Use 3 a 16 letras, números ou _.");
                    return;
                }

                if (getConfig().contains("clans." + clan)) {
                    msg(p, "Esse clan já existe.");
                    return;
                }

                getConfig().set("clans." + clan + ".leader", id(p));
                getConfig().set(
                        "clans." + clan + ".members",
                        List.of(id(p))
                );
                getConfig().set("players." + id(p) + ".clan", clan);
                saveConfig();

                msg(p, "Clan criado!");
            }

            case "convidar" -> {
                if (args.length != 2 || ownClan == null) {
                    msg(p, "Você precisa estar em um clan.");
                    return;
                }

                String leaderId = getConfig().getString(
                        "clans." + ownClan + ".leader"
                );

                if (!id(p).equals(leaderId)) {
                    msg(p, "Somente o líder pode convidar.");
                    return;
                }

                Player target = Bukkit.getPlayerExact(args[1]);

                if (target == null || target.equals(p) ||
                        getConfig().getString(
                                "players." + id(target) + ".clan"
                        ) != null) {
                    msg(p, "Jogador inválido ou já possui clan.");
                    return;
                }

                List<String> members = getConfig().getStringList(
                        "clans." + ownClan + ".members"
                );

                if (members.size() >= 8) {
                    msg(p, "O clan já tem 8 membros.");
                    return;
                }

                getConfig().set("clan-invites." + id(target), ownClan);
                saveConfig();

                msg(target, "Convite de clan recebido. Use /clan aceitar.");
                msg(p, "Convite enviado.");
            }

            case "aceitar" -> {
                String clan = getConfig().getString("clan-invites." + id(p));
                if (clan == null) {
                    msg(p, "Você não tem convites.");
                    return;
                }

                if (!getConfig().contains("clans." + clan)) {
                    getConfig().set("clan-invites." + id(p), null);
                    saveConfig();
                    msg(p, "Esse clan não está mais disponível.");
                    return;
                }

                if (getConfig().getString(
                        "players." + id(p) + ".clan"
                ) != null) {
                    msg(p, "Você já está em um clan.");
                    return;
                }

                List<String> members = getConfig().getStringList(
                        "clans." + clan + ".members"
                );

                if (members.size() >= 8) {
                    msg(p, "O clan está cheio.");
                    return;
                }

                getConfig().set("clan-invites." + id(p), null);
                members.add(id(p));

                getConfig().set("clans." + clan + ".members", members);
                getConfig().set("players." + id(p) + ".clan", clan);
                saveConfig();

                msg(p, "Você entrou no clan " + clan);
            }

            case "transferir" -> {
                if (args.length != 2 || ownClan == null) { msg(p, "Use /clan transferir <jogador>"); return; }
                if (!id(p).equals(getConfig().getString("clans." + ownClan + ".leader"))) { msg(p, "Somente o líder pode transferir."); return; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !ownClan.equals(getConfig().getString("players." + id(target) + ".clan"))) { msg(p, "O jogador precisa estar no seu clan e online."); return; }
                getConfig().set("clans." + ownClan + ".leader", id(target));
                saveConfig(); msg(p, "Liderança transferida para " + target.getName() + "."); msg(target, "Você agora é líder do clan.");
            }

            case "dissolver" -> {
                if (ownClan == null || !id(p).equals(getConfig().getString("clans." + ownClan + ".leader"))) { msg(p, "Somente o líder pode dissolver o clan."); return; }
                for (String member : getConfig().getStringList("clans." + ownClan + ".members")) getConfig().set("players." + member + ".clan", null);
                getConfig().set("clans." + ownClan, null); saveConfig(); msg(p, "Clan dissolvido.");
            }

            case "sair" -> {
                if (ownClan == null) {
                    msg(p, "Você não está em um clan.");
                    return;
                }

                String leader = getConfig().getString(
                        "clans." + ownClan + ".leader"
                );

                if (id(p).equals(leader)) {
                    msg(p, "O líder precisa transferir a liderança ou dissolver o clan.");
                    return;
                }

                List<String> members = getConfig().getStringList(
                        "clans." + ownClan + ".members"
                );

                members.remove(id(p));

                getConfig().set("clans." + ownClan + ".members", members);
                getConfig().set("players." + id(p) + ".clan", null);
                saveConfig();

                msg(p, "Você saiu do clan.");
            }

            case "info" -> {
                if (ownClan == null) {
                    msg(p, "Você não está em um clan.");
                    return;
                }

                List<String> members = getConfig().getStringList(
                        "clans." + ownClan + ".members"
                );

                msg(p, "Clan: §e" + ownClan +
                        " §f| Membros: " + members.size() + "/8");
            }

            case "expulsar" -> {
                if (args.length != 2 || ownClan == null) {
                    return;
                }

                String leader = getConfig().getString(
                        "clans." + ownClan + ".leader"
                );

                if (!id(p).equals(leader)) {
                    msg(p, "Somente o líder pode expulsar.");
                    return;
                }

                Player target = Bukkit.getPlayerExact(args[1]);

                if (target == null ||
                        !ownClan.equals(getConfig().getString(
                                "players." + id(target) + ".clan"
                        ))) {
                    msg(p, "Jogador não encontrado no seu clan.");
                    return;
                }

                if (target.equals(p)) {
                    msg(p, "Você não pode expulsar a si mesmo.");
                    return;
                }

                List<String> members = getConfig().getStringList(
                        "clans." + ownClan + ".members"
                );

                members.remove(id(target));

                getConfig().set("clans." + ownClan + ".members", members);
                getConfig().set("players." + id(target) + ".clan", null);
                saveConfig();

                msg(p, "Jogador expulso.");
                msg(target, "Você foi removido do clan.");
            }

            default -> msg(p, "Comando inválido. Use /clan ajuda.");
        }
    }
}

