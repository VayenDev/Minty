/*
 * Minty (Minty.main): Character.java
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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Character {
    private final int id;
    private final UUID owner;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] inventoryArmor;
    private double maxHealth;
    private double health;
    private int totalExperience;
    private int foodLevel;
    private Location spawnLocation;
    private Location respawnLocation;
    private int playerKills;
    private int mobKills;
    private int deaths;

    public static double heartToHealth(double hearts) {
        return hearts * 2;
    }

    public static double healthToHeart(double health) {
        return health / 2;
    }
}
