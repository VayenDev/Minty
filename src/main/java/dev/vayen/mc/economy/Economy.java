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
import dev.vayen.mc.economy.exception.BankNotFoundException;
import dev.vayen.mc.economy.exception.CustomerNotFoundException;
import dev.vayen.mc.economy.exception.InsufficientFundsException;
import dev.vayen.mc.economy.exception.InvalidPaymentAmountException;
import dev.vayen.mc.manager.BankManager;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Economy {
    private static final Lock lock = new ReentrantLock();

    public static void pay(String senderIban, String receiverIban, long amount) throws InvalidPaymentAmountException, NoSuchElementException, InsufficientFundsException, IOException, CustomerNotFoundException, BankNotFoundException {
        var bm = Minty.getInstance().getBankManager();
        var sender = bm.getCustomerByIban(senderIban).orElseThrow(() -> new CustomerNotFoundException(false));
        var receiver = bm.getCustomerByIban(receiverIban).orElseThrow(() -> new CustomerNotFoundException(true));
        var bank = bm.get(new BankManager.Params(sender.getBankUUID())).orElseThrow(() -> new BankNotFoundException(false));
        var receiverBank = bm.get(new BankManager.Params(receiver.getBankUUID())).orElseThrow(() -> new BankNotFoundException(true));
        var maxDebt = bank.getMaxDebt();

        if (amount <= 0) throw new InvalidPaymentAmountException();

        synchronized (sender) {
            synchronized (receiver) {
                if (sender.getBalance() - amount < (-maxDebt))
                    throw new InsufficientFundsException(maxDebt);
                var newReceiverBalance = receiver.getBalance() + amount;

                sender.setBalance(sender.getBalance() - amount);
                receiver.setBalance(newReceiverBalance);

                bm.saveCustomer(bank, sender.getPlayerUUID(), sender);
                bm.saveCustomer(receiverBank, receiver.getPlayerUUID(), receiver);
            }
        }
    }

    public static void deposit(String iban, long amount) throws NoSuchElementException, IOException, CustomerNotFoundException, BankNotFoundException {
        var bm = Minty.getInstance().getBankManager();
        var customer = bm.getCustomerByIban(iban).orElseThrow(() -> new CustomerNotFoundException(false));
        var bank = bm.getCached(customer.getBankUUID()).orElseThrow(() -> new BankNotFoundException(false));
        var newBalance = customer.getBalance() + amount;

        synchronized (customer) {
            customer.setBalance(customer.getBalance() + amount);
            bm.saveCustomer(bank, customer.getPlayerUUID(), customer);
        }
    }

    public static void withdraw(String iban, long amount) throws NoSuchElementException, InsufficientFundsException, IOException, CustomerNotFoundException, BankNotFoundException {
        var bm = Minty.getInstance().getBankManager();
        var customer = bm.getCustomerByIban(iban).orElseThrow(() -> new CustomerNotFoundException(false));
        var bank = bm.get(new BankManager.Params(customer.getBankUUID())).orElseThrow(() -> new BankNotFoundException(false));
        var maxDebt = bank.getMaxDebt();

        synchronized (customer) {
            if (customer.getBalance() - amount < (-maxDebt))
                throw new InsufficientFundsException(maxDebt);

            customer.setBalance(customer.getBalance() - amount);
            bm.saveCustomer(bank, customer.getPlayerUUID(), customer);
        }
    }
}
