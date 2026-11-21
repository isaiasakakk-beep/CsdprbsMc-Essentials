package com.csdprbsmc.essentials;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.SimpleForm;
import java.util.List;
import java.util.function.Consumer;

public final class BedrockMenus {
    public boolean isBedrock(Player player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

    public void openSelection(Player player, String title, String content,
                              List<String> buttons, Consumer<Integer> callback) {
        if (buttons == null || buttons.isEmpty()) throw new IllegalArgumentException("O formulário precisa ter ao menos um botão.");
        SimpleForm.Builder builder = SimpleForm.builder().title(title).content(content);
        for (String button : buttons) builder.button(button);
        builder.validResultHandler((submitted, response) -> { if (callback != null) callback.accept(response.clickedButtonId()); });
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
