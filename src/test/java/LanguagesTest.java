import net.lotushq.languages.FileStorage;
import net.lotushq.languages.LanguageFile;
import org.bukkit.ChatColor;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LanguagesTest {

    @Test
    public void testLanguageFile() {

        File directory = new File("src/test/resources");
        FileStorage<TestLanguageFile> storage = new FileStorage<>(directory, new TestLanguageFile());

        TestLanguageFile[] languageFiles = new TestLanguageFile[2];
        languageFiles[0] = storage.get(Locale.US);
        languageFiles[1] = storage.get(new Locale("es"));

        List<String> names = Arrays.asList("Carter", "Nygma", "Bella");

        for (TestLanguageFile lang : languageFiles) {
            System.out.println(lang.numberFormat(11));
            System.out.println(lang.nameList(names));
            System.out.println(lang.multiLine());
            System.out.println();
        }

    }

    private static class TestLanguageFile extends LanguageFile {

        @Override
        public String prefix() {
            return getString("prefix");
        }

        @Override
        public String unknown() {
            return ChatColor.RED + "Error";
        }

        public String numberFormat(int number) {
            return String.format(titled(getString("number-format")), number);
        }

        public String nameList(List<String> names) {
            return String.format(titled(getString("name-list")), String.join(", ", names));
        }

        public String multiLine() {
            return titled(getString("multi-line"));
        }

    }

}
