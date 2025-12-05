/*
 * Minty (Minty.main): Menu.java
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

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface Menu extends InventoryHolder {
    void click(Player player, int slot);

    void setItem(int slot, ItemStack item);

    void setItem(int slot, ItemStack item, Consumer<Player> action);

    void onSetItems();

    default void open(Player player) {
        onSetItems();
        player.openInventory(getInventory());
    }

    enum Rows {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

        private final int size;

        Rows(int rows) {
            this.size = rows * 9;
        }

        public int getSize() {
            return size;
        }
    }
}
