//model service
//andrew pham anp6338@g.harvard.edu

import java.util.HashMap;

class Event{
	String type, action subject;
}

class Device{
	String microphone, camera;
	int thermometer, co2meter;
	List<String> getSensorData(){
		List<String> data = new List();
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

class VirtualDevice(){
	String id, type, event;
	Pair<double, double> location;
	boolean status, enabled; //TODO INCORPERATE INTO STATE
	Hashmap<String, String> state;
	Device d;
	Simulator s;
	void command(String command){
	}
	void event(Event e){
	}
	void defVDevice(String t, String i, Pair l, boolean enabled){
	}
	void setState(String attribute, String value){
	}
}

class City{
	String id, name, account;
	Pair<double, double> location;
	int radius;
	List vDevices = new ArrayList();
	List residents = new ArrayList();
	List visitors = new ArrayList();
	List getInfo(){
	}
	void setInfo(String i, String n, String a, Pair l, int r){
		id=i; name=n; account=a; location=l; radius=r;
	}
	void command(String command){
	}
	void event(Event e){
	}
}

class Person{
	String id, bMetric, name, role, account;
	int phoneNumber;
	Pair<double, double> location;
	List getInfo(){
	}
	List setInfo(List l){
	}
}

class Controller {
	Hashmap<Integer, City> cities = new Hashmap();
	void command(String command){
		//tokenize command by spaces unless in quotes
		List<String> words = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
		while (m.find())
    		list.add(m.group(1));
		words[words.length-1] = words[words.length-1].replace("\n", "").replace("\r", "");//get rid of newline char
		
		//show
		if ("show".equals(words[0])) {
			if("city".equals(words[1])){
				System.out.println(cities.get(words[2]).getInfo());
			}
			if("person".equals(words[1])){
				
			}
		}
		//define
		if ("define".equals(words[0])) {
			//city
			if("city".equals(words[1])){
				City c = new City();
				c.setInfo(words[2], words[4], words[6], new Pair<double, double>(Integer.parseInt(words[8]), Integer.parseInt(words[10]));, words[12]);
				cities.put(words[2], c);
			}
			//people
			else if("resident".equals(words[1])){
			}
			else if("visitor".equals(words[1])){
			}
			//devices
			else{
				VirtualDevice d = new VirtualDevice();
				cities.get(words[2]).vDevices.add(d);//add to devices list of that city
				d.defVDevice(words[1], words[3], new Pair<double, double>(Integer.parseInt(words[6]), Integer.parseInt(words[8]));, words[9]);
				d.setState("text", words[11]);
			}
		}
		//update
		if ("update".equals(words[0])) {
			//store first token as key in device state, second as value, repeat
			for(int i=1;i<words.length;i+=2){
				cities.get(words[2]).vDevices.
			}
		}
		//simulate event
		if ("create".equals(words[0]) && "sensor-event".equals(words[2])) {
			
		}
		void event(Event e) {
			
		}
}

public class ModelService {
    public static void main(String[] args) {
    	//controller c = new controller();
        //c.processCommandFile("ledger.script");
        System.out.println("hi");
    }
}
