package com.alex.flyway_migrations.tools.migration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MigrationGeneratorApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MigrationGeneratorApp.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    public CommandLineRunner migrationRunner(ApplicationContext ctx,
            MigrationGeneratorConfig.MigrationGenerator generator) {
        return args -> {
            if (args.length < 1) {
                System.out.println("Usage: generate-migration <description>");
                System.exit(1);
            }

            String description = args[1];
            System.out.println("Generating migration for: " + description);

            try {
                String filePath = generator.generateMigration(description);
                System.out.println("Migration generated successfully at: " + filePath);
            } catch (Exception e) {
                System.err.println("Error generating migration: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

            System.exit(0);
        };
    }
}