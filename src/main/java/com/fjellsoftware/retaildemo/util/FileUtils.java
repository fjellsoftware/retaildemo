/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.util;

import com.fjellsoftware.retaildemo.ApplicationInternalException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class FileUtils {
    public static void purgeDirectory(File dir) {
        purgeDirectoryInternal(dir, 0);
    }
    private static void purgeDirectoryInternal(File dir, int level){
        if(level > 200){
            throw new ApplicationInternalException("Failed to purge directory, too deep nested.");
        }
        for (File file: dir.listFiles()) {
            if (file.isDirectory()) {
                purgeDirectoryInternal(file, level + 1);
            }
            file.delete();
        }
    }

    public static String loadFirstLineFromFile(String credentialsDirectory, String fileName){
        Scanner scanner;
        try {
            scanner = new Scanner(Path.of(credentialsDirectory, fileName));
        } catch (IOException e) {
            throw new ApplicationInternalException(String.format("File %s was not found in credentials directory.", fileName), e);
        }
        return scanner.nextLine();
    }
}
