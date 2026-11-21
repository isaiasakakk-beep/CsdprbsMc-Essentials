
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
    private static final String TPA_MENU = "§6§lTPA - JOGADORES";
    private static final String HOME_MENU = "§6§lMINHAS HOMES";
    private static final String WARP_MENU = "§6§lWARPS";
    private static final String SHOP_CATEGORIES = "§e§lLOJA - CATEGORIAS";
    private static final String SHOP_CATEGORY_PREFIX = "§e§lLOJA: ";
    private static final String BACK_BUTTON = "§c§lVOLTAR";
    private record ShopProduct(Material material, String name, double price, String category, boolean limited) {}
    private static final List<String> SHOP_CATEGORIES_LIST = List.of("Minérios", "Construção", "Agricultura", "Alimentos", "Utilidades", "Especiais");
    private static final List<ShopProduct> SHOP_PRODUCTS = List.of(
        new ShopProduct(Material.COAL,"Carvão",8,"Minérios",false), new ShopProduct(Material.RAW_IRON,"Ferro bruto",16,"Minérios",false),
        new ShopProduct(Material.IRON_INGOT,"Ferro",20,"Minérios",false), new ShopProduct(Material.RAW_GOLD,"Ouro bruto",28,"Minérios",false),
        new ShopProduct(Material.GOLD_INGOT,"Ouro",35,"Minérios",false), new ShopProduct(Material.COPPER_INGOT,"Cobre",12,"Minérios",false),
        new ShopProduct(Material.REDSTONE,"Redstone",12,"Minérios",false), new ShopProduct(Material.LAPIS_LAZULI,"Lápis-lazúli",15,"Minérios",false),
        new ShopProduct(Material.EMERALD,"Esmeralda",100,"Minérios",false), new ShopProduct(Material.DIAMOND,"Diamante",150,"Minérios",false),
        new ShopProduct(Material.COBBLESTONE,"Pedregulho",2,"Construção",false), new ShopProduct(Material.STONE,"Pedra",4,"Construção",false),
        new ShopProduct(Material.OAK_LOG,"Tronco de carvalho",10,"Construção",false), new ShopProduct(Material.GLASS,"Vidro",6,"Construção",false),
        new ShopProduct(Material.BRICKS,"Tijolos",20,"Construção",false), new ShopProduct(Material.SAND,"Areia",4,"Construção",false),
        new ShopProduct(Material.GRAVEL,"Cascalho",4,"Construção",false), new ShopProduct(Material.OBSIDIAN,"Obsidiana",80,"Construção",false),
        new ShopProduct(Material.WHEAT_SEEDS,"Sementes",2,"Agricultura",false), new ShopProduct(Material.WHEAT,"Trigo",5,"Agricultura",false),
        new ShopProduct(Material.CARROT,"Cenoura",4,"Agricultura",false), new ShopProduct(Material.POTATO,"Batata",4,"Agricultura",false),
        new ShopProduct(Material.BEETROOT_SEEDS,"Sementes de beterraba",3,"Agricultura",false), new ShopProduct(Material.SUGAR_CANE,"Cana-de-açúcar",6,"Agricultura",false),
        new ShopProduct(Material.PUMPKIN,"Abóbora",12,"Agricultura",false), new ShopProduct(Material.BREAD,"Pão",8,"Alimentos",false),
        new ShopProduct(Material.COOKED_BEEF,"Carne assada",18,"Alimentos",false), new ShopProduct(Material.COOKED_CHICKEN,"Frango assado",14,"Alimentos",false),
        new ShopProduct(Material.BAKED_POTATO,"Batata assada",7,"Alimentos",false), new ShopProduct(Material.APPLE,"Maçã",6,"Alimentos",false),
        new ShopProduct(Material.GOLDEN_CARROT,"Cenoura dourada",35,"Alimentos",false), new ShopProduct(Material.TORCH,"Tocha",2,"Utilidades",false),
        new ShopProduct(Material.CHEST,"Baú",15,"Utilidades",false), new ShopProduct(Material.CRAFTING_TABLE,"Bancada",10,"Utilidades",false),
        new ShopProduct(Material.FURNACE,"Fornalha",15,"Utilidades",false), new ShopProduct(Material.BUCKET,"Balde",40,"Utilidades",false),
        new ShopProduct(Material.WATER_BUCKET,"Balde de água",55,"Utilidades",false), new ShopProduct(Material.ANVIL,"Bigorna",180,"Utilidades",false),
        new ShopProduct(Material.ENDER_PEARL,"Pérola do Ender",60,"Especiais",true), new ShopProduct(Material.NAME_TAG,"Etiqueta",120,"Especiais",true),
        new ShopProduct(Material.SADDLE,"Sela",150,"Especiais",true), new ShopProduct(Material.EXPERIENCE_BOTTLE,"Frasco de experiência",100,"Especiais",true)
    );

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
            case 0 -> openTpaMenu(p);
            case 1 -> openHomeMenu(p);
            case 2 -> openWarpMenu(p);
            case 3 -> openClanMenu(p);
            case 4 -> { showTop(p, "kills"); showTop(p, "money"); }
            case 5 -> openShop(p);
            default -> { }
        }
    }

    private void openBedrockSelection(Player p, String title, String content, List<String> labels, Consumer<Integer> callback) {
        invokeBedrock("openSelection", new Class<?>[]{Player.class, String.class, String.class, List.class, Consumer.class},
                p, title, content, labels, callback);
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
    public void respawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTask(this, () -> giveCompass(event.getPlayer()));
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

    private boolean isBedrock(Player p) {
        return Boolean.TRUE.equals(invokeBedrock("isBedrock", new Class<?>[]{Player.class}, p));
    }

    private void openMenu(Player p) {
        if (isBedrock(p)) {
            openBedrockSelection(p, "CSDPRBSMC", "Escolha uma opção.",
                    List.of("TPA", "HOME", "WARPS", "CLANS", "RANKINGS", "LOJA/ECONOMIA"),
                    choice -> handleBedrockChoice(p, choice));
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
        if (!(event.getWhoClicked() instanceof Player p)) return;
        String title = event.getView().getTitle();
        if (!title.equals(MENU) && !title.equals(SHOP) && !title.equals(CLANS)
                && !title.equals(TPA_MENU) && !title.equals(HOME_MENU) && !title.equals(WARP_MENU)
                && !title.equals(SHOP_CATEGORIES) && !title.startsWith(SHOP_CATEGORY_PREFIX)) return;
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name.equals(BACK_BUTTON)) { openMenu(p); return; }

        if (title.equals(MENU)) {
            switch (name) {
                case "§0§lTPA" -> openTpaMenu(p);
                case "§e§lHOME" -> openHomeMenu(p);
                case "§0§lWARPS" -> openWarpMenu(p);
                case "§e§lCLANS" -> openClanMenu(p);
                case "§0§lRANKINGS" -> { p.closeInventory(); showTop(p, "kills"); showTop(p, "money"); }
                case "§e§lLOJA/ECONOMIA" -> openShop(p);
            }
        } else if (title.equals(TPA_MENU) && name.startsWith("§a")) {
            Player target = Bukkit.getPlayerExact(name.substring(2));
            if (target == null || target.equals(p)) { msg(p, "Esse jogador não está mais online."); openTpaMenu(p); return; }
            sendTpa(p, target); p.closeInventory();
        } else if (title.equals(HOME_MENU)) {
            if (name.equals("§a§lCRIAR HOME")) { createNextHome(p); openHomeMenu(p); }
            else if (name.startsWith("§aHOME: ")) {
                String home = name.substring("§aHOME: ".length());
                if (event.isShiftClick()) {
                    getConfig().set("players." + id(p) + ".homes." + home, null);
                    saveConfig(); msg(p, "Home " + home + " apagada."); openHomeMenu(p);
                } else teleportHome(p, home);
            }
        } else if (title.equals(WARP_MENU) && name.startsWith("§b")) {
            teleportWarp(p, name.substring(2));
        } else if (title.equals(SHOP_CATEGORIES) && name.startsWith("§e")) {
            openShopCategory(p, name.substring(2));
        } else if (title.startsWith(SHOP_CATEGORY_PREFIX)) {
            ShopProduct product = findProduct(name.replaceFirst("^§.", ""));
            if (product != null) buyProduct(p, product, event.isShiftClick() ? 16 : 1);
        } else if (title.equals(CLANS)) {
            p.closeInventory(); msg(p, "Use §e/clan ajuda §fpara ver os comandos.");
        } else if (title.equals(SHOP)) buy(p, name);
    }

    private void openTpaMenu(Player p) {
        List<? extends Player> targets = Bukkit.getOnlinePlayers().stream().filter(target -> !target.equals(p))
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList();
        if (isBedrock(p)) {
            List<String> labels = targets.stream().map(Player::getName).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            labels.add("Voltar");
            openBedrockSelection(p, "TPA", "Escolha quem receberá seu pedido.", labels, index -> {
                if (index >= 0 && index < targets.size()) sendTpa(p, targets.get(index)); else openMenu(p);
            });
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, TPA_MENU);
        int slot = 0;
        for (Player target : targets) { if (slot >= 45) break; inv.setItem(slot++, button(Material.PLAYER_HEAD, "§a" + target.getName())); }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void openHomeMenu(Player p) {
        String path = "players." + id(p) + ".homes";
        ConfigurationSection homes = getConfig().getConfigurationSection(path);
        List<String> names = homes == null ? new ArrayList<>() : new ArrayList<>(homes.getKeys(false));
        names.sort(String.CASE_INSENSITIVE_ORDER);
        if (isBedrock(p)) {
            List<String> labels = new ArrayList<>(); labels.add("Criar home (automática)"); labels.addAll(names); labels.add("Voltar");
            openBedrockSelection(p, "HOMES", "Você tem " + names.size() + "/5 homes.", labels, index -> {
                if (index == 0) { createNextHome(p); openHomeMenu(p); }
                else if (index > 0 && index <= names.size()) teleportHome(p, names.get(index - 1));
                else openMenu(p);
            });
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, HOME_MENU);
        inv.setItem(10, button(Material.EMERALD, "§a§lCRIAR HOME"));
        int[] slots = {12,13,14,15,16};
        for (int i=0; i<Math.min(names.size(), slots.length); i++) {
            ItemStack item=button(Material.RED_BED, "§aHOME: "+names.get(i));
            ItemMeta meta=item.getItemMeta(); meta.setLore(List.of("§7Clique: teleportar","§cShift + clique: apagar")); item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void createNextHome(Player p) {
        String path="players."+id(p)+".homes"; ConfigurationSection homes=getConfig().getConfigurationSection(path);
        int count=homes==null?0:homes.getKeys(false).size();
        if(count>=5){msg(p,"Você já tem 5 homes. Apague uma para criar outra.");return;}
        int index=1; while(getConfig().contains(path+".home"+index)) index++;
        getConfig().set(path+".home"+index,p.getLocation()); saveConfig(); msg(p,"Home home"+index+" criada!");
    }

    private void teleportHome(Player p,String home) {
        Location loc=getConfig().getLocation("players."+id(p)+".homes."+home);
        if(loc==null){msg(p,"Home não encontrada.");return;}
        p.teleport(loc); msg(p,"Teleportado para "+home+"."); p.closeInventory();
    }

    private void openWarpMenu(Player p) {
        ConfigurationSection section=getConfig().getConfigurationSection("warps");
        List<String> names=section==null?new ArrayList<>():new ArrayList<>(section.getKeys(false)); names.sort(String.CASE_INSENSITIVE_ORDER);
        if(isBedrock(p)) {
            List<String> labels=new ArrayList<>(names); labels.add("Voltar");
            openBedrockSelection(p,"WARPS","Escolha um destino.",labels,index->{if(index>=0&&index<names.size())teleportWarp(p,names.get(index));else openMenu(p);});
            return;
        }
        Inventory inv=Bukkit.createInventory(null,54,WARP_MENU); int slot=0;
        for(String name:names){if(slot>=45)break;inv.setItem(slot++,button(Material.ENDER_EYE,"§b"+name));}
        inv.setItem(49,button(Material.BARRIER,BACK_BUTTON));p.openInventory(inv);
    }

    private void teleportWarp(Player p,String warp) {
        Location loc=getConfig().getLocation("warps."+warp); if(loc==null){msg(p,"Warp não encontrada.");return;}
        p.teleport(loc);msg(p,"Teleportado para "+warp+".");p.closeInventory();
    }

    private void openShop(Player p) {
        if(isBedrock(p)){openBedrockSelection(p,"LOJA","Escolha uma categoria.",SHOP_CATEGORIES_LIST,index->{if(index>=0&&index<SHOP_CATEGORIES_LIST.size())openShopCategory(p,SHOP_CATEGORIES_LIST.get(index));});return;}
        Inventory inv=Bukkit.createInventory(null,27,SHOP_CATEGORIES);
        Material[] icons={Material.DIAMOND,Material.BRICKS,Material.WHEAT,Material.COOKED_BEEF,Material.CHEST,Material.ENDER_PEARL};
        for(int i=0;i<SHOP_CATEGORIES_LIST.size();i++)inv.setItem(10+i,button(icons[i],"§e"+SHOP_CATEGORIES_LIST.get(i)));
        inv.setItem(22,button(Material.BARRIER,BACK_BUTTON));p.openInventory(inv);
    }

    private void openShopCategory(Player p,String category) {
        List<ShopProduct> products=SHOP_PRODUCTS.stream().filter(product->product.category().equals(category)).toList();
        if(isBedrock(p)){
            List<String> labels=new ArrayList<>();for(ShopProduct product:products)labels.add(product.name()+" - $"+product.price()+(product.limited()?" (limitado)":""));labels.add("Voltar");
            openBedrockSelection(p,category,"Saldo: $"+money(p)+". Selecione um item.",labels,index->{if(index>=0&&index<products.size())buyProduct(p,products.get(index),1);else openShop(p);});return;
        }
        Inventory inv=Bukkit.createInventory(null,54,SHOP_CATEGORY_PREFIX+category);int slot=0;
        for(ShopProduct product:products){if(slot>=45)break;ItemStack item=new ItemStack(product.material());ItemMeta meta=item.getItemMeta();
            meta.setDisplayName("§f"+product.name());meta.setLore(List.of("§7Preço: §a$"+product.price(),"§eClique: 1 unidade","§eShift + clique: 16 unidades",product.limited()?"§cItem especial limitado":"§7Categoria: "+category));item.setItemMeta(meta);inv.setItem(slot++,item);}
        inv.setItem(49,button(Material.BARRIER,BACK_BUTTON));p.openInventory(inv);
    }

    private ShopProduct findProduct(String name){return SHOP_PRODUCTS.stream().filter(product->product.name().equals(name)).findFirst().orElse(null);}

    private void buyProduct(Player p,ShopProduct product,int amount){
        if(product.limited())amount=1;double total=product.price()*amount;
        if(!Double.isFinite(total)||money(p)<total){msg(p,"Saldo insuficiente. Preço: $"+total);return;}
        Map<Integer,ItemStack> leftovers=p.getInventory().addItem(new ItemStack(product.material(),amount));
        if(!leftovers.isEmpty()){msg(p,"Espaço insuficiente no inventário.");return;}
        money(p,money(p)-total);msg(p,"Compra realizada: "+amount+"x "+product.name()+" por $"+total+".");
    }

    private void sendTpa(Player p,Player target){
        if(target==null||target.equals(p)||!target.isOnline()){msg(p,"Jogador inválido.");return;}
        tpa.put(target.getUniqueId(),p.getUniqueId());tpaCreated.put(target.getUniqueId(),System.currentTimeMillis());
        msg(p,"Pedido enviado para "+target.getName()+". Expira em 60 segundos.");
        msg(target,p.getName()+" pediu teleporte. Use /tpaccept ou /tpdeny.");
    }

    private void openClanMenu(Player p) {
        if (isBedrock(p)) {
            openBedrockSelection(p, "CLANS", "Comandos disponíveis:",
                    List.of("Criar clan: /clan criar <nome>", "Convidar: /clan convidar <jogador>",
                            "Aceitar convite: /clan aceitar", "Informações: /clan info", "Sair: /clan sair", "Voltar"),
                    index -> openMenu(p));
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, CLANS);
        inv.setItem(13, button(Material.NAME_TAG, "§e§lCOMANDOS DE CLAN"));
        p.openInventory(inv);
    }

    private void buy(Player p, String name) {
        String clean = name.replaceFirst("^§.", "");
        ShopProduct product = findProduct(clean);
        if (product != null) buyProduct(p, product, 1);
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

                if (!args[0].matches("[a-zA-Z0-9_-]{1,16}")) {
                    msg(p, "Nome inválido. Use até 16 letras, números, _ ou -.");
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

                String homePath = "players." + id(p) + ".homes." + args[0];
                if (!getConfig().contains(homePath)) {
                    msg(p, "Home não encontrada.");
                    return true;
                }
                getConfig().set(homePath, null);
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

                if (!args[0].matches("[a-zA-Z0-9_-]{1,24}")) {
                    msg(p, "Nome inválido. Use até 24 letras, números, _ ou -.");
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

                if (!getConfig().contains("warps." + args[0])) {
                    msg(p, "Warp não encontrada.");
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

