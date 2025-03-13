package com.alex.flyway_migrations.tools.migration;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

@Configuration
public class MigrationGeneratorConfig {

    @Autowired
    private Environment env;

    @Autowired
    private EntityManager entityManager;

    @Bean
    @Profile("dev")
    public MigrationGenerator migrationGenerator() {
        return new MigrationGenerator();
    }

    public class MigrationGenerator {

        public String generateMigration(String description) throws Exception {

            Path migrationsDir = Paths.get("src/main/resources/db/migration");
            Files.createDirectories(migrationsDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = String.format("V%s__%s.sql", timestamp,
                    description.replaceAll("\\s+", "_").toLowerCase());

            Path filePath = migrationsDir.resolve(filename);
            File outputFile = filePath.toFile();
            MetadataSources metadata = new MetadataSources(
                    new StandardServiceRegistryBuilder()
                            .applySetting("hibernate.dialect",
                                    env.getProperty("spring.jpa.properties.dialect"))
                            .build());

            entityManager.getMetamodel().getEntities()
                    .forEach(entityType -> metadata.addAnnotatedClass(entityType.getJavaType()));

            try (FileWriter writer = new FileWriter(outputFile)) {
                SchemaUpdate schemaUpdate = new SchemaUpdate();
                schemaUpdate.setFormat(true);
                schemaUpdate.setOutputFile(outputFile.getAbsolutePath());
                schemaUpdate.execute(
                        EnumSet.of(TargetType.SCRIPT),
                        metadata.buildMetadata());
            }

            return filePath.toString();
        }
    }
}