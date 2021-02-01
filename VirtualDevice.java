//VIRTUAL DEVICE
//Part of city model service
//A virtual devices holds the state of the device.
//Virtual devices receive events and passes them to the controller.
//Virtual devices receive commands and pass them to the device.

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;

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