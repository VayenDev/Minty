/*
 * Minty (Minty.main): CharacterConfig.java
 * Copyright (C) 2025 mtctx
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the **GNU General Public License** as published
 * by the Free Software Foundation, either **version 3** of the License, or
 * (at your option) any later version.
 *
 * *This program is distributed WITHOUT ANY WARRANTY;** see the
 * GNU General Public License for more details, which you should have
 * received with this program.
 *
 * SPDX-FileCopyrightText: 2025 mtctx
 * SPDX-License-Identifier: GPL-3.0-only
 */

package dev.vayen.mc.character;

import de.exlll.configlib.Comment;
import dev.vayen.mc.Minty;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public record CharacterConfig(
        @Comment("The maximum amount of characters per player is 3")
        Map<String, Integer> maxCharactersPerPermission,

        String guiTitle,
        @Comment({"The following variables are available:", "- {character.owner}", "- {character.id}", "- {character.inventory_size}", "- {character.location_spawn}", "- {character.location_respawn}", "- {character.xp}", "- {character.hearts_max}", "- {character.hearts_current}", "- {character.food_level_max}", "- {character.food_level_current}"})
        String itemTitle,
        List<String> lore
) {
    public static final CharacterConfig DEFAULT = new CharacterConfig(
            Map.of("minty.character.1", 1, "minty.character.2", 2, "minty.character.3", 3),
            "<gray>Select a character<reset>",
            String.format("<white>Character %s<reset>", "{character.id}"),
            List.of(
                    String.format("<white>Hunger Level<reset> %s <green>%s<reset>", Minty.GRAY_ARROW, "{character.food_level_current}/{character.food_level_max}"),
                    String.format("<white>Inventory Size/Item Amount<reset> %s <green>%s<reset>", Minty.GRAY_ARROW, "{character.inventory_size}"),
                    String.format("<white>Hunger Level<reset> %s <green>%s<reset>", Minty.GRAY_ARROW, "{character.food_level_current}/{character.food_level_max}"),
                    String.format("<white>Hunger Level<reset> %s <green>%s<reset>", Minty.GRAY_ARROW, "{character.food_level_current}/{character.food_level_max}")
            )
    );

    public static Path PATH = Paths.get("configs", "character.yml");
}
