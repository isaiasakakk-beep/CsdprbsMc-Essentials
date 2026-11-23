
package com.csdprbsmc.essentials;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.concurrent.ThreadLocalRandom;

public final class CsdprbsMcEssentials extends JavaPlugin
        implements Listener, CommandExecutor {

    private final Map<UUID, UUID> tpa = new HashMap<>();
    private final Map<UUID, Boolean> tpaHere = new HashMap<>();
    private final Map<UUID, Boolean> tpaMode = new HashMap<>();
    private final Map<UUID, Long> tpaCreated = new HashMap<>();
    private final Map<UUID, String> tagCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> selectedHomes = new HashMap<>();
    private final Map<UUID, ItemStack> pendingSale = new HashMap<>();
    private final Map<UUID, Integer> pendingSaleSlot = new HashMap<>();
    private final Map<UUID, Map<Integer, SaleSelection>> saleMenuSelections = new HashMap<>();
    private final Map<UUID, String> pendingClanApplication = new HashMap<>();
    private final Map<UUID, String> pendingBountyTarget = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingClanColorPart = new HashMap<>();
    private static final String SELL_SELECT = "§8§lSELECIONE O ITEM PARA VENDER";
    private static final String SELL_CONFIRM = "§8§lCONFIRMAR VENDA";
    private static final String SELL_QUANTITY = "§8§lESCOLHA A QUANTIDADE";
    private static final String CLAN_COLORS = "§8§lCORES DO CLAN";
    private static final String MISSIONS = "§8§lMISSÕES E RECOMPENSAS";
    private static final String WEEKLY_MISSIONS = "§8§lMISSÕES SEMANAIS";
    private static final String SPECIFIC_MISSIONS = "§8§lMISSÕES ESPECÍFICAS";
    private static final String CLAN_INVITE_MENU = "§8§lCONVIDAR JOGADORES";
    private static final String CLAN_BROWSER_MENU = "§8§lCLANS DISPONÍVEIS";
    private static final String CLAN_REQUESTS_MENU = "§8§lSOLICITAÇÕES DE CLAN";
    private static final String CLAN_INVITES_MENU = "§8§lCONVITES DE CLAN";
    private static final String BOUNTY_MENU = "§8§lJOGADORES PROCURADOS";
    private static final String BOUNTY_TARGET_MENU = "§8§lESCOLHA O ALVO";
    private static final String BOUNTY_AMOUNT_MENU = "§8§lVALOR DA RECOMPENSA";
    private static final String CONTRACT_MENU = "§8§lCONTRATOS";
    private static final String TREASURE_MENU = "§8§lTESOUROS";
    private Location activeTreasure;

    private NamespacedKey compassKey;

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
    private final Map<UUID, Long> combatUntil = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> combatOpponent = new ConcurrentHashMap<>();
    private final Map<UUID, TradeSession> tradeByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> tradeRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, String>> auctionSlots = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, Integer>> auctionItemSlots = new ConcurrentHashMap<>();
    private final Map<UUID, AuctionSelection> pendingAuction = new ConcurrentHashMap<>();
    private static final String TRADE_MENU = "§6§lTROCA SEGURA";
    private static final String AUCTION_MENU = "§e§lLEILÃO DOS JOGADORES";
    private static final String TRADE_TARGET_MENU = "§6§lESCOLHA QUEM TROCAR";
    private static final String TRADE_REQUEST_MENU = "§6§lPEDIDOS DE TROCA";
    private static final String BANK_MENU = "§e§lBANCO";
    private static final String PROFILE_MENU = "§6§lMEU PERFIL";
    private static final String PAY_TARGET_MENU = "§6§lESCOLHA O JOGADOR";
    private static final String PAY_AMOUNT_MENU = "§6§lVALOR DA TRANSFERÊNCIA";
    private final Map<UUID, UUID> pendingPayTarget = new ConcurrentHashMap<>();
    private static final String AUCTION_ITEM_MENU = "§e§lESCOLHA O ITEM";
    private static final String AUCTION_PRICE_MENU = "§e§lDEFINIR PREÇO";
    private record AuctionSelection(int sourceSlot, ItemStack item) {}
    private static final class TradeSession {
        final UUID a, b; final Map<UUID, List<ItemStack>> offers = new HashMap<>(); final Set<UUID> accepted = new HashSet<>();
        TradeSession(UUID a, UUID b) { this.a=a; this.b=b; offers.put(a,new ArrayList<>()); offers.put(b,new ArrayList<>()); }
        UUID other(UUID id) { return id.equals(a) ? b : a; }
    }
    private static final String SHOP_SEARCH_MENU = "§e§lPESQUISA DA LOJA";
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
                    || Set.of("RECOVERY_COMPASS", "ENCHANTED_GOLDEN_APPLE", "BEDROCK", "BARRIER", "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK", "STRUCTURE_BLOCK", "STRUCTURE_VOID", "JIGSAW", "SPAWNER", "END_PORTAL_FRAME", "DEBUG_STICK", "KNOWLEDGE_BOOK", "LIGHT", "REINFORCED_DEEPSLATE").contains(key)
                    || used.contains(material)) continue;
            String category = shopCategory(material);
            double price = shopPrice(material, category);
            boolean limited = price >= 1200 || key.contains("NETHERITE") || key.contains("TOTEM") || key.equals("ELYTRA") || key.equals("END_CRYSTAL");
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
        if (n.contains("SWORD") || n.contains("PICKAXE") || n.contains("AXE") || n.contains("SHOVEL") || n.contains("HOE") || n.contains("HELMET") || n.contains("CHESTPLATE") || n.contains("LEGGINGS") || n.contains("BOOTS") || n.contains("BOW") || n.contains("CROSSBOW") || n.contains("TRIDENT") || n.contains("SHIELD") || n.contains("ARMOR") || n.contains("MACE")) return "Equipamentos";
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
        if (n.equals("MACE")) return 2500;
        if (category.equals("Equipamentos")) return n.contains("NETHERITE") ? 3200 : n.contains("DIAMOND") ? 900 : n.contains("IRON") ? 180 : 45;
        if (n.equals("BEACON") || n.equals("NETHER_STAR")) return 1500;
        if (n.equals("END_CRYSTAL")) return 800;
        if (n.equals("SHULKER_BOX")) return 1200;
        if (n.equals("RESPAWN_ANCHOR")) return 250;
        if (n.equals("TNT")) return 35;
        if (n.contains("DIAMOND")) return 150;
        if (n.contains("EMERALD")) return 100;
        if (n.contains("GOLD")) return 35;
        if (n.contains("IRON")) return 20;
        if (n.contains("ANCIENT_DEBRIS")) return 900;
        if (category.equals("Alimentos") || category.equals("Agricultura")) return 5;
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

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTask(this, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) { online.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); giveCompass(online); updatePlayerTag(online); }
        });

        String[] commands = {
                "ess", "tpa", "tpahere", "tpaccept", "tpdeny", "procurados", "contratos", "tesouro",
                "sethome", "home", "delhome",
                "setwarp", "warp", "delwarp",
                "clan", "bal", "pay", "shop",
                "topmoney", "topkills", "setmoney", "tagadmin", "reputacao", "bank", "trade", "auction", "shopadmin"
        };

        for (String name : commands) {
            PluginCommand command = getCommand(name);
            if (command != null) {
                command.setExecutor(this);
            }
        }

        getServer().getScheduler().runTaskTimer(this, this::spawnTreasure, 20L * 60 * 30, 20L * 60 * 30);
        getServer().getScheduler().runTaskTimer(this, this::updateCombatTimers, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, this::saveConfig, 20L * 60 * 5, 20L * 60 * 5);
        getLogger().info("CsdprbsMc Essentials iniciado! Loja carregada com " + SHOP_PRODUCTS.size() + " itens.");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    private void handleMainMenuChoice(Player p, int choice) {
        switch (choice) {
            case 0 -> openTpaMenu(p);
            case 1 -> openHomeMenu(p);
            case 2 -> openWarpMenu(p);
            case 3 -> openClanMenu(p);
            case 4 -> { showTop(p, "kills"); showTop(p, "money"); }
            case 5 -> openShop(p);
            case 6 -> openMissions(p);
            case 7 -> openBountyMenu(p);
            case 8 -> openContractMenu(p);
            case 9 -> openTreasureMenu(p);
            case 10 -> teleportBack(p);
            default -> { }
        }
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
        cancelTrade(event.getPlayer(), "Um jogador saiu do servidor.");
        applyCombatLog(event.getPlayer());
        combatUntil.remove(leaving); combatOpponent.remove(leaving);
        selectedHomes.remove(leaving);
        pendingSale.remove(leaving);
        pendingSaleSlot.remove(leaving);
        saleMenuSelections.remove(leaving);
        tpaHere.remove(leaving);
        tpaMode.remove(leaving);
        tradeRequests.remove(leaving); tradeRequests.values().removeIf(value -> value.equals(leaving));
        auctionSlots.remove(leaving);
        shopPages.remove(leaving);
        pendingClanApplication.remove(leaving);
        pendingBountyTarget.remove(leaving);
        pendingClanColorPart.remove(leaving);
        event.getPlayer().setCustomName(null);
        event.getPlayer().setCustomNameVisible(false);
        tpa.entrySet().removeIf(entry -> entry.getKey().equals(leaving) || entry.getValue().equals(leaving));
        tpaCreated.keySet().removeIf(target -> !tpa.containsKey(target));
        tpaHere.keySet().removeIf(target -> !tpa.containsKey(target));
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        Bukkit.getScheduler().runTask(this, () -> {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
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
            double walletStolen = money(victim);
            String victimBankPath = "players." + id(victim) + ".bank";
            double bankStolen = getConfig().getDouble(victimBankPath, 0);
            double stolen = walletStolen + bankStolen;
            money(victim, 0); getConfig().set(victimBankPath, 0); saveConfig();
            if (stolen > 0) { addMoney(killer, stolen); msg(killer, "Você roubou §a$" + stolen + " de " + victim.getName() + " (carteira + banco)."); }
            addReputation(killer, 1); addReputation(victim, -1);
            msg(victim, "§cVocê morreu! Você perdeu seu dinheiro roubado por " + killer.getName() + ". Tente recuperá-lo matando ela de volta!");
            combatUntil.remove(victim.getUniqueId()); combatUntil.remove(killer.getUniqueId());
            combatOpponent.remove(victim.getUniqueId()); combatOpponent.remove(killer.getUniqueId());
        }

        combatUntil.remove(victim.getUniqueId()); combatOpponent.remove(victim.getUniqueId());
        if (killer != null) { combatUntil.remove(killer.getUniqueId()); combatOpponent.remove(killer.getUniqueId()); }

        if (killer != null && !killer.equals(victim)) {
            String bountyPath = "bounties." + victim.getUniqueId();
            double bounty = getConfig().getDouble(bountyPath + ".reward", 0);
            if (bounty > 0) {
                addMoney(killer, bounty);
                getConfig().set(bountyPath, null); saveConfig();
                msg(killer, "§6Você recebeu $" + bounty + " pela recompensa de " + victim.getName() + "!");
                msg(victim, "Sua recompensa foi reclamada por " + killer.getName() + ".");
            }
        }
        Location deathLocation = victim.getLocation();
        String backPath = "death-back." + id(victim);
        getConfig().set(backPath + ".world", deathLocation.getWorld().getName());
        getConfig().set(backPath + ".x", deathLocation.getX());
        getConfig().set(backPath + ".y", deathLocation.getY());
        getConfig().set(backPath + ".z", deathLocation.getZ());
        getConfig().set(backPath + ".yaw", deathLocation.getYaw());
        getConfig().set(backPath + ".pitch", deathLocation.getPitch());
        getConfig().set(backPath + ".time", System.currentTimeMillis());
        saveConfig();
        msg(victim, "§eSeu local de morte foi salvo! Use §aBACK §ena bússola para voltar em até 30 minutos.");
        event.getDrops().removeIf(this::isCompass);
    }

    private String memberTag() { return "§6§l[§e§lMEMBRO§6§l]"; }

    private void addReputation(Player player, int amount) {
        String path = "players." + id(player) + ".reputation";
        getConfig().set(path, getConfig().getInt(path) + amount); saveConfig();
    }
    private boolean inCombat(Player player) {
        Long until = combatUntil.get(player.getUniqueId()); return until != null && until > System.currentTimeMillis();
    }
    private void startCombat(Player a, Player b) {
        long expiry = System.currentTimeMillis() + 15_000L;
        boolean newA = !inCombat(a), newB = !inCombat(b);
        combatUntil.put(a.getUniqueId(), expiry); combatUntil.put(b.getUniqueId(), expiry);
        combatOpponent.put(a.getUniqueId(), b.getUniqueId()); combatOpponent.put(b.getUniqueId(), a.getUniqueId());
        if (newA) { msg(a, "§c§lVocê está em PvP! Fugir resultará em derrota e perda dos itens."); a.sendTitle("§c§lEM COMBATE", "§fNão fuja!", 5, 35, 10); }
        if (newB) { msg(b, "§c§lVocê está em PvP! Fugir resultará em derrota e perda dos itens."); b.sendTitle("§c§lEM COMBATE", "§fNão fuja!", 5, 35, 10); }
    }
    private void updateCombatTimers() {
        long now = System.currentTimeMillis();
        for (UUID uuid : new HashSet<>(combatUntil.keySet())) {
            Long until = combatUntil.get(uuid); if (until == null) continue;
            if (until <= now) { combatUntil.remove(uuid); combatOpponent.remove(uuid); Player p = Bukkit.getPlayer(uuid); if (p != null) msg(p, "§aVocê saiu de combate."); }
            else { Player p = Bukkit.getPlayer(uuid); if (p != null) p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize("§cEm combate: §f" + Math.max(1, (until-now+999)/1000) + "s")); }
        }
    }
    private void applyCombatLog(Player quitter) {
        if (!inCombat(quitter)) return;
        UUID opponentId = combatOpponent.get(quitter.getUniqueId()); Player opponent = opponentId == null ? null : Bukkit.getPlayer(opponentId);
        Location at = quitter.getLocation();
        for (ItemStack item : quitter.getInventory().getStorageContents()) if (item != null && !item.getType().isAir() && !isCompass(item)) at.getWorld().dropItemNaturally(at, item.clone());
        for (ItemStack item : quitter.getInventory().getArmorContents()) if (item != null && !item.getType().isAir()) at.getWorld().dropItemNaturally(at, item.clone());
        ItemStack offhand = quitter.getInventory().getItemInOffHand(); if (offhand != null && !offhand.getType().isAir() && !isCompass(offhand)) at.getWorld().dropItemNaturally(at, offhand.clone());
        quitter.getInventory().clear(); quitter.getInventory().setArmorContents(null); quitter.getInventory().setItemInOffHand(null);
        if (opponent != null) {
            double walletStolen = money(quitter);
            String quitterBankPath = "players." + id(quitter) + ".bank";
            double bankStolen = getConfig().getDouble(quitterBankPath, 0);
            double stolen = walletStolen + bankStolen;
            money(quitter, 0); getConfig().set(quitterBankPath, 0); saveConfig();
            if (stolen > 0) addMoney(opponent, stolen);
            String kp = "players." + id(opponent) + ".kills"; getConfig().set(kp, getConfig().getInt(kp) + 1);
            addReputation(opponent, 1); addReputation(quitter, -1);
            msg(opponent, "§a" + quitter.getName() + " fugiu do combate! Você venceu e recebeu os itens e $" + stolen + ".");
        }
        Bukkit.broadcastMessage("§c" + quitter.getName() + " abandonou um combate e perdeu os itens!");
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void trackCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player direct) attacker = direct;
        else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile && projectile.getShooter() instanceof Player shooter) attacker = shooter;
        if (attacker != null && !attacker.equals(victim)) startCombat(attacker, victim);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockCombatTeleports(PlayerTeleportEvent event) {
        if (!inCombat(event.getPlayer())) return;
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN || cause == PlayerTeleportEvent.TeleportCause.COMMAND || cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || cause == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true); msg(event.getPlayer(), "§cVocê não pode fugir por teleporte enquanto estiver em combate!");
        }
    }

    private String playerTag(Player player) {
        String custom = getConfig().getString("players." + id(player) + ".customTag");
        String member = custom == null ? memberTag() : org.bukkit.ChatColor.translateAlternateColorCodes('&', custom);
        String clan = getConfig().getString("players." + id(player) + ".clan");
        String clanTag = "";
        if (clan != null) {
            String first = getConfig().getString("clans." + clan + ".color1", "§f");
            String second = getConfig().getString("clans." + clan + ".color2", first);
            String upper = clan.toUpperCase(Locale.ROOT);
            StringBuilder styled = new StringBuilder(" §6§l[");
            for (int i = 0; i < upper.length(); i++) styled.append("§l").append(i % 2 == 0 ? first : second).append(upper.charAt(i));
            clanTag = styled.append("§6§l]").toString();
        }
        return member + clanTag;
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
        int best = Integer.MAX_VALUE;
        if (kills > 0 && kills <= 3) best = Math.min(best, kills);
        if (money > 0 && money <= 3) best = Math.min(best, money);
        if (best != Integer.MAX_VALUE) tag += " §6§l[TOP " + best + "]";
        return tag;
    }

    private void updatePlayerTag(Player player) {
        String prefix = playerTag(player), rank = rankingTag(player);
        tagCache.put(player.getUniqueId(), prefix + rank);
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "csd" + player.getUniqueId().toString().replace("-", "").substring(0, 12);
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        player.setScoreboard(board);
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
        String prefix = tagCache.getOrDefault(source.getUniqueId(), memberTag());
        event.renderer((player, displayName, message, viewer) ->
                LegacyComponentSerializer.legacySection()
                        .deserialize(prefix + " §f§l" + player.getName() + "§r: ")
                        .append(message));
    }

    private void openMenu(Player p) {
        if (inCombat(p)) { msg(p, "§cVocê não pode usar a bússola durante o combate!"); return; }
        openInventoryMainMenu(p);
    }

    private void openInventoryMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 36, MENU);

        inv.setItem(10, button(Material.ENDER_PEARL, "§f§6§lTPA§f"));
        inv.setItem(11, button(Material.RED_BED, "§f§e§lHOME§f"));
        inv.setItem(12, button(Material.ENDER_EYE, "§f§6§lWARPS§f"));
        inv.setItem(13, button(Material.NAME_TAG, "§f§e§lCLANS§f"));
        inv.setItem(14, button(Material.GOLD_INGOT, "§f§6§lRANKINGS§f"));
        inv.setItem(15, button(Material.EMERALD, "§f§e§lLOJA/ECONOMIA§f"));
        inv.setItem(16, button(Material.WRITABLE_BOOK, "§f§6§lMISSÕES§f"));
        inv.setItem(19, button(Material.IRON_SWORD, "§f§e§lPROCURADOS§f"));
        inv.setItem(20, button(Material.PAPER, "§f§6§lCONTRATOS§f"));
        inv.setItem(21, button(Material.CHEST, "§f§e§lTESOUROS§f"));
        String backPath = "death-back." + id(p);
        boolean hasBack = getConfig().contains(backPath + ".world") && getConfig().getLong(backPath + ".time") > System.currentTimeMillis() - 30L * 60 * 1000;
        inv.setItem(22, button(Material.RECOVERY_COMPASS, hasBack ? "§a§lBACK - VOLTAR AO LOCAL DA MORTE" : "§7§lBACK - SEM LOCAL DISPONÍVEL"));
        inv.setItem(23, button(Material.GOLD_INGOT, "§f§6§lLEILÃO"));
        inv.setItem(24, button(Material.EMERALD, "§f§e§lTROCAS"));
        inv.setItem(25, button(Material.BARREL, "§f§6§lBANCO"));
        inv.setItem(26, button(Material.PLAYER_HEAD, "§f§e§lMEU PERFIL"));

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
        boolean managed = title.equals(plain(AUCTION_MENU)) || title.equals(plain(SHOP_SEARCH_MENU)) || title.equals(plain(SELL_SELECT)) || title.equals(plain(SELL_CONFIRM)) || title.equals(plain(SELL_QUANTITY))
                || title.equals(plain(CLAN_COLORS)) || title.equals(plain(MISSIONS)) || title.equals(plain(WEEKLY_MISSIONS)) || title.equals(plain(SPECIFIC_MISSIONS))
                || title.equals(plain(MENU)) || title.equals(plain(TRADE_TARGET_MENU)) || title.equals(plain(TRADE_REQUEST_MENU))
                || title.equals(plain(BANK_MENU)) || title.equals(plain(PROFILE_MENU)) || title.equals(plain(PAY_TARGET_MENU)) || title.equals(plain(PAY_AMOUNT_MENU))
                || title.equals(plain(AUCTION_ITEM_MENU)) || title.equals(plain(AUCTION_PRICE_MENU)) || title.equals(plain(TPA_MENU))
                || title.equals(plain(TPA_REQUEST_MENU)) || title.equals(plain(HOME_MENU))
                || title.equals(plain(HOME_ACTION_MENU)) || title.equals(plain(WARP_MENU))
                || title.equals(plain(CLANS)) || title.equals(plain(CLAN_INVITE_MENU)) || title.equals(plain(CLAN_BROWSER_MENU))
                || title.equals(plain(CLAN_REQUESTS_MENU)) || title.equals(plain(CLAN_INVITES_MENU))
                || title.equals(plain(BOUNTY_MENU)) || title.equals(plain(BOUNTY_TARGET_MENU)) || title.equals(plain(BOUNTY_AMOUNT_MENU)) || title.equals(plain(CONTRACT_MENU)) || title.equals(plain(TREASURE_MENU)) || title.equals(plain(SHOP))
                || title.equals(plain(SHOP_CATEGORIES))
                || title.startsWith(plain(SHOP_CATEGORY_PREFIX));
        if (!managed) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = plain(item.getItemMeta().getDisplayName());

        if (name.equals(plain(BACK_BUTTON))) {
            if (title.equals(plain(TRADE_MENU))) { cancelTrade(p, "Troca cancelada."); return; }
            if (title.equals(plain(TRADE_REQUEST_MENU))) { openTradeTargetMenu(p); return; }
            if (title.equals(plain(BANK_MENU)) || title.equals(plain(AUCTION_MENU)) || title.equals(plain(PROFILE_MENU))) { openMenu(p); return; }
            if (title.equals(plain(PAY_TARGET_MENU))) { openBankMenu(p); return; }
            if (title.equals(plain(PAY_AMOUNT_MENU))) { openPayTargetMenu(p); return; }
            if (title.equals(plain(AUCTION_ITEM_MENU))) { openAuction(p); return; }
            if (title.equals(plain(AUCTION_PRICE_MENU))) { openAuctionItemMenu(p); return; }
            if (title.equals(plain(SELL_SELECT)) || title.equals(plain(SELL_CONFIRM)) || title.equals(plain(SELL_QUANTITY))) { pendingSale.remove(p.getUniqueId()); pendingSaleSlot.remove(p.getUniqueId()); saleMenuSelections.remove(p.getUniqueId()); openShop(p); }
            else if (title.equals(plain(BOUNTY_TARGET_MENU))) openBountyMenu(p);
            else if (title.equals(plain(BOUNTY_AMOUNT_MENU))) openBountyTargetMenu(p);
            else if (title.equals(plain(SHOP_SEARCH_MENU))) openShop(p);
            else if (title.equals(plain(CLAN_INVITE_MENU)) || title.equals(plain(CLAN_BROWSER_MENU)) || title.equals(plain(CLAN_REQUESTS_MENU)) || title.equals(plain(CLAN_INVITES_MENU))) openClanMenu(p);
            else if (title.equals(plain(WEEKLY_MISSIONS)) || title.equals(plain(SPECIFIC_MISSIONS))) openMissions(p);
            else if (title.equals(plain(CLAN_COLORS)) || title.equals(plain(MISSIONS)) || title.equals(plain(CONTRACT_MENU)) || title.equals(plain(TREASURE_MENU))) openMenu(p);
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
                case "BACK - VOLTAR AO LOCAL DA MORTE" -> teleportBack(p);
                case "BACK - SEM LOCAL DISPONÍVEL" -> msg(p, "Você não tem um local de morte disponível. O BACK expira em 30 minutos.");
                case "PROCURADOS" -> openBountyMenu(p);
                case "CONTRATOS" -> openContractMenu(p);
                case "TESOUROS" -> openTreasureMenu(p);
                case "LEILÃO" -> openAuction(p);
                case "TROCAS" -> openTradeTargetMenu(p);
                case "BANCO" -> openBankMenu(p);
                case "MEU PERFIL" -> openProfileMenu(p);
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
            else if (name.equals("MISSÕES SEMANAIS")) openChallengeMenu(p, true);
            else if (name.equals("MISSÕES ESPECÍFICAS")) openChallengeMenu(p, false);
        } else if (title.equals(plain(WEEKLY_MISSIONS))) {
            if (name.startsWith("DESAFIO: ") || name.startsWith("RESGATADA: ")) claimChallenge(p, name.substring(name.indexOf(": ") + 2), true);
        } else if (title.equals(plain(SPECIFIC_MISSIONS))) {
            if (name.startsWith("DESAFIO: ") || name.startsWith("RESGATADA: ")) claimChallenge(p, name.substring(name.indexOf(": ") + 2), false);
        } else if (title.equals(plain(TPA_MENU))) {
            if (name.equals("EU IR ATÉ ELE")) { tpaMode.put(p.getUniqueId(), false); msg(p, "Modo selecionado: você irá até o jogador."); openTpaMenu(p); }
            else if (name.equals("ELE VIR ATÉ MIM")) { tpaMode.put(p.getUniqueId(), true); msg(p, "Modo selecionado: o jogador virá até você."); openTpaMenu(p); }
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
        } else if (title.equals(plain(PROFILE_MENU))) {
            // Perfil é somente leitura; o botão de voltar é tratado acima.
        } else if (title.equals(plain(PAY_TARGET_MENU))) {
            if (name.startsWith("JOGADOR: ")) {
                Player target = Bukkit.getPlayerExact(name.substring("JOGADOR: ".length()));
                if (target == null || target.equals(p)) { msg(p, "Esse jogador não está mais disponível."); openPayTargetMenu(p); return; }
                pendingPayTarget.put(p.getUniqueId(), target.getUniqueId());
                openPayAmountMenu(p);
            }
        } else if (title.equals(plain(PAY_AMOUNT_MENU))) {
            if (name.startsWith("VALOR: ")) {
                UUID targetId = pendingPayTarget.remove(p.getUniqueId());
                Player target = targetId == null ? null : Bukkit.getPlayer(targetId);
                if (target == null || target.equals(p)) { msg(p, "O jogador não está mais online."); openPayTargetMenu(p); return; }
                String amountText = name.substring("VALOR: ".length());
                double amount = amountText.equals("TUDO") ? money(p) : Double.parseDouble(amountText);
                if (!Double.isFinite(amount) || amount <= 0 || money(p) < amount) { msg(p, "Saldo insuficiente."); openPayTargetMenu(p); return; }
                money(p, money(p) - amount); addMoney(target, amount);
                msg(p, "§aVocê enviou $" + amount + " para " + target.getName() + ".");
                msg(target, "§aVocê recebeu $" + amount + " de " + p.getName() + ".");
                openBankMenu(p);
            }
        } else if (title.equals(plain(BANK_MENU))) {
            if (name.equals("TRANSFERIR DINHEIRO")) { openPayTargetMenu(p); return; }
            if (name.startsWith("DEPOSITAR: ")) {
                String amountText = name.substring("DEPOSITAR: ".length());
                double amount = amountText.equals("TUDO") ? money(p) : Double.parseDouble(amountText);
                transferBank(p, true, amount); openBankMenu(p);
            } else if (name.startsWith("SACAR: ")) {
                String amountText = name.substring("SACAR: ".length());
                double amount = amountText.equals("TUDO") ? getConfig().getDouble("players." + id(p) + ".bank") : Double.parseDouble(amountText);
                transferBank(p, false, amount); openBankMenu(p);
            }
        } else if (title.equals(plain(TRADE_TARGET_MENU))) {
            if (name.equals("PEDIDOS RECEBIDOS")) openTradeRequestsMenu(p);
            else if (name.startsWith("JOGADOR: ")) handleTradeCommand(p, new String[]{name.substring(9)});
        } else if (title.equals(plain(SHOP_SEARCH_MENU))) {
            if (name.equals("PÁGINA ANTERIOR")) openAllShopProducts(p, shopPages.getOrDefault(p.getUniqueId(), 0) - 1);
            else if (name.equals("PRÓXIMA PÁGINA")) openAllShopProducts(p, shopPages.getOrDefault(p.getUniqueId(), 0) + 1);
            else { ShopProduct product = findProduct(name); if (product != null) buyProduct(p, product, event.isShiftClick() ? 16 : 1); }
        } else if (title.equals(plain(SHOP_CATEGORIES))) {
            if (name.equals("TODOS OS ITENS")) openAllShopProducts(p, 0);
            else openShopCategory(p, name);
        } else if (title.startsWith(plain(SHOP_CATEGORY_PREFIX))) {
            String categoryPage = title.substring(plain(SHOP_CATEGORY_PREFIX).length());
            int marker = categoryPage.lastIndexOf(" #");
            String category = marker < 0 ? categoryPage : categoryPage.substring(0, marker);
            int page = 0;
            if (marker >= 0) { try { page = Integer.parseInt(categoryPage.substring(marker + 2)) - 1; } catch (NumberFormatException ignored) { } }
            if (name.equals("TODOS OS ITENS")) openAllShopProducts(p, 0);
            else if (name.equals("PÁGINA ANTERIOR")) openShopCategory(p, category, page - 1);
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
        } else if (title.equals(plain(TRADE_REQUEST_MENU))) {
            if (name.equals("ACEITAR TROCA")) {
                handleTradeCommand(p, new String[]{"aceitar"});
            } else if (name.equals("RECUSAR TROCA")) {
                handleTradeCommand(p, new String[]{"recusar"});
                openTradeTargetMenu(p);
            }
        } else if (title.equals(plain(CLAN_INVITES_MENU))) {
            if (name.startsWith("CONVITE: ")) { if (event.isRightClick()) { getConfig().set("clan-invites." + id(p), null); saveConfig(); msg(p, "Convite recusado."); openClanInvitesMenu(p); } else { clanCommand(p, new String[]{"aceitar"}); openClanMenu(p); } }
        } else if (title.equals(plain(BOUNTY_MENU))) {
            if (name.equals("CRIAR RECOMPENSA")) openBountyTargetMenu(p);
        } else if (title.equals(plain(BOUNTY_TARGET_MENU))) {
            if (name.startsWith("ALVO: ")) {
                Player target = Bukkit.getPlayerExact(name.substring(6));
                if (target != null && !target.equals(p)) {
                    pendingBountyTarget.put(p.getUniqueId(), target.getUniqueId().toString());
                    openBountyAmountMenu(p);
                }
            }
        } else if (title.equals(plain(BOUNTY_AMOUNT_MENU))) {
            if (name.startsWith("VALOR: ")) {
                String targetId = pendingBountyTarget.remove(p.getUniqueId());
                if (targetId == null) { msg(p, "Seleção expirada."); openBountyTargetMenu(p); return; }
                Player target;
                try { target = Bukkit.getPlayer(UUID.fromString(targetId)); } catch (Exception ex) { target = null; }
                if (target == null || target.equals(p)) { msg(p, "O alvo não está mais disponível."); openBountyTargetMenu(p); return; }
                double amount;
                try { amount = Double.parseDouble(name.substring(7)); } catch (NumberFormatException ex) { return; }
                setBounty(p, target, amount);
                openBountyMenu(p);
            }
        } else if (title.equals(plain(CONTRACT_MENU))) {
            if (name.startsWith("CONTRATO: ")) claimContract(p, name.substring(10));
        } else if (title.equals(plain(TREASURE_MENU))) {
            if (name.equals("ATUALIZAR")) openTreasureMenu(p);
        } else if (title.equals(plain(CLANS))) {
            p.closeInventory();
            if (name.equals("CRIAR CLAN")) {
                createClanFromCompass(p);
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
        if (title.equals(MENU) || title.equals(TRADE_TARGET_MENU) || title.equals(TRADE_REQUEST_MENU) || title.equals(BANK_MENU) || title.equals(PROFILE_MENU) || title.equals(PAY_TARGET_MENU) || title.equals(PAY_AMOUNT_MENU) || title.equals(AUCTION_ITEM_MENU) || title.equals(AUCTION_PRICE_MENU) || title.equals(TRADE_MENU) || title.equals(AUCTION_MENU) || title.equals(SHOP_SEARCH_MENU) || title.equals(SHOP) || title.equals(CLANS) || title.equals(CLAN_INVITE_MENU) || title.equals(CLAN_BROWSER_MENU) || title.equals(CLAN_REQUESTS_MENU) || title.equals(CLAN_INVITES_MENU) || title.equals(BOUNTY_MENU) || title.equals(BOUNTY_TARGET_MENU) || title.equals(BOUNTY_AMOUNT_MENU) || title.equals(CONTRACT_MENU) || title.equals(TREASURE_MENU) || title.equals(SELL_SELECT) || title.equals(SELL_CONFIRM) || title.equals(SELL_QUANTITY) || title.equals(CLAN_COLORS) || title.equals(MISSIONS)
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
        inv.setItem(45, button(Material.ENDER_PEARL, here ? "§7EU IR ATÉ ELE" : "§a§lEU IR ATÉ ELE"));
        inv.setItem(46, button(Material.PLAYER_HEAD, here ? "§a§lELE VIR ATÉ MIM" : "§7ELE VIR ATÉ MIM"));
        inv.setItem(47, button(Material.WRITABLE_BOOK, "§e§lPEDIDOS RECEBIDOS"));
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
        if (inCombat(p)) { msg(p, "§cVocê não pode usar HOME durante o combate!"); return; }
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
        if (inCombat(p)) { msg(p, "§cVocê não pode usar WARPS durante o combate!"); return; }
        Location loc=getConfig().getLocation("warps."+warp); if(loc==null){msg(p,"Warp não encontrada.");return;}
        p.teleport(loc);msg(p,"Teleportado para "+warp+".");p.closeInventory();
    }

    private void openShop(Player p) {
        Inventory inv = Bukkit.createInventory(null, 36, SHOP_CATEGORIES);
        Material[] icons = {Material.DIAMOND, Material.BRICKS, Material.OAK_LOG, Material.WHEAT, Material.COOKED_BEEF, Material.REDSTONE,
                Material.IRON_SWORD, Material.NETHERRACK, Material.PAINTING, Material.CHEST, Material.NETHER_STAR, Material.BARREL};
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23};
        for (int i = 0; i < SHOP_CATEGORIES_LIST.size(); i++) inv.setItem(slots[i], button(icons[i], "§e§l" + SHOP_CATEGORIES_LIST.get(i)));
        inv.setItem(25, button(Material.CHEST, "§e§lTODOS OS ITENS"));
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
            meta.setLore(List.of("§7Preço: §a$" + shopPrice(product), "§eClique: 1 unidade", "§eShift + clique: 16 unidades", shopLimited(product) ? "§cItem especial limitado" : "§7Categoria: " + category));
            item.setItemMeta(meta); inv.setItem(i - start, item);
        }
        inv.setItem(45, button(Material.ARROW, "§e§lPÁGINA ANTERIOR"));
        inv.setItem(46, button(Material.ARROW, "§e§lPRÓXIMA PÁGINA"));
        inv.setItem(47, button(Material.GOLD_INGOT, "§a§lVENDER ITEM DO INVENTÁRIO"));
        inv.setItem(48, button(Material.CHEST, SHOP_SELL_ALL));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        inv.setItem(50, button(Material.PAPER, "§fPágina " + (page + 1) + "/" + pages + " • " + products.size() + " itens"));
        inv.setItem(51, button(Material.CHEST, "§e§lTODOS OS ITENS"));
        p.openInventory(inv);
    }

    private double shopPrice(ShopProduct product) { return getConfig().getDouble("shop.prices." + product.material().name(), product.price()); }
    private boolean shopLimited(ShopProduct product) { return getConfig().getBoolean("shop.limited." + product.material().name(), product.limited()); }

    private ShopProduct findProduct(String name){return SHOP_PRODUCTS.stream().filter(product->product.name().equalsIgnoreCase(name)).findFirst().orElse(null);}
    private void openAllShopProducts(Player p, int requestedPage) {
        List<ShopProduct> products = SHOP_PRODUCTS.stream().sorted(Comparator.comparing(ShopProduct::name, String.CASE_INSENSITIVE_ORDER)).toList();
        int pages = Math.max(1, (products.size() + 44) / 45);
        int page = Math.max(0, Math.min(requestedPage, pages - 1));
        Inventory inv = Bukkit.createInventory(null, 54, SHOP_SEARCH_MENU);
        int start = page * 45;
        for (int i = start; i < Math.min(start + 45, products.size()); i++) {
            ShopProduct product = products.get(i);
            ItemStack item = new ItemStack(product.material());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f§l" + product.name());
            meta.setLore(List.of("§7Preço: §a$" + shopPrice(product), "§eClique: 1 unidade", "§eShift + clique: 16 unidades"));
            item.setItemMeta(meta);
            inv.setItem(i - start, item);
        }
        inv.setItem(45, button(Material.ARROW, "§e§lPÁGINA ANTERIOR"));
        inv.setItem(46, button(Material.ARROW, "§e§lPRÓXIMA PÁGINA"));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        inv.setItem(50, button(Material.PAPER, "§fPágina " + (page + 1) + "/" + pages + " • " + products.size() + " itens"));
        shopPages.put(p.getUniqueId(), page);
        p.openInventory(inv);
    }

    private void buyProduct(Player p, ShopProduct product, int amount) {
        if (product == null || amount < 1) return;
        if (shopLimited(product)) amount = 1;

        int maxStack = product.material().getMaxStackSize();
        if (amount > maxStack) {
            msg(p, "Quantidade acima do limite por compra.");
            return;
        }

        double total = shopPrice(product) * amount;
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
            meta.setLore(List.of("§7Quantidade: §f" + stack.getAmount(), "§7Valor total: §a$" + (Math.floor(shopPrice(product) * .5 * stack.getAmount() * 100) / 100), "§eClique para selecionar"));
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
        double total = Math.floor(shopPrice(product) * .5 * chosen.getAmount() * 100) / 100;
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
        double total = Math.floor(shopPrice(product) * .5 * selected.getAmount() * 100) / 100;
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
            sold += item.getAmount(); total += shopPrice(product) * 0.5 * item.getAmount();
            p.getInventory().setItem(slot, null);
        }
        total = Math.floor(total * 100.0) / 100.0;
        if (sold == 0) { msg(p, "Você não possui itens aceitos pela loja."); return; }
        addMoney(p, total); msg(p, "Você vendeu " + sold + " itens por $" + total + ".");
    }

    private void sendTpa(Player p, Player target, boolean targetComesToRequester) {
        if (inCombat(p) || (target != null && inCombat(target))) { msg(p, "§cNão é possível solicitar TPA durante um combate."); return; }
        if (target == null || target.equals(p) || !target.isOnline()) { msg(p, "Jogador inválido."); return; }
        tpa.put(target.getUniqueId(), p.getUniqueId());
        tpaCreated.put(target.getUniqueId(), System.currentTimeMillis());
        tpaHere.put(target.getUniqueId(), targetComesToRequester);
        msg(p, "Pedido enviado para " + target.getName() + ". Expira em 60 segundos.");
        msg(target, targetComesToRequester ? p.getName() + " pediu para você ir até ele. Abra a bússola > TPA > Pedidos recebidos." : p.getName() + " pediu para ir até você. Abra a bússola > TPA > Pedidos recebidos.");
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

    private void createClanFromCompass(Player p) {
        String ownClan = getConfig().getString("players." + id(p) + ".clan");
        if (ownClan != null) { msg(p, "Você já está em um clan."); openClanMenu(p); return; }
        String base = p.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        if (base.length() < 3) base = "clan" + base;
        if (base.length() > 12) base = base.substring(0, 12);
        String clan = base;
        int suffix = 2;
        while (getConfig().contains("clans." + clan)) {
            String tail = Integer.toString(suffix++);
            clan = base.substring(0, Math.min(base.length(), 16 - tail.length())) + tail;
        }
        getConfig().set("clans." + clan + ".leader", id(p));
        getConfig().set("clans." + clan + ".members", List.of(id(p)));
        getConfig().set("players." + id(p) + ".clan", clan);
        saveConfig(); refreshPlayerTags();
        msg(p, "§aClan criado pela bússola: " + clan + "!");
        openClanMenu(p);
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
        List<? extends Player> candidates = Bukkit.getOnlinePlayers().stream().filter(target -> !target.equals(p)
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

    private void openProfileMenu(Player p) {
        String path = "players." + id(p);
        Inventory inv = Bukkit.createInventory(null, 27, PROFILE_MENU);
        inv.setItem(10, button(Material.GOLD_INGOT, "§eCARTEIRA: §a$" + money(p)));
        inv.setItem(12, button(Material.BARREL, "§6BANCO: §a$" + getConfig().getDouble(path + ".bank")));
        inv.setItem(14, button(Material.NETHER_STAR, "§eREPUTAÇÃO: §f" + getConfig().getInt(path + ".reputation")));
        inv.setItem(16, button(Material.IRON_SWORD, "§cKILLS: §f" + getConfig().getInt(path + ".kills")));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openPayTargetMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, PAY_TARGET_MENU);
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers().stream().filter(other -> !other.equals(p)).sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList()) {
            if (slot >= 45) break;
            inv.setItem(slot++, playerHead(target, "§eJOGADOR: " + target.getName()));
        }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openPayAmountMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, PAY_AMOUNT_MENU);
        double[] amounts = {10, 50, 100, 500, 1000};
        int[] slots = {10, 11, 12, 13, 14};
        for (int i=0;i<amounts.length;i++) inv.setItem(slots[i], button(Material.GOLD_INGOT, "§eVALOR: " + (long)amounts[i]));
        inv.setItem(16, button(Material.EMERALD_BLOCK, "§a§lVALOR: TUDO"));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openBankMenu(Player p) {
        String path = "players." + id(p) + ".bank";
        Inventory inv = Bukkit.createInventory(null, 54, BANK_MENU);
        inv.setItem(4, button(Material.BARREL, "§6§lSALDO DO BANCO: §a$" + getConfig().getDouble(path)));
        inv.setItem(22, button(Material.GOLD_INGOT, "§e§lCARTEIRA: §a$" + money(p)));
        double[] values = {10, 50, 100, 500, 1000};
        int[] deposits = {10, 11, 12, 13, 14}, withdrawals = {28, 29, 30, 31, 32};
        for (int i=0;i<values.length;i++) {
            inv.setItem(deposits[i], button(Material.LIME_DYE, "§aDEPOSITAR: " + (long)values[i]));
            inv.setItem(withdrawals[i], button(Material.YELLOW_DYE, "§eSACAR: " + (long)values[i]));
        }
        inv.setItem(16, button(Material.EMERALD_BLOCK, "§a§lDEPOSITAR: TUDO"));
        inv.setItem(34, button(Material.GOLD_BLOCK, "§e§lSACAR: TUDO"));
        inv.setItem(40, button(Material.EMERALD, "§a§lTRANSFERIR DINHEIRO"));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void transferBank(Player p, boolean deposit, double amount) {
        if (!Double.isFinite(amount) || amount <= 0) { msg(p, "Valor inválido."); return; }
        String path = "players." + id(p) + ".bank";
        double bank = getConfig().getDouble(path);
        if (deposit) {
            if (money(p) + 0.000001 < amount) { msg(p, "Saldo da carteira insuficiente."); return; }
            money(p, money(p) - amount); getConfig().set(path, bank + amount);
        } else {
            if (bank + 0.000001 < amount) { msg(p, "Saldo bancário insuficiente."); return; }
            getConfig().set(path, bank - amount); addMoney(p, amount);
        }
        saveConfig();
        msg(p, deposit ? "§aDepósito realizado: $" + amount : "§aSaque realizado: $" + amount);
    }

    private void handleBankCommand(Player p, String[] args) {
        String path = "players." + id(p) + ".bank";
        if (args.length == 0) { msg(p, "Saldo em banco: §a$" + getConfig().getDouble(path) + " §f| Use /bank depositar <valor> ou /bank sacar <valor>."); return; }
        if (args.length != 2) { msg(p, "Use /bank depositar <valor> ou /bank sacar <valor>."); return; }
        try {
            double amount = Double.parseDouble(args[1].replace(",", "."));
            if (!Double.isFinite(amount) || amount <= 0) { msg(p, "Valor inválido."); return; }
            double bank = getConfig().getDouble(path);
            if (args[0].equalsIgnoreCase("depositar")) {
                if (money(p) < amount) { msg(p, "Saldo em mãos insuficiente."); return; }
                money(p, money(p)-amount); getConfig().set(path, bank+amount);
            } else if (args[0].equalsIgnoreCase("sacar")) {
                if (bank < amount) { msg(p, "Saldo bancário insuficiente."); return; }
                getConfig().set(path, bank-amount); addMoney(p, amount);
            } else { msg(p, "Use depositar ou sacar."); return; }
            saveConfig(); msg(p, "Operação concluída. Banco: §a$" + getConfig().getDouble(path));
        } catch (NumberFormatException ex) { msg(p, "Valor inválido."); }
    }

    private void openTradeTargetMenu(Player p) {
        if (inCombat(p)) { msg(p, "§cVocê não pode iniciar uma troca durante o combate."); return; }
        Inventory inv = Bukkit.createInventory(null, 54, TRADE_TARGET_MENU);
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers().stream().filter(other -> !other.equals(p) && !inCombat(other)).sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList()) {
            if (slot >= 45) break;
            ItemStack head = playerHead(target, "§eJOGADOR: " + target.getName());
            ItemMeta meta = head.getItemMeta(); meta.setLore(List.of("§7Clique para enviar uma solicitação de troca.")); head.setItemMeta(meta);
            inv.setItem(slot++, head);
        }
        inv.setItem(45, button(Material.WRITABLE_BOOK, "§e§lPEDIDOS RECEBIDOS"));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        if (slot == 0) msg(p, "Não há jogadores disponíveis para trocar agora.");
        p.openInventory(inv);
    }

    private void openTradeRequestsMenu(Player p) {
        UUID requesterId = tradeRequests.get(p.getUniqueId());
        Player requester = requesterId == null ? null : Bukkit.getPlayer(requesterId);
        Inventory inv = Bukkit.createInventory(null, 27, TRADE_REQUEST_MENU);
        if (requester != null) {
            ItemStack head = playerHead(requester, "§ePEDIDO DE: " + requester.getName());
            ItemMeta meta = head.getItemMeta();
            meta.setLore(List.of("§7Esse jogador quer negociar itens com você."));
            head.setItemMeta(meta);
            inv.setItem(13, head);
            inv.setItem(11, button(Material.LIME_WOOL, "§a§lACEITAR TROCA"));
            inv.setItem(15, button(Material.RED_WOOL, "§c§lRECUSAR TROCA"));
        } else {
            tradeRequests.remove(p.getUniqueId());
            inv.setItem(13, button(Material.BARRIER, "§cNENHUM PEDIDO PENDENTE"));
        }
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void handleTradeCommand(Player p, String[] args) {
        if (args.length == 0) { msg(p, "Use /trade <jogador>, /trade aceitar ou /trade recusar."); return; }
        if (args[0].equalsIgnoreCase("aceitar")) {
            UUID requesterId = tradeRequests.remove(p.getUniqueId());
            Player requester = requesterId == null ? null : Bukkit.getPlayer(requesterId);
            if (requester == null || inCombat(p) || (requester != null && inCombat(requester)) || tradeByPlayer.containsKey(p.getUniqueId()) || tradeByPlayer.containsKey(requester.getUniqueId())) { msg(p, "Solicitação expirada ou um jogador está indisponível para trocar."); return; }
            TradeSession session = new TradeSession(requester.getUniqueId(), p.getUniqueId());
            tradeByPlayer.put(session.a, session); tradeByPlayer.put(session.b, session);
            openTradeMenu(requester); openTradeMenu(p); msg(requester, p.getName() + " aceitou sua solicitação de troca.");
        } else if (args[0].equalsIgnoreCase("recusar")) {
            UUID requesterId = tradeRequests.remove(p.getUniqueId()); Player requester = requesterId == null ? null : Bukkit.getPlayer(requesterId);
            msg(p, "Solicitação recusada."); if (requester != null) msg(requester, p.getName() + " recusou a troca.");
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || target.equals(p) || tradeByPlayer.containsKey(p.getUniqueId()) || tradeByPlayer.containsKey(target.getUniqueId()) || inCombat(p) || inCombat(target)) { msg(p, "Jogador indisponível para troca."); return; }
            tradeRequests.put(target.getUniqueId(), p.getUniqueId()); msg(p, "Solicitação enviada para " + target.getName() + ".");
            msg(target, p.getName() + " quer trocar itens com você. Abra a bússola > Trocas > Pedidos recebidos.");
        }
    }

    private void openTradeMenu(Player p) {
        TradeSession session = tradeByPlayer.get(p.getUniqueId()); if (session == null) return;
        UUID own = p.getUniqueId(), other = session.other(own); Player partner = Bukkit.getPlayer(other);
        Inventory inv = Bukkit.createInventory(null, 54, TRADE_MENU);
        List<ItemStack> ownOffer = session.offers.get(own), otherOffer = session.offers.get(other);
        for (int i=0;i<Math.min(18,ownOffer.size());i++) inv.setItem(i, ownOffer.get(i).clone());
        for (int i=0;i<Math.min(18,otherOffer.size());i++) inv.setItem(27+i, otherOffer.get(i).clone());
        inv.setItem(45, button(Material.PAPER, "§e§lSEUS ITENS"));
        inv.setItem(47, button(Material.GOLD_INGOT, "§6§lPEDIR MAIS"));
        inv.setItem(49, button(session.accepted.contains(own) ? Material.LIME_WOOL : Material.GREEN_WOOL, session.accepted.contains(own) ? "§a§lACEITE REGISTRADO" : "§a§lACEITAR TROCA"));
        inv.setItem(51, button(Material.PAPER, "§e§lOFERTA DE " + (partner == null ? "JOGADOR" : partner.getName())));
        inv.setItem(53, button(Material.RED_WOOL, "§c§lRECUSAR TROCA"));
        p.openInventory(inv);
    }

    private void giveTradeItems(Player player, List<ItemStack> items) {
        if (player == null) return;
        for (ItemStack item : items) {
            Map<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
            left.values().forEach(extra -> player.getWorld().dropItemNaturally(player.getLocation(), extra));
        }
    }

    private void cancelTrade(Player player, String reason) {
        TradeSession session = tradeByPlayer.get(player.getUniqueId()); if (session == null) return;
        Player a = Bukkit.getPlayer(session.a), b = Bukkit.getPlayer(session.b);
        giveTradeItems(a, session.offers.get(session.a)); giveTradeItems(b, session.offers.get(session.b));
        tradeByPlayer.remove(session.a); tradeByPlayer.remove(session.b);
        if (a != null) { a.closeInventory(); msg(a, reason); }
        if (b != null) { b.closeInventory(); msg(b, reason); }
    }

    private void finishTrade(TradeSession session) {
        Player a = Bukkit.getPlayer(session.a), b = Bukkit.getPlayer(session.b);
        if (a == null || b == null) { if (a != null) cancelTrade(a, "Troca cancelada: jogador desconectou."); else if (b != null) cancelTrade(b, "Troca cancelada: jogador desconectou."); return; }
        giveTradeItems(a, session.offers.get(session.b)); giveTradeItems(b, session.offers.get(session.a));
        tradeByPlayer.remove(session.a); tradeByPlayer.remove(session.b);
        a.closeInventory(); b.closeInventory(); msg(a, "§aTroca concluída!"); msg(b, "§aTroca concluída!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void tradeInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p) || !plain(event.getView().getTitle()).equals(plain(TRADE_MENU))) return;
        TradeSession session = tradeByPlayer.get(p.getUniqueId()); if (session == null) { event.setCancelled(true); return; }
        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            int slot = event.getSlot(); if (slot < 0 || slot >= 36) return;
            ItemStack clicked = p.getInventory().getItem(slot); if (clicked == null || clicked.getType().isAir()) return;
            if (session.offers.get(p.getUniqueId()).size() >= 18) { msg(p, "Limite de 18 espaços de oferta por jogador."); return; }
            session.offers.get(p.getUniqueId()).add(clicked.clone()); p.getInventory().setItem(slot, null);
            session.accepted.clear(); Player other = Bukkit.getPlayer(session.other(p.getUniqueId())); if (other != null) openTradeMenu(other); openTradeMenu(p); return;
        }
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getSlot();
        if (slot >= 0 && slot < 18) {
            List<ItemStack> offer = session.offers.get(p.getUniqueId());
            if (slot < offer.size()) { giveTradeItems(p, List.of(offer.remove(slot))); session.accepted.clear(); Player other=Bukkit.getPlayer(session.other(p.getUniqueId())); if(other!=null)openTradeMenu(other);openTradeMenu(p); }
            return;
        }
        String name = event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() ? "" : plain(event.getCurrentItem().getItemMeta().getDisplayName());
        if (name.equals("PEDIR MAIS")) {
            Player other = Bukkit.getPlayer(session.other(p.getUniqueId())); session.accepted.clear();
            if (other != null) msg(other, "§e" + p.getName() + " pediu mais itens em troca dessa oferta!");
            openTradeMenu(p); if (other != null) openTradeMenu(other);
        } else if (name.equals("ACEITAR TROCA")) {
            session.accepted.add(p.getUniqueId()); Player other=Bukkit.getPlayer(session.other(p.getUniqueId()));
            if (other != null) msg(other, "§a" + p.getName() + " aceitou a oferta.");
            if (session.accepted.size() == 2) finishTrade(session); else openTradeMenu(p);
        } else if (name.equals("RECUSAR TROCA")) cancelTrade(p, "Troca recusada.");
    }

    @EventHandler
    public void tradeInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player p) || !plain(event.getView().getTitle()).equals(plain(TRADE_MENU))) return;
        Bukkit.getScheduler().runTask(this, () -> {
            if (tradeByPlayer.containsKey(p.getUniqueId()) && !plain(p.getOpenInventory().getTitle()).equals(plain(TRADE_MENU))) cancelTrade(p, "Troca cancelada porque um jogador fechou o menu.");
        });
    }

    private void handleAuctionCommand(Player p, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("vender")) {
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir() || isCompass(hand)) { msg(p, "Segure o item que deseja anunciar."); return; }
            try {
                double price = Double.parseDouble(args[1].replace(",", "."));
                if (!Double.isFinite(price) || price <= 0) { msg(p, "Preço inválido."); return; }
                String key = UUID.randomUUID().toString(); String path = "auction.listings." + key;
                getConfig().set(path + ".owner", id(p)); getConfig().set(path + ".name", p.getName()); getConfig().set(path + ".price", price); getConfig().set(path + ".item", hand.clone());
                p.getInventory().setItemInMainHand(null); saveConfig(); msg(p, "Item anunciado por $" + price + ". Código: " + key.substring(0,8)); openAuction(p);
            } catch (NumberFormatException ex) { msg(p, "Use /auction vender <preço>."); }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("cancelar")) {
            String key = findAuctionKey(args[1]); if (key == null) { msg(p, "Anúncio não encontrado."); return; }
            String path = "auction.listings." + key;
            if (!id(p).equals(getConfig().getString(path + ".owner"))) { msg(p, "Você só pode cancelar seus próprios anúncios."); return; }
            ItemStack item = getConfig().getItemStack(path + ".item"); if (item != null) giveTradeItems(p, List.of(item));
            getConfig().set(path, null); saveConfig(); msg(p, "Anúncio cancelado.");
        } else openAuction(p);
    }

    private String findAuctionKey(String prefix) {
        ConfigurationSection section = getConfig().getConfigurationSection("auction.listings"); if (section == null) return null;
        for (String key : section.getKeys(false)) if (key.startsWith(prefix)) return key;
        return null;
    }

    private void openAuction(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, AUCTION_MENU); Map<Integer,String> slots = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("auction.listings"); int slot=0;
        if (section != null) for (String key : section.getKeys(false)) {
            if (slot >= 45) break; String path="auction.listings."+key; ItemStack item=getConfig().getItemStack(path+".item");
            if (item == null) continue; ItemStack display=item.clone(); ItemMeta meta=display.getItemMeta();
            List<String> lore=meta.hasLore()?new ArrayList<>(meta.getLore()):new ArrayList<>();
            lore.add("§7Vendedor: §f"+getConfig().getString(path+".name","Desconhecido")); lore.add("§7Preço: §a$"+getConfig().getDouble(path+".price")); lore.add("§8ID: "+key.substring(0,8)); lore.add("§eClique para comprar");
            meta.setLore(lore); display.setItemMeta(meta); inv.setItem(slot,display); slots.put(slot,key); slot++;
        }
        auctionSlots.put(p.getUniqueId(),slots);
        inv.setItem(45,button(Material.GOLD_INGOT,"§a§lANUNCIAR ITEM"));
        inv.setItem(49,button(Material.BARRIER,BACK_BUTTON)); p.openInventory(inv);
    }

    private void openAuctionItemMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, AUCTION_ITEM_MENU);
        Map<Integer,Integer> slots = new HashMap<>();
        for (int source=0; source<36; source++) {
            ItemStack item = p.getInventory().getItem(source);
            if (item == null || item.getType().isAir() || isCompass(item)) continue;
            ItemStack display = item.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("§aClique para anunciar esta pilha");
            meta.setLore(lore); display.setItemMeta(meta);
            int target=slots.size(); if(target>=45) break;
            inv.setItem(target,display); slots.put(target,source);
        }
        auctionItemSlots.put(p.getUniqueId(),slots);
        inv.setItem(49,button(Material.BARRIER,BACK_BUTTON));
        p.openInventory(inv);
    }

    private void openAuctionPriceMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, AUCTION_PRICE_MENU);
        double[] prices={10,50,100,250,500,1000,5000}; int[] slots={10,11,12,13,14,15,16};
        for(int i=0;i<prices.length;i++) inv.setItem(slots[i],button(Material.GOLD_INGOT,"§ePREÇO: "+(long)prices[i]));
        inv.setItem(22,button(Material.BARRIER,BACK_BUTTON));
        p.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void auctionItemSelectionClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        String title=plain(event.getView().getTitle());
        if(title.equals(plain(AUCTION_ITEM_MENU))) {
            event.setCancelled(true);
            if(event.getClickedInventory()!=event.getView().getTopInventory()) return;
            Integer source=auctionItemSlots.getOrDefault(p.getUniqueId(),Map.of()).get(event.getSlot());
            if(source==null) return;
            ItemStack original=p.getInventory().getItem(source);
            if(original==null||original.getType().isAir()||isCompass(original)){msg(p,"Esse item não está mais disponível.");openAuctionItemMenu(p);return;}
            pendingAuction.put(p.getUniqueId(),new AuctionSelection(source,original.clone()));
            openAuctionPriceMenu(p);
        } else if(title.equals(plain(AUCTION_PRICE_MENU))) {
            event.setCancelled(true);
            if(event.getClickedInventory()!=event.getView().getTopInventory()) return;
            ItemStack clicked=event.getCurrentItem();
            if(clicked==null||!clicked.hasItemMeta()||!clicked.getItemMeta().hasDisplayName()) return;
            String label=plain(clicked.getItemMeta().getDisplayName()); if(!label.startsWith("PREÇO: ")) return;
            double price; try{price=Double.parseDouble(label.substring(7));}catch(NumberFormatException ex){return;}
            AuctionSelection selection=pendingAuction.remove(p.getUniqueId());
            if(selection==null){msg(p,"Seleção expirada. Escolha o item novamente.");openAuctionItemMenu(p);return;}
            ItemStack current=p.getInventory().getItem(selection.sourceSlot());
            if(current==null||!current.isSimilar(selection.item())||current.getAmount()<selection.item().getAmount()){
                msg(p,"Seu inventário mudou. O anúncio foi cancelado.");openAuctionItemMenu(p);return;
            }
            p.getInventory().setItem(selection.sourceSlot(),null);
            String key=UUID.randomUUID().toString(), listingPath="auction.listings."+key;
            getConfig().set(listingPath+".owner",id(p)); getConfig().set(listingPath+".name",p.getName());
            getConfig().set(listingPath+".price",price); getConfig().set(listingPath+".item",selection.item().clone());
            saveConfig(); msg(p,"§aAnúncio criado por $"+price+"!"); openAuction(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void auctionInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p) || !plain(event.getView().getTitle()).equals(plain(AUCTION_MENU))) return;
        event.setCancelled(true); if (event.getClickedInventory()!=event.getView().getTopInventory()) return;
        if (event.getSlot()==45) { openAuctionItemMenu(p); return; }
        String key=auctionSlots.getOrDefault(p.getUniqueId(),Map.of()).get(event.getSlot()); if(key==null)return;
        String path="auction.listings."+key; String owner=getConfig().getString(path+".owner");
        if (owner==null) { openAuction(p); return; }
        if (owner.equals(id(p))) {
            ItemStack ownItem=getConfig().getItemStack(path+".item");
            if(ownItem!=null) giveTradeItems(p,List.of(ownItem));
            getConfig().set(path,null); saveConfig();
            msg(p,"§eSeu anúncio foi cancelado e o item foi devolvido."); openAuction(p); return;
        }
        double price=getConfig().getDouble(path+".price"); if(money(p)<price){msg(p,"Saldo insuficiente.");return;}
        ItemStack item=getConfig().getItemStack(path+".item"); if(item==null){getConfig().set(path,null);saveConfig();openAuction(p);return;}
        if(p.getInventory().firstEmpty()==-1){msg(p,"Libere espaço no inventário antes de comprar.");return;}
        money(p,money(p)-price); getConfig().set("players."+owner+".money",getConfig().getDouble("players."+owner+".money",100)+price); refreshPlayerTags();
        Player seller=Bukkit.getPlayer(UUID.fromString(owner)); if(seller!=null)msg(seller,"§aSeu anúncio foi vendido por $"+price+".");
        p.getInventory().addItem(item); getConfig().set(path,null); saveConfig(); msg(p,"§aCompra realizada!"); openAuction(p);
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
            case "ess" -> {
                if (args.length == 2 && args[0].equalsIgnoreCase("menu")) {
                    try { handleMainMenuChoice(p, Integer.parseInt(args[1])); }
                    catch (NumberFormatException ex) { msg(p, "Opção inválida."); }
                } else openMenu(p);
            }

            case "bal" -> msg(p, "Seu saldo: §a$" + money(p) + " §f| Reputação: §e" + getConfig().getInt("players." + id(p) + ".reputation"));
            case "reputacao" -> msg(p, "Sua reputação: §e" + getConfig().getInt("players." + id(p) + ".reputation"));

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
                if (inCombat(p)) { msg(p, "§cVocê não pode aceitar TPA durante um combate."); return true; }
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
                if (inCombat(requester)) { msg(p, "§cO outro jogador está em combate. O teleporte foi cancelado."); return true; }

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

            case "procurados" -> openBountyMenu(p);

            case "contratos" -> openContractMenu(p);

            case "tesouro" -> openTreasureMenu(p);

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

            case "shopadmin" -> {
                if (!sender.hasPermission("csdprbsmc.admin")) { sender.sendMessage("Você não tem permissão."); return true; }
                if (args.length != 3) { sender.sendMessage("Use /shopadmin preco <MATERIAL> <valor> ou /shopadmin limite <MATERIAL> <true|false>"); return true; }
                Material material = Material.matchMaterial(args[1]); if (material == null || !material.isItem()) { sender.sendMessage("Material inválido."); return true; }
                if (args[0].equalsIgnoreCase("preco")) {
                    try { double value = Double.parseDouble(args[2].replace(",", ".")); if (!Double.isFinite(value) || value <= 0) throw new NumberFormatException(); getConfig().set("shop.prices." + material.name(), value); saveConfig(); sender.sendMessage("Preço atualizado para " + material.name() + ": $" + value); }
                    catch (NumberFormatException ex) { sender.sendMessage("Preço inválido."); }
                } else if (args[0].equalsIgnoreCase("limite")) {
                    if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) { sender.sendMessage("Use true ou false."); return true; }
                    getConfig().set("shop.limited." + material.name(), Boolean.parseBoolean(args[2])); saveConfig(); sender.sendMessage("Limite atualizado para " + material.name() + ".");
                } else sender.sendMessage("Use preco ou limite.");
            }
            case "bank" -> handleBankCommand(p, args);
            case "trade" -> handleTradeCommand(p, args);
            case "auction" -> handleAuctionCommand(p, args);
            case "tagadmin" -> {
                if (!sender.hasPermission("csdprbsmc.admin")) { sender.sendMessage("Você não tem permissão."); return true; }
                if (args.length < 2) { sender.sendMessage("Use /tagadmin <jogador> <tag...> ou /tagadmin <jogador> reset"); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]); String tagPath = "players." + target.getUniqueId() + ".customTag";
                if (args[1].equalsIgnoreCase("reset")) getConfig().set(tagPath, null);
                else { String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length)); if (raw.length() > 32 || raw.contains("\n") || raw.contains("\r")) { sender.sendMessage("Tag inválida: máximo 32 caracteres."); return true; } getConfig().set(tagPath, raw.replace('§', '&')); }
                getConfig().set("players." + target.getUniqueId() + ".name", args[0]); saveConfig(); Player online = target.getPlayer(); if (online != null) updatePlayerTag(online); sender.sendMessage("Tag atualizada para " + args[0] + ".");
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
    }

    @EventHandler
    public void missionBlockBreak(BlockBreakEvent event) {
        if (activeTreasure != null && event.getBlock().getLocation().equals(activeTreasure)) {
            event.setCancelled(true); msg(event.getPlayer(), "Esse baú faz parte de um evento. Abra-o para coletar o tesouro."); return;
        }
        Player p = event.getPlayer();
        Material type = event.getBlock().getType();
        if (type == Material.STONE) { addMissionProgress(p, "stone", 1); addContractProgress(p, "stone", 1); addChallengeProgress(p, "stone", 1); }
        if ((type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE)) { addMissionProgress(p, "diamonds", 1); addChallengeProgress(p, "diamonds", 1); }
        if (type == Material.ANCIENT_DEBRIS) { addMissionProgress(p, "debris", 1); addChallengeProgress(p, "debris", 1); }
        if (type == Material.OBSIDIAN) addMissionProgress(p, "obsidian", 1);
        if (type == Material.WHEAT && event.getBlock().getBlockData() instanceof org.bukkit.block.data.Ageable age
                && age.getAge() >= age.getMaximumAge()) addMissionProgress(p, "wheat", 1);
    }

    @EventHandler
    public void missionMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        switch (event.getEntityType()) {
            case ZOMBIE -> { addMissionProgress(killer, "zombies", 1); addContractProgress(killer, "zombies", 1); addChallengeProgress(killer, "zombies", 1); }
            case SKELETON -> addMissionProgress(killer, "skeletons", 1);
            case CREEPER -> addMissionProgress(killer, "creepers", 1);
            case ENDERMAN -> { addMissionProgress(killer, "endermen", 1); addChallengeProgress(killer, "endermen", 1); }
            case WITHER -> { addMissionProgress(killer, "wither", 1); addChallengeProgress(killer, "wither", 1); }
            case ENDER_DRAGON -> addMissionProgress(killer, "dragon", 1);
            default -> { }
        }
    }

    @EventHandler
    public void missionFishing(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            addMissionProgress(event.getPlayer(), "fish", 1);
            addContractProgress(event.getPlayer(), "fish", 1);
            addChallengeProgress(event.getPlayer(), "fish", 1);
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
        inv.setItem(37, button(Material.CLOCK, "§e§lMISSÕES SEMANAIS"));
        inv.setItem(43, button(Material.NETHER_STAR, "§6§lMISSÕES ESPECÍFICAS"));
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private record Challenge(String id, String name, Material icon, int target, double reward, String description) {}

    private List<Challenge> weeklyChallenges() {
        return List.of(new Challenge("stone", "Mineiro da semana", Material.STONE, 300, 1800, "Quebre 300 blocos de pedra"),
                new Challenge("zombies", "Caçador da semana", Material.IRON_SWORD, 50, 2200, "Derrote 50 zumbis"),
                new Challenge("diamonds", "Fortuna semanal", Material.DIAMOND, 16, 3000, "Minere 16 minérios de diamante"),
                new Challenge("fish", "Pescaria semanal", Material.FISHING_ROD, 20, 1200, "Pesque 20 peixes"));
    }

    private List<Challenge> specificChallenges() {
        return List.of(new Challenge("debris", "Caçador de relíquias", Material.ANCIENT_DEBRIS, 8, 4500, "Minere 8 detritos ancestrais"),
                new Challenge("endermen", "Olhos do vazio", Material.ENDER_PEARL, 15, 3500, "Derrote 15 endermen"),
                new Challenge("wither", "Quebra-maldições", Material.NETHER_STAR, 1, 8000, "Derrote 1 Wither"));
    }

    private String weeklyKey() { return LocalDate.now().with(DayOfWeek.MONDAY).toString(); }
    private String weeklyPath(Player p, String mission) { return "players." + id(p) + ".weekly." + weeklyKey() + "." + mission; }
    private String specificPath(Player p, String mission) { return "players." + id(p) + ".specific." + mission; }

    private void addChallengeProgress(Player p, String id, int amount) {
        String weekly = weeklyPath(p, id);
        Challenge weeklyChallenge = weeklyChallenges().stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
        if (weeklyChallenge != null && !getConfig().getBoolean(weekly + ".claimed"))
            getConfig().set(weekly + ".progress", Math.min(weeklyChallenge.target(), getConfig().getInt(weekly + ".progress") + amount));
        String specific = specificPath(p, id);
        Challenge specialChallenge = specificChallenges().stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
        if (specialChallenge != null && !getConfig().getBoolean(specific + ".claimed"))
            getConfig().set(specific + ".progress", Math.min(specialChallenge.target(), getConfig().getInt(specific + ".progress") + amount));
    }

    private void openChallengeMenu(Player p, boolean weekly) {
        List<Challenge> challenges = weekly ? weeklyChallenges() : specificChallenges();
        String title = weekly ? WEEKLY_MISSIONS : SPECIFIC_MISSIONS;
        Inventory inv = Bukkit.createInventory(null, 36, title);
        int[] slots = weekly ? new int[]{10, 12, 14, 16} : new int[]{11, 13, 15};
        for (int i = 0; i < challenges.size(); i++) {
            Challenge c = challenges.get(i); String path = weekly ? weeklyPath(p, c.id()) : specificPath(p, c.id());
            int progress = getConfig().getInt(path + ".progress"); boolean claimed = getConfig().getBoolean(path + ".claimed");
            ItemStack item = button(c.icon(), (claimed ? "§a§lRESGATADA: " : "§e§lDESAFIO: ") + c.id());
            ItemMeta meta = item.getItemMeta(); meta.setLore(List.of("§7" + c.description(), "§7Progresso: §f" + progress + "/" + c.target(), "§7Recompensa: §a$" + c.reward(), claimed ? "§aRecompensa recebida" : progress >= c.target() ? "§eClique para resgatar" : "§cEm andamento")); item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        if (weekly) inv.setItem(4, button(Material.CLOCK, "§7Reinicia toda segunda-feira"));
        else inv.setItem(4, button(Material.NETHER_STAR, "§7Desafios especiais permanentes"));
        inv.setItem(31, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void claimChallenge(Player p, String missionId, boolean weekly) {
        List<Challenge> list = weekly ? weeklyChallenges() : specificChallenges();
        Challenge c = list.stream().filter(x -> x.id().equals(missionId)).findFirst().orElse(null);
        if (c == null) return;
        String path = weekly ? weeklyPath(p, missionId) : specificPath(p, missionId);
        if (getConfig().getBoolean(path + ".claimed") || getConfig().getInt(path + ".progress") < c.target()) {
            msg(p, "Esse desafio ainda não pode ser resgatado."); openChallengeMenu(p, weekly); return;
        }
        getConfig().set(path + ".claimed", true); addMoney(p, c.reward()); saveConfig();
        msg(p, "Desafio concluído! Você recebeu §a$" + c.reward()); openChallengeMenu(p, weekly);
    }

    private void teleportBack(Player p) {
        if (inCombat(p)) { msg(p, "§cVocê não pode usar BACK durante o combate!"); return; }
        String path = "death-back." + id(p);
        long timestamp = getConfig().getLong(path + ".time", 0);
        if (timestamp == 0 || timestamp < System.currentTimeMillis() - 30L * 60 * 1000) {
            getConfig().set(path, null); saveConfig(); msg(p, "Seu BACK expirou ou não existe um local de morte salvo."); openMenu(p); return;
        }
        World world = Bukkit.getWorld(getConfig().getString(path + ".world", ""));
        if (world == null) { msg(p, "O mundo onde você morreu não está disponível."); return; }
        Location destination = new Location(world, getConfig().getDouble(path + ".x"), getConfig().getDouble(path + ".y"), getConfig().getDouble(path + ".z"), (float) getConfig().getDouble(path + ".yaw"), (float) getConfig().getDouble(path + ".pitch"));
        p.closeInventory();
        p.teleportAsync(destination).thenAccept(success -> Bukkit.getScheduler().runTask(this, () -> {
            if (success) { getConfig().set(path, null); saveConfig(); msg(p, "§aVocê voltou ao local onde morreu!"); }
            else msg(p, "§cNão foi possível voltar ao local da sua morte.");
        }));
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

    private record DailyContract(String id, String title, Material icon, int target, double reward, String description) {}

    private List<DailyContract> dailyContracts() {
        return List.of(new DailyContract("stone", "Minerador diário", Material.STONE, 64, 250, "Quebre 64 blocos de pedra"),
                new DailyContract("zombies", "Caçador diário", Material.IRON_SWORD, 10, 350, "Derrote 10 zumbis"),
                new DailyContract("fish", "Pescador diário", Material.FISHING_ROD, 5, 200, "Pesque 5 peixes"));
    }

    private void ensureDailyContracts(Player p) {
        String base = "players." + id(p) + ".daily-contracts";
        String today = LocalDate.now().toString();
        if (!today.equals(getConfig().getString(base + ".date"))) {
            getConfig().set(base + ".date", today);
            for (DailyContract contract : dailyContracts()) { getConfig().set(base + "." + contract.id() + ".progress", 0); getConfig().set(base + "." + contract.id() + ".claimed", false); }
            saveConfig();
        }
    }

    private void addContractProgress(Player p, String contractId, int amount) {
        ensureDailyContracts(p);
        String path = "players." + id(p) + ".daily-contracts." + contractId;
        if (getConfig().getBoolean(path + ".claimed")) return;
        DailyContract contract = dailyContracts().stream().filter(c -> c.id().equals(contractId)).findFirst().orElse(null);
        if (contract == null) return;
        getConfig().set(path + ".progress", Math.min(contract.target(), getConfig().getInt(path + ".progress") + amount));
    }

    private void openContractMenu(Player p) {
        ensureDailyContracts(p);
        Inventory inv = Bukkit.createInventory(null, 27, CONTRACT_MENU);
        int[] slots = {10, 13, 16};
        List<DailyContract> contracts = dailyContracts();
        for (int i = 0; i < contracts.size(); i++) {
            DailyContract c = contracts.get(i); String path = "players." + id(p) + ".daily-contracts." + c.id();
            int progress = getConfig().getInt(path + ".progress"); boolean claimed = getConfig().getBoolean(path + ".claimed");
            ItemStack item = button(c.icon(), (claimed ? "§a§lCONCLUÍDO: " : "§e§lCONTRATO: ") + c.id());
            ItemMeta meta = item.getItemMeta(); meta.setLore(List.of("§7" + c.description(), "§7Progresso: §f" + progress + "/" + c.target(), "§7Recompensa: §a$" + c.reward(), claimed ? "§aJá resgatado" : progress >= c.target() ? "§eClique para receber" : "§cEm andamento")); item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void claimContract(Player p, String contractId) {
        ensureDailyContracts(p);
        DailyContract c = dailyContracts().stream().filter(x -> x.id().equals(contractId)).findFirst().orElse(null);
        if (c == null) return;
        String path = "players." + id(p) + ".daily-contracts." + contractId;
        if (getConfig().getBoolean(path + ".claimed") || getConfig().getInt(path + ".progress") < c.target()) { msg(p, "Esse contrato ainda não pode ser resgatado."); openContractMenu(p); return; }
        getConfig().set(path + ".claimed", true); addMoney(p, c.reward()); saveConfig(); msg(p, "Contrato concluído! Você recebeu $" + c.reward() + "."); openContractMenu(p);
    }

    private void openBountyMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, BOUNTY_MENU);
        ConfigurationSection section = getConfig().getConfigurationSection("bounties");
        List<String> targets = section == null ? new ArrayList<>() : new ArrayList<>(section.getKeys(false));
        targets.sort(Comparator.comparingDouble((String uuid) -> getConfig().getDouble("bounties." + uuid + ".reward")).reversed());
        int slot = 0;
        for (String uuid : targets) {
            if (slot >= 45) break;
            try {
                ItemStack head = offlineHead(UUID.fromString(uuid), "§c" + getConfig().getString("bounties." + uuid + ".name", "Desconhecido"));
                ItemMeta meta = head.getItemMeta(); meta.setLore(List.of("§7Recompensa: §a$" + getConfig().getDouble("bounties." + uuid + ".reward"))); head.setItemMeta(meta); inv.setItem(slot++, head);
            } catch (Exception ignored) { }
        }
        inv.setItem(49, button(Material.GOLD_INGOT, "§e§lCRIAR RECOMPENSA"));
        inv.setItem(53, button(Material.BARRIER, BACK_BUTTON));
        if (slot == 0) msg(p, "Ainda não existem jogadores procurados.");
        p.openInventory(inv);
    }

    private void openBountyTargetMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, BOUNTY_TARGET_MENU);
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers().stream().filter(x -> !x.equals(p)).sorted(Comparator.comparing(Player::getName)).toList()) {
            if (slot >= 45) break;
            inv.setItem(slot++, playerHead(target, "§eALVO: " + target.getName()));
        }
        inv.setItem(49, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void openBountyAmountMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, BOUNTY_AMOUNT_MENU);
        double[] amounts = {100, 250, 500, 1000, 2500, 5000};
        int[] slots = {10, 11, 12, 13, 14, 15};
        for (int i = 0; i < amounts.length; i++) inv.setItem(slots[i], button(Material.GOLD_INGOT, "§eVALOR: " + (long) amounts[i]));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON));
        p.openInventory(inv);
    }

    private void setBounty(Player p, Player target, double amount) {
        if (target == null || target.equals(p) || !target.isOnline()) { msg(p, "Alvo inválido."); return; }
        if (!Double.isFinite(amount) || amount < 100 || amount > money(p)) { msg(p, "Você precisa ter pelo menos $100 e saldo suficiente."); return; }
        money(p, money(p) - amount);
        String targetId = target.getUniqueId().toString(), path = "bounties." + targetId;
        getConfig().set(path + ".name", target.getName());
        getConfig().set(path + ".reward", getConfig().getDouble(path + ".reward") + amount);
        saveConfig();
        msg(p, "Recompensa adicionada! Agora há $" + getConfig().getDouble(path + ".reward") + " pela cabeça de " + target.getName() + ".");
        msg(target, "§cVocê agora está PROCURADO! Recompensa atual: $" + getConfig().getDouble(path + ".reward") + ".");
    }

    private void openTreasureMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TREASURE_MENU);
        inv.setItem(13, button(activeTreasure == null ? Material.BARRIER : Material.CHEST, activeTreasure == null ? "§cNenhum tesouro ativo" : "§a§lTESOURO ESCONDIDO ATIVO"));
        inv.setItem(15, button(Material.PAPER, "§e§lATUALIZAR"));
        inv.setItem(22, button(Material.BARRIER, BACK_BUTTON)); p.openInventory(inv);
    }

    private void spawnTreasure() {
        if (activeTreasure != null) return;
        List<World> worlds = Bukkit.getWorlds(); if (worlds.isEmpty()) return;
        World world = worlds.get(0); Chunk[] chunks = world.getLoadedChunks(); if (chunks.length == 0) return;
        Chunk chunk = chunks[ThreadLocalRandom.current().nextInt(chunks.length)];
        int x = (chunk.getX() << 4) + ThreadLocalRandom.current().nextInt(16);
        int z = (chunk.getZ() << 4) + ThreadLocalRandom.current().nextInt(16);
        int y = Math.min(world.getMaxHeight() - 1, world.getHighestBlockYAt(x, z) + 1);
        org.bukkit.block.Block block = world.getBlockAt(x, y, z);
        if (!block.getType().isAir()) return;
        for (org.bukkit.block.Block neighbor : List.of(block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0), block.getRelative(0, 0, 1), block.getRelative(0, 0, -1))) {
            if (neighbor.getType() == Material.CHEST || neighbor.getType() == Material.TRAPPED_CHEST) return;
        }
        block.setType(Material.CHEST); activeTreasure = block.getLocation();
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
        chest.getBlockInventory().addItem(new ItemStack(Material.DIAMOND, 1 + ThreadLocalRandom.current().nextInt(3)),
                new ItemStack(Material.GOLD_INGOT, 8 + ThreadLocalRandom.current().nextInt(17)),
                new ItemStack(Material.EMERALD, 2 + ThreadLocalRandom.current().nextInt(5)));
        chest.update(); Bukkit.broadcastMessage("§6§l[CSDPRBS] §eUm tesouro misterioso apareceu em algum lugar do mundo! Encontre-o antes que desapareça.");
        Location treasure = activeTreasure;
        Bukkit.getScheduler().runTaskLater(this, () -> { if (activeTreasure != null && activeTreasure.equals(treasure)) { treasure.getBlock().setType(Material.AIR); activeTreasure = null; Bukkit.broadcastMessage("§7O tesouro misterioso desapareceu."); } }, 20L * 60 * 10);
    }

    @EventHandler
    public void treasureOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player) || activeTreasure == null) return;
        if (!(event.getInventory().getHolder() instanceof org.bukkit.block.Chest chest) || !chest.getLocation().equals(activeTreasure)) return;
        event.setCancelled(true);
        for (ItemStack loot : event.getInventory().getContents()) if (loot != null && !loot.getType().isAir()) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(loot.clone());
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        double reward = 250 + ThreadLocalRandom.current().nextInt(751); addMoney(player, reward);
        activeTreasure.getBlock().setType(Material.AIR); activeTreasure = null;
        msg(player, "§aVocê encontrou o tesouro e recebeu $" + reward + " mais os itens do baú!");
        Bukkit.broadcastMessage("§6" + player.getName() + " encontrou o tesouro misterioso!");
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

                msg(target, "Convite de clan recebido! Abra a bússola > CLANS > ACEITAR CONVITE.");
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

