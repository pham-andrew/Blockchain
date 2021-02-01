//controller service
//andrew pham anp6338@g.harvard.edu
//runs text input file in argument
//of interest to grader: controller.java and interfaces observer & subject

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class TestDriver {
    public static void main(String[] args) throws IOException, CommandException {
    	String commands = new String(Files.readAllBytes(Paths.get(args.toString())));//args.toString() can just be hard coded with file path
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
        c.c=c;
        for (String line : lines){
        	if(line.length()>1)//if not empty
        		System.out.println(line);//print command to console
        	c.command(line);
        }
    }
}
