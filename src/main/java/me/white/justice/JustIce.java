package me.white.justice;

import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class JustIce {
    private static final Options COMMAND_LINE_OPTIONS = new Options();
    private static final Option HELP_OPTION = Option.builder("h")
            .desc("Show this help screen")
            .longOpt("help")
            .get();
    private static final Option OUT_OPTION = Option.builder("o")
            .desc("Specify the output file")
            .hasArg()
                .type(File.class)
                .argName("file")
            .get();
    private static final Option PARSE_OPTION = Option.builder("p")
            .desc("Parse provided files")
            .longOpt("parse")
            .get();
    private static final Option MARSHAL_OPTION = Option.builder("m")
            .desc("Marshal provided files")
            .longOpt("marshal")
            .get();
    private static final Option COMPACT_OPTION = Option.builder("c")
            .desc("Compact module into a single line")
            .longOpt("compact")
            .get();
    private static final Option DRY_OPTION = Option.builder("d")
            .desc("Do a dry run without producing an output file")
            .longOpt("dry")
            .get();
    private static final Option SIN_OPTION = Option.builder("s").get();
    private final CommandLine commandLine;

    static {
        COMMAND_LINE_OPTIONS.addOption(HELP_OPTION);
        COMMAND_LINE_OPTIONS.addOption(OUT_OPTION);
        COMMAND_LINE_OPTIONS.addOptionGroup(new OptionGroup()
                .addOption(PARSE_OPTION)
                .addOption(MARSHAL_OPTION)
        );
        COMMAND_LINE_OPTIONS.addOption(COMPACT_OPTION);
        COMMAND_LINE_OPTIONS.addOption(DRY_OPTION);
        COMMAND_LINE_OPTIONS.addOption(SIN_OPTION);
    }

    private JustIce(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public static void main(String[] args) {
        CommandLine commandLine;
        try {
            commandLine = new DefaultParser().parse(COMMAND_LINE_OPTIONS, args);
            JustIce program = new JustIce(commandLine);
            program.start();
        } catch (ParseException e) {
            System.err.print("Could not parse command line arguments: " + e);
        }
    }

    private boolean isForParsing(Path[] paths) {
        if (commandLine.hasOption(PARSE_OPTION)) {
            return true;
        }
        if (commandLine.hasOption(MARSHAL_OPTION)) {
            return false;
        }
        boolean isDecompiling = commandLine.hasOption(MARSHAL_OPTION);
        for (Path path : paths) {
            String filename = path.getFileName().toString();
            int dot = filename.lastIndexOf('.');
            if (dot != -1 && filename.substring(dot + 1).equals("json")) {
                return false;
            }
        }
        return true;
    }

    private void start() throws ParseException {
        if (commandLine.hasOption(HELP_OPTION)) {
            help();
            return;
        }
        if (commandLine.hasOption(SIN_OPTION)) {
            sin();
            return;
        }
        String[] files = commandLine.getArgs();
        if (files.length == 0) {
            System.out.println("No files provided for processing");
            return;
        }
        Path[] paths = new Path[files.length];
        for (int i = 0; i < files.length; ++i) {
            try {
                paths[i] = Path.of(files[i]);
            } catch (InvalidPathException e) {
                System.err.print("Invalid file path '" + files[i] + "': " + e);
                return;
            }
        }
        File out = null;
        if (commandLine.hasOption(OUT_OPTION)) {
            out = commandLine.getParsedOptionValue(OUT_OPTION);
        }
        if (isForParsing(paths)) {
            parse(paths, out);
        } else {
            marshal(paths, out);
        }
    }

    private void help() {
        try {
            HelpFormatter.builder().setShowSince(false).get().printHelp("justice", null, COMMAND_LINE_OPTIONS, null, true);
        } catch (IOException e) {
            System.err.print("Could not display help message: " + e);
        }
    }

    private void sin() {
        String[] sins = new String[]{
                "WRATH",
                "GREED",
                "ENVY",
                "LUST",
                "SLOTH",
                "PRIDE",
                "GLUTTONY"
        };
        String[] praise = new String[]{
                "How Righteous.",
                "How Insatiable.",
                "How Inspiring.",
                "How Tender.",
                "How Restful.",
                "How Noble.",
                "How Delectable."
        };
        String[] motive = new String[]{
                "My fury is justified. My anger is pure.",
                "I only want what's mine—and theirs, and his, and yours.",
                "I see what others have, and I ache to take it for myself.",
                "I long for touch—to feel alive again.",
                "The world moves too fast. I crave stillness, and I want no part in its frenzy.",
                "I've carved myself from stone. I bow to no one.",
                "This world has prepared me a vast banquet, and I would be fool to not partake of it."
        };
        String[] truth = new String[]{
                "You wear vengeance like armor, but it corrodes from within. You will become what you aimed to destroy.",
                "The more you take, the less you have. You will starve surrounded by gold.",
                "Your gaze lingers too long on others' light. In time, you will forget how to kindle your own.",
                "You seek warmth in a fleeting fire. It will burn you hollow and leave you cold.",
                "Your apathy will rot your will. Even the soul needs motion, lest it wither in place.",
                "Even statues crack, and when you fall, no one will dare to catch you.",
                "And when the beasts' flesh runs scarce, you would brandish your fangs against your fellow man, beast too that you've become."
        };
        System.out.print("What sin do you relish the most? [" + String.join("/", sins) + "]: ");
        Scanner scanner = new Scanner(System.in);
        String sin;
        dialogue: while (true) {
            sin = scanner.nextLine();
            for (String option : sins) {
                if (sin.equalsIgnoreCase(option)) {
                    break dialogue;
                }
            }
            System.out.print("[WRATH/GREED/ENVY/LUST/SLOTH/PRIDE/GLUTTONY]: ");
        }
        int idx;
        for (idx = 0; idx < sins.length; ++idx) {
            if (sin.equalsIgnoreCase(sins[idx])) {
                break;
            }
        }
        try {
            Thread.sleep(2000);
            System.out.println(praise[idx]);
            Thread.sleep(3000);
            System.out.println("\"" + motive[idx] + "\"");
            Thread.sleep(5000);
            System.out.println(truth[idx]);
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void parse(Path[] paths, File out) {
        if (out == null) {
            out = new File("result.json");
        }
        Parser parser = new Parser();
        parser.setCompact(commandLine.hasOption(COMPACT_OPTION));
        for (Path path : paths) {
            String source = readFile(path);
            if (source == null) {
                return;
            }
            try {
                parser.parse(source);
            } catch (ParsingException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        if (commandLine.hasOption(DRY_OPTION)) {
            System.out.println("Dry run success!");
            return;
        }
        try (FileWriter file = new FileWriter(out)) {
            parser.write(file);
        } catch (IOException e) {
            System.err.print("Could not write file '" + out + "': " + e);
        }
    }

    private void marshal(Path[] paths, File out) {
        if (out == null) {
            out = new File("result.ice");
        }
        Marshal marshal = new Marshal();
        for (Path path : paths) {
            String source = readFile(path);
            if (source == null) {
                return;
            }
            try {
                marshal.marshal(source);
            } catch (MarshalException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        if (commandLine.hasOption(DRY_OPTION)) {
            System.out.println("Dry run success!");
            return;
        }
        try (FileWriter writer = new FileWriter(out)) {
            marshal.write(writer);
        } catch (IOException e) {
            System.err.print("Could not write file '" + out + "': " + e);
        }
    }

    public static String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (NoSuchFileException e) {
            System.err.println("File '" + path + "' does not exist");
        } catch (IOException e) {
            System.err.println("Could not read file '" + path + "': " + e);
        }
        return null;
    }
}
