public class Main {
    public static void main(String[] args) {

        String fileName ="ObjectModule.bsl";
        String endFileName = "resultFile.txt";

        FileParser fileParser = new FileParser(fileName, endFileName);

        if (fileParser.parseNstr()) {
            System.out.println("Успешно завершено. Создан файл "+endFileName);
        }

    }
}