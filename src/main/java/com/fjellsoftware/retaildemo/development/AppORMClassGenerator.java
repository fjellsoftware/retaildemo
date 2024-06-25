/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.development;

import com.fjellsoftware.retaildemo.CoreDependencies;
import com.fjellsoftware.retaildemo.LoggerInitializer;
import com.fjellsoftware.retaildemo.util.FileUtils;
import io.loppi.orm.ORMClassGenerator;
import io.loppi.orm.classgeneratorconfiguration.ORMClassGeneratorConfigurationVersionBeta;

import java.io.File;
import java.nio.file.Path;

public class AppORMClassGenerator {

    public static void main(String[] args) {
        LoggerInitializer.initializeDevelopmentConsoleLogger();
        new AppORMClassGenerator().generateClasses();
    }

    public static final String PROJECT_ROOT_PATH = System.getProperty("user.dir");
    public static final Path autogeneratedDirectoryPath = Path.of(PROJECT_ROOT_PATH, "src", "main", "java", "com",
            "fjellsoftware", "retaildemo", "autogenerated");
    public void generateClasses(){
        Path ormPath = Path.of(autogeneratedDirectoryPath.toString(), "orm", "main");
        File directory = ormPath.toFile();
        FileUtils.purgeDirectory(directory);
        ORMClassGenerator.generateORMClassFiles(
                CoreDependencies.createDataSource(System.getProperty("user.home")), ormPath,
                "com.fjellsoftware.retaildemo.autogenerated.orm.main",
                new ORMClassGeneratorConfigurationVersionBeta());
    }
}
