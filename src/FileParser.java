import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileParser {
    private String fileName;
    private String endFileName;

    private int countRows = 0;
    static final String DELIMITER = ":";
    static final String SPACE = " ";
    public FileParser(String fileName, String endFileName) {
        this.fileName = fileName;
        this.endFileName = endFileName;
    }

    public boolean parseNstr() {
        List<String> result = new ArrayList<>();
        try {
            List<String> lines =  Files.readAllLines(Paths.get(fileName));
            int curLine = 0;
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext()) {
                String oneLine = iterator.next();
                curLine ++;
               /* if (oneLine.split("NStr").length>2) {//возможны несколько NStr на одной строке
                    String [] doubleNstr = oneLine.split("NStr");
                    for(int i=1; i < doubleNstr.length ;i++) {//0 - не Nstr функция
                        oneLine = "Nstr"+doubleNstr[i];
                        result.add(workWithOneLine(oneLine,curLine));
                    }
                    if (iterator.hasNext()) {
                        oneLine = iterator.next();
                        curLine ++;
                    }
                }*/
                if (oneLine.contains("NStr(")) {
                    while (!oneLine.contains("'\"")) {
                        curLine ++;
                        oneLine = oneLine.concat(iterator.next());
                    }
                    result.add(workWithOneLine(oneLine,curLine));

                }
            }

            addListToFile(result);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private String workWithOneLine(String oneLine, int curLine) {

        String nstr = oneLine.substring(oneLine.indexOf("NStr(\"")+6, oneLine.lastIndexOf("'\""));
        int countSlash = 0;
        if (nstr.contains("|")) {
            countSlash = nstr.split("\\|").length-1;
        }
        String[] languages = nstr.split("';");
        StringBuilder resStr = new StringBuilder();
        for (String oneLanguage : languages) {
            String[] pair = oneLanguage.split("=");
            resStr.append(curLine-countSlash);
            resStr.append(DELIMITER).append(SPACE);
            resStr.append(filterString(pair[0])).append(DELIMITER).append(SPACE);
            resStr.append(filterString(pair[1]));
            resStr.append("\n");
        }
        return resStr.toString();
    }

    private void addListToFile(List<String> result) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(endFileName))) {
            for (String line : result) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
    private String filterString(String str) {
        return str
                .trim()
                .replaceAll("\t","")
                .replace("|","")
                .replace("'","");
    }


}
