package bg.tu_varna.sit.repository;

import bg.tu_varna.sit.exceptions.CalendarFileException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.Event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Реализация на XML хранилище с ръчно парсване на низове.
 */
public class XmlCalendarRepository implements CalendarRepository {

    /**
     * Създава инстанция на XML хранилището.
     */
    public XmlCalendarRepository() {
    }

    /**
     * Зарежда календарни данни от XML.
     *
     * @param filePath път до XML файл
     * @return зареден контекст
     * @throws CalendarFileException при неуспешно зареждане
     */
    @Override
    public CalendarContext load(String filePath) throws CalendarFileException {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                save(filePath, new CalendarContext());
                return new CalendarContext();
            } catch (CalendarFileException e) {
                throw e;
            }
        }

        StringBuilder xml = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                xml.append(line.trim());
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            try {
                save(filePath, new CalendarContext());
                return new CalendarContext();
            } catch (CalendarFileException ex) {
                throw new CalendarFileException("Неуспешно създаване на липсващ файл: " + filePath, ex);
            }
        } catch (IOException e) {
            throw new CalendarFileException("Неуспешно прочитане на файл: " + filePath, e);
        }

        return parseXml(xml.toString());
    }

    /**
     * Записва календарни данни в XML.
     *
     * @param filePath път до XML файл
     * @param context контекст за запис
     * @throws CalendarFileException при неуспешен запис
     */
    @Override
    public void save(String filePath, CalendarContext context) throws CalendarFileException {
        StringBuilder xml = new StringBuilder();
        xml.append("<calendar>\n");
        xml.append("  <holidays>\n");
        for (LocalDate holiday : context.getHolidays()) {
            xml.append("    <holiday>").append(holiday).append("</holiday>\n");
        }
        xml.append("  </holidays>\n");
        xml.append("  <events>\n");
        for (Event event : context.getEvents()) {
            xml.append("    <event>\n");
            xml.append("      <date>").append(event.getDate()).append("</date>\n");
            xml.append("      <startTime>").append(event.getStartTime()).append("</startTime>\n");
            xml.append("      <endTime>").append(event.getEndTime()).append("</endTime>\n");
            xml.append("      <name>").append(escapeXml(event.getName())).append("</name>\n");
            xml.append("      <note>").append(escapeXml(event.getNote())).append("</note>\n");
            xml.append("    </event>\n");
        }
        xml.append("  </events>\n");
        xml.append("</calendar>\n");

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.print(xml.toString());
        } catch (IOException e) {
            throw new CalendarFileException("Неуспешен запис във файл: " + filePath, e);
        }
    }

    /**
     * Парсва XML ръчно чрез базови операции с низове.
     *
     * @param xml суров XML текст
     * @return парсиран контекст
     * @throws CalendarFileException при невалиден XML формат
     */
    private CalendarContext parseXml(String xml) throws CalendarFileException {
        if (xml.indexOf("<calendar>") < 0 || xml.indexOf("</calendar>") < 0) {
            throw new CalendarFileException("Невалиден XML календар: липсва коренов таг <calendar>.");
        }

        CalendarContext context = new CalendarContext();

        int searchFrom = 0;
        while (true) {
            int holidayStart = xml.indexOf("<holiday>", searchFrom);
            if (holidayStart < 0) {
                break;
            }
            int holidayEnd = xml.indexOf("</holiday>", holidayStart);
            if (holidayEnd < 0) {
                throw new CalendarFileException("Невалиден XML: липсва затварящ таг </holiday>.");
            }
            String value = xml.substring(holidayStart + 9, holidayEnd).trim();
            if (value.length() > 0) {
                try {
                    context.addHoliday(LocalDate.parse(value));
                } catch (Exception e) {
                    throw new CalendarFileException("Невалидна дата за празник: " + value, e);
                }
            }
            searchFrom = holidayEnd + 10;
        }

        searchFrom = 0;
        while (true) {
            int eventStart = xml.indexOf("<event>", searchFrom);
            if (eventStart < 0) {
                break;
            }
            int eventEnd = xml.indexOf("</event>", eventStart);
            if (eventEnd < 0) {
                throw new CalendarFileException("Невалиден XML: липсва затварящ таг </event>.");
            }

            String block = xml.substring(eventStart, eventEnd + 8);
            Event event = parseEventBlock(block);
            context.addEvent(event);

            searchFrom = eventEnd + 8;
        }

        return context;
    }

    /**
     * Парсва един XML блок за събитие.
     *
     * @param block XML блок на събитие
     * @return инстанция на събитие
     * @throws CalendarFileException при невалиден блок
     */
    private Event parseEventBlock(String block) throws CalendarFileException {
        String dateText = extractTagValue(block, "date");
        String startText = extractTagValue(block, "startTime");
        String endText = extractTagValue(block, "endTime");
        String nameText = unescapeXml(extractTagValue(block, "name"));
        String noteText = unescapeXml(extractTagValue(block, "note"));

        try {
            LocalDate date = LocalDate.parse(dateText);
            LocalTime start = LocalTime.parse(startText);
            LocalTime end = LocalTime.parse(endText);
            return new Event(date, start, end, nameText, noteText);
        } catch (Exception e) {
            throw new CalendarFileException("Невалиден XML блок за събитие.", e);
        }
    }

    /**
     * Извлича стойност на таг от XML фрагмент.
     *
     * @param text XML фрагмент
     * @param tag име на таг
     * @return съдържание на тага
     * @throws CalendarFileException при липсващ таг
     */
    private String extractTagValue(String text, String tag) throws CalendarFileException {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = text.indexOf(open);
        int end = text.indexOf(close);
        if (start < 0 || end < 0 || end < start) {
            throw new CalendarFileException("Невалиден XML: липсва таг " + tag);
        }
        return text.substring(start + open.length(), end).trim();
    }

    /**
     * Екранира специалните XML символи.
     *
     * @param text обикновен текст
     * @return екраниран текст
     */
    private String escapeXml(String text) {
        String value = text;
        value = value.replace("&", "&amp;");
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("\"", "&quot;");
        value = value.replace("'", "&apos;");
        return value;
    }

    /**
     * Възстановява екранираните XML символи.
     *
     * @param text екраниран текст
     * @return обикновен текст
     */
    private String unescapeXml(String text) {
        String value = text;
        value = value.replace("&lt;", "<");
        value = value.replace("&gt;", ">");
        value = value.replace("&quot;", "\"");
        value = value.replace("&apos;", "'");
        value = value.replace("&amp;", "&");
        return value;
    }
}
