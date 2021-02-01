//COMMAND EXCEPTION
//Checks if we are referencing an object correctly in our commands

import java.util.*;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.regex.*;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;


class CommandException extends Exception{
    String reason;
    public CommandException(String r){
        reason = r;
    }
}