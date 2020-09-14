package com.csci97.ledger;

import java.util.Map;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;

class Account {
    String address;
    int balance;
}

class Transaction {
    String transactionId;
    String payer, receiver, note;
    int amount, fee;
}

class Block {
    Transaction[] transactions = new Transaction[10];
    int currentTransaction=0;
    Map<String, Account> accounts = new HashMap<>();
    int blockNumber;
    String previousHash, hash;
    void setBlock(int n, String p, String h){
        blockNumber=n;
        previousHash=p;
        hash=h;
    }
}

//blockchain implemented as a linked list
class BlockChain{
    Block current;
    BlockChain left, right;
}

class Ledger {
    String name, description, seed;
    BlockChain chain;
    
    void init(Block genesis) {
        //create master account
        Account account = new Account();
        account.address="master";
        account.balance=Integer.MAX_VALUE;
        //create genesis block
        chain = new BlockChain();
        chain.current=genesis;
        chain.current.accounts.put("master", account);
    }
    
    String createAccount(String accountId) {
        Account account = new Account();
        account.address=accountId;
        account.balance=0;
        chain.current.accounts.put(accountId, account);
        return accountId;
    }
    String processTransaction(Transaction transaction) {
        chain.current.accounts.get(transaction.payer).balance=getAccountBalance(transaction.payer)-transaction.amount-transaction.fee;//update payer account
        chain.current.accounts.get(transaction.receiver).balance=getAccountBalance(transaction.receiver)+transaction.amount;//update receiver account
        chain.current.transactions[chain.current.currentTransaction+1]=transaction;//add transaction to block
        chain.current.currentTransaction++;
        //TODO ensure they have enough money
        //TODO if on tenth transaction save and create new block
        return transaction.transactionId;
    }
    int getAccountBalance(String address) {
        return chain.current.accounts.get(address).balance;
    }
    Map getAccountBalances() {
        return chain.current.accounts;
    }
    //tree search to find block we need from root
    Block getBlock(int blockNumber) throws LedgerException {
        if (chain.current.blockNumber==blockNumber)
                return chain.current;
        return getBlock(blockNumber, chain);
    }
    //recursive function to search down the tree
    Block getBlock(int blockNumber, BlockChain b) throws LedgerException{
        if(b.current.blockNumber==blockNumber)
            return b.current;
        if(b.left!=null)
            return getBlock(blockNumber, b.left);
        if(b.right!=null)
            return getBlock(blockNumber, b.right);
        throw new LedgerException("getBlock", "cant find block number");
    }
    
    Transaction getTransaction(String transactionId) throws LedgerException {
        for(int i=0;i<10;i++)
            if(chain.current.transactions[i].transactionId.equals(transactionId))
                return chain.current.transactions[i];
        return getTransaction(transactionId, chain);
    }
    Transaction getTransaction(String transactionId, BlockChain b) throws LedgerException{
        for(int i=0;i<10;i++)
            if(chain.current.transactions[i].transactionId.equals(transactionId))
                return chain.current.transactions[i];
        if(b.left!=null)
            return getTransaction(transactionId, b.left);
        if(b.right!=null)
            return getTransaction(transactionId, b.right);
        throw new LedgerException("getBlock", "cant find transaction");
    }
    
    void validate() {
    }
}

class LedgerException extends Exception{
    String action, reason;
    LedgerException(String a, String r) {
        action=a;
        reason=r;
    }
}

class CommandProcessor {
    //processcommand takes individual commands
    void processCommand(String command, Ledger ledger) throws LedgerException {
        String words[] = command.split(" ");
        words[words.length-1] = words[words.length-1].replace("\n", "").replace("\r", "");//get rid of newline char
        if ("create-account".equals(words[0])) {
            ledger.createAccount(words[1]);
            System.out.println("Account created: " + words[1]);
        }
        if ("process-transaction".equals(words[0])) {
            Transaction transaction = new Transaction();
            transaction.transactionId = words[1];
            transaction.amount = Integer.parseInt(words[3]);
            transaction.fee = Integer.parseInt(words[5]);
            int payer = 0;
            for (int i = 0; i < words.length; i++)
                if ("payer".equals(words[i]))
                    payer = i;
            transaction.note = words[7];
            for (int i = 7; i < payer; i++){
                ledger.description += words[i];
            }
            //process
            transaction.payer=words[payer+1];
            transaction.receiver=words[payer+3];
            System.out.println("transaction " + ledger.processTransaction(transaction)+ " processed\n");
        }
        if ("get-account-balance".equals(words[0]))
            System.out.println(ledger.getAccountBalance(words[1]));
        if ("get-account-balances".equals(words[0]))
            System.out.println(ledger.getAccountBalances());
        if ("get-block".equals(words[0])) {
            ledger.getBlock(Integer.parseInt(words[1]));
            
            //TODO output details for block number
        }
        if ("get-transaction".equals(words[0])) {
            System.out.println("ID: " + ledger.getTransaction(words[1]).transactionId);
            System.out.println("Payer: " + ledger.getTransaction(words[1]).payer);
            System.out.println("Receiver: " + ledger.getTransaction(words[1]).receiver);
            System.out.println("Amount: " +ledger.getTransaction(words[1]).amount);
        }
        if ("validate".equals(words[0])) {
            System.out.println("validate\n");
            ledger.validate();
        }
    }
    
    //takes text file, splits it into lines, and feeds to processCommand
    void processCommandFile(String commandfile) throws IOException, CommandProcessorException, LedgerException {
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
            System.out.println("Ledger Created\n");
            
            //parse name, desc, seed
            ledger.name = words[1];
            int seed = 0;
            for (int i = 0; i < words.length; i++) {
                if ("seed".equals(words[i]))
                    seed = i;
            }
            ledger.description = words[3];
            for (int i = 3; i < seed; i++)//TODO CHECK ON ONE WORD DESC
                ledger.description += words[i];
            ledger.seed = words[seed + 1];
            
            //initialize the blockchain
            Block block = new Block();
            block.setBlock(0,"0","0");
            ledger.init(block);
            
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
    String command, reason;
    int lineNumber;
    public CommandProcessorException(String c, String r, int l){
        command = c;
        reason = r;
        lineNumber = l;
    }
}

public class TestDriver {
    public static void main(String[] args) throws IOException, CommandProcessorException, LedgerException {
        CommandProcessor cp = new CommandProcessor();
        cp.processCommandFile("C:\\Users\\Andrew\\Documents\\NetBeansProjects\\ledger\\target\\classes\\com\\csci97\\ledger\\testinput.txt");
    }
}
