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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model for a distinctive ID for translatable content in coaches
 *
 * @author Matthias Luppi
 */
public class TextUnitId {

    private static final Logger log = LoggerFactory.getLogger(TextUnitId.class);

    public static final String COACH_READABLE_NAME = "coach-readable-name";
    public static final String COACH_DESCRIPTION = "coach-description";
    public static final String QST_TEXT = "text";
    public static final String QST_INTRODUCTION = "introduction";
    public static final String QST_READ_MORE = "read-more";
    public static final String QST_INFOTEXT = "infotext";
    public static final String QST_INSTRUCTION = "instruction";
    public static final String OPT_TEXT = "text";
    public static final String OPT_COMMENT = "comment";
    public static final String DK_TEXT = "text";

    /**
     * Pattern to parse an ID to question-id, option-id, dictionary-key and attribute
     */
    private static final Pattern idPattern = Pattern.compile("^(?:QST:(?<qid>.*?)\\|\\|(?:OPT:(?<oid>.*?)\\|\\|)?)?(?:DK:(?<dkey>.*?)\\|\\|)?(?<attr>.*)$");

    private final String attribute;
    private String questionId;
    private String optionId;
    private String dictionaryKey;

    private TextUnitId(String attribute) {
        this.attribute = attribute;
    }

    /**
     * Sets the attribute
     *
     * @param attribute the attribute-name to be set
     * @return the {@link TextUnitId} with the attribute-name set
     */
    public static TextUnitId attr(String attribute) {
        return new TextUnitId(attribute);
    }

    /**
     * Sets the question-id
     *
     * @param question the {@link Question} of which the question-id should be set
     * @return the {@link TextUnitId} with the question-id set
     */
    public TextUnitId qst(Question question) {
        this.questionId = question.getId();
        return this;
    }

    /**
     * Set the question-id
     *
     * @param questionId the question-id to be set
     * @return the {@link TextUnitId} with the question-id set
     */
    public TextUnitId qst(String questionId) {
        this.questionId = questionId;
        return this;
    }

    /**
     * Sets the option-id
     *
     * @param option the {@link Option} of which the option-id should be set
     * @return the {@link TextUnitId} with the option-id set
     */
    public TextUnitId opt(Option option) {
        this.optionId = option.getId();
        return this;
    }

    /**
     * Set the option-id
     *
     * @param optionId the option-id to be set
     * @return the {@link TextUnitId} with the option-id set
     */
    public TextUnitId opt(String optionId) {
        this.optionId = optionId;
        return this;
    }

    /**
     * Sets the dictionary-key
     *
     * @param entry the {@link DictionaryEntry} of which the dictionary-key should be set
     * @return the {@link TextUnitId} with the dictionary-key set
     */
    public TextUnitId opt(DictionaryEntry entry) {
        this.optionId = entry.getKey();
        return this;
    }

    /**
     * Set the dictionary-key
     *
     * @param dictionaryKey the dictionary-key to be set
     * @return the {@link TextUnitId} with the dictionary-key set
     */
    public TextUnitId dkey(String dictionaryKey) {
        this.dictionaryKey = dictionaryKey;
        return this;
    }

    /**
     * Parses an ID according to the set properties
     *
     * @param input an ID used as 'trans-id' in an XLIFF file
     * @return a {@link TextUnitId} with attributes according to the input
     */
    public static TextUnitId parse(String input) {
        final Matcher m = idPattern.matcher(input);
        if (!m.matches()) {
            log.error("Could not parse '{}'", input);
            throw new IllegalArgumentException("Could not parse '" + input + "'");
        }
        try {
            return TextUnitId
                    .attr(m.group("attr"))
                    .qst(m.group("qid"))
                    .opt(m.group("oid"))
                    .dkey(m.group("dkey"));
        } catch (IllegalStateException e) {
            log.error("Could not parse '{}'", input);
            throw new IllegalArgumentException("Could not parse '" + input + "'", e);
        }
    }

    /**
     * Generates the ID according to the set properties
     *
     * @return ID to be used as 'trans-id' in XLIFF files
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (questionId != null) {
            sb.append("QST:");
            sb.append(questionId);
            sb.append("||");
            if (optionId != null) {
                sb.append("OPT:");
                sb.append(optionId);
                sb.append("||");
            }
        } else if (dictionaryKey != null) {
            sb.append("DK:");
            sb.append(dictionaryKey);
            sb.append("||");
        }
        sb.append(attribute);
        return sb.toString();
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getOptionId() {
        return optionId;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getDictionaryKey() {
        return dictionaryKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TextUnitId that = (TextUnitId) o;
        if (!attribute.equals(that.attribute)) return false;
        if (!Objects.equals(questionId, that.questionId)) return false;
        if (!Objects.equals(optionId, that.optionId)) return false;
        return Objects.equals(dictionaryKey, that.dictionaryKey);
    }

    @Override
    public int hashCode() {
        int result = attribute.hashCode();
        result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
        result = 31 * result + (optionId != null ? optionId.hashCode() : 0);
        result = 31 * result + (dictionaryKey != null ? dictionaryKey.hashCode() : 0);
        return result;
    }
}
