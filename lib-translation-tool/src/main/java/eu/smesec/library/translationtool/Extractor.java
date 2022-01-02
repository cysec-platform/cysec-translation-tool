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
package eu.smesec.library.translationtool;

import eu.smesec.cysec.platform.bridge.generated.Option;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.filterwriter.XLIFFWriterParameters;
import net.sf.okapi.common.resource.TextUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extracts translatable content from an original coach to XLIFF
 *
 * @author Matthias Luppi
 */
public class Extractor {

    private static final Logger log = LoggerFactory.getLogger(Extractor.class);

    private final Path inputFile;
    private final Path outputFile;

    public Extractor(final Path inputFile, final Path outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void extract() throws IOException, JAXBException {
        if (inputFile == null) {
            throw new IllegalArgumentException("Invalid input file");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Invalid output file");
        }
        if (Files.notExists(outputFile.getParent())) {
            Files.createDirectories(outputFile.getParent());
        }

        JAXBContext context = JAXBContext.newInstance(Questionnaire.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final Questionnaire questionnaire = (Questionnaire) unmarshaller.unmarshal(inputFile.toFile());

        // get source language from XML or use English as fallback
        final LocaleId srcLocale = LocaleId.fromString(StringUtils.defaultString(questionnaire.getLanguage(), "en"));

        try (XLIFFWriter writer = new XLIFFWriter()) {
            final String originalFileName = inputFile.getFileName().toString();
            writer.create(outputFile.toAbsolutePath().toString(), null, srcLocale, null, "xml", originalFileName, null);

            log.info("Starting extraction of translatable content from '{}'", inputFile);

            XLIFFWriterParameters paramsXliff = writer.getParameters();
            paramsXliff.setPlaceholderMode(true);
            paramsXliff.setCopySource(false);
            paramsXliff.setIncludeAltTrans(true);
            paramsXliff.setIncludeCodeAttrs(false);
            paramsXliff.setEscapeGt(true);

            // general translatable attributes
            writer.writeTextUnit(new TextUnit(
                    TextUnitId.attr(TextUnitId.COACH_READABLE_NAME).toString(),
                    questionnaire.getReadableName()
            ));
            writer.writeTextUnit(new TextUnit(
                    TextUnitId.attr(TextUnitId.COACH_DESCRIPTION).toString(),
                    questionnaire.getDescription()));

            // translatable content of questions
            for (Question question : questionnaire.getQuestions().getQuestion()) {
                writer.writeTextUnit(new TextUnit(
                        TextUnitId.attr(TextUnitId.QST_TEXT).qst(question).toString(),
                        question.getText()
                ));
                if (question.getOptions() != null) {
                    for (Option option : question.getOptions().getOption()) {
                        if (StringUtils.isNotBlank(option.getText())) {
                            writer.writeTextUnit(new TextUnit(
                                    TextUnitId.attr(TextUnitId.OPT_TEXT).qst(question).opt(option).toString(),
                                    option.getText()
                            ));
                        }
                        if (StringUtils.isNotBlank(option.getShort())) {
                            writer.writeTextUnit(new TextUnit(
                                    TextUnitId.attr(TextUnitId.OPT_SHORT).qst(question).opt(option).toString(),
                                    option.getShort()
                            ));
                        }
                        if (StringUtils.isNotBlank(option.getComment())) {
                            writer.writeTextUnit(new TextUnit(
                                    TextUnitId.attr(TextUnitId.OPT_COMMENT).qst(question).opt(option).toString(),
                                    option.getComment()
                            ));
                        }
                    }
                }
                if (StringUtils.isNotBlank(question.getReadMore())) {
                    writer.writeTextUnit(new TextUnit(
                            TextUnitId.attr(TextUnitId.QST_READ_MORE).qst(question).toString(),
                            question.getReadMore()
                    ));
                }
                if (question.getInstruction() != null && StringUtils.isNotBlank(question.getInstruction().getText())) {
                    writer.writeTextUnit(new TextUnit(
                            TextUnitId.attr(TextUnitId.QST_INSTRUCTION).qst(question).toString(),
                            question.getInstruction().getText()
                    ));
                }
            }

            log.info("Translatable content extracted to '{}'", outputFile);
        }
    }
}
