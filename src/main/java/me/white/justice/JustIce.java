package me.white.justice;

import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class JustIce {
    private static final Options COMMAND_LINE_OPTIONS = new Options();
    private static final Option HELP_OPTION = Option.builder()
            .desc("Show this help screen")
            .option("h")
            .longOpt("help")
            .get();
    private static final Option OUT_OPTION = Option.builder()
            .desc("Specify the output file")
            .option("o")
            .hasArg()
            .type(File.class)
            .get();
    private static final Option PARSE_OPTION = Option.builder()
            .desc("Parse provided files")
            .option("p")
            .longOpt("parse")
            .get();
    private static final Option MARSHAL_OPTION = Option.builder()
            .desc("Marshal provided files")
            .option("m")
            .longOpt("marshal")
            .get();
    private static final Option COMPACT_OPTION = Option.builder()
            .desc("Pack module into a single line")
            .option("p")
            .longOpt("pack")
            .get();
    private final CommandLine commandLine;

    static {
        COMMAND_LINE_OPTIONS.addOption(HELP_OPTION);
        COMMAND_LINE_OPTIONS.addOption(OUT_OPTION);
        COMMAND_LINE_OPTIONS.addOptionGroup(new OptionGroup()
                .addOption(PARSE_OPTION)
                .addOption(MARSHAL_OPTION)
        );
        COMMAND_LINE_OPTIONS.addOption(COMPACT_OPTION);
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
            System.err.print("Could not parse command line arguments: ");
            e.printStackTrace();
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

    private void start() throws org.apache.commons.cli.ParseException {
        if (commandLine.hasOption(HELP_OPTION)) {
            help();
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
                System.err.println("Invalid file path '" + files[i] + "': ");
                e.printStackTrace();
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
            System.err.print("Could not display help message: ");
            e.printStackTrace();
        }
    }

    private void parse(Path[] paths, File out) {
        if (out == null) {
            out = new File("result.json");
        }
        Parser parser = new Parser();
        parser.setCompact(commandLine.hasOption(COMPACT_OPTION));
        for (Path path : paths) {
            try {
                String source = Files.readString(path);
                parser.parse(source);
            } catch (IOException e) {
                System.err.print("Could not read file '" + path + "': ");
                e.printStackTrace();
                return;
            } catch (ParsingException e) {
                System.out.println("Compile error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        try (FileWriter file = new FileWriter(out)) {
            parser.write(file);
        } catch (IOException e) {
            System.err.print("Could not write file '" + out + "': ");
            e.printStackTrace();
        }
    }

    private void marshal(Path[] paths, File out) {
        if (out == null) {
            out = new File("result.ice");
        }
        Marshal marshal = new Marshal();
        for (Path path : paths) {
            try {
                String source = Files.readString(path);
                marshal.marshal(source);
            } catch (IOException e) {
                System.err.print("Could not read file '" + path + "': ");
                e.printStackTrace();
                return;
            } catch (MarshalException e) {
                System.out.println("Marshal error: " + e.getMessage());
            }
        }
        try (FileWriter writer = new FileWriter(out)) {
            marshal.write(writer);
        } catch (IOException e) {
            System.err.print("Could not write file '" + out + "': ");
            e.printStackTrace();
        }
    }
}
