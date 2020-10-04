//model service
//andrew pham anp6338@g.harvard.edu

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;

class Event{
	String type, action, subject;
}

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

class Simulator{
	void simulateEvent(String command){
		Event e = new Event();
		//TODO
	}
}

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

class City{
	String id, name, account;
	Pair<String, String> location;
	int radius;
	Map<String, VirtualDevice> vDevices = new HashMap();
	List people = new ArrayList();
	//List getInfo(){
	//}
	void setInfo(String i, String n, String a, Pair l, int r){
		id=i; name=n; account=a; location=l; radius=r;
	}
	void command(String command){
	}
	void event(Event e){
	}
}

class Person{
	HashMap<String, String> attributes;
	Boolean isResident = true;
}

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
				//set attributes
				for(int i=2;i<words.length;i+=2)
					r.attributes.put(words[i], words[i+1]);
				//set isResident
				if("visitor".equals(words[1]))
					r.isResident=false;
			}
			//devices
			else{
				VirtualDevice d = new VirtualDevice();
				cities.get(words[2]).vDevices.put(words[2], d);//add to devices list of that city
				d.defVDevice(words[1], words[3], new Pair<String, String>(words[6], words[8]), words[9]);
				d.state.put("text", words[11]);
			}
		}
		//show
		if ("show".equals(words[0])) {
			if("city".equals(words[1]))
				//System.out.println(cities.get(words[2]).getInfo());
				System.out.println("todo city info goes here");
			if("person".equals(words[1]))
				//System.out.println(cities.get(words[2]).people.get(words[3]));
				System.out.println("todo person info goes here");
		}
		//update
		if ("update".equals(words[0]))
			//store first token as key in device state, second as value, repeat
			for(int i=1;i<words.length;i+=2)
				cities.get(words[2]).vDevices.get(words[3]).state.put(words[i], words[i+1]);
		//simulate event
		//if ("create".equals(words[0]) && "sensor-event".equals(words[2])) {
		//}
		//void event(Event e) {
		//}
	}
}

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
        	System.out.println(line);
        	c.command(line);
        }
    }
}
