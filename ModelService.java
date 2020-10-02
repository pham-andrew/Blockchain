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
	boolean status, enabled;
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
		String words[] = command.split(" ");
        words[words.length-1] = words[words.length-1].replace("\n", "").replace("\r", "");//get rid of newline char
	if ("define".equals(words[0]) && "city".equals(words[1])) {
		City c = new City();
		c.setInfo(words[2], words[4], words[6], Pair<double, double> pair = new Pair<>(Integer.parseInt(words[8]), Integer.parseInt(words[10]));, words[12]);
		cities.put(words[2], c);
	}
	if ("show".equals(words[0]) && "city".equals(words[1])) {
		System.out.println(cities.get(words[2]).getInfo());
	}
	if ("define".equals(words[0])) {
		VirtualDevice d = new VirtualDevice();
		//todo words[2] is city
		//todo everything after : in words[2] is device id
		d.defVDevice(words[1], words[2], Pair<double, double> pair = new Pair<>(Integer.parseInt(words[5]), Integer.parseInt(words[7]));, words[9]);
		d.setState(words[10], )//TODO BUILD STRING OF TEXT FROM END
	}
	void event(Event e){
	}
}

public class ModelService {
    public static void main(String[] args) {
    	//controller c = new controller();
        //c.processCommandFile("ledger.script");
        System.out.println("hi");
    }
}
