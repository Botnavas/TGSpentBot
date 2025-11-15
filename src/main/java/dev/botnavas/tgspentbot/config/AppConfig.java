package dev.botnavas.tgspentbot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Properties;

import dev.botnavas.tgspentbot.Main;
import dev.botnavas.tgspentbot.config.exception.ConfigException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AppConfig {
    @Getter
    private static String botToken;
    @Getter
    private static String dbUrl;
    @Getter
    private static String dbUser;
    @Getter
    private static String dbPass;

    public static void loadConfig(String configFile) {
        Path jarPath = null;
        try {
            jarPath = Paths.get(Main.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                            .getParent();
        } catch (Exception ignore) {
        }

        var path = jarPath == null ? configFile : jarPath.resolve(configFile).toString();

        log.trace(String.format("Finding config in dir - %s", path));

        File externalConfig = new File(path);
        if (!externalConfig.exists()) {
            throw new ConfigException(String.format("Config file does not exists - %s", path));
        }

        try (FileInputStream input = new FileInputStream(externalConfig)) {
            var properties = new Properties();
            properties.load(input);
            loadConfig(properties);
            log.info(String.format("Config loaded - %s", path));
        } catch (IOException e) {
            throw new ConfigException(String.format("IOException while loading %s\n%s", path, e.getMessage()));
        }
    }

    private static void loadConfig(Properties prop) {
        botToken = getProperty(prop, "bot.token");
        dbUrl = getProperty(prop, "db.url");
        dbUser = getProperty(prop, "db.user");
        dbPass = getProperty(prop, "db.password");
    }

    private static String getProperty(Properties prop, String prop_name) {
        var result = prop.getProperty(prop_name);
        if (result == null) {
            throw new ConfigException(String.format("Property %s not found in config", prop_name));
        }
        return result;
    }
}
