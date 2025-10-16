package org.main;
import java.io.*;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";
    static {
        loadConfig();
    }

    private static void loadConfig() {
        // Сначала пытаемся загрузить из внешнего файла (вне JAR)
        File externalConfig = new File(CONFIG_FILE);
        if (externalConfig.exists()) {
            try (FileInputStream input = new FileInputStream(externalConfig)) {
                properties.load(input);
                System.out.println("Loaded configuration from external file: " + CONFIG_FILE);
            } catch (IOException e) {
                System.err.println("Error loading external config: " + e.getMessage());
            }
        } else {
            // Если внешнего файла нет, загружаем из ресурсов (внутри JAR)
            try (InputStream input = AppConfig.class.getClassLoader()
                    .getResourceAsStream(CONFIG_FILE)) {
                if (input != null) {
                    properties.load(input);
                    System.out.println("Loaded configuration from resources");
                } else {
                    throw new RuntimeException("Config file not found in resources: " + CONFIG_FILE);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading config from resources", e);
            }
        }
    }

    public static String getDatabaseUrl() {
        return properties.getProperty("db.url");
    }

    public static String getBotToken() {
        return properties.getProperty("bot.token");
    }
}
