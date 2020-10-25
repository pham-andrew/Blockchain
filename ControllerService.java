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
	public void update(Event e, Device origin) throws CommandException;
}
interface Subject{
	public void notify(Event e) throws CommandException;
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
	public void notify(Event e) throws CommandException{
		c.update(e, self);
	}
	Event sensorEvent(Event e) throws CommandException{
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
	Simulator s = new Simulator();
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
	void sensorEvent(Event event) throws CommandException{
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
	}
	public boolean withdraw(Integer amount, String account){
		if(accounts.get(account)-amount > 0){
			accounts.put(account, accounts.get(account)+amount);
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
    Ledger ledger = new Ledger();
	Controller c;
	Map<String, City> cities = new HashMap();
	//command controls, defines, and updates people, cities, and devices. returns true if command executed
	boolean command(String command) throws CommandException{
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
		//define
		if ("define".equals(words[0])) {
			//city
			if("city".equals(words[1])){
				City c = new City();
				c.setInfo(words[2], words[4], words[6], new Pair<String, String>(words[8], words[10]), Integer.parseInt(words[12]));
				cities.put(words[2], c);
			}
			//people
			else if("resident".equals(words[1]) || "visitor".equals(words[1])){
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
			//devices
			else{
				VirtualDevice d = new VirtualDevice();
				cities.get(words[2]).vDevices.put(words[3], d);//add to devices list of that city
				d.defVDevice(words[1], words[3], new Pair<String, String>(words[5], words[7]), words[9], c, cities.get(words[2]));
				for(int i=8;i<words.length;i+=2)
					d.state.put(words[i], words[i+1]);
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
				for(int i=4;i<words.length;i+=2)
					cities.get(words[2]).people.get(words[3]).attributes.put(words[i], words[i+1]);		
			//device
			else
				//store first token as key in device state, second as value, repeat
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
			if(ledger.withdraw(Integer.parseInt(words[1]), cities.get(words[3]).people.get(words[2]).getInfo().get("account")))
				return false;
		//deposit
		if("deposit".equals(words[0]))
			ledger.deposit(Integer.parseInt(words[1]), cities.get(words[3]).people.get(words[2]).getInfo().get("account"));
		if("create-account".equals(words[0]))
			ledger.createAccount(Integer.parseInt(words[2]), words[1]);
		return true;
	}
	
	//update checks if any rules are triggered and executes commands if necessary
	int CO2Count;
	boolean carsOn;
	Controller(){CO2Count=0; carsOn=true;}//needed to count how many devices report
	public void update(Event e, Device origin) throws CommandException{
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
				command("withdraw 10 " + e.getSubject() + " " + origin.city.id);//charge vehicle for parking 1 hr
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
			if(e.getValue().startsWith("reserve_2_seats_for_the_9 pm_showing_of_Casablanca")){//reserve two seats
				if(command("withdraw 10 " + e.getSubject() + " " + origin.city.id)) //charge person for two seats 10 units
					origin.action("says seats reserverd\n");//say seats reserved
				else
					origin.action("insufficient funds");
			}
		}
	}
}

//COMMAND EXCEPTION
//Checks if we are referencing an object correctly in our commands
class CommandException extends Exception{
    String reason;
    public CommandException(String r){
        reason = r;
    }
}


public class ControllerService {
    public static void main(String[] args) throws IOException, CommandException {
    	String commands = new String(Files.readAllBytes(Paths.get("C:\\Users\\Andrew\\Documents\\JCreator Pro\\MyProjects\\ControllerService\\src\\test.txt")));
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
