/*
 * Minty (Minty.main): Economy.java
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

package dev.vayen.mc.economy;

import dev.vayen.mc.Minty;
import dev.vayen.mc.economy.exception.InsufficientFundsException;
import dev.vayen.mc.economy.exception.InvalidPaymentAmountException;

import java.util.NoSuchElementException;

public class Economy {
    public void pay(String senderIban, String receiverIban, long amount) throws InvalidPaymentAmountException, NoSuchElementException, InsufficientFundsException {
        var bm = Minty.INSTANCE.getBankManager();
        var sender = bm.getCustomerByIban(senderIban).orElseThrow();
        var receiver = bm.getCustomerByIban(receiverIban).orElseThrow();
        var maxDebt = bm.getCached(sender.getBankUUID()).orElseThrow().getMaxDebt();

        if (amount <= 0) throw new InvalidPaymentAmountException();
        if (sender.getBalance() - amount < (-maxDebt))
            throw new InsufficientFundsException(maxDebt);
        var newReceiverBalance = receiver.getBalance() + amount;

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(newReceiverBalance);
    }

    public void deposit(String iban, long amount) throws NoSuchElementException {
        var bm = Minty.INSTANCE.getBankManager();
        var customer = bm.getCustomerByIban(iban).orElseThrow();
        var newBalance = customer.getBalance() + amount;

        customer.setBalance(customer.getBalance() + amount);
    }

    public void withdraw(String iban, long amount) throws NoSuchElementException, InsufficientFundsException {
        var bm = Minty.INSTANCE.getBankManager();
        var customer = bm.getCustomerByIban(iban).orElseThrow();
        var maxDebt = bm.getCached(customer.getBankUUID()).orElseThrow().getMaxDebt();

        if (customer.getBalance() - amount < (-maxDebt))
            throw new InsufficientFundsException(maxDebt);

        customer.setBalance(customer.getBalance() - amount);
    }
}
