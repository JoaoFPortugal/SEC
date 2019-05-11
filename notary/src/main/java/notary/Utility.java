package notary;

import javax.swing.*;
import java.io.Console;

public class Utility {

    public static void println(String str) {
        System.out.println(str);
    }

    public static String readString(String prompt) {

        String input = "";
        Console c = System.console();

        if (c == null) {
            input = JOptionPane.showInputDialog(prompt);
        } else {
            println(prompt);
            input = c.readLine();

        }

        return input;
    }

    public static int readInt(String prompt) {
        int i;
        while (true) {
            try {
                i = Integer.parseInt(readString(prompt));
                break;
            } catch (NumberFormatException e) {
                println("Not a valid number.");
            }
        }
        return i;
    }
}
