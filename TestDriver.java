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
    /*String processTransaction(Transaction transaction) {
        return transaction.transactionId;
    }
    int getAccountBalance(String address) {
        return balance;
    }
    Map getAccountBalances() {
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
    //process commands takes individual commands
    void processCommand(String command, Ledger ledger) {
        String words[] = command.split(" ");
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
            for (int i = 7; i < payer; i++){//TODO
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
    
    //takes text file, splits it into lines, and feeds to processCommand
    void processCommandFile(String commandfile) throws IOException, CommandProcessorException {
        String commands = new String(Files.readAllBytes(Paths.get(commandfile)));
        //remove comment lines
        String lines[] = commands.split("\n");
        for(int i=0;i<lines.length;i++)
            if(lines[i].startsWith("#"))
                lines[i]="";
        //recombine into single string
        StringBuilder finalStringBuilder = new StringBuilder("");
        for(String s:lines){
            if(!s.equals(""))
                finalStringBuilder.append(s).append(System.getProperty("line.separator"));
        }  
        commands = finalStringBuilder.toString();
        //ensure that the ledger is created
        String words[] = commands.split(" ");
        if ("create-ledger".equals(words[0])) {
            Ledger ledger = new Ledger();
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
            
            //read each line into process command
            for (String line : lines)
                processCommand(line, ledger);
        } else {
            System.out.println("Must create ledger first!\n");
            throw new CommandProcessorException(words[0],"must create ledger first");
        }
    }
}

class CommandProcessorException extends Exception{
    String command;
    String reason;
    int lineNumber;
    public CommandProcessorException(String c, String r){
        command = c;
        reason = r;
        lineNumber= new Throwable().getStackTrace()[0].getLineNumber();
    }
}

public class TestDriver {
    public static void main(String[] args) throws IOException, CommandProcessorException {
        CommandProcessor cp = new CommandProcessor();
        cp.processCommandFile("C:\\Users\\Andrew\\Documents\\NetBeansProjects\\ledger\\target\\classes\\com\\csci97\\ledger\\testinput.txt");
    }
}
