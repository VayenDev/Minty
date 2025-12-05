/*
 * Minty (Minty.main): BankManager.java
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
import dev.vayen.mc.economy.bank.Bank;
import dev.vayen.mc.economy.bank.BankCustomer;
import org.bson.BsonBinaryReader;
import org.bson.ByteBufNIO;
import org.bson.codecs.Codec;
import org.bson.io.ByteBufferBsonInput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class BankManager extends DataManager<Bank, UUID, BankManager.Params> {
    private static final Codec<Bank> bankCodec = Minty.POJO_CODEC_REGISTRY.get(Bank.class);
    private static final Codec<BankCustomer> customerCodec = Minty.POJO_CODEC_REGISTRY.get(BankCustomer.class);

    private static final Pattern UUID_END_WITH_BSON = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}.bson\\b");

    private final Cache<@NotNull String, BankCustomer> ibanToCustomerCache;

    public BankManager(Cache<@NotNull UUID, Bank> cache, Cache<@NotNull String, BankCustomer> ibanToCustomerCache) {
        this.cache = cache;
        this.ibanToCustomerCache = ibanToCustomerCache;
    }

    private Path generatePath(UUID uuid) {
        return Minty.INSTANCE.getDataPath().resolve("banks").resolve(String.format("%s/general.bson", uuid.toString()));
    }

    private Path getCustomerFolderPath(UUID bankUUID) {
        return Minty.INSTANCE.getDataPath().resolve("banks").resolve(String.format("%s/customers/", bankUUID.toString()));
    }

    private Path generateCustomerPath(UUID bankUUID, UUID customerUUID) {
        return Minty.INSTANCE.getDataPath().resolve("banks").resolve(String.format("%s/customers/%s.bson", bankUUID.toString(), customerUUID.toString()));
    }

    @Override
    public Optional<Bank> load(Params params) {
        var filePath = generatePath(params.bankUUID);
        byte[] data;
        try {
            data = Files.readAllBytes(filePath);
        } catch (IOException e) {
            return Optional.empty();
        }

        Bank loaded;
        try (var reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(data))))) {
            loaded = bankCodec.decode(reader, null);
        }

        if (loaded != null) {
            cache.put(params.bankUUID, loaded);

            final List<BankCustomer> customers = new ArrayList<>();
            try {
                try (var files = Files.list(getCustomerFolderPath(params.bankUUID))) {
                    files.filter(path -> UUID_END_WITH_BSON.matcher(path.getFileName().toString()).matches()).forEach(path -> {
                        byte[] fileData;
                        try {
                            fileData = Files.readAllBytes(path);
                        } catch (IOException e) {
                            Minty.INSTANCE.LOGGER.fine(String.format("Failed to read customer file %s for Bank %s (%s)", path.getFileName(), loaded.getName(), loaded.getUuid()));
                            return;
                        }

                        try (var reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(fileData))))) {
                            var customer = customerCodec.decode(reader, null);
                            customers.add(customer);
                        }
                    });
                }
            } catch (IOException ignored) {
                Minty.INSTANCE.LOGGER.fine(String.format("Failed to load customers for Bank %s (%s)", loaded.getName(), loaded.getUuid()));
            }
            loaded.setCustomers(customers);
        }
        return Optional.ofNullable(loaded);
    }

    @Override
    public Optional<Bank> getCached(UUID identifier) {
        return Optional.ofNullable(cache.getIfPresent(identifier));
    }

    @Override
    public Optional<Bank> get(UUID identifier) {
        var cached = getCached(identifier);
        return cached.isPresent() ? cached : load(new Params(identifier));
    }

    public void save(Bank bank) throws IOException {
        var bankUUID = bank.getUuid();
        super.save(generatePath(bankUUID), bankCodec, bank);

        for (var customer : bank.getCustomers()) {
            super.save(generateCustomerPath(bankUUID, customer.getPlayerUUID()), customerCodec, customer);
        }
    }

    public void delete(Params params) throws IOException {
        super.delete(generatePath(params.bankUUID), params.bankUUID);
    }

    public void unload(Params params) throws IOException {
        super.unload(generatePath(params.bankUUID), bankCodec, params.bankUUID);
    }

    private String generateIban(UUID bankUUID, UUID playerUUID) {
        long playerNum = Math.abs(playerUUID.getMostSignificantBits() % 1_000_00000);
        long bankNum = Math.abs(bankUUID.getMostSignificantBits() % 1_000_00000);
        return String.format("MC%08d%08d", bankNum, playerNum);
    }

    public void createCustomer(Bank bank, UUID playerUUID) {
        var iban = generateIban(bank.getUuid(), playerUUID);
        var customer = new BankCustomer(playerUUID, bank.getUuid(), iban, 0);
        bank.addCustomer(customer);
        ibanToCustomerCache.put(iban, customer);
    }

    public void deleteCustomer(Bank bank, UUID playerUUID) {
        bank.removeCustomer(playerUUID);
        ibanToCustomerCache.invalidate(generateIban(bank.getUuid(), playerUUID));
    }

    public void updateCustomer(Bank bank, BankCustomer newCustomer) {
        bank.removeCustomer(newCustomer.getPlayerUUID());
        bank.addCustomer(newCustomer);
    }

    public Optional<BankCustomer> getCustomerByIban(String iban) {
        return Optional.ofNullable(ibanToCustomerCache.getIfPresent(iban));
    }

    @Override
    public Map<String, Object> variables(Bank bank) {
        return Map.of(
                "bank.uuid", bank.getUuid(),
                "bank.name", bank.getName(),
                "bank.customers.size", bank.getCustomers().size(),
                "bank.loans.size", bank.getLoans().size()
        );
    }

    public record Params(UUID bankUUID) implements DataManager.Params {
    }
}
