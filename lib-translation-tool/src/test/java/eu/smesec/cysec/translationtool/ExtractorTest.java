/*-
 * #%L
 * CYSEC Translation Tool Library
 * %%
 * Copyright (C) 2021 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.translationtool;

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
 * Tests for the {@link Extractor}.
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
        assertEquals(16, testUnitList.size());
    }

}
