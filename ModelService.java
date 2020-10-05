//model service
//andrew pham anp6338@g.harvard.edu

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;

//EVENT
//an event has a type action and subject
class Event{
	String type, action, subject;
	void setEvent(String t, String a, String s){
		type=t; action=a; subject=s;
	}
}

//PHYSICAL DEVICE
//A deivce can receiver commands and create events from sensors
class Device{
	String microphone, camera;
	int thermometer, co2meter;
	Event e;
	ArrayList<String> getSensorData(){
		ArrayList<String> data = new ArrayList();
		data.add(microphone);
		data.add(camera);
		data.add(Integer.toString(thermometer));
		data.add(Integer.toString(co2meter));
		return data;
	}
	void command(String command){}
}

//SIMULATOR
//A simulator takes a command and creates an event as if it was the physical device
class Simulator{
	Event simulateEvent(String t, String a, String s){
		Event e = new Event();
		e.setEvent(t, a, s);
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
	void defVDevice(String t, String i, Pair l, String en){
		type=t; id=i; location=l;
		state.put("enabled", en);
	}
	String getInfo(){
		StringBuilder str = new StringBuilder();
		str.append(
			  "ID: "+id+"\n"+
			  "Type: "+type+"\n"+
			  "Lat: "+location.getKey()+" Lon: "+location.getValue()+"\n"+
			  "State: "+state+"\n"+
			  "Event: "+"  Type: " + e.type + " Action: " + e.action + " Subject: " + e.subject+"\n");
		return str.toString();
	}
	void simulateEvent(String t, String v, String subject){
		e=s.simulateEvent(t, v, subject);
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
class Controller {
	Map<String, City> cities = new HashMap();
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
				d.defVDevice(words[1], words[3], new Pair<String, String>(words[5], words[7]), words[9]);
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
			if("resident".equals(words[1]) || "visitor".equals(words[1]))
				for(int i=4;i<words.length;i+=2)
					cities.get(words[2]).people.get(words[3]).attributes.put(words[i], words[i+1]);		
			//device
			else
				//store first token as key in device state, second as value, repeat
				for(int i=4;i<words.length;i+=2)
					cities.get(words[2]).vDevices.get(words[3]).state.put(words[i], words[i+1]);
		//simulate event
		if ("create".equals(words[0]) && "sensor-event".equals(words[1])) {
			cities.get(words[2]).vDevices.get(words[3]).simulateEvent(words[5], words[7], "");
			if(words.length==10)//if there is a subject
				cities.get(words[2]).vDevices.get(words[3]).simulateEvent(words[5], words[7], words[9]);
		}
		//command
		if("command".equals(words[0]))
			cities.get(words[1]).vDevices.get(words[2]).command(words[3]);
	}
}

class CommandException extends Exception{
    String reason;
    public CommandException(String r){
        reason = r;
    }
}

//MODEL SERVICE
//The service contains the main class
//The main class assists with parsing commands and feeds commands to the controller
public class ModelService {
    public static void main(String[] args) throws IOException, CommandException {
    	String commands = new String(Files.readAllBytes(Paths.get("com/cscie97/model/smart_city_sample.txt")));
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
        for (String line : lines){
        	if(line.length()>1)//if not empty
        		System.out.println(line);//print command to console
        	c.command(line);
        }
    }
}
