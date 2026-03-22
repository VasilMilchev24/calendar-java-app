package bg.tu_varna.sit.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Парсва входа от командния ред с ръчна токенизация и поддръжка на кавички.
 */
public class InputParser {

    /**
     * Създава инстанция на парсъра за входни команди.
     */
    public InputParser() {
    }

    /**
     * Парсва един ред команда в масив от аргументи.
     *
     * @param line суров вход от конзолата
     * @return масив с парсирани аргументи
     */
    public String[] parseLine(String line) {
        List<String> tokens = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (ch == '"') {
                insideQuotes = !insideQuotes;
                i++;
                continue;
            }

            if (Character.isWhitespace(ch) && !insideQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
            i++;
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        String[] result = new String[tokens.size()];
        int index = 0;
        for (String token : tokens) {
            result[index] = token;
            index++;
        }
        return result;
    }
}
