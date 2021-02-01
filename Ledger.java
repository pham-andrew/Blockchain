import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;

//LEDGER
//Part of ledger service
//simple ledger to keep track of bank account balances
class Ledger{
	Map<String, Integer> accounts = new HashMap();//key is account id, value is balance
	public void createAccount(Integer amount, String accountid){
		accounts.put(accountid, amount);
	}
	public void deposit(Integer amount, String account){
		accounts.put(account, accounts.get(account)+amount);
		System.out.println("Deposited " + amount + " to " + account + "\n");
	}
	public boolean withdraw(Integer amount, String account){
		if(accounts.get(account)-amount > 0){
			accounts.put(account, accounts.get(account)+amount);
			System.out.println("Withdrew " + amount + " from " + account + "\n");
			return true;
		}
		return false;
	}
}