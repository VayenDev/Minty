/*
 * Minty (Minty.main): DataManager.java
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
import lombok.Data;
import org.bson.BsonBinaryWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Data
public abstract class DataManager<T, ID, P extends DataManager.Params> {
    protected Cache<@NotNull ID, T> cache;

    abstract public Optional<T> load(P params) throws IOException;

    public abstract Optional<T> getCached(ID identifier);

    public abstract Optional<T> get(ID identifier);

    protected <D> void save(Path filePath, Codec<D> codec, D data) throws IOException {
        Files.createDirectories(filePath.getParent());
        if (!Files.exists(filePath)) Files.createFile(filePath);

        var buffer = new BasicOutputBuffer();
        codec.encode(new BsonBinaryWriter(buffer), data, EncoderContext.builder().build());
        var bytes = buffer.toByteArray();

        Files.write(filePath, bytes);
    }


    protected void delete(Path filePath, ID identifier) throws IOException {
        Files.deleteIfExists(filePath);
        cache.invalidate(identifier);
    }

    protected void unload(Path filePath, Codec<T> codec, ID identifier) throws IOException {
        var data = cache.getIfPresent(identifier);
        if (data != null) {
            save(filePath, codec, data);
            cache.invalidate(identifier);
        }
    }

    abstract public Map<String, Object> variables(T data);

    protected interface Params {
    }
}