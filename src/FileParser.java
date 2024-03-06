import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class FileParser {
    private String fileName;
    private String endFileName;
    static final String DELIMITER = ":";
    static final String SPACE = " ";

    public FileParser(String fileName, String endFileName) {
        this.fileName = fileName;
        this.endFileName = endFileName;
    }

    public boolean parseNstr() {
        List<String> result = new ArrayList<>();
        int currentRow = 0;
        try {
            Stream<String> lines = Files.lines(Paths.get(fileName));
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext()) {
                String oneLine = iterator.next();
                currentRow++;
                //возможны несколько NStr на одной строке
                if (oneLine.split("NStr").length > 2) {
                    String[] doubleNstr = oneLine.split("NStr");
                    for (int i = 1; i < doubleNstr.length; i++) {//0 элемент - не Nstr функция
                        oneLine = "NStr" + doubleNstr[i];
                        result.add(processOneLine(oneLine, currentRow));
                    }
                    if (iterator.hasNext()) {
                        oneLine = iterator.next();
                        currentRow++;
                    }
                }

                if (oneLine.contains("NStr(")) {
                    while (!oneLine.contains("'\"")) {//NStr может быть на несколько строк, ищем конец функции NStr
                        currentRow++;
                        oneLine = oneLine.concat(iterator.next());
                    }
                    result.add(processOneLine(oneLine, currentRow));

                }
            }

            writeListToFile(result);

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла на строке: " + currentRow + "\n" + e.getMessage());
        }
        return true;
    }

    private String processOneLine(String oneLine, int curLine) {

        String nstr = oneLine.substring(oneLine.toLowerCase().indexOf("nstr(\"") + 6, oneLine.lastIndexOf("'\""));
        //нужно для корректного вывода строки у функции NStr с аргументом в несколько строк, разделенных |
        int countSlash = 0;
        if (nstr.contains("|")) {
            countSlash = nstr.split("\\|").length - 1;
        }

        nstr = nstr.replace("en = \"\"", "en = '").replace("\"\";", "';");//имеется одна строка с двойными кавчками вместо одинарных

        String[] localAndTextArray = nstr.split("';");
        StringBuilder resStr = new StringBuilder();
        for (String oneLocalAndText : localAndTextArray) {
            String[] pair = oneLocalAndText.split("=");
            resStr.append(curLine - countSlash);
            resStr.append(DELIMITER).append(SPACE);
            resStr.append(filterString(pair[0])).append(DELIMITER).append(SPACE);
            resStr.append(filterString(pair[1]));
            resStr.append("\n");
        }
        return resStr.toString();
    }

    private void writeListToFile(List<String> result) {
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
                .replaceAll("\t", "")
                .replace("|", "")
                .replace("'", "");
    }


}
