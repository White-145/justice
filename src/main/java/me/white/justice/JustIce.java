package me.white.justice;

import me.white.justice.parser.Handler;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JustIce {
    private static final Options COMMAND_LINE_OPTIONS = new Options();
    private static final Option HELP_OPTION = new Option("h", "help", false, "Shows this screen");
    private static final Option OUT_OPTION = new Option("o", true, "Specifies the output file");
    private static final Option COMPILE_OPTION = new Option("c", "compile", false, "Compiles provided files");
    private static final Option DECOMPILE_OPTION = new Option("d", "decompile", false, "Deompiles provided files");
    private final CommandLine commandLine;

    static {
        COMMAND_LINE_OPTIONS.addOption(HELP_OPTION);
        OUT_OPTION.setType(File.class);
        COMMAND_LINE_OPTIONS.addOption(OUT_OPTION);
        OptionGroup compilationOptions = new OptionGroup();
        compilationOptions.addOption(COMPILE_OPTION);
        compilationOptions.addOption(DECOMPILE_OPTION);
        COMMAND_LINE_OPTIONS.addOptionGroup(compilationOptions);
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

    private JustIce(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    private boolean isCompiling(Path[] paths) {
        if (commandLine.hasOption(COMPILE_OPTION)) {
            return true;
        }
        if (commandLine.hasOption(DECOMPILE_OPTION)) {
            return false;
        }
        boolean isDecompiling = commandLine.hasOption(DECOMPILE_OPTION);
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
        if (isCompiling(paths)) {
            compile(paths, out);
        } else {
            decompile(paths, out);
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

    private void compile(Path[] paths, File out) {
        if (out == null) {
            out = new File("result.json");
        }
        List<Handler> handlers = new ArrayList<>();
        for (Path path : paths) {
            try {
                String source = Files.readString(path);
                handlers.addAll(Compiler.parse(source));
            } catch (IOException e) {
                System.err.print("Could not read file '" + path + "': ");
                e.printStackTrace();
                return;
            } catch (CompilationException e) {
                System.out.println("Compile error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        try (FileWriter file = new FileWriter(out)) {
            Compiler.write(file, handlers);
        } catch (IOException e) {
            System.err.print("Could not write file '" + out + "': ");
            e.printStackTrace();
        }
    }

    private void decompile(Path[] paths, File out) {
        if (out == null) {
            out = new File("result.ice");
        }
        List<Handler> handlers = new ArrayList<>();
        for (Path path : paths) {
            String source;
            try {
                source = Files.readString(path);
            } catch (IOException e) {
                System.err.print("Could not read file '" + path + "': ");
                e.printStackTrace();
                return;
            }
            handlers.addAll(Decompiler.read(source));
        }
        try (FileWriter writer = new FileWriter(out)) {
            Decompiler.write(writer, handlers);
        } catch (IOException e) {
            System.err.print("Could not write file '" + out + "': ");
            e.printStackTrace();
        }
    }
}
