//controller service
//andrew pham anp6338@g.harvard.edu

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;

//EVENT
//an event has a type value and subject
class Event{
	String type, value, subject;
	void setEvent(String t, String v, String s){
		type=t; value=v; subject=s;
	}
	String getType(){return type;}
	String getValue(){return value;}
	String getSubject(){return subject;}
}

//OBSERVER PATTERN DESIGN
interface Observer{
	public void update(Event e, Device origin) throws CommandException, AuthException;
}
interface Subject{
	public void notify(Event e) throws CommandException, AuthException;
}

//DEVICE
//A deivce can receiver commands and create events from sensors
class Device implements Subject{
	String id;
	Map<String, String> sensors = new HashMap();//values for microphone, camera, thermemeter, and co2 meter held in sensors
	Pair<String, String> location;
	City city;
	Map<String, String> getSensorData(){
		return sensors;
	}
	void action(String command){
		System.out.println(id +": "+ command);
	}
	Controller c;
	Device self;
	public void notify(Event e) throws CommandException, AuthException{
		c.update(e, self);
	}
	Event sensorEvent(Event e) throws CommandException, AuthException{
		sensors.put(e.type, e.value);
		notify(e);
		return e;
	}
}

//VIRTUAL DEVICE
//A virtual devices holds the state of the device.
//Virtual devices receive events and passes them to the controller.
//Virtual devices receive commands and pass them to the device.
class VirtualDevice{
	String id, type;
	Pair<String, String> location;
	Map<String, String> state = new HashMap();//state also records required attributes status and enabled
	Event e = new Event();//to be set by physical device or simulator
	Device d = new Device();
	void defVDevice(String t, String i, Pair l, String en, Controller c, City city){
		type=t; id=i; location=l;
		state.put("enabled", en);
		d.c=c; d.location=l; d.self=d; d.city=city; d.id=id;
	}
	String getInfo(){
		StringBuilder str = new StringBuilder();
		str.append(
			  "ID: "+id+"\n"+
			  "Type: "+type+"\n"+
			  "Lat: "+location.getKey()+" Lon: "+location.getValue()+"\n"+
			  "State: "+state+"\n"+
			  "Event: "+"  Type: " + e.type + " Value: " + e.value + " Subject: " + e.subject+"\n");
		return str.toString();
	}
	void sensorEvent(Event event) throws CommandException, AuthException{
		e=d.sensorEvent(event);
	}
	void command(String command){
		d.action(command);
	}
}

//CITY
//A city holds people and devices.
class City{
	String id, name, account;
	Pair<String, String> location;
	int radius;
	Map<String, VirtualDevice> vDevices = new HashMap();
	Map<String, Person> people = new HashMap();
	void setInfo(String i, String n, String a, Pair l, int r){
		id=i; name=n; account=a; location=l; radius=r;
	}
	//returns string of id, name, account, location, people, and IoT devices
	String getInfo(){
		//add all the people's ids to a list
		List<String> names = new ArrayList<>(people.keySet());
		//add all the devices ids to a list
		List<String> devices = new ArrayList<>(vDevices.keySet());
		//build a string with all the info
		StringBuilder str = new StringBuilder();
		str.append("City: "+id+"\n"+
			  "Name: "+name+"\n"+
			  "Account: "+account+"\n"+
			  "Lat: "+location.getKey()+" Lon: "+location.getValue()+"\n"+
			  "People: "+names+"\n"+
			  "Devices: "+devices+"\n");
		return str.toString();
	}
}

//PERSON
//A person can be a resident or visitor.
//A persons attributes are all stired in Hashmap attributes.
class Person{
	Map<String, String> attributes = new HashMap();
	Boolean isResident = true;
	String id;
	Map<String, String> getInfo(){
		return attributes;
	}
}

//LEDGER
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

//CONTROLLER
//The controller receives and processes all commands.
//The controller prints to the console
//The controller keeps a list of all cities it manages.
class Controller implements Observer{
    Ledger ledger = new Ledger();//singleton
    Authenticator auth = new Authenticator();//singleton
	Controller c;
	String commander; String credential;
	Map<String, City> cities = new HashMap();
	//command controls, defines, and updates people, cities, and devices. returns true if command executed
	boolean command(String command) throws CommandException, AuthException{
		//command parsing
		//if empty command, ignore
		if("".equals(command))
			return false;
		//tokenize command by spaces unless in quotes
		List<String> l = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
		while (m.find())
    		l.add(m.group(1));
    	String words[] = l.toArray(new String[l.size()]);
		words[words.length-1] = words[words.length-1].replace("\n", "").replace("\r", "");//get rid of newline char
		
		//model commands
		//define
		if ("define".equals(words[0])) {
			//city
			if("city".equals(words[1])){
				//check for city admin permissions
				if(auth.hasPermission(commander, credential, "scms_define_city").active){//TODO AND TIME OUT
					City c = new City();
					c.setInfo(words[2], words[4], words[6], new Pair<String, String>(words[8], words[10]), Integer.parseInt(words[12]));
					cities.put(words[2], c);
				}
				else throw new AuthException("No Permission");
			}
			//people
			else if("resident".equals(words[1]) || "visitor".equals(words[1])){
				//check for city admin permissions
				if(auth.hasPermission(commander, credential, "scms_manage_city").active){
					Person r = new Person();
					cities.get(words[2]).people.put(words[3], r);//assign person to city
					//set attributes
					r.id=words[3];
					for(int i=4;i<words.length;i+=2)
						r.attributes.put(words[i], words[i+1]);
					//set isResident
					if("visitor".equals(words[1]))
						r.isResident=false;
				}
			}
			//devices
			else{
				//check for device admin permissions
				if(auth.hasPermission(commander, credential, "scms_manage_device").active){
					VirtualDevice d = new VirtualDevice();
					cities.get(words[2]).vDevices.put(words[3], d);//add to devices list of that city
					d.defVDevice(words[1], words[3], new Pair<String, String>(words[5], words[7]), words[9], c, cities.get(words[2]));
					for(int i=8;i<words.length;i+=2)
						d.state.put(words[i], words[i+1]);
				}
			}
		}
		//show
		if ("show".equals(words[0])) {
			//if city id can't be found, throw exception
			if(cities.get(words[2])==null)
				throw new CommandException("Cannot find city");
			//print
			if("city".equals(words[1]))
				System.out.println(cities.get(words[2]).getInfo());
			if("person".equals(words[1])){
				//exception if cant find person
				if(cities.get(words[2]).people.get(words[3])==null)
					throw new CommandException("Cannot find person");
				System.out.println(cities.get(words[2]).people.get(words[3]).getInfo() + "\n");
			}
			if("device".equals(words[1])){
				//exception if cant find device
				if(words.length>3)
					if(cities.get(words[2]).vDevices.get(words[3])==null)
						throw new CommandException("Cannot find device");
				//show devices
				if(words.length>3){//if device is specified
					System.out.println("\n" + cities.get(words[2]).vDevices.get(words[3]).getInfo());
				}else{//show all devices in city
					Map<String, VirtualDevice> map = cities.get(words[2]).vDevices;
					for(Map.Entry<String, VirtualDevice> entry : map.entrySet())
						System.out.println("\n" + entry.getValue().getInfo());
				}
			}
		}
		//update
		if ("update".equals(words[0]))
			//person
			if("resident".equals(words[1]) || "visitor".equals(words[1]) || "person".equals(words[1]))
				if(auth.hasPermission(commander, credential, "scms_manage_city").active)
					for(int i=4;i<words.length;i+=2)
						cities.get(words[2]).people.get(words[3]).attributes.put(words[i], words[i+1]);	
			//device
			else
				//store first token as key in device state, second as value, repeat
				if(auth.hasPermission(commander, credential, "scms_manage_city").active)
					for(int i=4;i<words.length;i+=2)
						cities.get(words[2]).vDevices.get(words[3]).state.put(words[i], words[i+1]);
		
		//event
		if ("create".equals(words[0]) && "sensor-event".equals(words[1])) {
			Event e = new Event();
			if(words.length==10){//if there is a subject
				e.setEvent(words[5], words[7], words[9]);
				cities.get(words[2]).vDevices.get(words[3]).sensorEvent(e);
			}else{
				e.setEvent(words[5], words[7], "");
				cities.get(words[2]).vDevices.get(words[3]).sensorEvent(e);
			}
		}
		
		//triggered commands
		//command
		if("command".equals(words[0]))
			cities.get(words[1]).vDevices.get(words[2]).command(words[3]);
		//announce
		if("announce".equals(words[0])){
			for(Map.Entry<String, VirtualDevice> entry : cities.get(words[2]).vDevices.entrySet())
			    entry.getValue().command("announcing " + words[1] + " in " + words[2]);
			System.out.println();
		}
		//scramble: half robots go to help half go to evacuate
		if("scramble".equals(words[0])){
			boolean helping=true;//we need to send half to help so every other will be helping
			for(Map.Entry<String, VirtualDevice> entry : cities.get(words[1]).vDevices.entrySet()){
				if(helping){
					entry.getValue().command("addressing " + words[2] + " at " + words[3] + " " + words[4]);
					helping=false;
				}else{
					entry.getValue().command("helping people find shelter");
					helping=true;
				}
			}
			System.out.println();
		}
		//address
		if("address".equals(words[0])){
			for(Map.Entry<String, VirtualDevice> entry : cities.get(words[2]).vDevices.entrySet()) {
			    if(entry.getValue().type.equals("robot")){
			    	entry.getValue().command("addressing " + words[1] + " at " + words[3] + " " + words[4]);
			    	break;
			    }
			}
			System.out.println();
		}
		//disable cars
		if("disable_cars".equals(words[0])){
			for(Map.Entry<String, VirtualDevice> entry : cities.get(words[1]).vDevices.entrySet()) {
			    if(entry.getValue().type.equals("vehicle")){
			    	entry.getValue().state.put("enabled", "false");
			    	System.out.println(entry.getValue().id + " disabled");
			    }
			}
			System.out.println();
		}
		//enable cars
		if("enable_cars".equals(words[0])){
			for(Map.Entry<String, VirtualDevice> entry : cities.get(words[1]).vDevices.entrySet())
				if(entry.getValue().type.equals("vehicle")){
			    	entry.getValue().state.put("enabled", "true");
				    System.out.println(entry.getValue().id + " enabled");
				}
			System.out.println();
		}
		//find child
		if("find".equals(words[0])){
			String lat = cities.get(words[2]).people.get(words[1]).getInfo().get("lat");//locate child
			String lon = cities.get(words[2]).people.get(words[1]).getInfo().get("long");
			for(Map.Entry<String, VirtualDevice> entry : cities.get(words[2]).vDevices.entrySet())
				if(entry.getValue().type.equals("robot")){
				    	entry.getValue().command("retrieving " + words[1] + " at " + lat + " " + lon);
				    	break;
				}
			System.out.println();
		}
		
		//ledger commands
		//withdraw
		if("withdraw".equals(words[0]))
			if(!ledger.withdraw(Integer.parseInt(words[1]), cities.get(words[3]).people.get(words[2]).getInfo().get("account")))
				return false;
		//deposit
		if("deposit".equals(words[0]))
			ledger.deposit(Integer.parseInt(words[1]), cities.get(words[3]).people.get(words[2]).getInfo().get("account"));
		if("create-account".equals(words[0]))
			ledger.createAccount(Integer.parseInt(words[2]), words[1]);
		//parking
		if("parking".equals(words[0]))
			if(!ledger.withdraw(Integer.parseInt(words[1]), cities.get(words[3]).people.get(cities.get(words[3]).vDevices.get(words[2]).state.get("driver")).getInfo().get("account")))
				return false;
		
		
		//authenticator commands
		//login
		if("login".equals(words[0])){
			commander=words[1];
			credential=words[2];
		}
		//define permission
		if("define_permission".equals(words[0]))
			auth.definePermission(words[1],words[2],words[3]);
		//define roles
		if("define_role".equals(words[0]))
			auth.defineRole(words[1],words[2],words[3]);
		//define resource
		if("define_resource".equals(words[0]))
			auth.defineResource(words[1]);
		//add permission
		if("add_permission_to_role".equals(words[0]))
			auth.addPermission(words[1],words[2]);
		//create user
		if("create_user".equals(words[0]))
			auth.createUser(words[1],words[2]);
		//add user credential
		if("add_user_credential".equals(words[0]))
			auth.addCredential(words[1],words[3]);
		//add role to user
		if("add_role_to_user".equals(words[0]))
			auth.addRole(words[1],words[2]);
		//add resource to role
		if("add_resource_to_role".equals(words[0]))
			auth.addResource(words[1],words[2]);
		//printout
		if("auth_printout".equals(words[0]))
			auth.printOut();
			
		
		return true;
	}
	
	//update checks if any rules are triggered and executes commands if necessary
	int CO2Count;
	boolean carsOn;
	Controller(){CO2Count=0; carsOn=true;}//needed to count how many devices report
	public void update(Event e, Device origin) throws CommandException, AuthException{
		//camera
		if(e.getType().equals("camera")){
			//emergency
			if(e.getValue().equals("fire")||e.getValue().equals("flood")||e.getValue().equals("earthquake")||e.getValue().equals("weather")){
				command("announce " + e.getValue() + " " + origin.city.id);//announce
				command("scramble" + origin.city);//send half robots to help and send half robots to evacuate others
			}
			if(e.getValue().equals("traffic_accident")){//traffic accident
				origin.action("announcing stay calm help is on the way");//reporting device announces stay calm help is on way
				command("address traffic_accident " + origin.city.id + " " + origin.location.getKey() + " " + origin.location.getValue());//address emergency at location
			}
			//litter
			if(e.getValue().equals("littering")){
				origin.action("says please do not litter");//please do not litter
				command("address litter " + origin.city.id + " " + origin.location.getKey() + " " + origin.location.getValue());//robot cleans garbage
				command("withdraw 10 " + e.getSubject() + " " + origin.city.id); //charge person for two seats 10 units
			}
			if(e.getValue().equals("person_seen")){//person seen
				command("update person " + origin.city.id + " " + e.subject + " lat " + origin.location.getKey() + " long " + origin.location.getValue());//update person location
			}
			//person boards bus
			if(e.getValue().equals("boards_bus")){
				origin.action("says hello good to see you\n");//hello good to see you
				command("withdraw 10 " + e.getSubject() + " " + origin.city.id);//charge person for bus
			}
			//car parks
			if(e.getValue().equals("parked"))
				command("parking 10 " + e.getSubject() + " " + origin.city.id);//charge vehicle for parking 1 hr
		}
		//CO2
		if(e.getType().equals("co2meter")){
			if(Integer.parseInt(e.getValue())>=1000){//co2 level over 1000
				//if reported by more than 3 devices, disable all cars
				CO2Count++;
				if(CO2Count>3 && carsOn==true){
					command("disable_cars " + origin.city.id);
					CO2Count=0;
					carsOn=false;
				}
			}else if(Integer.parseInt(e.getValue())<1000){
				//if reported by more than 3 devices, enable all cars
				CO2Count++;
				if(CO2Count>=3 && carsOn==false){//co2 level under 1000
					command("enable_cars " + origin.city.id);
					CO2Count=0;
					carsOn=true;
				}
			}
		}
		//microphone
		if(e.getType().equals("microphone")){
			if(e.getValue().equals("broken_glass_sound"))//sound of broken glass
				command("address broken_glass " + origin.city.id + " " + origin.location.getKey() + " " + origin.location.getValue());//robot cleans up broken glass at location
			if(e.getValue().startsWith("find")){//asking to help find person
				command("find " + e.getValue().substring(5) + " " + origin.city.id);//locate and retrieve child
				origin.action("says stay here we will retrieve the child\n");
			}
			if(e.getValue().startsWith("Does_this_bus_go_to_central_square?"))//bus route help
				origin.action("says yes\n");
			if(e.getValue().startsWith("what_movies_are_showing_tonight?"))//what movies are showing
				origin.action("says casablanca displays poster\n");//casablanca
			if(e.getValue().equals("reserve_2_seats_for_the_9_pm_showing_of_Casablanca")){//reserve two seats
				if(command("withdraw 10 " + e.getSubject() + " " + origin.city.id)) //charge person for two seats 10 units
					origin.action("says seats reserverd\n");//say seats reserved
				else
					origin.action("insufficient funds");
			}
		}
	}
}

interface Visitor{
	public boolean visit(String id, String user);
}
interface Visitable{
	public boolean accept(String id);
}
interface Composite{
	public void add(String id, Permission p, Resource r);
}

//AUTHENTICATION TOKEN
class AuthToken{
	String id;
	long expiration;
	boolean active;
}

//AUTHENTICATOR
class Authenticator implements Visitor{
	Map<String, User> users = new HashMap();
	Map<String, Role> roles = new HashMap();
	Map<String, Permission> permissions = new HashMap();
	Map<String, Resource> resources = new HashMap();
	AuthToken hasPermission(String user, String credential, String permission) throws AuthException{
		AuthToken a = new AuthToken();
		a.active=false;
		if(visit(permission, user) && users.get(user).credential.equals(credential)){
			a.id=permission;
			a.expiration=System.currentTimeMillis() + 120000;
			a.active=true;
		}
		if(!users.get(user).credential.equals(credential))
			throw new AuthException("wrong credential");
		return a;
	}
	void definePermission(String id, String name, String description){
		Permission p = new Permission(); 
		p.id=id; p.name=name; p.description=description;
		permissions.put(id, p);
	}
	void defineRole(String id, String name, String description){
		Role r = new Role();
		r.id=id; r.name=name; r.description=description;
		roles.put(id, r);
	}
	void defineResource(String id){
		Resource r = new Resource();
		r.id=id;
		resources.put(id, r);
	}
	void addPermission(String role, String permission){
		roles.get(role).add(permission, permissions.get(permission), null);
	}
	void addResource(String role, String resource){
		roles.get(role).add(resource, null, resources.get(resource));
	}
	void createUser(String id, String username){
		User u = new User();
		u.id=id; u.username=username;
		users.put(id, u);
	}
	void addCredential(String id, String credential){
		users.get(id).credential=credential;
	}
	void addRole(String id, String role){
		users.get(id).roles.put(role, roles.get(role));
	}
	void printOut(){
		System.out.println("defined permissions: " + permissions);
		System.out.println("defined roles: " + roles);
		System.out.println("defined resources: " + resources);
		for(Map.Entry<String, Role> entry : roles.entrySet()) {
			System.out.println(entry.getValue().id + " Permissions: " + entry.getValue().permissions + " Resources: " + entry.getValue().resources );
		}
		
		System.out.println("users: " + users);
		for(Map.Entry<String, User> entry : users.entrySet()) {
			entry.getValue().printOut();
		}
		System.out.println();
	}
	public boolean visit(String id, String user){
		if(users.get(user).accept(id))
			return true;
		return false;
	}
}

//USER
class User implements Visitable{
	Map<String, Permission> permissions = new HashMap();
	Map<String, Role> roles = new HashMap();
	String id, username, credential;
	void assignRole(String id, Role r){
		roles.put(id, r);
	}
	void assignPermission(String id, Permission p){
		permissions.put(id, p);
	}
	void printOut(){
		System.out.println("id: " + id + " roles: " + roles + " permissions: " + permissions + " credential: " + credential);
	}
	public boolean accept(String id){
		for(Map.Entry<String, Permission> entry : permissions.entrySet())
			if(entry.getValue().accept(id))
				return true;
		for(Map.Entry<String, Role> entry : roles.entrySet())
			if(entry.getValue().accept(id))
				return true;
		return false;
	}
}

//Entitlements
class Permission implements Visitable{
	String id;
	String name;
	String description;
	public boolean accept(String i){
		if(id.equals(i))
			return true;
		return false;
	}
}
class Resource implements Visitable{
	String id;
	public boolean accept(String i){
		if(id.equals(i))
			return true;
		return false;
	}
}
class Role implements Composite, Visitable{
	Map<String, Permission> permissions = new HashMap();
	Map<String, Resource> resources = new HashMap();
	String id;
	String name;
	String description;
	public void add(String id, Permission p, Resource r){
		if(r==null)
			permissions.put(id, p);
		else
			resources.put(id, r);
	}
	public boolean accept(String i){
		for(Map.Entry<String, Permission> entry : permissions.entrySet()) {
			if(entry.getValue().accept(i))
				return true;
		}
		for(Map.Entry<String, Resource> entry : resources.entrySet()) {
			if(entry.getValue().accept(i))
				return true;
		}
		return false;
	}
}

//EXCEPTIONS
//Checks if we are referencing an object correctly in our commands
class CommandException extends Exception{
    String reason;
    public CommandException(String r){
        reason = r;
    }
}
class AuthException extends Exception{
    String reason;
    public AuthException(String r){
        reason = r;
    }
}

public class TestDriver {
    public static void main(String[] args) throws IOException, CommandException, AuthException {
    	String commands = new String(Files.readAllBytes(Paths.get("C:\\Users\\Andrew\\Documents\\JCreator Pro\\MyProjects\\TestDriver\\script.txt")));
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
        //process each line
        Controller c = new Controller();
        c.c=c;
        for (String line : lines){
        	if(line.length()>1)//if not empty
        		System.out.println(line);//print command to console
        	c.command(line);
        }
    }
}
