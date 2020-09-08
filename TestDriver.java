package com.csci97.ledger;

import java.util.Map;
import java.io.IOException;
import java.nio.file.*;

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
	Ledger ledger = new Ledger();
	void processCommand(String command) {
		String words[] = command.split(" ");
		if(words[0]=="create-ledger") {
			ledger.name=words[1];
			int seed=0;
			for(int i=0;i<words.length;i++)
				if(words[i]=="seed")
					seed=i;
			ledger.description=words[3];
			for(int i=3;i<seed;i++)//TODO CHECK ON ONE WORD DESC
				ledger.description+=words[i];
			ledger.seed=words[seed+1];
		}
		if(words[0]=="create-account") {
			Account account = new Account();
			account.address=words[1];
		}
		if(words[0]=="process-transaction") {
			Transaction transaction = new Transaction();
			transaction.transactionId=words[1];
			transaction.amount=Integer.parseInt(words[3]);
			transaction.fee=Integer.parseInt(words[5]);
			int payer=0;
			for(int i=0;i<words.length;i++)
				if(words[i]=="payer")
					payer=i;
			transaction.note=words[7];
			for(int i=7;i<payer;i++)//TODO
				ledger.description+=words[i];
			//TODO process payer at payer+1 and receiver at payer+3
			System.out.println(ledger.processTransaction(transaction));
		}
		if(words[0]=="get-account-balance")
			ledger.getAccountBalance(words[1]);
		if(words[0]=="get-account-balances")
			System.out.println(ledger.getAccountBalances());
		if(words[0]=="get-block") {
			ledger.getBlock(Integer.parseInt(words[1]));
			//TODO output details for block number
		}
		if(words[0]=="get-transaction") {
			ledger.getTransaction(words[1]);
			//TODO output details of transaction id
		}
		if(words[0]=="validate")
			ledger.validate();
	}
	void processCommandFile(String commandfile) throws IOException {
		String commands = ""; 
	    commands = new String(Files.readAllBytes(Paths.get(commandfile)));
	    String lines[] = commands.split("\n");
	    for(int i=0;i<lines.length;i++)
	    	processCommand(lines[i]);
	}
}

class CommandProcessorException{
	String command;
	String reason;
	int lineNumber;
}


public class TestDriver {
	public static void main(String[] args) {
		CommandProcessor.processCommandFile("ledger.script"); 
	}
}
