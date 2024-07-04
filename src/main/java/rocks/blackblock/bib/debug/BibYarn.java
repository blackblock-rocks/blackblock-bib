package rocks.blackblock.bib.debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibLog;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Work with Yarn mappings, if they are available
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public class BibYarn {

    public static BibYarn INSTANCE = null;

    private static final Pattern FQ_CLASS_NAMES = Pattern.compile("net\\.minecraft\\.class_\\d+");
    private static final Pattern SIMPLE_CLASS_NAMES = Pattern.compile("class_\\d+");
    private static final Pattern METHOD_NAMES = Pattern.compile("method_\\d+");

    private Map<String, ClassInfo> class_mappings = null;
    private Map<String, FieldInfo> field_mappings = null;
    private Map<String, MethodInfo> method_mappings = null;

    /**
     * Instantiate the class with the path to the tiny file
     *
     * @since    0.2.0
     */
    public BibYarn(@NotNull String mappings_path) {
        this.loadMappings(mappings_path);
    }

    /**
     * Try to load the mappings
     *
     * @since    0.2.0
     */
    public static BibYarn from(@NotNull String mappings_path) {
        try {
            return new BibYarn(mappings_path);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Load the mappings file
     *
     * @since    0.2.0
     */
    private void loadMappings(@NotNull String mappings_path) {

        this.class_mappings = new HashMap<>(8500);
        this.field_mappings = new HashMap<>(40000);
        this.method_mappings = new HashMap<>(42000);

        try (BufferedReader reader = new BufferedReader(new FileReader(mappings_path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                switch (parts[0]) {
                    case "CLASS":
                        if (parts.length >= 4) {
                            this.parseClassLine(parts);
                        }
                        break;
                    case "FIELD":
                        if (parts.length >= 6) {
                            this.parseFieldLine(parts);
                        }
                        break;
                    case "METHOD":
                        if (parts.length >= 6) {
                            this.parseMethodLine(parts);
                        }
                        break;
                    default:
                        // Ignore other lines or handle them if needed
                        break;
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        BibLog.log("Got", this.class_mappings.size(), "class mappings");
        BibLog.log("Got", this.field_mappings.size(), "field mappings");
        BibLog.log("Got", this.method_mappings.size(), "method mappings");
    }

    /**
     * Parse a class line
     *
     * @since    0.2.0
     */
    private void parseClassLine(String[] parts) {
        String obfuscated_path = parts[2];
        String mapped_path = parts[3];
        ClassInfo info = new ClassInfo(obfuscated_path, mapped_path);
        this.class_mappings.put(info.getSimpleObfuscatedName(), info);
    }

    /**
     * Parse a field line
     *
     * @since    0.2.0
     */
    private void parseFieldLine(String[] parts) {
        String typeName = parts[2];
        String obfuscatedName = parts[4];
        String namedName = parts[5];
        this.field_mappings.put(obfuscatedName, new FieldInfo(typeName, obfuscatedName, namedName));
    }

    /**
     * Parse a method line
     *
     * @since    0.2.0
     */
    private void parseMethodLine(String[] parts) {
        String signature = parts[2];
        String obfuscatedName = parts[4];
        String namedName = parts[5];
        this.method_mappings.put(obfuscatedName, new MethodInfo(signature, obfuscatedName, namedName));
    }

    /**
     * Get the deobfuscated class name
     *
     * @since    0.2.0
     */
    @Nullable
    public String deobfuscateSimpleClassName(String obfuscated) {

        if (obfuscated.contains("$$")) {
            return this.deobfuscateStackTrace(obfuscated);
        }

        ClassInfo info = this.class_mappings.get(obfuscated);

        if (info == null) {
            return null;
        }

        return info.simple_mapped_name;
    }

    /**
     * Get the deobfuscated class name
     *
     * @since    0.2.0
     */
    @Nullable
    public String lookupObfuscatedPath(String obfuscated_path) {

        int last_slash_index = obfuscated_path.lastIndexOf('/');
        String simple_obfuscated_name = last_slash_index != -1 ? obfuscated_path.substring(last_slash_index + 1) : obfuscated_path;

        ClassInfo info = this.class_mappings.get(simple_obfuscated_name);

        if (info == null) {
            return null;
        }

        return info.getMappedPath();
    }

    /**
     * Get the deobfuscated class name
     *
     * @since    0.2.0
     */
    @Nullable
    public String lookupObfuscatedSimpleClassName(String simple_obfuscated_name) {

        ClassInfo info = this.class_mappings.get(simple_obfuscated_name);

        if (info == null) {
            return null;
        }

        return info.getSimpleMappedName();
    }

    /**
     * Get the deobfuscated class name
     *
     * @since    0.2.0
     */
    @Nullable
    public String lookupObfuscatedMethodName(String obfuscated_method) {

        MethodInfo info = this.method_mappings.get(obfuscated_method);

        if (info == null) {
            return null;
        }

        return info.mapped_name;
    }

    /**
     * Deobfuscate a stack trace
     *
     * @since    0.2.0
     */
    @NotNull
    public String deobfuscateStackTrace(String trace) {

        if (trace == null) {
            return "";
        }

        // Replace fully qualified class names
        trace = replaceMatches(trace, FQ_CLASS_NAMES, this::lookupObfuscatedPath);

        // Replace simple class names
        trace = replaceMatches(trace, SIMPLE_CLASS_NAMES, this::lookupObfuscatedSimpleClassName);

        // Replace method names
        trace = replaceMatches(trace, METHOD_NAMES, this::lookupObfuscatedMethodName);

        return trace;
    }

    /**
     * Configure the System output interceptor.
     * This won't intercept Log4j messages though (which is most calls)
     *
     * @since    0.2.0
     */
    public void setupOutputInterceptor() {

        // Save the original System.out and System.err
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        // Create the intercepting OutputStream and PrintStream for System.out
        InterceptOutputStream interceptOutStream = new InterceptOutputStream(originalOut, this);
        PrintStream interceptOutPrintStream = new PrintStream(interceptOutStream);

        // Create the intercepting OutputStream and PrintStream for System.err
        InterceptOutputStream interceptErrStream = new InterceptOutputStream(originalErr, this);
        PrintStream interceptErrPrintStream = new PrintStream(interceptErrStream);

        // Redirect System.out and System.err to the intercepting PrintStreams
        System.setOut(interceptOutPrintStream);
        System.setErr(interceptErrPrintStream);
    }

    /**
     * Deobfuscate a stack trace
     *
     * @since    0.2.0
     */
    private String replaceMatches(String input, String regex, Replacement replacement) {
        Pattern pattern = Pattern.compile(regex);
        return replaceMatches(input, pattern, replacement);
    }

    /**
     * Deobfuscate a stack trace
     *
     * @since    0.2.0
     */
    private String replaceMatches(String input, Pattern pattern, Replacement replacement) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group();
            String replacementText = replacement.replace(match);
            if (replacementText != null) {
                matcher.appendReplacement(result, replacementText.replaceAll("\\$", "\\\\\\$"));
            } else {
                matcher.appendReplacement(result, match.replaceAll("\\$", "\\\\\\$"));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    // Functional interface to replace the matches
    @FunctionalInterface
    interface Replacement {
        String replace(String match);
    }

    /**
     * Class information class
     *
     * @since    0.2.0
     */
    public static class ClassInfo {
        String obfuscated_path;
        String mapped_path;
        String simple_obfuscated_name;
        String simple_mapped_name;

        protected ClassInfo(String obfuscated_path, String mapped_path) {
            this.obfuscated_path = obfuscated_path;
            this.mapped_path = mapped_path;

            int last_slash_index = obfuscated_path.lastIndexOf('/');
            this.simple_obfuscated_name = last_slash_index != -1 ? obfuscated_path.substring(last_slash_index + 1) : obfuscated_path;

            if (this.simple_obfuscated_name.contains("$")) {
                int dollar_index = this.simple_obfuscated_name.lastIndexOf('$');

                if (dollar_index != 1) {
                    this.simple_obfuscated_name = this.simple_obfuscated_name.substring(dollar_index + 1);
                }
            }

            last_slash_index = mapped_path.lastIndexOf('/');
            this.simple_mapped_name = last_slash_index != -1 ? mapped_path.substring(last_slash_index + 1) : mapped_path;
        }

        public String getSimpleObfuscatedName() {
            return this.simple_obfuscated_name;
        }

        public String getMappedPath() {
            return this.mapped_path;
        }

        public String getSimpleMappedName() {
            return this.simple_mapped_name;
        }
    }

    /**
     * Field information class
     *
     * @since    0.2.0
     */
    public static class FieldInfo {
        String type;
        String obfuscated_name;
        String mapped_name;

        protected FieldInfo(String type, String obfuscated_name, String mapped_name) {
            this.type = type;
            this.obfuscated_name = obfuscated_name;
            this.mapped_name = mapped_name;
        }
    }

    /**
     * Method information class
     *
     * @since    0.2.0
     */
    public static class MethodInfo {
        String signature;
        String obfuscated_name;
        String mapped_name;

        public MethodInfo(String signature, String obfuscated_name, String mapped_name) {
            this.signature = signature;
            this.obfuscated_name = obfuscated_name;
            this.mapped_name = mapped_name;
        }
    }

    /**
     * Output stream that does deobfuscating
     * Only works in the most low-level cases,
     * since log4j circumvents this
     *
     * @since    0.2.0
     */
    public static class InterceptOutputStream extends OutputStream {
        private final OutputStream original;
        private final BibYarn bibYarn;

        public InterceptOutputStream(OutputStream original, BibYarn bibYarn) {
            this.original = original;
            this.bibYarn = bibYarn;
        }

        @Override
        public void write(int b) throws IOException {
            // Write to the original OutputStream
            original.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // Interception logic for byte arrays
            String data = new String(b, off, len);

            data = this.bibYarn.deobfuscateStackTrace(data);

            // Write to the original OutputStream
            original.write(data.getBytes(), 0, data.length());
        }
    }
}
