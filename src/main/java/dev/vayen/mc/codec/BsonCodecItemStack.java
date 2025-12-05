/*
 * Minty (Minty.main): BsonCodecItemStack.java
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

import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bukkit.inventory.ItemStack;

public class BsonCodecItemStack implements Codec<ItemStack> {
    @Override
    public ItemStack decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        var itemStackBytes = reader.readBinaryData().getData();
        reader.readEndDocument();
        return ItemStack.deserializeBytes(itemStackBytes);
    }

    @Override
    public void encode(BsonWriter writer, ItemStack value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeBinaryData(new BsonBinary(value.serializeAsBytes()));
        writer.writeEndDocument();
    }

    @Override
    public Class<ItemStack> getEncoderClass() {
        return ItemStack.class;
    }
}
