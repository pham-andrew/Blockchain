interface Observer{
	public void update(Event e, Device origin) throws CommandException;
}