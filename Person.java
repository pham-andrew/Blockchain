import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;

//PERSON
//Part of city model service
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