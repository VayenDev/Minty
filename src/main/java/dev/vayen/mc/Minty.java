/*
 * Minty (Minty.main): Minty.java
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

package dev.vayen.mc;

import com.github.benmanes.caffeine.cache.Caffeine;
import dev.vayen.mc.economy.bank.Bank;
import dev.vayen.mc.manager.BankManager;
import dev.vayen.mc.menu.MenuListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class Minty extends JavaPlugin {
    public static final String GRAY_ARROW = "<dark_gray>»<reset>";
    public static final CodecRegistry POJO_CODEC_REGISTRY = CodecRegistries.fromRegistries(CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    @Getter
    private static Minty instance;
    public final Logger LOGGER = getLogger();
    public final PluginManager PLUGIN_MANAGER = getServer().getPluginManager();

    @Getter
    private BankManager bankManager;

    @Override
    public void onLoad() {
        instance = this;

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        bankManager = new BankManager(
                Caffeine.newBuilder().maximumSize(10).build(), // 10 Banks loaded on the server with all customers and loans.
                Caffeine.newBuilder().maximumSize(getServer().getMaxPlayers() * 10L).build()
        );

        LOGGER.info("Minty is loading!");
    }

    @Override
    public void onEnable() {
        PLUGIN_MANAGER.registerEvents(new MenuListener(), this);
        LOGGER.info("Minty is enabled!");

        Bukkit.getAsyncScheduler().runAtFixedRate(this, scheduledTask -> {
            for (var bank : bankManager.getCache().asMap().values()) {
                try {
                    bankManager.save(bank);
                } catch (IOException e) {
                    LOGGER.warning("Failed to save bank " + bank.getName() + " (" + bank.getUuid() + ")!");
                }
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        for (Bank bank : bankManager.getCache().asMap().values()) {
            bankManager.unload(new BankManager.Params(bank.getUuid()));
        }

        LOGGER.info("Minty is disabled!");
    }
}
