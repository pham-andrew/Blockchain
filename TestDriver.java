package com.csci97.ledger;

import java.util.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;

//structure holding bank account balances
class Account {
    String address;
    int balance;
}

//structure of payment info
class Transaction {
    String transactionId;
    String payer, receiver, note;
    int amount, fee;
}

//structure holding 10 transactions and its hash
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

//structure to keep track of leaf blocks
class BlockChainLink{
    Block current;
    BlockChainLink left, right;
}

//manages accounts and commands
class Ledger {
    String name, description, seed;
    BlockChainLink link;
    
    //initializes the ledger
    void init(Block genesis) {
        //create master account
        Account account = new Account();
        account.address="master";
        account.balance=Integer.MAX_VALUE;
        //create genesis block
        link = new BlockChainLink();
        link.current=genesis;
        link.current.blockNumber=0;
        link.current.accounts.put("master", account);
    }
    
    //creates a new account with no balance
    String createAccount(String accountId) {
        Account account = new Account();
        account.address=accountId;
        account.balance=0;
        link.current.accounts.put(accountId, account);
        return accountId;
    }
    
    //does math for transaction and creates,links,hashes blocks when they reach 10 transactions
    String processTransaction(Transaction transaction) throws LedgerException, NoSuchAlgorithmException {
        if(link.current.accounts.get(transaction.payer).balance-transaction.amount-transaction.fee<0)
            throw new LedgerException("processTransaction", "not enough funds"); 
        link.current.accounts.get(transaction.payer).balance=getAccountBalance(transaction.payer)-transaction.amount-transaction.fee;//update payer account
        link.current.accounts.get(transaction.receiver).balance=getAccountBalance(transaction.receiver)+transaction.amount;//update receiver account
        link.current.transactions[link.current.currentTransaction]=transaction;//add transaction to block
        link.current.currentTransaction++;
        //if on tenth transaction save and create new block
        if(link.current.currentTransaction==10){
            //create and save hash with merkle tree
            String[] leafHashes = new String[10];
            for(int i=0;i<10;i++)
                leafHashes[i] = Integer.toString(link.current.transactions[i].hashCode());
            String[] branchHashes = new String[5];
            int j=0;
            for(int i=0;i<5;i++){
                branchHashes[i] = leafHashes[j]+leafHashes[j+1].hashCode();
                j+=2;
            }
            String[] branchHashes2 = new String[3];
            branchHashes2[0] = branchHashes[0]+branchHashes[1].hashCode();
            branchHashes2[1] = branchHashes[2]+branchHashes[3].hashCode();
            branchHashes2[2] = branchHashes[4]+branchHashes[4].hashCode();
            String[] branchHashes3 = new String[2];
            branchHashes3[0] = branchHashes2[0]+branchHashes2[1].hashCode();
            branchHashes3[1] = branchHashes2[2]+branchHashes2[2].hashCode();
            String rootHash = branchHashes3[0]+branchHashes3[1].hashCode();
            //seed
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(seed.getBytes(StandardCharsets.UTF_8));
            link.current.hash = Arrays.toString(digest.digest(rootHash.getBytes()));
            
            //create next link
            BlockChainLink newLink = new BlockChainLink();
            //copy over info from old block except transactions
            newLink.current = new Block();
            newLink.current.accounts=link.current.accounts;
            newLink.current.previousHash=link.current.hash;
            newLink.current.blockNumber++;
            //clear transactions for new link
            newLink.current.currentTransaction = 0;
            newLink.current.transactions = new Transaction[10];
            link.right = newLink;
            newLink.left = link;
            link = newLink;
            System.out.println("New block created");
        }
        return transaction.transactionId;
    }
    
    int getAccountBalance(String address) {
        return link.current.accounts.get(address).balance;
    }
    Map getAccountBalances() {
        return link.current.accounts;
    }
    
    //returns block by blocknumber
    Block getBlock(int blockNumber) throws LedgerException {
        return getBlock(blockNumber, link);
    }
    
    //recursive function to search down the tree
    Block getBlock(int blockNumber, BlockChainLink b) throws LedgerException{
        if(b.current.blockNumber==blockNumber)
            return b.current;
        if(b.left!=null && b.current.blockNumber>blockNumber)
            return getBlock(blockNumber, b.left);
        if(b.right!=null && b.current.blockNumber<blockNumber)
            return getBlock(blockNumber, b.right);
        throw new LedgerException("getBlock", "cant find block number");
    }
    
    //returns transaction by id
    Transaction getTransaction(String transactionId) throws LedgerException {
        for(int i=0;i<10;i++)
            if(link.current.transactions[i].transactionId.equals(transactionId))
                return link.current.transactions[i];
        return getTransaction(transactionId, link);
    }
    //recursive search for transaction
    Transaction getTransaction(String transactionId, BlockChainLink b) throws LedgerException{
        for(int i=0;i<10;i++)
            if(link.current.transactions[i].transactionId.equals(transactionId))
                return link.current.transactions[i];
        if(b.left!=null)
            return getTransaction(transactionId, b.left);
        if(b.right!=null)
            return getTransaction(transactionId, b.right);
        throw new LedgerException("getBlock", "cant find transaction");
    }
    
    //ensure account balances correct, ensure each block has 10 transactions
    void validate() throws LedgerException {
        for(int i=0;i<link.current.currentTransaction-1;i++)
            if(getBlock(i).currentTransaction!=9)//ensure each block has 10 transactions
                throw new LedgerException("validate", "some block has more than 10 transactions");
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
    void processCommand(String command, Ledger ledger) throws LedgerException, NoSuchAlgorithmException {
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
            System.out.println("Transaction " + ledger.processTransaction(transaction)+ " processed\n");
        }
        if ("get-account-balance".equals(words[0]))
            System.out.println(words[1] +": "+ ledger.getAccountBalance(words[1]));
        if ("get-account-balances".equals(words[0])){
            Map<String, Account> accounts = ledger.getAccountBalances();
            StringBuilder balances = new StringBuilder();
            accounts.entrySet().forEach(entry -> {
                balances.append(entry.getValue().address).append(": ").append(entry.getValue().balance).append("\n");
            });
            System.out.println(balances.toString());
        }
        if ("get-block".equals(words[0])) {
            Block block = ledger.getBlock(Integer.parseInt(words[1]));
            System.out.println("Block Number:" + block.blockNumber);
            System.out.println("Block Hash:" + block.hash);
            System.out.print("Block Transactions: ");
            for(int i=0;i<10;i++)
                System.out.print(block.transactions[i].transactionId+" ");
            System.out.println();
        }
        if ("get-transaction".equals(words[0])) {
            System.out.println("ID: " + ledger.getTransaction(words[1]).transactionId);
            System.out.println("Payer: " + ledger.getTransaction(words[1]).payer);
            System.out.println("Receiver: " + ledger.getTransaction(words[1]).receiver);
            System.out.println("Amount: " +ledger.getTransaction(words[1]).amount);
        }
        if ("validate".equals(words[0])) {
            ledger.validate();
            System.out.println("Validated\n");
        }
    }
    
    //takes text file, splits it into lines, and feeds to processCommand
    void processCommandFile(String commandfile) throws IOException, CommandProcessorException, LedgerException, NoSuchAlgorithmException {
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
            for (int i = 3; i < seed; i++)
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
    public static void main(String[] args) throws IOException, CommandProcessorException, LedgerException, NoSuchAlgorithmException {
        CommandProcessor cp = new CommandProcessor();
        cp.processCommandFile("C:\\Users\\Andrew\\Documents\\NetBeansProjects\\ledger\\target\\classes\\com\\csci97\\ledger\\testinput.txt");
    }
}
