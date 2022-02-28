# CYSEC Translation Tool

This tool supports the translation and build process for multilingual coaches.
It allows extracting text elements of a coach to XLIFF while assigning stable identifiers
for the text blocks. Furthermore, the tool can merge translations in XLIFF format with the
original coach in order to get a translated coach.

## Usage

### Translation Tool Library

#### Maven
Use this library as a dependency like this:

```xml
<dependency>
    <groupId>eu.smesec.cysec</groupId>
    <artifactId>lib-translation-tool</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### Example for `Extractor`
```java
// define variables
Path coachXmlPath = Paths.get("coach.xml");
Path xlfOutputPath = Paths.get("source.xml");

// create extractor
Extractor extractor = new Extractor(coachXmlPath, xlfOutputPath);

// execute extraction process
extractor.extract();
```

#### Example for `Merger`
```java
// define variables
Path coachXmlPath = Paths.get("coach.xml");
Path xlfTargetPath = Paths.get("source-de.xlf");
String targetLang = "de";
Path outputPath = Paths.get("coach-de.xml");

// create merger
Merger merger = new Merger(coachXmlPath, xlfTargetPath, targetLang, outputPath);

// execute merge process
merger.merge();
```

## Notes

### Identifiers
The IDs of text-units are structured as follows:
```text
Attributes of the coach:   <attr>
Attributes of a question:  QST:<qid>||<attr>
Attributes of an option:   QST:<qid>||OPT:<oid>||<attr>
Dictionary entries:        DK:<dkey>||<attr>
```
where the placeholders represent
```text
<attr>  ->  Attribute name
<qid>   ->  Question-ID
<oid>   ->  Option-ID
<dkey>  ->  Dictionary key
```

### Supported text elements

| Category         | Element                                    | Attribute |
|------------------|--------------------------------------------|-----------|
| Coach            | Displayed name of coach                    | `coach-readable-name` |
| Coach            | Description of coach                       | `coach-description` |
| Question         | Question text                              | `text` |
| Question         | Introduction (shown above question)        | `introduction` |
| Question         | Instruction (text on right side)           | `instruction` |
| Question         | Infotext (used on special question type)   | `infotext` |
| Question         | Read-more (shown in collapsible at bottom) | `read-more` |
| Option           | Option text (answer)                       | `text` |
| Option           | Comment (shown if option selected)         | `comment` |
| Dictionary entry | Text of dictionary entry                   | `text` |

## License
This project is licensed under the Apache 2.0 license, see [LICENSE](LICENSE).
