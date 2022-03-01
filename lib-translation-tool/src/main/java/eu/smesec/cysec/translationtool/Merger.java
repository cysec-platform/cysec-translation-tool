/*-
 * #%L
 * CYSEC Translation Tool Library
 * %%
 * Copyright (C) 2021 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.bridge.generated.DictionaryEntry;
import eu.smesec.cysec.platform.bridge.generated.Option;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Merges an original coach with translations in XLIFF format.
 *
 * @author Matthias Luppi
 */
public class Merger {

    private static final Logger log = LoggerFactory.getLogger(Merger.class);

    private final Path inputFile;
    private final Path xlfFile;
    private final Path outputFile;
    private final LocaleId trgLocale;

    public Merger(final Path inputFile, final Path xlfFile, final String targetLang, final Path outputFile) {
        this.inputFile = inputFile;
        this.xlfFile = xlfFile;
        this.trgLocale = new LocaleId(targetLang);
        this.outputFile = outputFile;
    }

    /**
     * Executes the merge operation.
     *
     * @return true if all translations found, false if there were missing translations
     * @throws IOException if an I/O error occurs
     * @throws JAXBException if an error occurred while handling the XML files
     */
    public boolean merge() throws IOException, JAXBException {
        if (inputFile == null) {
            throw new IllegalArgumentException("Invalid input file");
        }
        if (xlfFile == null) {
            throw new IllegalArgumentException("Invalid XLF file");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Invalid output file");
        }
        if (Files.notExists(outputFile.getParent())) {
            Files.createDirectories(outputFile.getParent());
        }

        log.info("Base coach for translations is '{}'", inputFile);

        final JAXBContext context = JAXBContext.newInstance(Questionnaire.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final Questionnaire questionnaire = (Questionnaire) unmarshaller.unmarshal(inputFile.toFile());

        // get source language from XML or use English as fallback
        final LocaleId srcLocale;
        if (StringUtils.isNotBlank(questionnaire.getLanguage())) {
            srcLocale = new LocaleId(questionnaire.getLanguage());
            log.info("Detected source language is '{}'", srcLocale.getLanguage());
        } else {
            srcLocale = new LocaleId("en");
            log.info("No source language set, falling back to '{}'", srcLocale.getLanguage());
        }

        final TranslationApplier ta = new TranslationApplier(trgLocale);

        // load all available translations
        log.info("Reading translation entries from '{}'", xlfFile);
        try (IFilter filter = new XLIFFFilter()) {
            filter.open(new RawDocument(xlfFile.toUri(), StandardCharsets.UTF_8.name(), srcLocale, trgLocale));
            while (filter.hasNext()) {
                Event event = filter.next();
                if (event.getEventType() == EventType.TEXT_UNIT) {
                    final ITextUnit textUnit = event.getTextUnit();
                    if (!textUnit.getTargetLocales().isEmpty()) {
                        if (textUnit.getTargetLocales().size() > 1) {
                            throw new IllegalArgumentException("More than one target language in XLIFF file for id=" + textUnit.getId());
                        }
                        final LocaleId localeId = textUnit.getTargetLocales().iterator().next();
                        if (trgLocale != localeId) {
                            throw new IllegalArgumentException("Requested target languages does not match translation (id=" + textUnit.getId() + ")");
                        }
                    } else {
                        log.debug("Translation entry contains no target element -> {}", textUnit.getId());
                    }
                    ta.learn(textUnit);
                }
            }
        }
        log.info("Loaded {} translation entries", ta.memoryCount());

        // update general attributes with translations
        ta.apply(TextUnitId.attr(TextUnitId.COACH_READABLE_NAME), questionnaire::setReadableName);
        ta.apply(TextUnitId.attr(TextUnitId.COACH_DESCRIPTION), questionnaire::setDescription);

        // update content of questions with translations
        if (questionnaire.getQuestions() != null) {
            for (Question question : questionnaire.getQuestions().getQuestion()) {
                ta.apply(TextUnitId.attr(TextUnitId.QST_TEXT).qst(question), question::setText);
                ta.apply(TextUnitId.attr(TextUnitId.QST_INTRODUCTION).qst(question), question::setIntroduction);
                if (question.getOptions() != null) {
                    for (Option option : question.getOptions().getOption()) {
                        if (StringUtils.isNotBlank(option.getText())) {
                            ta.apply(TextUnitId.attr(TextUnitId.OPT_TEXT).qst(question).opt(option), option::setText);
                        }
                        if (StringUtils.isNotBlank(option.getComment())) {
                            ta.apply(TextUnitId.attr(TextUnitId.OPT_COMMENT).qst(question).opt(option), option::setComment);
                        }
                    }
                }
                if (StringUtils.isNotBlank(question.getInfotext())) {
                    ta.apply(TextUnitId.attr(TextUnitId.QST_INFOTEXT).qst(question), question::setInfotext);
                }
                if (StringUtils.isNotBlank(question.getReadMore())) {
                    ta.apply(TextUnitId.attr(TextUnitId.QST_READ_MORE).qst(question), question::setReadMore);
                }
                if (question.getInstruction() != null && StringUtils.isNotBlank(question.getInstruction().getText())) {
                    ta.apply(TextUnitId.attr(TextUnitId.QST_INSTRUCTION).qst(question), s -> question.getInstruction().setText(s));
                }
            }
        }

        // update dictionary with translations
        if (questionnaire.getDictionary() != null) {
            for (DictionaryEntry entry : questionnaire.getDictionary().getEntry()) {
                ta.apply(TextUnitId.attr(TextUnitId.DK_TEXT).dkey(entry.getKey()), entry::setValue);
            }
        }

        log.info("Applied {} translations", ta.getApplyCount());
        log.warn("Could not find {} translations", ta.getNotFoundCount());

        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(questionnaire, outputFile.toFile());

        log.info("Translated coach written to '{}'", outputFile);

        return (ta.getNotFoundCount() == 0);
    }

    private static class TranslationApplier {

        private final LocaleId trgLocale;
        private final Map<TextUnitId, ITextUnit> textUnitById = new HashMap<>();
        private long applyCount = 0;
        private long notFoundCount = 0;

        public TranslationApplier(final LocaleId trgLocale) {
            this.trgLocale = trgLocale;
        }

        public void learn(ITextUnit textUnit) {
            textUnitById.put(TextUnitId.parse(textUnit.getId()), textUnit);
        }

        public void apply(TextUnitId id, Consumer<String> fieldSetter) {
            ITextUnit textUnit = textUnitById.get(id);
            if (textUnit != null && textUnit.getTarget(trgLocale) != null && !textUnit.getTarget(trgLocale).isEmpty()) {
                fieldSetter.accept(textUnit.getTarget(trgLocale).toString());
                log.info("Translation applied -> {} ", id);
                applyCount++;
            } else {
                log.warn("Translation not found -> {}", id);
                notFoundCount++;
            }
        }

        public int memoryCount() {
            return textUnitById.size();
        }

        public long getApplyCount() {
            return applyCount;
        }

        public long getNotFoundCount() {
            return notFoundCount;
        }
    }

}
