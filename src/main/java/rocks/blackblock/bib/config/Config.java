package rocks.blackblock.bib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.util.BibJson;
import rocks.blackblock.bib.util.BibLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base Config class
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public abstract class Config {

    // All loaded configurations
    private static Map<String, Config> CONFIGS = new HashMap<>();

    // Have all the configs been initialized?
    private static boolean initialized = false;

    // The directory where the config files should be stored
    protected File config_dir = null;

    // The actual config file to read/write
    protected File config_file = null;

    // The source parsed object
    protected JsonObject source = null;

    // the name of this config
    protected final String name;

    // Callbacks once loaded
    protected List<Runnable> once_loaded = null;

    // Has this config been initialized?
    protected boolean has_loaded = false;

    // Has the configuration been read?
    protected boolean has_read_config = false;

    /**
     * Create a new Config instance and make sure it only exists once
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static <T extends Config> T getOrCreateConfig(String name, Function<String, T> creator) {

        Config result = CONFIGS.get(name);

        if (result == null) {
            T created = creator.apply(name);

            if (created == null) {
                throw new RuntimeException("Unable to create config " + name);
            }

            if (!created.has_read_config) {
                try {
                    created.readConfig();
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read config file " + created.name, e);
                }
            }

            return created;
        }

        return (T) result;
    }

    /**
     * Create a new Config instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public Config(String name) {

        BibLog.log("Creating config named '" + name + "'");

        this.name = name;

        if (CONFIGS.containsKey(name)) {
            Config existing = CONFIGS.get(name);
            existing.overload();
        }

        // Get the path to the config dir
        this.config_dir = BibMod.PLATFORM.getConfigDirPath().toFile();

        // Create the config dir if it doesn't exist
        if (!this.config_dir.exists()) {
            this.config_dir.mkdirs();
        }

        // Create the config file
        this.config_file = new File(this.config_dir, name + ".json");

        // Create the config file if it doesn't exist
        if (!this.config_file.exists()) {
            try {
                this.config_file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create config file " + name, e);
            }
        }

        CONFIGS.put(name, this);
    }

    /**
     * Read in the config file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected void readConfig() throws FileNotFoundException {

        this.has_read_config = true;

        JsonElement data = BibJson.parse(this.config_file);

        if (data == null) {
            throw new RuntimeException("Unable to parse config file " + this.config_file.getAbsolutePath());
        }

        if (data.isJsonNull()) {
            // Allow empty configs, but pass an object to the method later anyway
            data = new JsonObject();
        }

        if (!data.isJsonObject()) {
            throw new RuntimeException("Config file " + this.config_file.getAbsolutePath() + " is not a json object");
        }

        this.source = data.getAsJsonObject();

        this.parseConfig(this.source.deepCopy());

        this.has_loaded = true;

        this.parsed();

        if (this.once_loaded != null) {
            for (Runnable runner : this.once_loaded) {
                runner.run();
            }

            this.once_loaded = null;
        }
    }

    /**
     * We encountered a fatal error
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected void exitDueToConfigError(String message) {

        BibLog.attention("Fatal error in config file " + this.config_file.getAbsolutePath());
        BibLog.log(message);
        BibLog.log("Next is a stack trace...");

        throw new RuntimeException(message);
    }

    /**
     * What to do when overloaded
     * (a new config with the same name exists)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected void overload() {
        throw new RuntimeException("Config " + this.name + " already exists");
    }

    /**
     * What to do after the config has been parsed
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected void parsed() {
        // Nothing by default
    }

    /**
     * What to do when the server is starting
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected void initializing() {
        // Nothing by default
    }

    /**
     * Parse the config JSON
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    abstract protected void parseConfig(@NotNull JsonObject data);

    /**
     * Has this config loaded yet?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean hasLoaded() {
        return this.has_loaded;
    }

    /**
     * Schedule something to do once the config has loaded
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public void afterLoad(Runnable runnable) {

        if (this.has_loaded) {
            runnable.run();
            return;
        }

        if (this.once_loaded == null) {
            this.once_loaded = new ArrayList<>();
        }

        this.once_loaded.add(runnable);
    }

    /**
     * Initialize all the configs
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void initializeAllConfigs() {

        if (initialized) {
            return;
        }

        BibLog.attention("Initializing all Blackblock configs!");

        initialized = true;

        for (Config config : CONFIGS.values()) {

            if (!config.has_read_config) {
                try {
                    config.readConfig();
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read config file " + config.name, e);
                }
            }

            config.initializing();
        }

        BibLog.log("All Blackblock configs have initialized!");
    }
}
