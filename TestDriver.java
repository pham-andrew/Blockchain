package com.csci97.ledger;

import java.util.Map;
import java.io.IOException;
import java.nio.file.*;

class Account {
    String address;
    int balance;
    void setAddress(String a){address=a;}
    void setBalance(int b){balance=b;}
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
    void setBlock(int n, String p, String h){
        blockNumber=n;
        previousHash=p;
        hash=h;
    }
}

class Ledger {
    String name;
    String description;
    String seed;
    Map accounts;
    
    //creates first account with address of 0
    void createMasterAccount() {
        Account account = new Account();
        account.setAddress("0");
        account.setBalance(Integer.MAX_VALUE);
        accounts.put("0", 0);
    }
    String createAccount(String accountId) {
        Account account = new Account();
        account.setAddress("accountId");
        account.setBalance(0);
        accounts.put(accountId, 0);
        return accountId;
    }
    String processTransaction(Transaction transaction) {
        return transaction.transactionId;
    }
    int getAccountBalance(String address) {
        return (int) accounts.get(address);
    }
    Map getAccountBalances() {
        return accounts;
    }
    Block getBlock(int blockNumber) {
        return block;
    }
    Transaction getTransaction(String transactionId) {
        return transaction;
    }
    void validate() {
    }
}
class LedgerException {
    String action;
    String reason;
}
class CommandProcessor {
    //processcommand takes individual commands
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
            ledger.getTransaction(words[1]);
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
            System.out.println("ledger created\n");
            ledger.name = words[1];
            int seed = 0;
            for (int i = 0; i < words.length; i++) {
                if ("seed".equals(words[i])) {
                    System.out.println("seed\n");
                    seed = i;
                }
            }
            ledger.description = words[3];
            for (int i = 3; i < seed; i++){//TODO CHECK ON ONE WORD DESC
                ledger.description += words[i];
            }
            ledger.seed = words[seed + 1];
            
            //initialize the blockchain
            //create genesis block
            Block block = new Block();
            block.setBlock(0,"0","0");
            //create master account
            ledger.createMasterAccount();
            
            //read each line into process command
            for (String line : lines)
                processCommand(line, ledger);
            
        } else {
            System.out.println("Must create ledger first!\n");
            throw new CommandProcessorException(words[0],"must create ledger first",0);
        }
    }
}

class CommandProcessorException extends Exception{
    String command;
    String reason;
    int lineNumber;
    public CommandProcessorException(String c, String r, int l){
        command = c;
        reason = r;
        lineNumber= l;
    }
}

public class TestDriver {
    public static void main(String[] args) throws IOException, CommandProcessorException {
        CommandProcessor cp = new CommandProcessor();
        cp.processCommandFile("C:\\Users\\Andrew\\Documents\\NetBeansProjects\\ledger\\target\\classes\\com\\csci97\\ledger\\testinput.txt");
    }
}
