/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author Benjamin This class logs every attempted sign in.
 */
public class Logger {

    public Logger() {
    }

    public static void addToLog(String login) throws IOException {

        /**
         * EVALUATOR: This is where the log is saved on your system.
         */
        File folder = new File("C:/C195-Shelton");
        File log = new File("C:/C195-Shelton/log.txt");

        BufferedWriter writer = null;

        try {
            //tries to initialize the writer
            writer = new BufferedWriter(new FileWriter(log, true));

        } catch (FileNotFoundException ex) {
            //if text file or folder path doesn't already exist, this creates it
            folder.mkdirs();
            log.createNewFile();

        } finally {
            //appends the passed String to the end of the text file
            writer = new BufferedWriter(new FileWriter(log, true));
            writer.append(login);
            writer.newLine();
            writer.newLine();
            writer.flush();
            writer.close();
        }

    }

}
