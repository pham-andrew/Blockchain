//model service
//andrew pham anp6338@g.harvard.edu

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
	void setInfo(List l){
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
	List cities = new ArrayList();
	void command(String command){
		String words[] = command.split(" ");
        words[words.length-1] = words[words.length-1].replace("\n", "").replace("\r", "");//get rid of newline char
	if ("define".equals(words[0]) && "city".equals(words[1])) {
		
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
