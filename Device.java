//DEVICE
//Part of city model service
//A deivce can receiver commands and create events from sensors

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;

class Device implements Subject{
	String id;
	Map<String, String> sensors = new HashMap();//values for microphone, camera, thermometer, and co2 meter held in sensors
	Pair<String, String> location;
	City city;
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