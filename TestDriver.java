package com.csci97.ledger;

import java.util.Map;
import java.io.IOException;
import java.nio.file.*;

class Account {
    String address;
    int balance;
}

class Transaction {
    String transactionId;
    int amount;
    int fee;
    String note;
}

class Block {
    int blockNumber;
    String previousHash;
    String hash;
}

class Ledger {
    String name;
    String description;
    String seed;
    String createAccount(String accountId) {
        Account account = new Account();
        account.address = accountId;
        account.balance = 0;
        return accountId;
    }
    /*
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
	}*/
    void validate() {
    }
}
class LedgerException {
    String action;
    String reason;
}
class CommandProcessor {
    Ledger ledger = new Ledger();
    void processCommand(String command) {
        String words[] = command.split(" ");
        if ("create-ledger".equals(words[0])) {
            System.out.println("create ledger\n");
            ledger.name = words[1];
            int seed = 0;
            for (int i = 0; i < words.length; i++) {
                if ("seed".equals(words[i])) {
                    System.out.println("seed\n");
                    seed = i;
                }
            }
            ledger.description = words[3];
            for (int i = 3; i < seed; i++)//TODO CHECK ON ONE WORD DESC
            {
                ledger.description += words[i];
            }
            ledger.seed = words[seed + 1];
        }
        if ("create-account".equals(words[0])) {
            System.out.println("create account\n");
            Account account = new Account();
            account.address = words[1];
        }
        if ("process-transaction".equals(words[0])) {
            System.out.println("process transaction\n");
            Transaction transaction = new Transaction();
            transaction.transactionId = words[1];
            transaction.amount = Integer.parseInt(words[3]);
            transaction.fee = Integer.parseInt(words[5]);
            int payer = 0;
            for (int i = 0; i < words.length; i++) {
                if ("payer".equals(words[i])) {
                    System.out.println("payer\n");
                    payer = i;
                }
            }
            transaction.note = words[7];
            for (int i = 7; i < payer; i++)//TODO
            {
                ledger.description += words[i];
            }
            //TODO process payer at payer+1 and receiver at payer+3
            //System.out.println(ledger.processTransaction(transaction));
        }
        if ("get-account-balance".equals(words[0])){ 
            System.out.println("get account balance\n");
            //ledger.getAccountBalance(words[1]);
            if ("get-account-balances".equals(words[0])){ 
                System.out.println("get account balances\n");
                //System.out.println(ledger.getAccountBalances());
                if ("get-block".equals(words[0])) {
                    System.out.println("get block\n");
                    //ledger.getBlock(Integer.parseInt(words[1]));
                    //TODO output details for block number
                }
            }
        }
        if ("get-transaction".equals(words[0])) {
            System.out.println("get transaction\n");
            //ledger.getTransaction(words[1]);
            //TODO output details of transaction id
        }
        if ("validate".equals(words[0])) {
            System.out.println("validate\n");
            ledger.validate();
        }
    }
    void processCommandFile(String commandfile) throws IOException {
        String commands = "";
        commands = new String(Files.readAllBytes(Paths.get(commandfile)));
        String lines[] = commands.split("\n");
        for (String line : lines) {
            processCommand(line);
        }
    }
}

class CommandProcessorException {

    String command;
    String reason;
    int lineNumber;
}

public class TestDriver {

    public static void main(String[] args) throws IOException {
        CommandProcessor cp = new CommandProcessor();
        cp.processCommandFile("ledger.script");
    }
}
