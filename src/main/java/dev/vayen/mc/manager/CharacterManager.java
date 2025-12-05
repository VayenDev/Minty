/*
 * Minty (Minty.main): CharacterManager.java
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

package dev.vayen.mc.manager;

import com.github.benmanes.caffeine.cache.Cache;
import dev.vayen.mc.Minty;
import dev.vayen.mc.character.Character;
import org.bson.BsonBinaryReader;
import org.bson.ByteBufNIO;
import org.bson.codecs.Codec;
import org.bson.io.ByteBufferBsonInput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CharacterManager extends DataManager<Character, UUID, CharacterManager.Params> {
    private static final Codec<Character> codec = Minty.POJO_CODEC_REGISTRY.get(Character.class);

    public CharacterManager(Cache<@NotNull UUID, Character> cache) {
        this.cache = cache;
    }

    private Path generatePath(UUID owner, int id) {
        return Minty.INSTANCE.getDataPath().resolve("characters").resolve(String.format("%s.%d.bson", owner.toString(), id));
    }

    @Override
    public Optional<Character> load(Params params) {
        var filePath = generatePath(params.owner, params.id);
        byte[] data;
        try {
            data = Files.readAllBytes(filePath);
        } catch (IOException e) {
            return Optional.empty();
        }

        Character loaded;
        try (var reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(data))))) {
            loaded = codec.decode(reader, null);
        }

        if (loaded != null) cache.put(params.owner, loaded);
        return Optional.ofNullable(loaded);
    }

    @Override
    public Optional<Character> getCached(UUID identifier) {
        return Optional.ofNullable(cache.getIfPresent(identifier));
    }

    @Override
    public Optional<Character> get(UUID identifier) {
        var cached = getCached(identifier);
        return cached.isPresent() ? cached : load(new Params(identifier, 0));
    }

    public void save(Character character) throws IOException {
        super.save(generatePath(character.getOwner(), character.getId()), codec, character);
    }

    public void delete(Params params) throws IOException {
        super.delete(generatePath(params.owner, params.id), params.owner);
    }

    public void unload(Params params) throws IOException {
        super.unload(generatePath(params.owner, params.id), codec, params.owner);
    }

    public Map<String, Object> variables(Character character) {
        var map = new HashMap<String, Object>();
        map.put("character.owner", character.getOwner());
        map.put("character.id", character.getId());
        map.put("character.inventory_size", character.getInventoryArmor().length + character.getInventoryContents().length);
        map.put("character.location_spawn", character.getSpawnLocation());
        map.put("character.location_respawn", character.getRespawnLocation());
        map.put("character.xp", character.getTotalExperience());
        map.put("character.hearts_max", character.getMaxHealth());
        map.put("character.hearts_current", character.getHealth());
        map.put("character.food_level_current", character.getFoodLevel());
        map.put("character.food_level_max", 20);
        map.put("character.kills_player", character.getPlayerKills());
        map.put("character.kills_mob", character.getMobKills());
        map.put("character.deaths", character.getDeaths());
        return map;
    }

    public record Params(UUID owner, int id) implements DataManager.Params {
    }
}
