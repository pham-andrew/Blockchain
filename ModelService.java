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
}

//PHYSICAL DEVICE
//A deivce can receiver commands and create events from sensors
class Device{
	String microphone, camera;
	int thermometer, co2meter;
	ArrayList<String> getSensorData(){
		ArrayList<String> data = new ArrayList();
		data.add(microphone);
		data.add(camera);
		data.add(Integer.toString(thermometer));
		data.add(Integer.toString(co2meter));
		return data;
	}
	void command(String command){
	}
}

//SIMULATOR
//A simulator takes a command and creates an event as if it was the physical device
class Simulator{
	void simulateEvent(String command){
		Event e = new Event();
		//TODO
	}
}

//VIRTUAL DEVICE
//A virtual devices holds the state of the device.
//Virtual devices receive events and passes them to the controller.
//Virtual devices receive commands and pass them to the device.
class VirtualDevice{
	String id, type, event;
	Pair<String, String> location;
	Map<String, String> state = new HashMap();//state also records required attributes status and enabled
	Device d = new Device();
	Simulator s = new Simulator();
	void command(String command){
	}
	void event(Event e){
	}
	void defVDevice(String t, String i, Pair l, String e){
		type=t; id=t; location=l;
		state.put("enabled", e);
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
	void command(String command){
	}
	void event(Event e){
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
	void command(String command){
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
				d.defVDevice(words[1], words[3], new Pair<String, String>(words[6], words[8]), words[9]);
				d.state.put("text", words[11]);
			}
		}
		//show
		if ("show".equals(words[0])) {
			if("city".equals(words[1]))
				System.out.println(cities.get(words[2]).getInfo());
			if("person".equals(words[1]))
				System.out.println(cities.get(words[2]).people.get(words[3]).getInfo() + "\n");
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
		//if ("create".equals(words[0]) && "sensor-event".equals(words[2])) {
		//}
		//void event(Event e) {
		//}
	}
}

//MODEL SERVICE
//The service contains the main class
//The main class assists with parsing commands and feeds commands to the controller
public class ModelService {
    public static void main(String[] args) throws IOException {
    	String commands = new String(Files.readAllBytes(Paths.get("C:\\Users\\Andrew\\Documents\\JCreator Pro\\MyProjects\\model service\\ModelService\\src\\smart_city_sample.txt")));
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
