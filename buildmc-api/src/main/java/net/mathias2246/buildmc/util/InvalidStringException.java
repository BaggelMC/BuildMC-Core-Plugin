package net.mathias2246.buildmc.util;

import java.util.regex.Pattern;

public class InvalidStringException extends RuntimeException {

    public static void throwIfInvalid(String string, Pattern validPattern) {
        if (!validPattern.matcher(string).matches()) {
            throw new InvalidStringException(validPattern.pattern());
        }
    }

    public InvalidStringException(String patternString) {
        super("The string didn't match the pattern '" + patternString + "'");
    }
}
