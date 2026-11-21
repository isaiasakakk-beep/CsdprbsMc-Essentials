package com.csdprbsmc.essentials;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.SimpleForm;
import java.util.List;
import java.util.function.Consumer;

public final class BedrockMenus {
    public boolean isBedrock(Player player) {
        try { return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId()); }
        catch (Throwable ignored) { return false; }
    }

    public void openSelection(Player player, String title, String content,
                              List<String> buttons, Consumer<Integer> callback) {
        SimpleForm.Builder builder = SimpleForm.builder().title(title).content(content);
        for (String button : buttons) builder.button(button);
        builder.validResultHandler((submitted, response) -> callback.accept(response.clickedButtonId()));
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
