package com.alex.flyway_migrations.tools.migration;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
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
import java.util.Properties;

import javax.sql.DataSource;

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

        public String generateMigration(String description) throws Exception {

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

            entityManager.getMetamodel().getEntities()
                    .forEach(entityType -> metadataSources.addAnnotatedClass(entityType.getJavaType()));

            Metadata metadata = metadataSources.buildMetadata();

            try (FileWriter writer = new FileWriter(outputFile)) {
                SchemaUpdate schemaUpdate = new SchemaUpdate();
                schemaUpdate.setFormat(true);
                schemaUpdate.setOutputFile(outputFile.getAbsolutePath());
                schemaUpdate.execute(
                        EnumSet.of(TargetType.SCRIPT),
                        metadata);
            }

            return filePath.toString();
        }
    }
}