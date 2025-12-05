/*
 * Minty (Minty.main): BsonCodecLocation.java
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

package dev.vayen.mc.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class BsonCodecLocation implements Codec<Location> {
    @Override
    public Location decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        var world = UUID.fromString(reader.readString("world"));
        var x = reader.readDouble("x");
        var y = reader.readDouble("y");
        var z = reader.readDouble("z");
        var pitch = reader.readDouble("pitch");
        var yaw = reader.readDouble("yaw");
        reader.readEndDocument();
        return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
    }

    @Override
    public void encode(BsonWriter writer, Location value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("world", value.getWorld().getUID().toString());
        writer.writeDouble("x", value.getX());
        writer.writeDouble("y", value.getY());
        writer.writeDouble("z", value.getZ());
        writer.writeDouble("pitch", value.getPitch());
        writer.writeDouble("yaw", value.getYaw());
        writer.writeEndDocument();
    }

    @Override
    public Class<Location> getEncoderClass() {
        return Location.class;
    }
}
