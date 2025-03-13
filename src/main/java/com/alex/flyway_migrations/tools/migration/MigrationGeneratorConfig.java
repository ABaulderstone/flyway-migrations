package com.alex.flyway_migrations.tools.migration;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.alex.flyway_migrations.user.entities.User;

import jakarta.persistence.EntityManager;

import javax.sql.DataSource;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Properties;

@Configuration
public class MigrationGeneratorConfig {

    @Autowired
    private Environment env;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Bean

    @Profile("dev")
    public MigrationGenerator migrationGenerator() {
        return new MigrationGenerator();
    }

    public class MigrationGenerator {

        public String generateMigration(String description, String mode) throws Exception {

            Path migrationsDir = Paths.get("src/main/resources/db/migration");
            Files.createDirectories(migrationsDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = String.format("V%s__%s.sql", timestamp,
                    description.replaceAll("\\s+", "_").toLowerCase());

            Path filePath = migrationsDir.resolve(filename);
            File outputFile = filePath.toFile();

            Properties hibernateProperties = new Properties();
            hibernateProperties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.dialect"));
            hibernateProperties.put("hibernate.connection.datasource", dataSource);
            hibernateProperties.put("hibernate.format_sql", "true");
            hibernateProperties.put("hibernate.hbm2ddl.auto", "none");

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(hibernateProperties)
                    .build();

            MetadataSources metadataSources = new MetadataSources(serviceRegistry);
            metadataSources.addAnnotatedClass(User.class);

            Metadata metadata = metadataSources.buildMetadata();

            if ("create".equalsIgnoreCase(mode)) {

                SchemaExport schemaExport = new SchemaExport();
                schemaExport.setFormat(true);
                schemaExport.setOutputFile(outputFile.getAbsolutePath());
                schemaExport.setDelimiter(";");
                schemaExport.execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.CREATE, metadata);

                System.out.println("Generated complete schema creation script.");
            } else {

                SchemaUpdate schemaUpdate = new SchemaUpdate();
                schemaUpdate.setFormat(true);
                schemaUpdate.setOutputFile(outputFile.getAbsolutePath());
                schemaUpdate.setDelimiter(";");

                // Print what would be updated
                schemaUpdate.execute(EnumSet.of(TargetType.STDOUT, TargetType.SCRIPT), metadata);

                System.out.println("Generated incremental schema update script.");
            }

            // Check if file is empty and add a note if it is
            if (Files.size(filePath) == 0) {
                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write("-- No schema changes detected between entity model and database.\n");
                    writer.write("-- This could mean your database is already up to date with the entity model.\n");
                }
                System.out.println("No schema differences detected. Added explanatory comment to file.");
            }

            return filePath.toString();
        }

        public String generateMigration(String description) throws Exception {
            return generateMigration(description, "update");
        }
    }
}