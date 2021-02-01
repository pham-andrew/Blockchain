//EVENT
//describes a sensor event
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