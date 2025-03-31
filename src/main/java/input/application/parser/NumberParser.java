package input.application.parser;

public class NumberParser {
    public static Number parse(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value, e);
        }
    }
}