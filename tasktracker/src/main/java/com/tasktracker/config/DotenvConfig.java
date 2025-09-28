package com.tasktracker.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//whenever the application finds the .env file it will load the environment variables from there
//it will upload in the spring so that mavwn can use it

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            // Check if .env file exists in the current directory
            File envFile = new File(".env");
            if (envFile.exists()) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(".")
                        .filename(".env")
                        .load();

                Map<String, Object> envMap = new HashMap<>();
                dotenv.entries().forEach(entry -> {
                    envMap.put(entry.getKey(), entry.getValue());
                });

                environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));
            }
        } catch (Exception e) {
            // If loading .env fails, continue without it (fallback to default values)
            System.out.println("Could not load .env file: " + e.getMessage());
        }
    }
}
