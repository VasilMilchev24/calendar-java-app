package bg.tu_varna.sit;

import bg.tu_varna.sit.commands.AddHolidayCommand;
import bg.tu_varna.sit.commands.AgendaCommand;
import bg.tu_varna.sit.commands.BookCommand;
import bg.tu_varna.sit.commands.BusyDaysCommand;
import bg.tu_varna.sit.commands.ChangeCommand;
import bg.tu_varna.sit.commands.ClearDayCommand;
import bg.tu_varna.sit.commands.CloseCommand;
import bg.tu_varna.sit.commands.Command;
import bg.tu_varna.sit.commands.ExitCommand;
import bg.tu_varna.sit.commands.FindSlotCommand;
import bg.tu_varna.sit.commands.FindSlotWithCommand;
import bg.tu_varna.sit.commands.HelpCommand;
import bg.tu_varna.sit.commands.MergeCommand;
import bg.tu_varna.sit.commands.OpenCommand;
import bg.tu_varna.sit.commands.RemoveHolidayCommand;
import bg.tu_varna.sit.commands.RescheduleCommand;
import bg.tu_varna.sit.commands.SaveAsCommand;
import bg.tu_varna.sit.commands.SaveCommand;
import bg.tu_varna.sit.commands.UnbookCommand;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.parser.InputParser;
import bg.tu_varna.sit.repository.CalendarRepository;
import bg.tu_varna.sit.repository.XmlCalendarRepository;
import bg.tu_varna.sit.services.CalendarService;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Основно ядро за командите и маршрутизацията.
 */
public class Engine {

    /**
     * Контекст на календара в паметта.
     */
    private final CalendarContext context;

    /**
     * Инстанция на календарната услуга.
     */
    private final CalendarService service;

    /**
     * Парсър на команди с поддръжка на двойни кавички.
     */
    private final InputParser parser;

    /**
     * Регистър на командите.
     */
    private final Map<String, Command> commands;

    /**
     * Създава ядрото на приложението.
     */
    public Engine() {
        this.context = new CalendarContext();
        CalendarRepository repository = new XmlCalendarRepository();
        this.service = new CalendarService(repository);
        this.parser = new InputParser();
        this.commands = new HashMap<String, Command>();
        registerCommands();
    }

    /**
     * Стартира цикъла за четене на команди.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Личен календар (CLI). Въведете 'help' за списък с команди.");

        while (!context.isExitRequested()) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine();
            if (line.trim().length() == 0) {
                continue;
            }

            try {
                String[] args = parser.parseLine(line);
                if (args.length == 0) {
                    continue;
                }

                String commandName = args[0].toLowerCase(Locale.ROOT);
                Command command = commands.get(commandName);
                if (command == null) {
                    System.out.println("Непозната команда. Въведете 'help'.");
                    continue;
                }

                command.execute(args, context, service, scanner);
            } catch (DateTimeParseException e) {
                System.out.println("Невалиден формат за дата/час. Използвайте yyyy-MM-dd и HH:mm.");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Регистрира обработчиците на команди.
     */
    private void registerCommands() {
        String helpText = buildHelpText();

        commands.put("open", new OpenCommand());
        commands.put("close", new CloseCommand());
        commands.put("save", new SaveCommand());
        commands.put("saveas", new SaveAsCommand());
        commands.put("book", new BookCommand());
        commands.put("unbook", new UnbookCommand());
        commands.put("agenda", new AgendaCommand());
        commands.put("change", new ChangeCommand());
        commands.put("findslot", new FindSlotCommand());
        commands.put("findslotwith", new FindSlotWithCommand());
        commands.put("busydays", new BusyDaysCommand());
        commands.put("merge", new MergeCommand());
        commands.put("holiday", new AddHolidayCommand());
        commands.put("removeholiday", new RemoveHolidayCommand());
        commands.put("clearday", new ClearDayCommand());
        commands.put("reschedule", new RescheduleCommand());
        commands.put("help", new HelpCommand(helpText));
        commands.put("exit", new ExitCommand());
    }

    /**
     * Създава статичния помощен текст.
     *
     * @return помощно съобщение
     */
    private String buildHelpText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Налични команди:\n");
        builder.append("  open <filePath>\n");
        builder.append("  close\n");
        builder.append("  save\n");
        builder.append("  saveas <filePath>\n");
        builder.append("  book <yyyy-MM-dd> <HH:mm> <HH:mm> \"name\" \"note\"\n");
        builder.append("  unbook <yyyy-MM-dd> <HH:mm> \"name\"\n");
        builder.append("  agenda <yyyy-MM-dd>\n");
        builder.append("  change <date> <start> \"name\" <newDate> <newStart> <newEnd> \"newName\" \"newNote\"\n");
        builder.append("  findslot <yyyy-MM-dd> <durationHours>\n");
        builder.append("  findslotwith <yyyy-MM-dd> <durationHours> <filePath>\n");
        builder.append("  busydays <from> <to>\n");
        builder.append("  merge <filePath>\n");
        builder.append("  holiday <yyyy-MM-dd>\n");
        builder.append("  removeholiday <yyyy-MM-dd>\n");
        builder.append("  clearday <name of file of calendar> <yyyy-MM-dd>\n");
        builder.append("  reschedule <name of file of calendar> <yyyy-MM-dd>\n");
        builder.append("  help\n");
        builder.append("  exit\n\n");
        builder.append("Бележки:\n");
        builder.append("- Датите са във формат yyyy-MM-dd.\n");
        builder.append("- Часовете са във формат HH:mm.\n");
        builder.append("- Текст с интервали се огражда в двойни кавички.");
        return builder.toString();
    }
}

