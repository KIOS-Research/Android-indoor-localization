package dev.kios.airplace;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.kios.airplace.ui.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RadioMap {
    private final Skin skin;
    private final String inputFolderPath;
    private final String outputFolderPath;

    private final int defaultNaNValue = -110;
    private final ArrayList<String> uniqueBSSIDs;
    private final HashMap<String, HashMap<Integer, ArrayList<Float>>> magneticRadioMap;
    private final HashMap<String, HashMap<Integer, HashMap<String, Integer>>> rssRadioMap;

    public RadioMap(Skin skin, String inputFolderPath, String outputFolderPath) {
        this.skin = skin;
        this.inputFolderPath = inputFolderPath;
        this.outputFolderPath = outputFolderPath;

        rssRadioMap = new HashMap<>();
        uniqueBSSIDs = new ArrayList<>();
        magneticRadioMap = new HashMap<>();

        createRadioMap();
    }

    private void writeRadioMap() {
        FileOutputStream fosRss;
        FileOutputStream fosRssMean;

        FileOutputStream fosMag;
        FileOutputStream fosMagMean;

        ArrayList<Integer> meanRSSValues = new ArrayList<>();
        ArrayList<String> meanBSSIDValues = new ArrayList<>();
        ArrayList<Float> meanMagneticValues = new ArrayList<>();

        File rssRadioMapFile = new File("rssRadioMap.rrm");
        File magRadioMapFile = new File("magRadioMap.mrm");

        File rssMeanRadioMapFile = new File("rssMeanRadioMap.rrm");
        File magMeanRadioMapFile = new File("magMeanRadioMap.mrm");

        if (rssRadioMap.isEmpty()) {
            return;
        }

        try {
            if (!new File(outputFolderPath).mkdirs() && !new File(outputFolderPath).exists())
                throw new Exception("Directory cannot be created. Path:" + outputFolderPath);

            fosRss = new FileOutputStream(outputFolderPath + File.separator + rssRadioMapFile, false);
            fosMag = new FileOutputStream(outputFolderPath + File.separator + magRadioMapFile, false);

            fosRssMean = new FileOutputStream(outputFolderPath + File.separator + rssMeanRadioMapFile, false);
            fosMagMean = new FileOutputStream(outputFolderPath + File.separator + magMeanRadioMapFile, false);
        } catch (Exception e) {
            rssRadioMapFile.deleteOnExit();
            magRadioMapFile.deleteOnExit();

            rssMeanRadioMapFile.deleteOnExit();
            magMeanRadioMapFile.deleteOnExit();

            e.printStackTrace();
            return;
        }

        try {
            int group;
            int heading;
            int degrees = 360;
            int orientations = 4;

            String x_y;
            String header = "# X, Y, Heading, ";
            String magHeader = "# X, Y, Heading, Magnetic Field X, Y, Z\n";
            String NaNValue = "# NaN " + defaultNaNValue + "\n";

            fosRss.write(NaNValue.getBytes());
            fosRssMean.write(NaNValue.getBytes());

            fosRss.write(header.getBytes());
            fosRssMean.write(header.getBytes());

            fosMag.write(magHeader.getBytes());
            fosMagMean.write(magHeader.getBytes());

            int last = uniqueBSSIDs.size() - 1;
            for (int i = 0; i < uniqueBSSIDs.size(); i++) {
                if (i == last) {
                    fosRss.write((uniqueBSSIDs.get(i).toLowerCase() + "\n").getBytes());
                    fosRssMean.write((uniqueBSSIDs.get(i).toLowerCase() + "\n").getBytes());
                } else {
                    fosRss.write((uniqueBSSIDs.get(i).toLowerCase() + ", ").getBytes());
                    fosRssMean.write((uniqueBSSIDs.get(i).toLowerCase() + ", ").getBytes());
                }
            }

            for (int i = 0; i < orientations; i++) {
                for (Map.Entry<String, HashMap<Integer, HashMap<String, Integer>>> e : rssRadioMap.entrySet()) {
                    group = (degrees / orientations);
                    x_y = e.getKey();

                    int j = 0;
                    for (Map.Entry<Integer, HashMap<String, Integer>> ee : e.getValue().entrySet()) {
                        heading = ee.getKey() % orientations;
                        if (heading != i)
                            continue;

                        ArrayList<Float> magneticValues = magneticRadioMap.get(x_y).get(heading);

                        j++;
                        heading *= group;
                        fosRss.write((x_y + ", " + heading + ", ").getBytes());
                        fosMag.write((x_y + ", " + heading + ", " + magneticValues.get(0) + ", " + magneticValues.get(1) + ", " + magneticValues.get(2) + "\n").getBytes());

                        int k = 0;
                        last = ee.getValue().size() - 1;

                        if (meanMagneticValues.isEmpty()) {
                            meanMagneticValues.add(magneticValues.get(0));
                            meanMagneticValues.add(magneticValues.get(1));
                            meanMagneticValues.add(magneticValues.get(2));
                        } else {
                            meanMagneticValues.set(0, meanMagneticValues.get(0) + magneticValues.get(0));
                            meanMagneticValues.set(1, meanMagneticValues.get(1) + magneticValues.get(1));
                            meanMagneticValues.set(2, meanMagneticValues.get(2) + magneticValues.get(2));
                        }

                        for (Map.Entry<String, Integer> eee : ee.getValue().entrySet()) {
                            if (k == last) {
                                fosRss.write((eee.getValue() + "\n").getBytes());
                            } else {
                                fosRss.write((eee.getValue() + ", ").getBytes());
                            }
                            k++;

                            if (!meanBSSIDValues.contains(eee.getKey().toLowerCase())) {
                                meanBSSIDValues.add(eee.getKey().toLowerCase());
                                meanRSSValues.add(eee.getValue());
                            } else {
                                int idx = meanBSSIDValues.indexOf(eee.getKey());
                                meanRSSValues.set(idx, meanRSSValues.get(idx) + eee.getValue());
                            }
                        }
                    }

                    int k = 0;
                    last = meanRSSValues.size() - 1;

                    if (last != -1) {
                        fosRssMean.write((x_y + ", " + i * group + ", ").getBytes());
                        fosMagMean.write((x_y + ", " + i * group + ", " + meanMagneticValues.get(0) / j + ", " + meanMagneticValues.get(1) / j + ", " + meanMagneticValues.get(2) / j + "\n").getBytes());
                    }

                    for (Integer rss : meanRSSValues) {
                        float meanRSS = (float) (Math.round(((float) rss / j) * 10.0) / 10.0);

                        if (k == last) {
                            if (Math.floor(meanRSS) != meanRSS) {
                                fosRssMean.write((meanRSS + "\n").getBytes());
                            } else {
                                fosRssMean.write(((int) meanRSS + "\n").getBytes());
                            }
                        } else {
                            if (Math.floor(meanRSS) != meanRSS) {
                                fosRssMean.write((meanRSS + ", ").getBytes());
                            } else {
                                fosRssMean.write(((int) meanRSS + ", ").getBytes());
                            }
                        }
                        k++;
                    }

                    meanRSSValues.clear();
                    meanBSSIDValues.clear();
                    meanMagneticValues.clear();
                }
            }

            fosRss.close();
            fosMag.close();

            fosRssMean.close();
            fosMagMean.close();
            Toast.makeToast("Done", Toast.LENGTH_SHORT, skin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRadioMap() {
        File inputFile = new File(inputFolderPath);

        if (!inputFile.exists() || !inputFile.isDirectory())
            return;

        createRadioMapFromFile(inputFile, 0);
        createRadioMapFromFile(inputFile, 1);
        writeRadioMap();
    }

    private void createRadioMapFromFile(File inputFile, int operation) {
        if (inputFile.exists()) {
            if (inputFile.canExecute() && inputFile.isDirectory()) {
                String[] list = inputFile.list();

                if (list != null) {
                    for (String s : list) {
                        createRadioMapFromFile(new File(inputFile, s), operation);
                    }
                }
            } else if (inputFile.canRead() && inputFile.isFile() && getExtensionOfFile(inputFile).equals("rf")) {
                if (operation == 0) {
                    authenticateRouteFile(inputFile);
                } else if (operation == 1) {
                    parseRouteFileToRadioMap(inputFile);
                }
            }
        }
    }

    private void parseRouteFileToRadioMap(File inputFile) {
        FileReader fileReader;
        BufferedReader bufferedReader;

        ArrayList<Integer> rssValues = new ArrayList<>();
        ArrayList<String> bssidValues = new ArrayList<>();
        ArrayList<Float> magneticValues = new ArrayList<>();

        int group;
        int degrees = 360;
        int orientations = 4;
        int range = degrees / orientations;
        int deviation = range / 2;

        try {
            String key;
            String line;

            fileReader = new FileReader(inputFile);
            bufferedReader = new BufferedReader(fileReader);

            line = bufferedReader.readLine();
            while (line != null) {
                if (!(line.startsWith("#") || line.trim().isEmpty())) {
                    String[] temp_0 = line.split(" ");

                    key = temp_0[1] + ", " + temp_0[2];

                    group = ((Math.round(Float.parseFloat(temp_0[3]) + deviation) % degrees) / range) % orientations;

                    bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    String[] temp_1 = line.split(" ");

                    magneticValues.add(Float.parseFloat(temp_1[0]));
                    magneticValues.add(Float.parseFloat(temp_1[1]));
                    magneticValues.add(Float.parseFloat(temp_1[2]));

                    bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    while (!(line.startsWith("#") || line.trim().isEmpty())) {
                        String[] temp_2 = line.split(" ");

                        //BSSID
                        bssidValues.add(temp_2[0].toLowerCase());

                        //RSS
                        rssValues.add(Integer.parseInt(temp_2[1]));

                        line = bufferedReader.readLine();
                        if (line == null)
                            break;
                    }

                    HashMap<String, Integer> sample = new HashMap<>();

                    for (String bssid : uniqueBSSIDs) {
                        if (bssidValues.contains(bssid)) {
                            sample.put(bssid, rssValues.get(bssidValues.indexOf(bssid)));
                        } else {
                            sample.put(bssid, defaultNaNValue);
                        }
                    }

                    HashMap<Integer, ArrayList<Float>> magneticHeadingList = magneticRadioMap.get(key);
                    HashMap<Integer, HashMap<String, Integer>> rssHeadingList = rssRadioMap.get(key);

                    if (rssHeadingList == null) {
                        rssHeadingList = new HashMap<>();
                        magneticHeadingList = new HashMap<>();
                    } else if (rssHeadingList.get(group) != null) {
                        group += orientations;
                        while (rssHeadingList.get(group) != null) {
                            group += orientations;
                        }
                    }

                    rssHeadingList.put(group, sample);
                    magneticHeadingList.put(group, new ArrayList<>(magneticValues));

                    rssRadioMap.put(key, rssHeadingList);
                    magneticRadioMap.put(key, magneticHeadingList);

                    rssValues.clear();
                    bssidValues.clear();
                    magneticValues.clear();
                }
                line = bufferedReader.readLine();
            }

            fileReader.close();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void authenticateRouteFile(File inputFile) {
        FileReader fileReader;
        BufferedReader bufferedReader;

        try {
            String line;
            fileReader = new FileReader(inputFile);
            bufferedReader = new BufferedReader(fileReader);

            line = bufferedReader.readLine();
            while (line != null) {
                if (!(line.startsWith("#") || line.trim().isEmpty())) {
                    String[] temp = line.split(" ");

                    //Timestamp
                    Double.parseDouble(temp[0]);

                    //X, Y, Heading
                    Float.parseFloat(temp[1]);
                    Float.parseFloat(temp[2]);
                    Float.parseFloat(temp[3]);

                    //Status
                    if (!temp[4].trim().equals("Still") && !temp[4].trim().equals("Walk") && !temp[4].trim().equals("N/A")) {
                        throw new Exception("For input string: " + temp[4]);
                    }

                    bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    temp = line.split(" ");

                    //Magnetic Field X, Y, Z
                    Float.parseFloat(temp[0]);
                    Float.parseFloat(temp[1]);
                    Float.parseFloat(temp[2]);

                    bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    while (!(line.startsWith("#") || line.trim().isEmpty())) {
                        temp = line.split(" ");

                        //BSSID
                        if (!temp[0].matches("[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}")) {
                            throw new Exception("For input string: " + temp[0]);
                        } else {
                            if (!uniqueBSSIDs.contains(temp[0].toLowerCase()))
                                uniqueBSSIDs.add(temp[0].toLowerCase());
                        }

                        //RSS
                        Integer.parseInt(temp[1]);

                        line = bufferedReader.readLine();
                        if (line == null)
                            break;
                    }
                }
                line = bufferedReader.readLine();
            }

            fileReader.close();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getExtensionOfFile(File file) {
        String fileExtension = "";
        // Get file Name first
        String fileName = file.getName();

        // If fileName do not contain "." or starts with "." then it is not a valid file
        if (fileName.contains(".") && fileName.lastIndexOf(".") != 0) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        return fileExtension;
    }
}
