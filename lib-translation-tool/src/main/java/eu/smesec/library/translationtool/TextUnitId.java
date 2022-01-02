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
    public static final String COACH_READABLE_CLASS = "coach-readable-class";
    public static final String COACH_DESCRIPTION = "coach-description";
    public static final String QST_TEXT = "text";
    public static final String QST_READ_MORE = "read-more";
    public static final String QST_INSTRUCTION = "instruction";
    public static final String OPT_TEXT = "text";
    public static final String OPT_SHORT = "short";
    public static final String OPT_COMMENT = "comment";

    /**
     * Pattern to parse an ID to question-id, option-id and attribute
     */
    private static final Pattern pattern = Pattern.compile("^(?:QST:(?<qid>.*?)\\|\\|(?:OPT:(?<oid>.*?)\\|\\|)?)?(?<attr>.*)$");

    private final String attribute;
    private String questionId;
    private String optionIn;

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
        this.optionIn = option.getId();
        return this;
    }

    /**
     * Set the option-id
     *
     * @param optionId the option-id to be set
     * @return the {@link TextUnitId} with the option-id set
     */
    public TextUnitId opt(String optionId) {
        this.optionIn = optionId;
        return this;
    }

    /**
     * Parses an ID according to the set properties (question-id, option-id and attribute)
     *
     * @param input an ID used as 'trans-id' in an XLIFF file
     * @return a {@link TextUnitId} with attributes according to the input
     */
    public static TextUnitId parse(String input) {
        final Matcher m = pattern.matcher(input);
        if (!m.matches()) {

            log.error("Could not parse '{}'", input);
            throw new IllegalArgumentException("Could not parse '" + input + "'");
        }
        try {
            return TextUnitId
                    .attr(m.group("attr"))
                    .qst(m.group("qid"))
                    .opt(m.group("oid"));
        } catch (IllegalStateException e) {
            log.error("Could not parse '{}'", input);
            throw new IllegalArgumentException("Could not parse '" + input + "'", e);
        }
    }

    /**
     * Generates the ID according to the set properties (question-id, option-id and attribute)
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
            if (optionIn != null) {
                sb.append("OPT:");
                sb.append(optionIn);
                sb.append("||");
            }
        }
        sb.append(attribute);
        return sb.toString();
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getOptionIn() {
        return optionIn;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TextUnitId that = (TextUnitId) o;
        if (!attribute.equals(that.attribute)) return false;
        if (!Objects.equals(questionId, that.questionId)) return false;
        return Objects.equals(optionIn, that.optionIn);
    }

    @Override
    public int hashCode() {
        int result = attribute.hashCode();
        result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
        result = 31 * result + (optionIn != null ? optionIn.hashCode() : 0);
        return result;
    }
}
