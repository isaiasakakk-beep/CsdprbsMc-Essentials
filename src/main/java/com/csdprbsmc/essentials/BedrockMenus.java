package com.csdprbsmc.essentials;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.SimpleForm;
import java.util.function.Consumer;

public final class BedrockMenus {
    public boolean isBedrock(Player player) {
        try { return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId()); }
        catch (Throwable ignored) { return false; }
    }

    public void openMenu(Player player, Consumer<Integer> callback) {
        SimpleForm form = SimpleForm.builder().title("CSDPRBSMC")
            .content("Escolha uma opção").button("TPA").button("HOME")
            .button("WARPS").button("CLANS").button("RANKINGS").button("LOJA/ECONOMIA")
            .validResultHandler((submitted, response) -> callback.accept(response.clickedButtonId()))
            .build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    public void openClan(Player player) {
        SimpleForm form = SimpleForm.builder().title("CLANS")
            .content("Use /clan ajuda para ver os comandos de clan.").button("OK").build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    public void openShop(Player player, double balance, Consumer<Integer> callback) {
        SimpleForm.Builder builder = SimpleForm.builder().title("LOJA").content("Saldo: $" + balance);
        String[] labels = {"Ferro - $20", "Ouro - $35", "Diamante - $150",
            "Esmeralda - $100", "Madeira - $10", "Pão - $8", "Obsidiana - $80"};
        for (String label : labels) builder.button(label);
        builder.validResultHandler((form, response) -> callback.accept(response.clickedButtonId()));
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
