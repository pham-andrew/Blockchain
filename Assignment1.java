package com.csci97.ledger;

import java.util.Map;

class Account{
	String address;
	int balance;
}

class Transaction{
	String transactionId;
	int amount;
	int fee;
	String note;
}

class Block{
	int blockNumber;
	String previousHash;
	String hash;
}

class Ledger{
	String name;
	String description;
	String seed;
	String createAccount(String accountId) {
		Account account = new Account();
		account.address=accountId;
		account.balance=0;
		return accountId;
	}
	String processTransaction(Transaction transaction) {
		return transaction.transactionId;
	}
	int getAccountBalance(String address) {
		return balance;
	}
	Map getAccountBalances(){
		return accountbalances;
	}
	Block getBlock(int blockNumber) {
		return block;
	}
	Transaction getTransaction(String transactionId) {
		return transaction;
	}
	void validate() {}
}

class LedgerException{
	String action;
	String reason;
}

class CommandProcessor{
	void processCommand(String command) {}
	void processCommandFile(String commandfile) {}
}

class CommandProcessorException{
	String command;
	String reason;
	int lineNumber;
}


public class Assignment1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
