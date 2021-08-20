package dev.kios.airplace;

import java.io.File;
import java.util.Scanner;

public class LogFileReader {
    private Scanner routeFile;

    public LogFileReader(String path) {
        try {
            routeFile = new Scanner(new File(path));
            routeFile.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LogSample scanNewSample() {
        if (routeFile.hasNextLine()) {
            LogSample logSample = new LogSample();

            String routeLine = routeFile.nextLine();

            logSample.timeStamp = Long.parseLong(routeLine.split(" ")[0]);
            logSample.x = Float.parseFloat(routeLine.split(" ")[1]);
            logSample.y = Float.parseFloat(routeLine.split(" ")[2]);
            logSample.heading = Float.parseFloat(routeLine.split(" ")[3]);
            logSample.status = routeLine.split(" ")[4];

            routeFile.nextLine();
            routeLine = routeFile.nextLine();

            logSample.magneticField[0] = Float.parseFloat(routeLine.split(" ")[0]);
            logSample.magneticField[1] = Float.parseFloat(routeLine.split(" ")[1]);
            logSample.magneticField[2] = Float.parseFloat(routeLine.split(" ")[2]);

            routeFile.nextLine();
            routeLine = routeFile.nextLine();
            while (!routeLine.startsWith("#")) {
                logSample.wiFi.add(new LogRecord(routeLine.split(" ")[0], Integer.parseInt(routeLine.split(" ")[1])));

                if (!routeFile.hasNextLine()) {
                    break;
                }

                routeLine = routeFile.nextLine();
            }
            return logSample;
        }
        return null;
    }
}
