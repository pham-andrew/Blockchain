//controller service
//andrew pham anp6338@g.harvard.edu

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;

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
	public void update();
}
interface Subject{
	public void notify(Event e);
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
	void command(String commmand){
		System.out.println(id +": "+ command);
	}
	Controller c;
	Device self;
	void notify(Event e){
		c.update(e, self);
	}
	Event sensorEvent(Event e){
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
	void sensorEvent(Event event){
		e=d.sensorEvent(event);
	}
	void command(String command){
		d.command(command);
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

//CONTROLLER
//The controller receives and processes all commands.
//The controller prints to the console
//The controller keeps a list of all cities it manages.
class Controller implements Observer{
	Controller c;
	Map<String, City> cities = new HashMap();
	//command controls, defines, and updates people, cities, and devices
	void command(String command) throws CommandException{
		//if empty command, ignore
		if("".equals(command))
			return;
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
				d.defVDevice(words[1], words[3], new Pair<String, String>(words[5], words[7]), words[9], c, words[2]);
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
			e.setEvent(words[5], words[7], "");
			cities.get(words[2]).vDevices.get(words[3]).sensorEvent(e);
			if(words.length==10){//if there is a subject
				e.setEvent(words[5], words[7], words[9]);
				cities.get(words[2]).vDevices.get(words[3]).sensorEvent(e);
			}
		}
		//command
		if("command".equals(words[0]))
			cities.get(words[1]).vDevices.get(words[2]).command(words[3]);
		//announce
		if("announce".equals(words[0])){
			for(Map.Entry<String, HashMap> entry : cities.get(words[1]).vDevices.entrySet()) {
			    HashMap value = entry.getValue();
			    value.command("announcing " + words[1] + "in " + words[2]);
			}
		}
		//scramble: half robots go to help half go to evacuate
		if("scramble".equals(words[0])){
			while(cities.get(words[1]).vDevices.hasNext().hasNext()) { 
            	Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
            	mapElement.getValue().command("addressing emergency at " + words[1] + " " + words[2]);
            	mapElement = (Map.Entry)hmIterator.next();
            	mapElement.getValue().command("helping people find shelter");
        	} 
		}
		//address
		int robotcount=0;
		if("address".equals(words[0])){
			for(Map.Entry<String, HashMap> entry : cities.get(words[2]).vDevices.entrySet()) {
			    if(entry.getValue().type.equals("robot")){
			    	entry.getValue().command("addressing " + words[1] + " at " + words[3] + " " + words[4]);
			    	robotcount++;
			    	if(robotcount==2){
			    		robotcount=0;
			    		break;
			    	}
			    }
			}
		}
		//disable cars
		if("disable_cars".equals(words[0])){
			for(Map.Entry<String, HashMap> entry : cities.get(words[1]).vDevices.entrySet()) {
			    if(entry.getValue().type.equals("car")){
			    	entry.getValue().state.put("enabled", "false");
			    	System.out.println(entry.getValue().id + " disabled");
			    }
			}
		}
		//enable cars
		if("enable_cars".equals(words[0])){
			for(Map.Entry<String, HashMap> entry : cities.get(words[1]).vDevices.entrySet()) {
			    for(Map.Entry<String, HashMap> entry : cities.get(words[1]).vDevices.entrySet())
				    if(entry.getValue().type.equals("car")){
				    	entry.getValue().state.put("enabled", "true");
				    	System.out.println(entry.getValue().id + " enabled");
				    }
			}
		}
		//retrieve child
		if(e.getValue().equals("retrieve_child")){
			String lat = cities.get(words[2]).people.get(words[1]).location.first;//locate child
			string lon = cities.get(words[2]).people.get(words[1]).location.second;
			for(Map.Entry<String, HashMap> entry : cities.get(words[1]).vDevices.entrySet())
				if(entry.getValue().type.equals("robot")){
				    	entry.getValue().command("retrieving " + words[1] + " at " + lat + " " + lon);
				}
		}
	}
	
	//update checks if any rules are triggered and executes commands if necessary
	int CO2Count;
	boolean carsOn;
	Controller(){CO2Count=0; carsOn=true;}//needed to count how many devices report
	void update(Event e, Device origin){
		//camera
		if(e.getType()=="camera"){
			//emergency
			if(e.getValue().equals("fire")||e.getValue().equals("flood")||e.getValue().equals("earthquake")||e.getValue().equals("weather")){
				command("announce " + e.getValue() + " " + origin.city);//announce
				command("scramble" + origin.city);//send half robots to help and send half robots to evacuate others
			}
			if(e.getValue().equals("traffic_accident")){//traffic accident
				origin.command("announcing stay calm help is on the way");//reporting device announces stay calm help is on way
				command("address traffic_accident " + origin.location.first + " " + origin.location.second);//address emergency at location
			}
			//litter
			if(e.getValue().equals("litter")){
				origin.command("says please do not litter");//please do not litter
				command("address litter " + origin.city + " " + origin.location.first + " " + origin.location.second);//robot cleans garbage
				//charge person 50 units for littering
			}
			if(e.getValue().equals("person_seen")){//person seen
				command("update " + e.subject + " lat " + origin.location.first + " long " + origin.location.second);//update person location
			}
			//person boards bus
			if(e.getValue().equals("person_board_bus")){
				origin.command("says hello good to see you");//hello good to see you
				//charge person for bus
			}
			//car parks
			if(e.getValue().equals("car_parks")){
				//charge vehicle for parking 1 hr
			}
		}
		//CO2
		if(e.getType()=="CO2"){
			if(e.getValue>1000){//co2 level over 1000
				//if reported by more than 3 devices, disable all cars
				CO2Count++;
				if(CO2Count>3 || carsOn==true){
					command("disable_cars " + origin.city);
					C02Count=0;
					carsOn=false;
				}
				if(CO2Count>3 || carsOn==false){//co2 level under 1000
					//if reported by more than 3 devices, enable all cars
					command("enable_cars " + origin.city);
					C02Count=0;
					carsOn=true;
				}
			}
		}
		//microphone
		if(e.getType()=="microphone"){
			if(e.getValue.equals("broken_glass"))//sound of broken glass
				command("address broken_glass " + origin.city + " " + origin.location.getKey() + " " + origin.location.getValue());//robot cleans up broken glass at location
			if(e.getValue.startsWith("can you help me find my child")){//asking to help find child
				String child = e.getValue.substring(test.lastIndexOf(" ")+1);//parse last word as child id
				command("retrieve_child " + child + " "+ origin.city);//locate and retrieve child
				origin.command("says stay here we will retrieve the child");
			}
			if(e.getValue.startsWith("Does this bus go to central square?"))//bus route help
				origin.command("says yes");
			if(e.getValue.startsWith("what movies are showing tonight?"))//what movies are showing
				origin.command("says casablanca displays poster");//casablanca
			if(e.getValue.startsWith("reserve 2 seats for the 9 pm showing of Casablanca")){//reserve two seats
				//charge person for two seats 10 units
				origin.command("says seats reserverd");//say seats reserved
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
