package core.managers;

import java.io.*;
import java.util.Scanner;

public class ServerManager {
    public static String runCommand(String command){
        String output = null;
        try{
            Scanner scanner = new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A");
            if(scanner.hasNext()) output = scanner.next();
        }catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
        return output;
    }

    public static String getWorkingDir() {
        return System.getProperty("user.dir");
    }

    public static String readFile(File file){
        StringBuilder output = new StringBuilder();
        try{
            String line;
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) output.append(line+"\n");
            bufferedReader.close();
        }catch (IOException error){
            error.printStackTrace();
        }
        return output.toString();
    }

    public static void writeFile(File file, String content){
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            writer.write(content);
            writer.close();
        }catch (IOException error){
            error.printStackTrace();
        }
    }
}
