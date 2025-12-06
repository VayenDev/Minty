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
import dev.vayen.mc.economy.bank.BankLoan;
import org.bson.BsonBinaryReader;
import org.bson.ByteBufNIO;
import org.bson.codecs.Codec;
import org.bson.io.ByteBufferBsonInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class BankManager extends DataManager<Bank, UUID, BankManager.Params> {
    private static final Codec<Bank> bankCodec = Minty.POJO_CODEC_REGISTRY.get(Bank.class);
    private static final Codec<BankCustomer> customerCodec = Minty.POJO_CODEC_REGISTRY.get(BankCustomer.class);
    private static final Codec<BankLoan> loanCodec = Minty.POJO_CODEC_REGISTRY.get(BankLoan.class);

    private static final Pattern UUID_END_WITH_BSON_PATTERN = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}.bson\\b");
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    private final Cache<@NotNull String, BankCustomer> ibanToCustomerCache;

    public BankManager(Cache<@NotNull UUID, Bank> cache, Cache<@NotNull String, BankCustomer> ibanToCustomerCache) {
        this.cache = cache;
        this.ibanToCustomerCache = ibanToCustomerCache;
    }

    private Path generatePath(UUID uuid) {
        return Minty.getInstance().getDataPath().resolve("banks").resolve(String.format("%s/general.bson", uuid.toString()));
    }

    private Path getCustomerFolderPath(UUID bankUUID) {
        return Minty.getInstance().getDataPath().resolve("banks").resolve(String.format("%s/customers/", bankUUID.toString()));
    }

    private Path generateCustomerPath(UUID bankUUID, UUID customerUUID) {
        return Minty.getInstance().getDataPath().resolve("banks").resolve(String.format("%s/customers/%s.bson", bankUUID.toString(), customerUUID.toString()));
    }

    private Path getLoanFolderPath(UUID bankUUID) {
        return Minty.getInstance().getDataPath().resolve("banks").resolve(String.format("%s/loans/", bankUUID.toString()));
    }

    private Path generateLoanPath(UUID bankUUID, UUID loanUUID) {
        return Minty.getInstance().getDataPath().resolve("banks").resolve(String.format("%s/loans/%s.bson", bankUUID.toString(), loanUUID.toString()));
    }

    @Override
    public List<Bank> loadAllFromFile() throws IOException {
        var list = new ArrayList<Bank>();
        try (var files = Files.list(Minty.getInstance().getDataPath().resolve("banks"))) {
            files.filter(Files::isDirectory).filter(path -> UUID_PATTERN.matcher(path.getFileName().toString()).matches()).forEach(path -> {
                try {
                    loadFromFile(new Params(UUID.fromString(path.getParent().getFileName().toString()))).ifPresent(list::add);
                } catch (IllegalArgumentException ignored) {
                    Minty.getInstance().LOGGER.warning(String.format("Failed to load Bank %s (%s)", path.getFileName(), path.getParent()));
                }
            });
        }
        return list;
    }

    @Override
    public Optional<Bank> loadFromFile(Params params) {
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

            try {
                loaded.setCustomers(loadList(getCustomerFolderPath(params.bankUUID), customerCodec, loaded));
                loaded.getCustomers().forEach(customer -> ibanToCustomerCache.put(customer.getIban(), customer));
            } catch (IOException ignored) {
                Minty.getInstance().LOGGER.warning(String.format("Failed to load customers for Bank %s (%s)", loaded.getName(), loaded.getUuid()));
            }

            try {
                loaded.setLoans(loadList(getLoanFolderPath(params.bankUUID), loanCodec, loaded));
            } catch (IOException e) {
                Minty.getInstance().LOGGER.warning(String.format("Failed to load loans for Bank %s (%s)", loaded.getName(), loaded.getUuid()));
            }
        }
        return Optional.ofNullable(loaded);
    }

    private <T> List<T> loadList(Path folderPath, Codec<T> codec, Bank bank) throws IOException {
        final List<T> list = new ArrayList<>();
        try (var files = Files.list(folderPath)) {
            files.filter(Files::isRegularFile).filter(path -> UUID_END_WITH_BSON_PATTERN.matcher(path.getFileName().toString()).matches()).forEach(path -> {
                byte[] fileData;
                try {
                    fileData = Files.readAllBytes(path);
                } catch (IOException e) {
                    Minty.getInstance().LOGGER.warning(String.format("Failed to read data file %s for Bank %s (%s)", path.toAbsolutePath(), bank.getName(), bank.getUuid()));
                    return;
                }

                try (var reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(fileData))))) {
                    var data = codec.decode(reader, null);
                    list.add(data);
                }
            });
        }
        return list;
    }

    public synchronized void save(Bank bank) throws IOException {
        var bankUUID = bank.getUuid();
        super.save(generatePath(bankUUID), bankCodec, bank);

        for (var customer : bank.getCustomers()) {
            super.save(generateCustomerPath(bankUUID, customer.getPlayerUUID()), customerCodec, customer);
        }
        for (var loan : bank.getLoans()) {
            super.save(generateLoanPath(bankUUID, loan.getUuid()), loanCodec, loan);
        }
    }

    public void delete(Params params) throws IOException {
        super.delete(generatePath(params.bankUUID), params.bankUUID);
        Files.deleteIfExists(getCustomerFolderPath(params.bankUUID));
        Files.deleteIfExists(getLoanFolderPath(params.bankUUID));
    }

    public void unload(Params params) throws IOException {
        super.unload(generatePath(params.bankUUID), bankCodec, params.bankUUID);
        cache.invalidate(params.bankUUID);
        ibanToCustomerCache.asMap().entrySet().removeIf(entry -> entry.getValue().getBankUUID().equals(params.bankUUID));
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

    public void saveCustomer(Bank bank, UUID playerUUID, @Nullable BankCustomer customer) throws IOException {
        if (customer == null) {
            var customerOptional = getCustomerByUUID(playerUUID);
            if (customerOptional.isEmpty()) return;
            customer = customerOptional.get();
        }
        super.save(generateCustomerPath(bank.getUuid(), playerUUID), customerCodec, customer);
    }

    public Optional<BankCustomer> getCustomerByIban(String iban) {
        return Optional.ofNullable(ibanToCustomerCache.getIfPresent(iban));
    }

    public Optional<BankCustomer> getCustomerByUUID(UUID uuid) {
        return cache.asMap().values().stream().flatMap(bank -> bank.getCustomers().stream()).filter(customer -> customer.getPlayerUUID().equals(uuid)).findFirst();
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

    public record Params(UUID bankUUID) implements DataManager.Params<UUID> {
        @Override
        public UUID getIdentifier() {
            return bankUUID;
        }
    }
}
