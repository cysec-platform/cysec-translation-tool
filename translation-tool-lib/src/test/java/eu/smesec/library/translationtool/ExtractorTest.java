package eu.smesec.library.translationtool;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Test for {@link Extractor}.
 *
 * @author Matthias Luppi
 */
public class ExtractorTest {

    private static final Path INPUT_FILE = Paths.get("src", "test", "resources", "fhnw", "coach.xml");
    private static final Path OUTPUT_FILE = Paths.get("target", "test-output-extractor", "source.xlf");

    @Test
    public void testNoArgs() {
        final Extractor extractor = new Extractor(null, null);
        assertThrows(IllegalArgumentException.class, extractor::extract);
    }

    @Test
    public void testExtraction() throws Exception {
        try {
            Files.createDirectories(OUTPUT_FILE.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Could not prepare test", e);
        }
        final Extractor extractor = new Extractor(INPUT_FILE, OUTPUT_FILE);
        extractor.extract();

        // read the generated XLF file
        final List<ITextUnit> testUnitList = new ArrayList<>();
        try (IFilter filter = new XLIFFFilter()) {
            filter.open(new RawDocument(OUTPUT_FILE.toUri(), StandardCharsets.UTF_8.name(), new LocaleId("en"), new LocaleId("de")));
            while (filter.hasNext()) {
                Event event = filter.next();
                if (event.getEventType() == EventType.TEXT_UNIT) {
                    final ITextUnit textUnit = event.getTextUnit();
                    testUnitList.add(textUnit);
                }
            }
        }
        assertEquals(12, testUnitList.size());
    }

}
