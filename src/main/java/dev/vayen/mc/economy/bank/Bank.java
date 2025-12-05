/*
 * Minty (Minty.main): Bank.java
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

package dev.vayen.mc.economy.bank;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Data
public class Bank {
    UUID uuid;
    String name;
    @BsonIgnore
    List<BankCustomer> customers;
    @BsonIgnore
    List<BankLoan> loans;
    long maxDebt;

    public void addCustomer(BankCustomer customer) {
        customers.add(customer);
    }

    public void removeCustomer(UUID uuid) {
        customers.removeIf(c -> c.getPlayerUUID().equals(uuid));
    }

    public Optional<BankCustomer> getCustomer(UUID uuid) {
        return customers.stream().filter(c -> c.getPlayerUUID().equals(uuid)).distinct().findFirst();
    }

    public void addLoan(BankLoan loan) {
        loans.add(loan);
    }

    public void removeLoan(UUID uuid) {
        loans.removeIf(c -> c.getUuid().equals(uuid));
    }

    public Optional<BankLoan> getLoan(UUID uuid) {
        return loans.stream().filter(c -> c.getUuid().equals(uuid)).distinct().findFirst();
    }

    public Optional<BankLoan> getLoanByCustomer(UUID customerUUID) {
        return loans.stream().filter(c -> c.getCustomerUUID().equals(uuid)).distinct().findFirst();
    }
}
