package hds_security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageLogger{

    public static void log(String name, Level level, byte[] bytes){
        Logger LOGGER = Logger.getLogger(name);
        FileHandler fh = null;
        try {
            //não sei se o dir está bem
            fh = new FileHandler("../resources/LogFiles.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fh != null;
        LOGGER.addHandler(fh);
        LOGGER.log(level,new String(bytes, StandardCharsets.UTF_8));
        fh.close();
    }
}