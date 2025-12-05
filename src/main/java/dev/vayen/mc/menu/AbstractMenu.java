/*
 * Minty (Minty.main): AbstractMenu.java
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

package dev.vayen.mc.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class AbstractMenu implements Menu {
    private final HashMap<Integer, Consumer<Player>> actions = new HashMap<>();
    private final Inventory inventory;

    public AbstractMenu(Component title, Rows rows) {
        this.inventory = Bukkit.createInventory(this, rows.getSize(), title);
    }

    @Override
    public void click(Player player, int slot) {
        actions.getOrDefault(slot, p -> {
        }).accept(player);
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, p -> {
        });
    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<Player> action) {
        inventory.setItem(slot, item);
        actions.put(slot, action);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
