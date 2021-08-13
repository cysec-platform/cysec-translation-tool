package eu.smesec.library.translationtool;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertThrows;

/**
 * Test for {@link Merger}.
 *
 * @author Matthias Luppi
 */
public class MergerTest {

    private static final Path INPUT_FILE = Paths.get("src", "test", "resources", "fhnw", "coach.xml");
    private static final Path XLF_FILE = Paths.get("src", "test", "resources", "fhnw", "de.xlf");
    private static final Path OUTPUT_FILE = Paths.get("target", "test-output-merger", "coach-de.xml");

    @Test
    public void testNoArgs() {
        final Merger merger = new Merger(null, null, "de", null);
        assertThrows(IllegalArgumentException.class, merger::merge);
    }

    @Test(expected = Test.None.class)
    public void testMerge() throws Exception {
        try {
            Files.createDirectories(OUTPUT_FILE.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Could not prepare test", e);
        }
        final Merger merger = new Merger(INPUT_FILE, XLF_FILE, "de", OUTPUT_FILE);
        merger.merge();
    }

}
