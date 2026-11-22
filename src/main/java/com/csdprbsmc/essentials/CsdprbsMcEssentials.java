
package com.csdprbsmc.essentials;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.reflect.Method;

public final class CsdprbsMcEssentials extends JavaPlugin
        implements Listener, CommandExecutor {

    private final Map<UUID, UUID> tpa = new HashMap<>();
    private final Map<UUID, Boolean> tpaHere = new HashMap<>();
    private final Map<UUID, Boolean> tpaMode = new HashMap<>();
    private final Map<UUID, Long> tpaCreated = new HashMap<>();
    private final Map<UUID, String> tagCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> selectedHomes = new HashMap<>();
    private final Map<UUID, String> pendingClanInput = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack> pendingSale = new HashMap<>();
    private final Map<UUID, Integer> pendingSaleSlot = new HashMap<>();
    private final Map<UUID, Map<Integer, SaleSelection>> saleMenuSelections = new HashMap<>();
    private final Map<UUID, String> pendingClanApplication = new HashMap<>();
    private final Map<UUID, String> pendingBountyTarget = new HashMap<>();
    private final Map<UUID, String> pendingClanColorPart = new HashMap<>();
    private static final String SELL_SELECT = "§8§lSELECIONE O ITEM PARA VENDER";
    private static final String SELL_CONFIRM = "§8§lCONFIRMAR VENDA";
    private static final String SELL_QUANTITY = "§8§lESCOLHA A QUANTIDADE";
    private static final String CLAN_COLORS = "§8§lCORES DO CLAN";
    private static final String MISSIONS = "§8§lMISSÕES E RECOMPENSAS";
    private static final String CLAN_INVITE_MENU = "§8§lCONVIDAR JOGADORES";
    private static final String CLAN_BROWSER_MENU = "§8§lCLANS DISPONÍVEIS";
    private static final String CLAN_REQUESTS_MENU = "§8§lSOLICITAÇÕES DE CLAN";
    private static final String CLAN_INVITES_MENU = "§8§lCONVITES DE CLAN";
    private static final String BOUNTY_MENU = "§8§lJOGADORES PROCURADOS";
    private static final String CONTRACT_MENU = "§8§lCONTRATOS";
    private static final String TREASURE_MENU = "§8§lTESOUROS";
    private Location activeTreasure;

    private NamespacedKey compassKey;
    private Object bedrockMenus;
    private String floodgatePrefix = ".";

    private static final String MENU = "§6§lCSDPRBSMC";
    private static final String SHOP = "§e§lLOJA";
    private static final String CLANS = "§e§lCLANS";
    private static final String TPA_MENU = "§6§lTPA - JOGADORES";
    private static final String HOME_MENU = "§6§lMINHAS HOMES";
    private static final String HOME_ACTION_MENU = "§6§lGERENCIAR HOME";
    private static final String TPA_REQUEST_MENU = "§6§lPEDIDOS TPA";
    private static final String SHOP_SELL_HAND = "§a§lVENDER ITEM NA MÃO";
    private static final String SHOP_SELL_ALL = "§a§lVENDER ITENS DO INVENTÁRIO";
    private static final String WARP_MENU = "§6§lWARPS";
    private static final String SHOP_CATEGORIES = "§e§lLOJA - CATEGORIAS";
    private static final String SHOP_CATEGORY_PREFIX = "§e§lLOJA: ";
    private final Map<UUID, Integer> shopPages = new HashMap<>();
    private static final String BACK_BUTTON = "§c§lVOLTAR";
    private record ShopProduct(Material material, String name, double price, String category, boolean limited) {}
    private record SaleSelection(int sourceSlot, ItemStack item) {}
    private static final List<String> SHOP_CATEGORIES_LIST = List.of("Minérios", "Construção", "Madeiras", "Agricultura", "Alimentos", "Redstone", "Equipamentos", "Nether e End", "Decoração", "Utilidades", "Especiais", "Diversos");
    private static final List<ShopProduct> BASE_SHOP_PRODUCTS = List.of(
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
        new ShopProduct(Material.SADDLE,"Sela",150,"Especiais",true), new ShopProduct(Material.EXPERIENCE_BOTTLE,"Frasco de experiência",100,"Especiais",true),
        new ShopProduct(Material.QUARTZ, "Quartzo", 18, "Minérios", false),
        new ShopProduct(Material.AMETHYST_SHARD, "Fragmento de ametista", 22, "Minérios", false),
        new ShopProduct(Material.RAW_COPPER, "Cobre bruto", 8, "Minérios", false),
        new ShopProduct(Material.COPPER_BLOCK, "Bloco de cobre", 75, "Minérios", false),
        new ShopProduct(Material.IRON_BLOCK, "Bloco de ferro", 175, "Minérios", false),
        new ShopProduct(Material.GOLD_BLOCK, "Bloco de ouro", 300, "Minérios", false),
        new ShopProduct(Material.DIAMOND_BLOCK, "Bloco de diamante", 1350, "Minérios", false),
        new ShopProduct(Material.EMERALD_BLOCK, "Bloco de esmeralda", 900, "Minérios", false),
        new ShopProduct(Material.NETHERITE_SCRAP, "Fragmento de netherita", 900, "Minérios", true),
        new ShopProduct(Material.NETHERITE_INGOT, "Barra de netherita", 3600, "Minérios", true),
        new ShopProduct(Material.OAK_PLANKS, "Tábuas de carvalho", 3, "Construção", false),
        new ShopProduct(Material.SPRUCE_LOG, "Tronco de pinheiro", 10, "Construção", false),
        new ShopProduct(Material.SPRUCE_PLANKS, "Tábuas de pinheiro", 3, "Construção", false),
        new ShopProduct(Material.BIRCH_LOG, "Tronco de bétula", 10, "Construção", false),
        new ShopProduct(Material.BIRCH_PLANKS, "Tábuas de bétula", 3, "Construção", false),
        new ShopProduct(Material.STONE_BRICKS, "Tijolos de pedra", 8, "Construção", false),
        new ShopProduct(Material.DEEPSLATE, "Ardósia", 5, "Construção", false),
        new ShopProduct(Material.DEEPSLATE_BRICKS, "Tijolos de ardósia", 9, "Construção", false),
        new ShopProduct(Material.ANDESITE, "Andesito", 4, "Construção", false),
        new ShopProduct(Material.DIORITE, "Diorito", 4, "Construção", false),
        new ShopProduct(Material.GRANITE, "Granito", 4, "Construção", false),
        new ShopProduct(Material.MOSS_BLOCK, "Bloco de musgo", 15, "Construção", false),
        new ShopProduct(Material.GLASS_PANE, "Painel de vidro", 3, "Construção", false),
        new ShopProduct(Material.WHITE_WOOL, "Lã branca", 12, "Construção", false),
        new ShopProduct(Material.TERRACOTTA, "Terracota", 10, "Construção", false),
        new ShopProduct(Material.MELON, "Melancia", 8, "Agricultura", false),
        new ShopProduct(Material.MELON_SLICE, "Fatia de melancia", 2, "Agricultura", false),
        new ShopProduct(Material.COCOA_BEANS, "Sementes de cacau", 5, "Agricultura", false),
        new ShopProduct(Material.BAMBOO, "Bambu", 3, "Agricultura", false),
        new ShopProduct(Material.CACTUS, "Cacto", 5, "Agricultura", false),
        new ShopProduct(Material.KELP, "Alga", 3, "Agricultura", false),
        new ShopProduct(Material.NETHER_WART, "Fungo do Nether", 14, "Agricultura", false),
        new ShopProduct(Material.BROWN_MUSHROOM, "Cogumelo marrom", 4, "Agricultura", false),
        new ShopProduct(Material.RED_MUSHROOM, "Cogumelo vermelho", 4, "Agricultura", false),
        new ShopProduct(Material.HONEYCOMB, "Favo de mel", 18, "Agricultura", false),
        new ShopProduct(Material.COOKED_PORKCHOP, "Costeleta assada", 16, "Alimentos", false),
        new ShopProduct(Material.COOKED_MUTTON, "Carneiro assado", 15, "Alimentos", false),
        new ShopProduct(Material.COOKED_RABBIT, "Coelho assado", 14, "Alimentos", false),
        new ShopProduct(Material.COOKIE, "Biscoito", 3, "Alimentos", false),
        new ShopProduct(Material.CAKE, "Bolo", 45, "Alimentos", false),
        new ShopProduct(Material.PUMPKIN_PIE, "Torta de abóbora", 18, "Alimentos", false),
        new ShopProduct(Material.GOLDEN_APPLE, "Maçã dourada", 180, "Alimentos", true),
        new ShopProduct(Material.DRIED_KELP, "Alga seca", 3, "Alimentos", false),
        new ShopProduct(Material.ARROW, "Flecha", 4, "Utilidades", false),
        new ShopProduct(Material.SHIELD, "Escudo", 90, "Utilidades", false),
        new ShopProduct(Material.FISHING_ROD, "Vara de pesca", 35, "Utilidades", false),
        new ShopProduct(Material.SHEARS, "Tesoura", 35, "Utilidades", false),
        new ShopProduct(Material.FLINT_AND_STEEL, "Pederneira", 40, "Utilidades", false),
        new ShopProduct(Material.COMPASS, "Bússola", 50, "Utilidades", false),
        new ShopProduct(Material.CLOCK, "Relógio", 65, "Utilidades", false),
        new ShopProduct(Material.LEAD, "Laço", 30, "Utilidades", false),
        new ShopProduct(Material.ITEM_FRAME, "Moldura", 20, "Utilidades", false),
        new ShopProduct(Material.ENDER_CHEST, "Baú do Ender", 500, "Utilidades", true),
        new ShopProduct(Material.TOTEM_OF_UNDYING, "Totem da imortalidade", 1800, "Especiais", true),
        new ShopProduct(Material.ELYTRA, "Élitros", 5000, "Especiais", true),
        new ShopProduct(Material.SHULKER_SHELL, "Casco de shulker", 650, "Especiais", true),
        new ShopProduct(Material.TRIDENT, "Tridente", 2400, "Especiais", true),
        new ShopProduct(Material.HEART_OF_THE_SEA, "Coração do mar", 1200, "Especiais", true)
    );

    private static final List<ShopProduct> SHOP_PRODUCTS = buildShopProducts();

    private static List<ShopProduct> buildShopProducts() {
        List<ShopProduct> products = new ArrayList<>(BASE_SHOP_PRODUCTS);
        Set<Material> used = new HashSet<>();
        for (ShopProduct product : products) used.add(product.material());
        for (Material material : Material.values()) {
            if (products.size() >= 500) break;
            String key = material.name();
            if (!material.isItem() || material.isAir() || key.endsWith("_SPAWN_EGG")
                    || material == Material.RECOVERY_COMPASS || used.contains(material)) continue;
            String category = shopCategory(material);
            double price = shopPrice(material, category);
            boolean limited = price >= 1200 || key.contains("NETHERITE") || key.contains("TOTEM") || key.equals("ELYTRA");
            products.add(new ShopProduct(material, prettyMaterialName(material), price, category, limited));
            used.add(material);
        }
        return List.copyOf(products.subList(0, Math.min(500, products.size())));
    }

    private static String prettyMaterialName(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
        StringJoiner name = new StringJoiner(" ");
        for (String part : parts) name.add(part.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1));
        return name.toString();
    }

    private static String shopCategory(Material material) {
        String n = material.name();
        if (n.contains("NETHERITE") || n.contains("NETHER") || n.contains("END_STONE") || n.contains("PURPUR") || n.contains("CHORUS") || n.contains("SHULKER") || n.contains("ENDER")) return "Nether e End";
        if (n.contains("ORE") || n.contains("INGOT") || n.contains("NUGGET") || n.startsWith("RAW_") || n.contains("DIAMOND") || n.contains("EMERALD") || n.contains("LAPIS") || n.contains("REDSTONE") || n.contains("QUARTZ") || n.contains("AMETHYST") || n.contains("COPPER")) return "Minérios";
        if (material.isEdible()) return "Alimentos";
        if (n.contains("LOG") || n.contains("WOOD") || n.contains("PLANK") || n.contains("FENCE") || n.contains("DOOR") || n.contains("TRAPDOOR") || n.contains("SIGN") || n.contains("BOAT") || n.contains("CHEST_BOAT")) return "Madeiras";
        if (n.contains("SEED") || n.contains("WHEAT") || n.contains("CARROT") || n.contains("POTATO") || n.contains("BEETROOT") || n.contains("SUGAR_CANE") || n.contains("PUMPKIN") || n.contains("MELON") || n.contains("CROP") || n.contains("SAPLING") || n.contains("FUNGUS") || n.contains("MUSHROOM") || n.contains("BAMBOO") || n.contains("CACTUS") || n.contains("KELP") || n.contains("WART")) return "Agricultura";
        if (n.contains("REDSTONE") || n.contains("REPEATER") || n.contains("COMPARATOR") || n.contains("PISTON") || n.contains("OBSERVER") || n.contains("HOPPER") || n.contains("DISPENSER") || n.contains("DROPPER") || n.contains("RAIL") || n.contains("LEVER") || n.contains("BUTTON") || n.contains("PRESSURE_PLATE") || n.contains("DAYLIGHT")) return "Redstone";
        if (n.contains("SWORD") || n.contains("PICKAXE") || n.contains("AXE") || n.contains("SHOVEL") || n.contains("HOE") || n.contains("HELMET") || n.contains("CHESTPLATE") || n.contains("LEGGINGS") || n.contains("BOOTS") || n.contains("BOW") || n.contains("CROSSBOW") || n.contains("TRIDENT") || n.contains("SHIELD") || n.contains("ARMOR") || n.contains("MACE")) return "Equipamentos";
        if (n.contains("WOOL") || n.contains("CARPET") || n.contains("GLASS") || n.contains("BANNER") || n.contains("PANE") || n.contains("CANDLE") || n.contains("FLOWER") || n.contains("POT") || n.contains("PAINTING") || n.contains("FRAME") || n.contains("HEAD") || n.contains("SKULL") || n.contains("CORAL") || n.contains("DECOR")) return "Decoração";
        if (n.contains("STONE") || n.contains("BRICK") || n.contains("SAND") || n.contains("DIRT") || n.contains("GRAVEL") || n.contains("CONCRETE") || n.contains("TERRACOTTA") || n.contains("SLAB") || n.contains("STAIRS") || n.contains("WALL") || n.contains("OBSIDIAN") || n.contains("ICE") || n.contains("SNOW")) return "Construção";
        if (n.contains("FISH") || n.contains("BOAT") || n.contains("MINECART") || n.contains("SADDLE") || n.contains("RAIL") || n.contains("LEAD")) return "Utilidades";
        if (n.contains("TOTEM") || n.contains("ELYTRA") || n.contains("BEACON") || n.contains("DRAGON") || n.contains("NETHER_STAR")) return "Especiais";
        return "Diversos";
    }

    private static double shopPrice(Material material, String category) {
        String n = material.name();
        if (n.contains("NETHERITE")) return 3200;
        if (n.contains("TOTEM")) return 1800;
        if (n.equals("ELYTRA")) return 5000;
        if (n.contains("DIAMOND")) return 150;
        if (n.contains("EMERALD")) return 100;
        if (n.contains("GOLD")) return 35;
        if (n.contains("IRON")) return 20;
        if (n.contains("ANCIENT_DEBRIS")) return 900;
        if (category.equals("Alimentos") || category.equals("Agricultura")) return 5;
        if (category.equals("Equipamentos")) return n.contains("DIAMOND") ? 900 : n.contains("IRON") ? 180 : 45;
        if (category.equals("Especiais")) return 350;
        if (category.equals("Minérios")) return 18;
        if (category.equals("Decoração")) return 12;
        if (category.equals("Redstone")) return 16;
        return 6;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        compassKey = new NamespacedKey(this, "main_compass");
        initializeBedrockMenus();

        Bukkit.getPluginManager().registerEvents(this, this);

        String[] commands = {
                "ess", "tpa", "tpahere", "tpaccept", "tpdeny",
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
        org.bukkit.plugin.Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
        if (floodgate == null) {
            for (org.bukkit.plugin.Plugin installed : Bukkit.getPluginManager().getPlugins()) {
                if (installed.getName().equalsIgnoreCase("floodgate")) {
                    floodgate = installed;
                    break;
                }
            }
        }
        if (floodgate == null || !floodgate.isEnabled()) {
            bedrockMenus = null;
            getLogger().warning("Floodgate não está ativo. Jogadores Bedrock usarão o menu de inventário; instale/ative o Floodgate para abrir formulários.");
            return;
        }
        try {
            if (floodgate instanceof JavaPlugin floodgatePlugin) {
                floodgatePrefix = floodgatePlugin.getConfig().getString("username-prefix", ".");
            }
            Class<?> type = Class.forName("com.csdprbsmc.essentials.BedrockMenus");
            Object bridge = type.getConstructor().newInstance();
            type.getMethod("isBedrock", Player.class);
            type.getMethod("openSelection", Player.class, String.class, String.class, List.class, Consumer.class);
            bedrockMenus = bridge;
            getLogger().info("Integração de formulários Bedrock ativada via Floodgate.");
        } catch (Throwable error) {
            bedrockMenus = null;
            getLogger().severe("Floodgate está ativo, mas a integração de formulários falhou: " + error.getClass().getName() + ": " + error.getMessage());
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
        refreshPlayerTags();
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

        if (meta == null) return false;
        // Geyser pode não preservar o PDC no caminho de interação Bedrock;
        // o nome exclusivo da bússola funciona como identificação secundária.
        return meta.getPersistentDataContainer().has(compassKey, PersistentDataType.BYTE)
                || (meta.hasDisplayName()
                    && org.bukkit.ChatColor.stripColor(meta.getDisplayName())
                            .equalsIgnoreCase("CSDPRBSMC MENU"));
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        UUID leaving = event.getPlayer().getUniqueId();
        tagCache.remove(leaving);
        pendingClanInput.remove(leaving);
        selectedHomes.remove(leaving);
        pendingSale.remove(leaving);
        pendingSaleSlot.remove(leaving);
        saleMenuSelections.remove(leaving);
        tpaHere.remove(leaving);
        tpaMode.remove(leaving);
        shopPages.remove(leaving);
        pendingClanApplication.remove(leaving);
        pendingBountyTarget.remove(leaving);
        pendingClanColorPart.remove(leaving);
        event.getPlayer().setCustomName(null);
        event.getPlayer().setCustomNameVisible(false);
        tpa.entrySet().removeIf(entry -> entry.getKey().equals(leaving) || entry.getValue().equals(leaving));
        tpaCreated.keySet().removeIf(target -> !tpa.containsKey(target));
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        Bukkit.getScheduler().runTask(this, () -> {
            giveCompass(p);
            updatePlayerTag(p);

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
        if (event.getAction() == Action.PHYSICAL) return;
        Player player = event.getPlayer();
        ItemStack used = event.getItem();
        boolean menuCompass = isCompass(used)
                || isCompass(player.getInventory().getItemInMainHand())
                || isCompass(player.getInventory().getItemInOffHand());
        if (!menuCompass) return;
        event.setCancelled(true);
        openMenu(player);
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
            saveConfig();
            addMoney(killer, 10);
            refreshPlayerTags();
            msg(killer, "Você ganhou §a$10 §fe uma kill!");
        }

        event.getDrops().removeIf(this::isCompass);
    }

    private boolean isBedrock(Player p) {
        Object result = invokeBedrock("isBedrock", new Class<?>[]{Player.class}, p);
        if (result instanceof Boolean value && value) return true;
        return floodgatePrefix != null && !floodgatePrefix.isEmpty()
                && p.getName().startsWith(floodgatePrefix);
    }

    private String memberTag() {
        return "§e§l[MEMBRO]";
    }

    private String playerTag(Player player) {
        String clan = getConfig().getString("players." + id(player) + ".clan");
        String clanTag = "";
        if (clan != null) {
            String first = getConfig().getString("clans." + clan + ".color1", "§f");
            String second = getConfig().getString("clans." + clan + ".color2", first);
            String upper = clan.toUpperCase(Locale.ROOT);
            StringBuilder styled = new StringBuilder(" §f§l[");
            for (int i = 0; i < upper.length(); i++) styled.append(i % 2 == 0 ? first : second).append(upper.charAt(i));
            clanTag = styled.append("§f§l]").toString();
        }
        return memberTag() + clanTag;
    }

    private int rankOf(Player player, String stat) {
        ConfigurationSection players = getConfig().getConfigurationSection("players");
        if (players == null) return -1;
        List<Map.Entry<String, Double>> sorted = new ArrayList<>();
        for (String uuid : players.getKeys(false)) {
            double value = getConfig().getDouble("players." + uuid + "." + stat);
            if (value > 0) sorted.add(Map.entry(uuid, value));
        }
        sorted.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        for (int i = 0; i < sorted.size(); i++) if (sorted.get(i).getKey().equals(id(player))) return i + 1;
        return -1;
    }

    private String rankingTag(Player player) {
        int kills = rankOf(player, "kills"), money = rankOf(player, "money");
        String tag = "";
        if (kills > 0 && kills <= 3) tag += " §6§lTOP" + kills + "-K";
        if (money > 0 && money <= 3) tag += " §e§lTOP" + money + "-$";
        int best = Integer.MAX_VALUE;
        if (kills > 0 && kills <= 3) best = Math.min(best, kills);
        if (money > 0 && money <= 3) best = Math.min(best, money);
        if (best != Integer.MAX_VALUE) tag += " §6§l[TOP " + best + "]";
        return tag;
    }

    private void updatePlayerTag(Player player) {
        String prefix = playerTag(player), rank = rankingTag(player);
        tagCache.put(player.getUniqueId(), prefix);
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "csd" + player.getUniqueId().toString().replace("-", "").substring(0, 12);
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        for (String entry : new HashSet<>(team.getEntries())) team.removeEntry(entry);
        team.setPrefix(prefix.length() > 64 ? prefix.substring(0, 64) : prefix);
        team.setSuffix(rank);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.addEntry(player.getName());
        player.setCustomName(null);
        player.setCustomNameVisible(false);
    }

    private void refreshPlayerTags() {
        for (Player online : Bukkit.getOnlinePlayers()) updatePlayerTag(online);
    }

    @EventHandler
    public void formatChat(AsyncChatEvent event) {
        Player source = event.getPlayer();
        String pending = pendingClanInput.remove(source.getUniqueId());
        if (pending != null) {
            String input = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> {
                if (!input.isBlank()) clanCommand(source, new String[]{pending, input});
                else msg(source, "Entrada vazia. Abra o menu de clans e tente novamente.");
            });
            return;
        }
        String prefix = tagCache.getOrDefault(source.getUniqueId(), memberTag()) + rankingTag(source);
        event.renderer((player, displayName, message, viewer) ->
                LegacyComponentSerializer.legacySection()
                        .deserialize(prefix + " §f§l" + player.getName() + "§r: ")
                        .append(message));
    }

    private void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU);

        inv.setItem(10, button(Material.ENDER_PEARL, "§f§6§lTPA§f"));
        inv.setItem(11, button(Material.RED_BED, "§f§e§lHOME§f"));
        inv.setItem(12, button(Material.ENDER_EYE, "§f§6§lWARPS§f"));
        inv.setItem(13, button(Material.NAME_TAG, "§f§e§lCLANS§f"));
        inv.setItem(14, button(Material.GOLD_INGOT, "§f§6§lRANKINGS§f"));
        inv.setItem(15, button(Material.EMERALD, "§f§e§lLOJA/ECONOMIA§f"));
        inv.setItem(16, button(Material.WRITABLE_BOOK, "§f§6§lMISSÕES§f"));

        p.openInventory(inv);
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack playerHead(Player owner, String displayName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(owner);
        meta.setDisplayName(displayName);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack offlineHead(UUID ownerId, String displayName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerId));
        meta.setDisplayName(displayName);
        head.setItemMeta(meta);
        return head;
    }

    private String plain(String value) {
        return value == null ? "" : org.bukkit.ChatColor.stripColor(value);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void menuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (event.getClickedInventory() == null) return;
        String title = plain(event.getView().getTitle());
        boolean sellingMenu = title.equals(plain(SELL_SELECT)) || title.equals(plain(SELL_CONFIRM)) || title.equals(plain(SELL_QUANTITY));
        if (sellingMenu && event.getClickedInventory() != event.getView().getTopInventory()) { event.setCancelled(true); return; }
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        boolean managed = title.equals(plain(SELL_SELECT)) || title.equals(plain(SELL_CONFIRM)) || title.equals(plain(SELL_QUANTITY))
                || title.equals(plain(CLAN_COLORS)) || title.equals(plain(MISSIONS))
                || title.equals(plain(MENU)) || title.equals(plain(TPA_MENU))
                || title.equals(plain(TPA_REQUEST_MENU)) || title.equals(plain(HOME_MENU))
                || title.equals(plain(HOME_ACTION_MENU)) || title.equals(plain(WARP_MENU))
                || title.equals(plain(CLANS)) || title.equals(plain(CLAN_INVITE_MENU)) || title.equals(plain(CLAN_BROWSER_MENU))
                || title.equals(plain(CLAN_REQUESTS_MENU)) || title.equals(plain(CLAN_INVITES_MENU))
                || title.equals(plain(BOUNTY_MENU)) || title.equals(plain(CONTRACT_MENU)) || title.equals(plain(TREASURE_MENU)) || title.equals(plain(SHOP))
                || title.equals(plain(SHOP_CATEGORIES))
                || title.startsWith(plain(SHOP_CATEGORY_PREFIX));
        if (!managed) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = plain(item.getItemMeta().getDisplayName());

        if (name.equals(plain(BACK_BUTTON))) {
            if (title.equals(plain(SELL_SELECT)) || title.equals(plain(SELL_CONFIRM)) || title.equals(plain(SELL_QUANTITY))) { pendingSale.remove(p.getUniqueId()); pendingSaleSlot.remove(p.getUniqueId()); saleMenuSelections.remove(p.getUniqueId()); openShop(p); }
            else if (title.equals(plain(CLAN_COLORS)) || title.equals(plain(MISSIONS))) openMenu(p);
            else if (title.startsWith(plain(SHOP_CATEGORY_PREFIX))) openShop(p);
            else if (title.equals(plain(HOME_ACTION_MENU))) openHomeMenu(p);
            else if (title.equals(plain(TPA_REQUEST_MENU))) openTpaMenu(p);
            else openMenu(p);
            return;
        }

        if (title.equals(plain(MENU))) {
            switch (name) {
                case "TPA" -> openTpaMenu(p);
                case "HOME" -> openHomeMenu(p);
                case "WARPS" -> openWarpMenu(p);
                case "CLANS" -> openClanMenu(p);
                case "RANKINGS" -> { p.closeInventory(); showTop(p, "kills"); showTop(p, "money"); }
                case "LOJA/ECONOMIA" -> openShop(p);
                case "MISSÕES" -> openMissions(p);
                default -> { }
            }
        } else if (title.equals(plain(SELL_SELECT))) {
            SaleSelection selection = saleMenuSelections.getOrDefault(p.getUniqueId(), Map.of()).get(event.getSlot());
            if (selection != null) openSellQuantity(p, selection);
        } else if (title.equals(plain(SELL_QUANTITY))) {
            ItemStack selected = pendingSale.get(p.getUniqueId());
            if (selected != null) {
                int amount = selected.getAmount();
                if (name.startsWith("QUANTIDADE: ")) {
                    try { amount = Math.min(Integer.parseInt(name.substring(12)), selected.getAmount()); } catch (NumberFormatException ignored) { }
                } else if (name.startsWith("TUDO: ")) amount = selected.getAmount();
                else { openSellSelection(p); return; }
                selected.setAmount(amount);
                pendingSale.put(p.getUniqueId(), selected);
                openSellConfirmation(p, selected);
            }
        } else if (title.equals(plain(SELL_CONFIRM))) {
            if (name.equals("CONFIRMAR VENDA")) confirmSale(p);
            else if (name.equals("CANCELAR")) { pendingSale.remove(p.getUniqueId()); pendingSaleSlot.remove(p.getUniqueId()); openSellSelection(p); }
        } else if (title.equals(plain(CLAN_COLORS))) {
            if (name.startsWith("COR: ")) {
                String code = colorCode(name.substring(5));
                String clan = getConfig().getString("players." + id(p) + ".clan");
                if (clan == null || !id(p).equals(getConfig().getString("clans." + clan + ".leader"))) { msg(p, "Somente o líder pode alterar as cores."); openClanMenu(p); return; }
                String part = pendingClanColorPart.getOrDefault(p.getUniqueId(), "color1");
                getConfig().set("clans." + clan + "." + part, code);
                if (part.equals("color1")) { pendingClanColorPart.put(p.getUniqueId(), "color2"); msg(p, "Primeira cor salva. Escolha a segunda."); openClanColorMenu(p); }
                else { pendingClanColorPart.remove(p.getUniqueId()); saveConfig(); refreshPlayerTags(); msg(p, "Gradiente do clan atualizado!"); openClanMenu(p); }
                saveConfig();
            }
        } else if (title.equals(plain(MISSIONS))) {
            if (name.startsWith("MISSÃO: ")) claimMission(p, name.substring(8));
        } else if (title.equals(plain(TPA_MENU))) {
            if (name.startsWith("MODO: ")) { tpaMode.put(p.getUniqueId(), !tpaMode.getOrDefault(p.getUniqueId(), false)); openTpaMenu(p); }
            else if (name.equals("PEDIDOS RECEBIDOS")) openTpaRequests(p);
            else if (name.equals("MEUS PEDIDOS")) { cancelOutgoingTpa(p); openTpaMenu(p); }
            else {
                Player target = Bukkit.getPlayerExact(name);
                if (target == null || target.equals(p)) { msg(p, "Esse jogador não está mais online."); openTpaMenu(p); return; }
                sendTpa(p, target, tpaMode.getOrDefault(p.getUniqueId(), false));
                p.closeInventory();
            }
        } else if (title.equals(plain(TPA_REQUEST_MENU))) {
            if (name.equals("ACEITAR PEDIDO")) respondTpa(p, true);
            else if (name.equals("RECUSAR PEDIDO")) respondTpa(p, false);
        } else if (title.equals(plain(HOME_MENU))) {
            if (name.equals("CRIAR HOME")) { createNextHome(p); openHomeMenu(p); }
            else if (name.startsWith("HOME: ")) openHomeActions(p, name.substring("HOME: ".length()));
        } else if (title.equals(plain(HOME_ACTION_MENU))) {
            String home = selectedHomes.get(p.getUniqueId());
            if (home == null) { openHomeMenu(p); return; }
            if (name.equals("TELEPORTAR")) teleportHome(p, home);
            else if (name.equals("EDITAR LOCAL")) {
                getConfig().set("players." + id(p) + ".homes." + home, p.getLocation());
                saveConfig(); msg(p, "Local da home " + home + " atualizado."); openHomeMenu(p);
            } else if (name.equals("APAGAR HOME")) {
                deleteHome(p, home); selectedHomes.remove(p.getUniqueId()); openHomeMenu(p);
            }
        } else if (title.equals(plain(WARP_MENU))) {
            teleportWarp(p, name);
        } else if (title.equals(plain(SHOP_CATEGORIES))) {
            openShopCategory(p, name);
        } else if (title.startsWith(plain(SHOP_CATEGORY_PREFIX))) {
            String categoryPage = title.substring(plain(SHOP_CATEGORY_PREFIX).length());
            int marker = categoryPage.lastIndexOf(" #");
            String category = marker < 0 ? categoryPage : categoryPage.substring(0, marker);
            int page = 0;
            if (marker >= 0) { try { page = Integer.parseInt(categoryPage.substring(marker + 2)) - 1; } catch (NumberFormatException ignored) { } }
            if (name.equals("PÁGINA ANTERIOR")) openShopCategory(p, category, page - 1);
            else if (name.equals("PRÓXIMA PÁGINA")) openShopCategory(p, category, page + 1);
            else if (name.equals("VENDER ITEM DO INVENTÁRIO")) openSellSelection(p);
            else if (name.equals(plain(SHOP_SELL_ALL))) sellAll(p);
            else {
                ShopProduct product = findProduct(name);
                if (product != null) buyProduct(p, product, event.isShiftClick() ? 16 : 1);
            }
        } else if (title.equals(plain(CLAN_INVITE_MENU))) {
            String targetName = name.startsWith("JOGADOR: ") ? name.substring(9) : "";
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) { clanCommand(p, new String[]{"convidar", target.getName()}); openClanInviteMenu(p); }
        } else if (title.equals(plain(CLAN_BROWSER_MENU))) {
            String clan = name.startsWith("CLAN: ") ? name.substring(6) : "";
            if (!clan.isBlank()) applyToClan(p, clan);
        } else if (title.equals(plain(CLAN_REQUESTS_MENU))) {
            if (name.startsWith("SOLICITANTE: ")) { String applicant = name.substring(13); handleClanApplication(p, applicant, event.isRightClick()); }
        } else if (title.equals(plain(CLAN_INVITES_MENU))) {
            if (name.startsWith("CONVITE: ")) { if (event.isRightClick()) { getConfig().set("clan-invites." + id(p), null); saveConfig(); msg(p, "Convite recusado."); openClanInvitesMenu(p); } else clanCommand(p, new String[]{"aceitar"}); }
        } else if (title.equals(plain(CLANS))) {
            p.closeInventory();
            if (name.equals("CRIAR CLAN")) {
                pendingClanInput.put(p.getUniqueId(), "criar");
                msg(p, "Digite no chat o nome do novo clan.");
            } else if (name.equals("CONVIDAR JOGADOR")) {
                openClanInviteMenu(p);
            } else if (name.equals("CLANS DISPONÍVEIS")) {
                openClanBrowserMenu(p);
            } else if (name.equals("SOLICITAÇÕES")) {
                openClanRequestsMenu(p);
            } else if (name.equals("PERSONALIZAR CORES")) {
                String clan = getConfig().getString("players." + id(p) + ".clan");
                if (clan == null || !id(p).equals(getConfig().getString("clans." + clan + ".leader"))) { msg(p, "Somente o líder do clan pode personalizar as cores."); openClanMenu(p); return; }
                pendingClanColorPart.put(p.getUniqueId(), "color1"); openClanColorMenu(p);
            } else if (name.equals("ACEITAR CONVITE")) openClanInvitesMenu(p);
            else if (name.equals("INFORMAÇÕES DO CLAN")) clanCommand(p, new String[]{"info"});
            else if (name.equals("SAIR DO CLAN")) clanCommand(p, new String[]{"sair"});
        }
    }

    @EventHandler
    public void menuDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(MENU) || title.equals(SHOP) || title.equals(CLANS) || title.equals(CLAN_INVITE_MENU) || title.equals(CLAN_BROWSER_MENU) || title.equals(CLAN_REQUESTS_MENU) || title.equals(CLAN_INVITES_MENU) || title.equals(BOUNTY_MENU) || title.equals(CONTRACT_MENU) || title.equals(TREASURE_MENU) || title.equals(SELL_SELECT) || title.equals(SELL_CONFIRM) || title.equals(SELL_QUANTITY) || title.equals(CLAN_COLORS) || title.equals(MISSIONS)
                || title.equals(TPA_MENU) || title.equals(TPA_REQUEST_MENU)
                || title.equals(HOME_MENU) || title.equals(HOME_ACTION_MENU)
                || title.equals(WARP_MENU) || title.equals(SHOP_CATEGORIES)
                || title.startsWith(SHOP_CATEGORY_PREFIX)) {
            event.setCancelled(true);
        }
    }

    private void openTpaMenu(Player p) {
        List<? extends Player> targets = Bukkit.getOnlinePlayers().stream().filter(target -> !target.equals(p))
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList();
        Inventory inv = Bukkit.createInventory(null, 54, TPA_MENU);
        int slot = 0;
        for (Player target : targets) { if (slot >= 45) break; inv.setItem(slot++, playerHead(target, "§a" + target.getName())); }
        boolean here = tpaMode.getOrDefault(p.getUniqueId(), false);
        inv.setItem(45, button(Material.COMPASS, here ? "§e§lMODO: ELE VIR ATÉ MIM" : "§e§lMODO: EU IR ATÉ ELE"));
        inv.setItem(46, button(Material.WRITABLE_BOOK, "§e§lPEDIDOS RECEBIDOS"));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        inv.setItem(53, button(Material.PAPER, "§e§lMEUS PEDIDOS"));
        p.openInventory(inv);
    }

    private void openHomeMenu(Player p) {
        String path = "players." + id(p) + ".homes";
        ConfigurationSection homes = getConfig().getConfigurationSection(path);
        List<String> names = homes == null ? new ArrayList<>() : new ArrayList<>(homes.getKeys(false));
        names.sort(String.CASE_INSENSITIVE_ORDER);
        Inventory inv = Bukkit.createInventory(null, 27, HOME_MENU);
        inv.setItem(10, button(Material.EMERALD, "§a§lCRIAR HOME"));
        int[] slots = {12,13,14,15,16};
        for (int i=0; i<Math.min(names.size(), slots.length); i++) {
            ItemStack item=button(Material.RED_BED, "§aHOME: "+names.get(i));
            ItemMeta meta=item.getItemMeta(); meta.setLore(List.of("§7Clique para abrir opções:","§aTeleportar","§eEditar localização","§cApagar home")); item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void openHomeActions(Player p, String home) {
        if (!getConfig().contains("players." + id(p) + ".homes." + home)) { msg(p, "Home não encontrada."); openHomeMenu(p); return; }
        selectedHomes.put(p.getUniqueId(), home);
        Inventory inv = Bukkit.createInventory(null, 27, HOME_ACTION_MENU);
        inv.setItem(10, button(Material.ENDER_PEARL, "§a§lTELEPORTAR"));
        inv.setItem(13, button(Material.COMPASS, "§e§lEDITAR LOCAL"));
        inv.setItem(16, button(Material.BARRIER, "§c§lAPAGAR HOME"));
        inv.setItem(22, button(Material.PAPER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openTpaRequests(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TPA_REQUEST_MENU);
        UUID requesterId = tpa.get(p.getUniqueId());
        Player requester = requesterId == null ? null : Bukkit.getPlayer(requesterId);
        if (requester != null) {
            inv.setItem(11, button(Material.LIME_WOOL, "§a§lACEITAR PEDIDO"));
            inv.setItem(15, button(Material.RED_WOOL, "§c§lRECUSAR PEDIDO"));
            inv.setItem(13, button(Material.PLAYER_HEAD, "§fPedido de: " + requester.getName()));
        } else inv.setItem(13, button(Material.BARRIER, "§cNenhum pedido pendente"));
        inv.setItem(22, button(Material.PAPER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void createNextHome(Player p) {
        String path="players."+id(p)+".homes"; ConfigurationSection homes=getConfig().getConfigurationSection(path);
        int count=homes==null?0:homes.getKeys(false).size();
        if(count>=5){msg(p,"Você já tem 5 homes. Apague uma para criar outra.");return;}
        int index=1; while(getConfig().contains(path+".home"+index)) index++;
        getConfig().set(path+".home"+index,p.getLocation()); saveConfig(); msg(p,"Home home"+index+" criada!");
    }

    private void deleteHome(Player p, String home) {
        String path = "players." + id(p) + ".homes." + home;
        if (!getConfig().contains(path)) { msg(p, "Home não encontrada."); return; }
        getConfig().set(path, null); saveConfig(); msg(p, "Home " + home + " apagada.");
    }

    private void teleportHome(Player p,String home) {
        Location loc=getConfig().getLocation("players."+id(p)+".homes."+home);
        if(loc==null){msg(p,"Home não encontrada.");return;}
        p.teleport(loc); msg(p,"Teleportado para "+home+"."); p.closeInventory();
    }

    private void openWarpMenu(Player p) {
        ConfigurationSection section=getConfig().getConfigurationSection("warps");
        List<String> names=section==null?new ArrayList<>():new ArrayList<>(section.getKeys(false)); names.sort(String.CASE_INSENSITIVE_ORDER);
        Inventory inv=Bukkit.createInventory(null,54,WARP_MENU); int slot=0;
        for(String name:names){if(slot>=45)break;inv.setItem(slot++,button(Material.ENDER_EYE,"§b"+name));}
        inv.setItem(49,button(Material.BARRIER,BACK_BUTTON));p.openInventory(inv);
    }

    private void teleportWarp(Player p,String warp) {
        Location loc=getConfig().getLocation("warps."+warp); if(loc==null){msg(p,"Warp não encontrada.");return;}
        p.teleport(loc);msg(p,"Teleportado para "+warp+".");p.closeInventory();
    }

    private void openShop(Player p) {
        Inventory inv = Bukkit.createInventory(null, 36, SHOP_CATEGORIES);
        Material[] icons = {Material.DIAMOND, Material.BRICKS, Material.OAK_LOG, Material.WHEAT, Material.COOKED_BEEF, Material.REDSTONE,
                Material.IRON_SWORD, Material.NETHERRACK, Material.PAINTING, Material.CHEST, Material.NETHER_STAR, Material.BARREL};
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23};
        for (int i = 0; i < SHOP_CATEGORIES_LIST.size(); i++) inv.setItem(slots[i], button(icons[i], "§e§l" + SHOP_CATEGORIES_LIST.get(i)));
        inv.setItem(31, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void openShopCategory(Player p, String category) { openShopCategory(p, category, 0); }

    private void openShopCategory(Player p, String category, int requestedPage) {
        List<ShopProduct> products = SHOP_PRODUCTS.stream().filter(product -> product.category().equals(category)).toList();
        int pages = Math.max(1, (products.size() + 44) / 45);
        int page = Math.max(0, Math.min(requestedPage, pages - 1));
        shopPages.put(p.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(null, 54, SHOP_CATEGORY_PREFIX + category + " #" + (page + 1));
        int start = page * 45;
        for (int i = start; i < Math.min(start + 45, products.size()); i++) {
            ShopProduct product = products.get(i); ItemStack item = new ItemStack(product.material()); ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f§l" + product.name() + "§f");
            meta.setLore(List.of("§7Preço: §a$" + product.price(), "§eClique: 1 unidade", "§eShift + clique: 16 unidades", product.limited() ? "§cItem especial limitado" : "§7Categoria: " + category));
            item.setItemMeta(meta); inv.setItem(i - start, item);
        }
        inv.setItem(45, button(Material.ARROW, "§e§lPÁGINA ANTERIOR"));
        inv.setItem(46, button(Material.ARROW, "§e§lPRÓXIMA PÁGINA"));
        inv.setItem(47, button(Material.GOLD_INGOT, "§a§lVENDER ITEM DO INVENTÁRIO"));
        inv.setItem(48, button(Material.CHEST, SHOP_SELL_ALL));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        inv.setItem(50, button(Material.PAPER, "§fPágina " + (page + 1) + "/" + pages + " • " + products.size() + " itens"));
        p.openInventory(inv);
    }

    private ShopProduct findProduct(String name){return SHOP_PRODUCTS.stream().filter(product->product.name().equals(name)).findFirst().orElse(null);}

    private void buyProduct(Player p, ShopProduct product, int amount) {
        if (product == null || amount < 1) return;
        if (product.limited()) amount = 1;

        int maxStack = product.material().getMaxStackSize();
        if (amount > maxStack) {
            msg(p, "Quantidade acima do limite por compra.");
            return;
        }

        double total = product.price() * amount;
        double balance = money(p);
        if (!Double.isFinite(total) || total <= 0 || balance < total) {
            msg(p, "Saldo insuficiente. Preço: $" + total);
            return;
        }

        // Confere o espaço antes de inserir qualquer item para evitar compras parciais grátis.
        int capacity = 0;
        ItemStack sample = new ItemStack(product.material());
        for (ItemStack content : p.getInventory().getStorageContents()) {
            if (content == null || content.getType().isAir()) {
                capacity += maxStack;
            } else if (content.isSimilar(sample)) {
                capacity += Math.max(0, content.getMaxStackSize() - content.getAmount());
            }
            if (capacity >= amount) break;
        }
        if (capacity < amount) {
            msg(p, "Espaço insuficiente no inventário. Nenhum valor foi cobrado.");
            return;
        }

        ItemStack[] inventoryBefore = p.getInventory().getStorageContents();
        for (int i = 0; i < inventoryBefore.length; i++) {
            if (inventoryBefore[i] != null) inventoryBefore[i] = inventoryBefore[i].clone();
        }
        Map<Integer, ItemStack> leftovers = p.getInventory().addItem(new ItemStack(product.material(), amount));
        if (!leftovers.isEmpty()) {
            p.getInventory().setStorageContents(inventoryBefore);
            msg(p, "A compra foi cancelada por falta de espaço. Nenhum valor foi cobrado.");
            return;
        }

        money(p, balance - total);
        msg(p, "Compra realizada: " + amount + "x " + product.name() + " por $" + total + ".");
    }

    private void respondTpa(Player target, boolean accept) {
        UUID requesterId = tpa.remove(target.getUniqueId());
        Long created = tpaCreated.remove(target.getUniqueId());
        boolean targetComesToRequester = tpaHere.getOrDefault(target.getUniqueId(), false);
        tpaHere.remove(target.getUniqueId());
        if (requesterId == null || created == null) { msg(target, "Você não tem pedidos."); openTpaRequests(target); return; }
        Player requester = Bukkit.getPlayer(requesterId);
        if (System.currentTimeMillis() - created > 60_000L || requester == null) {
            msg(target, "Pedido expirado ou jogador offline."); openTpaMenu(target); return;
        }
        if (accept) { if (targetComesToRequester) target.teleport(requester.getLocation()); else requester.teleport(target.getLocation()); msg(target, "Pedido aceito."); msg(requester, "Seu pedido foi aceito."); }
        else { msg(target, "Pedido recusado."); msg(requester, "Seu pedido foi recusado."); }
        openTpaMenu(target);
    }

    private void cancelOutgoingTpa(Player requester) {
        UUID targetId = tpa.entrySet().stream().filter(entry -> entry.getValue().equals(requester.getUniqueId()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
        if (targetId == null) { msg(requester, "Você não tem pedido enviado pendente."); return; }
        tpa.remove(targetId); tpaCreated.remove(targetId); tpaHere.remove(targetId); msg(requester, "Pedido cancelado.");
    }

    private void openSellSelection(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, SELL_SELECT);
        Map<Integer, SaleSelection> selections = new HashMap<>();
        int slot = 0;
        ItemStack[] contents = p.getInventory().getStorageContents();
        for (int sourceSlot = 0; sourceSlot < contents.length; sourceSlot++) {
            ItemStack stack = contents[sourceSlot];
            if (stack == null || stack.getType().isAir() || isCompass(stack)) continue;
            ShopProduct product = SHOP_PRODUCTS.stream().filter(x -> x.material() == stack.getType()).findFirst().orElse(null);
            if (product == null || slot >= 45) continue;
            ItemStack original = stack.clone();
            ItemStack display = stack.clone();
            ItemMeta meta = display.getItemMeta();
            meta.setLore(List.of("§7Quantidade: §f" + stack.getAmount(), "§7Valor total: §a$" + (Math.floor(product.price() * .5 * stack.getAmount() * 100) / 100), "§eClique para selecionar"));
            display.setItemMeta(meta);
            inv.setItem(slot, display);
            selections.put(slot, new SaleSelection(sourceSlot, original));
            slot++;
        }
        saleMenuSelections.put(p.getUniqueId(), selections);
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        if (slot == 0) msg(p, "Você não possui itens aceitos pela loja.");
        p.openInventory(inv);
    }

    private void openSellQuantity(Player p, SaleSelection selection) {
        ItemStack chosen = selection.item().clone();
        pendingSale.put(p.getUniqueId(), chosen.clone());
        pendingSaleSlot.put(p.getUniqueId(), selection.sourceSlot());
        Inventory inv = Bukkit.createInventory(null, 27, SELL_QUANTITY);
        int[] amounts = {1, 16, 32, 64};
        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < amounts.length; i++) {
            int amount = Math.min(amounts[i], chosen.getAmount());
            inv.setItem(slots[i], button(Material.PAPER, "§e§lQUANTIDADE: " + amount));
        }
        inv.setItem(22, button(Material.EMERALD, "§a§lTUDO: " + chosen.getAmount()));
        inv.setItem(26, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openSellConfirmation(Player p, ItemStack chosen) {
        pendingSale.put(p.getUniqueId(), chosen.clone());
        ShopProduct product = SHOP_PRODUCTS.stream().filter(x -> x.material() == chosen.getType()).findFirst().orElse(null);
        if (product == null) return;
        double total = Math.floor(product.price() * .5 * chosen.getAmount() * 100) / 100;
        Inventory inv = Bukkit.createInventory(null, 27, SELL_CONFIRM);
        inv.setItem(11, button(Material.LIME_WOOL, "§a§lCONFIRMAR VENDA"));
        inv.setItem(13, chosen.clone());
        inv.setItem(15, button(Material.RED_WOOL, "§c§lCANCELAR"));
        p.openInventory(inv);
        msg(p, "Confirme a venda de " + chosen.getAmount() + "x " + product.name() + " por $" + total + ".");
    }

    private void confirmSale(Player p) {
        ItemStack selected = pendingSale.remove(p.getUniqueId());
        Integer sourceSlot = pendingSaleSlot.remove(p.getUniqueId());
        saleMenuSelections.remove(p.getUniqueId());
        if (selected == null || sourceSlot == null) { openSellSelection(p); return; }
        ItemStack current = p.getInventory().getItem(sourceSlot);
        if (current == null || !current.isSimilar(selected) || current.getAmount() < selected.getAmount()) {
            msg(p, "A pilha selecionada mudou de lugar ou foi modificada. Venda cancelada para proteger seus itens.");
            openSellSelection(p);
            return;
        }
        current.setAmount(current.getAmount() - selected.getAmount());
        if (current.getAmount() <= 0) p.getInventory().setItem(sourceSlot, null);
        ShopProduct product = SHOP_PRODUCTS.stream().filter(x -> x.material() == selected.getType()).findFirst().orElse(null);
        if (product == null) return;
        double total = Math.floor(product.price() * .5 * selected.getAmount() * 100) / 100;
        addMoney(p, total);
        msg(p, "Você vendeu " + selected.getAmount() + "x " + product.name() + " por $" + total + ".");
        openSellSelection(p);
    }

    private void sellAll(Player p) {
        int sold = 0; double total = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = p.getInventory().getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            ShopProduct product = SHOP_PRODUCTS.stream().filter(entry -> entry.material() == item.getType()).findFirst().orElse(null);
            if (product == null) continue;
            sold += item.getAmount(); total += product.price() * 0.5 * item.getAmount();
            p.getInventory().setItem(slot, null);
        }
        total = Math.floor(total * 100.0) / 100.0;
        if (sold == 0) { msg(p, "Você não possui itens aceitos pela loja."); return; }
        addMoney(p, total); msg(p, "Você vendeu " + sold + " itens por $" + total + ".");
    }

    private void sendTpa(Player p, Player target, boolean targetComesToRequester) {
        if (target == null || target.equals(p) || !target.isOnline()) { msg(p, "Jogador inválido."); return; }
        tpa.put(target.getUniqueId(), p.getUniqueId());
        tpaCreated.put(target.getUniqueId(), System.currentTimeMillis());
        tpaHere.put(target.getUniqueId(), targetComesToRequester);
        msg(p, "Pedido enviado para " + target.getName() + ". Expira em 60 segundos.");
        msg(target, targetComesToRequester ? p.getName() + " pediu para você ir até ele. Use /tpaccept ou /tpdeny." : p.getName() + " pediu para ir até você. Use /tpaccept ou /tpdeny.");
    }

    private void openClanColorMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, CLAN_COLORS);
        String[] labels = {"BRANCO","AMARELO","VERMELHO","AZUL","VERDE","ROSA","CIANO","LARANJA","ROXO","AZUL ESCURO","VERDE ESCURO","VERMELHO ESCURO"};
        String[] codes = {"§f","§e","§c","§9","§a","§d","§b","§6","§5","§3","§2","§4"};
        for (int i = 0; i < labels.length; i++) inv.setItem(i, button(Material.WHITE_WOOL, "§fCOR: " + labels[i]));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private String colorCode(String label) {
        return switch (label) {
            case "BRANCO" -> "§f"; case "AMARELO" -> "§e"; case "VERMELHO" -> "§c";
            case "AZUL" -> "§9"; case "VERDE" -> "§a"; case "ROSA" -> "§d";
            case "CIANO" -> "§b"; case "LARANJA" -> "§6"; case "ROXO" -> "§5";
            case "AZUL ESCURO" -> "§3"; case "VERDE ESCURO" -> "§2"; case "VERMELHO ESCURO" -> "§4";
            default -> "§f";
        };
    }

    private void openClanMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, CLANS);
        String ownClan = getConfig().getString("players." + id(p) + ".clan");
        inv.setItem(10, button(Material.NAME_TAG, "§e§lCRIAR CLAN"));
        inv.setItem(11, button(Material.PLAYER_HEAD, "§a§lCONVIDAR JOGADOR"));
        inv.setItem(12, button(Material.BOOKSHELF, "§b§lCLANS DISPONÍVEIS"));
        inv.setItem(13, button(Material.EMERALD, "§a§lACEITAR CONVITE"));
        inv.setItem(14, button(Material.PAINTING, "§e§lPERSONALIZAR CORES"));
        inv.setItem(15, button(Material.BOOK, "§e§lINFORMAÇÕES DO CLAN"));
        inv.setItem(16, button(Material.OAK_DOOR, "§c§lSAIR DO CLAN"));
        if (ownClan != null && id(p).equals(getConfig().getString("clans." + ownClan + ".leader"))) inv.setItem(21, button(Material.WRITABLE_BOOK, "§6§lSOLICITAÇÕES"));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openClanInviteMenu(Player p) {
        String clan = getConfig().getString("players." + id(p) + ".clan");
        if (clan == null || !id(p).equals(getConfig().getString("clans." + clan + ".leader"))) { msg(p, "Somente o líder de um clan pode convidar jogadores."); openClanMenu(p); return; }
        List<Player> candidates = Bukkit.getOnlinePlayers().stream().filter(target -> !target.equals(p)
                && getConfig().getString("players." + id(target) + ".clan") == null)
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList();
        Inventory inv = Bukkit.createInventory(null, 54, CLAN_INVITE_MENU);
        int slot = 0;
        for (Player target : candidates) {
            if (slot >= 45) break;
            ItemStack head = playerHead(target, "§aJOGADOR: " + target.getName());
            ItemMeta meta = head.getItemMeta(); meta.setLore(List.of("§7Clique para enviar convite ao jogador.")); head.setItemMeta(meta);
            inv.setItem(slot++, head);
        }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        if (slot == 0) msg(p, "Não há jogadores online disponíveis para convidar.");
        p.openInventory(inv);
    }

    private void openClanBrowserMenu(Player p) {
        ConfigurationSection clans = getConfig().getConfigurationSection("clans");
        Inventory inv = Bukkit.createInventory(null, 54, CLAN_BROWSER_MENU);
        if (clans != null) {
            List<String> names = new ArrayList<>(clans.getKeys(false)); names.sort(String.CASE_INSENSITIVE_ORDER);
            int slot = 0;
            for (String clan : names) {
                if (slot >= 45) break;
                String leader = getConfig().getString("clans." + clan + ".leader");
                UUID leaderId;
                try { leaderId = UUID.fromString(leader); } catch (Exception ex) { continue; }
                ItemStack head = offlineHead(leaderId, "§eCLAN: " + clan);
                ItemMeta meta = head.getItemMeta();
                meta.setLore(List.of("§7Líder: §f" + getConfig().getString("players." + leader + ".name", "Desconhecido"),
                        "§7Membros: §f" + getConfig().getStringList("clans." + clan + ".members").size() + "/8",
                        "§aClique para solicitar entrada"));
                head.setItemMeta(meta); inv.setItem(slot++, head);
            }
        }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void applyToClan(Player p, String clan) {
        if (getConfig().getString("players." + id(p) + ".clan") != null) { msg(p, "Você já está em um clan."); return; }
        if (!getConfig().contains("clans." + clan)) { msg(p, "Esse clan não existe mais."); openClanBrowserMenu(p); return; }
        if (getConfig().getStringList("clans." + clan + ".members").size() >= 8) { msg(p, "Esse clan está cheio."); openClanBrowserMenu(p); return; }
        String path = "clan-applications." + clan + "." + id(p);
        if (getConfig().contains(path)) { msg(p, "Você já solicitou entrada nesse clan."); return; }
        getConfig().set(path, p.getName()); saveConfig();
        String leaderId = getConfig().getString("clans." + clan + ".leader");
        Player leader = null; try { leader = Bukkit.getPlayer(UUID.fromString(leaderId)); } catch (Exception ignored) { }
        if (leader != null) msg(leader, "§e" + p.getName() + " solicitou entrar no seu clan. Veja as solicitações na bússola.");
        msg(p, "Solicitação enviada ao líder do clan " + clan + "."); p.closeInventory();
    }

    private void openClanRequestsMenu(Player p) {
        String clan = getConfig().getString("players." + id(p) + ".clan");
        if (clan == null || !id(p).equals(getConfig().getString("clans." + clan + ".leader"))) { msg(p, "Somente o líder pode ver as solicitações."); openClanMenu(p); return; }
        Inventory inv = Bukkit.createInventory(null, 54, CLAN_REQUESTS_MENU);
        ConfigurationSection requests = getConfig().getConfigurationSection("clan-applications." + clan);
        int slot = 0;
        if (requests != null) for (String applicant : requests.getKeys(false)) {
            if (slot >= 45) break;
            try {
                UUID applicantId = UUID.fromString(applicant);
                String playerName = requests.getString(applicant, "Desconhecido");
                ItemStack head = offlineHead(applicantId, "§eSOLICITANTE: " + applicant);
                ItemMeta meta = head.getItemMeta(); meta.setLore(List.of("§7Jogador: §f" + playerName, "§aClique esquerdo: aprovar", "§cClique direito: recusar")); head.setItemMeta(meta);
                inv.setItem(slot++, head);
            } catch (Exception ignored) { }
        }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        if (slot == 0) msg(p, "Seu clan não tem solicitações pendentes.");
        p.openInventory(inv);
    }

    private void handleClanApplication(Player leader, String applicant, boolean reject) {
        String clan = getConfig().getString("players." + id(leader) + ".clan");
        if (clan == null || !id(leader).equals(getConfig().getString("clans." + clan + ".leader"))) { msg(leader, "Somente o líder pode administrar solicitações."); return; }
        String path = "clan-applications." + clan + "." + applicant;
        if (!getConfig().contains(path)) { msg(leader, "Essa solicitação não está mais disponível."); openClanRequestsMenu(leader); return; }
        if (reject) { getConfig().set(path, null); saveConfig(); msg(leader, "Solicitação recusada."); openClanRequestsMenu(leader); return; }
        List<String> members = getConfig().getStringList("clans." + clan + ".members");
        if (members.size() >= 8) { msg(leader, "Seu clan já tem 8 membros."); return; }
        if (getConfig().getString("players." + applicant + ".clan") != null) { getConfig().set(path, null); saveConfig(); msg(leader, "Esse jogador já entrou em outro clan."); openClanRequestsMenu(leader); return; }
        members.add(applicant); getConfig().set("clans." + clan + ".members", members);
        getConfig().set("players." + applicant + ".clan", clan); getConfig().set(path, null); saveConfig(); refreshPlayerTags();
        Player joined = null; try { joined = Bukkit.getPlayer(UUID.fromString(applicant)); } catch (Exception ignored) { }
        if (joined != null) msg(joined, "Sua solicitação foi aceita! Você entrou no clan " + clan + ".");
        msg(leader, "Jogador adicionado ao clan."); openClanRequestsMenu(leader);
    }

    private void openClanInvitesMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, CLAN_INVITES_MENU);
        String clan = getConfig().getString("clan-invites." + id(p));
        if (clan != null && getConfig().contains("clans." + clan)) {
            String leader = getConfig().getString("clans." + clan + ".leader");
            try {
                ItemStack head = offlineHead(UUID.fromString(leader), "§eCONVITE: " + clan);
                ItemMeta meta = head.getItemMeta(); meta.setLore(List.of("§aClique esquerdo para aceitar", "§cClique direito para recusar")); head.setItemMeta(meta); inv.setItem(13, head);
            } catch (Exception ignored) { }
        } else inv.setItem(13, button(Material.BARRIER, "§cNenhum convite pendente"));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
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

                sendTpa(p, target, false);
            }

            case "tpahere" -> {
                if (args.length != 1) { msg(p, "Use /tpahere <jogador>"); return true; }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null || target.equals(p)) { msg(p, "Jogador inválido."); return true; }
                sendTpa(p, target, true);
            }

            case "tpaccept" -> {
                UUID requesterId = tpa.remove(p.getUniqueId());
                Long created = tpaCreated.remove(p.getUniqueId());
                boolean targetComesToRequester = tpaHere.getOrDefault(p.getUniqueId(), false);
                tpaHere.remove(p.getUniqueId());

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

                if (targetComesToRequester) p.teleport(requester.getLocation()); else requester.teleport(p.getLocation());
                msg(p, "Pedido aceito.");
                msg(requester, "Seu pedido foi aceito.");
            }

            case "tpdeny" -> {
                tpa.remove(p.getUniqueId());
                tpaCreated.remove(p.getUniqueId());
                tpaHere.remove(p.getUniqueId());
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

            case "missions" -> openMissions(p);

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

    private record Mission(String id, String name, Material icon, int target, double reward, String description) {}

    private List<Mission> missions() {
        return List.of(
                new Mission("stone", "Minerador veterano", Material.STONE, 512, 1200, "Quebre 512 blocos de pedra"),
                new Mission("zombies", "Exterminador", Material.IRON_SWORD, 100, 2500, "Derrote 100 zumbis"),
                new Mission("wheat", "Mestre agricultor", Material.WHEAT, 256, 1000, "Colha 256 trigos maduros"),
                new Mission("diamonds", "Caçador de diamantes", Material.DIAMOND, 64, 5000, "Minere 64 minérios de diamante"),
                new Mission("debris", "Relíquia do Nether", Material.ANCIENT_DEBRIS, 16, 10000, "Minere 16 detritos ancestrais"),
                new Mission("obsidian", "Arquiteto do portal", Material.OBSIDIAN, 256, 3500, "Quebre 256 blocos de obsidiana"),
                new Mission("skeletons", "Quebra-ossos", Material.BOW, 100, 3000, "Derrote 100 esqueletos"),
                new Mission("creepers", "Perigo explosivo", Material.GUNPOWDER, 50, 4000, "Derrote 50 creepers"),
                new Mission("endermen", "Olhar do vazio", Material.ENDER_PEARL, 30, 6000, "Derrote 30 endermen"),
                new Mission("wither", "Desafio das trevas", Material.NETHER_STAR, 1, 15000, "Derrote 1 Wither"),
                new Mission("fish", "Pescador lendário", Material.FISHING_ROD, 128, 2500, "Pesque 128 peixes"),
                new Mission("dragon", "Lenda do End", Material.DRAGON_EGG, 1, 25000, "Derrote o Dragão do End")
        );
    }

    private String missionPath(Player p, String missionId) { return "players." + id(p) + ".missions." + missionId; }

    private void addMissionProgress(Player p, String missionId, int amount) {
        String path = missionPath(p, missionId);
        if (getConfig().getBoolean(path + ".claimed")) return;
        Mission m = missions().stream().filter(x -> x.id().equals(missionId)).findFirst().orElse(null);
        if (m == null) return;
        getConfig().set(path + ".progress", Math.min(m.target(), getConfig().getInt(path + ".progress") + amount));
        saveConfig();
    }

    @EventHandler
    public void missionBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Material type = event.getBlock().getType();
        if (type == Material.STONE) addMissionProgress(p, "stone", 1);
        if ((type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE)) addMissionProgress(p, "diamonds", 1);
        if (type == Material.ANCIENT_DEBRIS) addMissionProgress(p, "debris", 1);
        if (type == Material.OBSIDIAN) addMissionProgress(p, "obsidian", 1);
        if (type == Material.WHEAT && event.getBlock().getBlockData() instanceof org.bukkit.block.data.Ageable age
                && age.getAge() >= age.getMaximumAge()) addMissionProgress(p, "wheat", 1);
    }

    @EventHandler
    public void missionMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        switch (event.getEntityType()) {
            case ZOMBIE -> addMissionProgress(killer, "zombies", 1);
            case SKELETON -> addMissionProgress(killer, "skeletons", 1);
            case CREEPER -> addMissionProgress(killer, "creepers", 1);
            case ENDERMAN -> addMissionProgress(killer, "endermen", 1);
            case WITHER -> addMissionProgress(killer, "wither", 1);
            case ENDER_DRAGON -> addMissionProgress(killer, "dragon", 1);
            default -> { }
        }
    }

    @EventHandler
    public void missionFishing(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            addMissionProgress(event.getPlayer(), "fish", 1);
        }
    }

    private void openMissions(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, MISSIONS);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        List<Mission> all = missions();
        for (int i = 0; i < all.size(); i++) {
            Mission m = all.get(i);
            String path = missionPath(p, m.id());
            int progress = getConfig().getInt(path + ".progress");
            boolean claimed = getConfig().getBoolean(path + ".claimed");
            ItemStack item = button(m.icon(), (claimed ? "§a§lCONCLUÍDA: " : "§e§lMISSÃO: ") + m.id());
            ItemMeta meta = item.getItemMeta();
            meta.setLore(List.of("§7" + m.description(), "§7Progresso: §f" + progress + "/" + m.target(),
                    "§7Recompensa: §a$" + m.reward(), claimed ? "§aRecompensa recebida" :
                    progress >= m.target() ? "§eClique para resgatar" : "§cContinue trabalhando"));
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void claimMission(Player p, String missionId) {
        Mission m = missions().stream().filter(x -> x.id().equals(missionId)).findFirst().orElse(null);
        if (m == null) return;
        String path = missionPath(p, missionId);
        if (getConfig().getBoolean(path + ".claimed")) { msg(p, "Você já resgatou essa missão."); openMissions(p); return; }
        if (getConfig().getInt(path + ".progress") < m.target()) { msg(p, "Missão ainda não concluída."); openMissions(p); return; }
        getConfig().set(path + ".claimed", true);
        addMoney(p, m.reward());
        saveConfig();
        msg(p, "Missão concluída! Você recebeu §a$" + m.reward());
        openMissions(p);
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
                saveConfig(); refreshPlayerTags();

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
                saveConfig(); refreshPlayerTags();

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
                    saveConfig(); refreshPlayerTags();
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
                saveConfig(); refreshPlayerTags();

                msg(p, "Você entrou no clan " + clan);
            }

            case "transferir" -> {
                if (args.length != 2 || ownClan == null) { msg(p, "Use /clan transferir <jogador>"); return; }
                if (!id(p).equals(getConfig().getString("clans." + ownClan + ".leader"))) { msg(p, "Somente o líder pode transferir."); return; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !ownClan.equals(getConfig().getString("players." + id(target) + ".clan"))) { msg(p, "O jogador precisa estar no seu clan e online."); return; }
                getConfig().set("clans." + ownClan + ".leader", id(target));
                saveConfig(); refreshPlayerTags(); msg(p, "Liderança transferida para " + target.getName() + "."); msg(target, "Você agora é líder do clan.");
            }

            case "dissolver" -> {
                if (ownClan == null || !id(p).equals(getConfig().getString("clans." + ownClan + ".leader"))) { msg(p, "Somente o líder pode dissolver o clan."); return; }
                for (String member : getConfig().getStringList("clans." + ownClan + ".members")) getConfig().set("players." + member + ".clan", null);
                getConfig().set("clans." + ownClan, null); saveConfig(); refreshPlayerTags(); msg(p, "Clan dissolvido.");
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
                saveConfig(); refreshPlayerTags();

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
                saveConfig(); refreshPlayerTags();

                msg(p, "Jogador expulso.");
                msg(target, "Você foi removido do clan.");
            }

            default -> msg(p, "Comando inválido. Use /clan ajuda.");
        }
    }
}

