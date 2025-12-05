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
import de.exlll.configlib.YamlConfigurations;
import dev.vayen.mc.character.CharacterConfig;
import dev.vayen.mc.codec.BsonCodecItemStack;
import dev.vayen.mc.codec.BsonCodecLocation;
import dev.vayen.mc.manager.BankManager;
import dev.vayen.mc.manager.CharacterManager;
import dev.vayen.mc.menu.MenuListener;
import lombok.Getter;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.util.logging.Logger;

public final class Minty extends JavaPlugin {
    public static final Minty INSTANCE = getPlugin(Minty.class);
    public static final String GRAY_ARROW = "<dark_gray>»<reset>";
    public static final CodecRegistry POJO_CODEC_REGISTRY = CodecRegistries.fromRegistries(CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()), CodecRegistries.fromCodecs(new BsonCodecLocation(), new BsonCodecItemStack()));
    public final Logger LOGGER = getLogger();
    public final PluginManager PLUGIN_MANAGER = getServer().getPluginManager();
    @Getter
    private CharacterConfig characterConfig = CharacterConfig.DEFAULT;

    @Getter
    private CharacterManager characterManager;
    @Getter
    private BankManager bankManager;

    @Override
    public void onLoad() {
        if (getDataFolder().exists()) getDataFolder().mkdirs();

        characterManager = new CharacterManager(Caffeine.newBuilder().maximumSize(getServer().getMaxPlayers()).build());
        bankManager = new BankManager(Caffeine.newBuilder().maximumSize(10).build()); // 10 Banks loaded on the server with all customers and loans.

        if (!Files.exists(getDataPath().resolve(CharacterConfig.PATH)))
            YamlConfigurations.save(CharacterConfig.PATH, CharacterConfig.class, characterConfig);
        else characterConfig = YamlConfigurations.load(CharacterConfig.PATH, CharacterConfig.class);

        LOGGER.info("Minty is loading!");
    }

    @Override
    public void onEnable() {
        PLUGIN_MANAGER.registerEvents(new MenuListener(), this);
        LOGGER.info("Minty is enabled!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Minty is disabled!");
    }
}
