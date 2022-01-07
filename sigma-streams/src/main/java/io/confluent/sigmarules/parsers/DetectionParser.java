package io.confluent.sigmarules.parsers;

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.fieldmapping.FieldMapper;
import io.confluent.sigmarules.models.OperatorType;
import io.confluent.sigmarules.models.SigmaDetection;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DetectionParser {
    static final String OPEN_BRACKET = "{";
    static final String CLOSE_BRACKET = "}";
    static final String OPEN_ARRAY = "[";
    static final String CLOSE_ARRAY = "]";
    static final String EQUALS = "=";
    static final String SEPERATOR = "|";
    static final String COMMA_SEP = ",";

    private FieldMapper fieldMapper = null;

    public DetectionParser() {

    }

    public DetectionParser(FieldMapper fieldMapper) {
        this.fieldMapper = fieldMapper;
    }

    public SigmaDetection parseDetection(String detection) throws InvalidSigmaRuleException {
        SigmaDetection detectionModel = new SigmaDetection();

        parseName(detectionModel, detection);
        parseValues(detectionModel, detection);
        
        return detectionModel;
    }

    private void parseName(SigmaDetection detectionModel, String name) {
        String parsedString = StringUtils.substringBetween(name, OPEN_BRACKET, EQUALS);
        String parsedName = StringUtils.substringBefore(parsedString, SEPERATOR);

        detectionModel.setSigmaName(parsedName);
        detectionModel.setName(parsedName);

        // override name with mapped name
        if (fieldMapper != null) {
            // TODO: mapped fields can be an array - only taking first value for now
            List<String> mappedField = fieldMapper.getSigmaFields().getSigmaField(parsedName);
            if (mappedField.isEmpty() == false) {
                System.out.println("mappedField: " + mappedField.get(0));
                detectionModel.setName(mappedField.get(0));
            }
        }

        // handles the case where the operator is piped with the name (ex. field|endswith)
        if (StringUtils.contains(name, SEPERATOR))
            detectionModel.setOperator(OperatorType.getEnum(StringUtils.substringAfter(parsedString, SEPERATOR)));
    }

    private void parseValues(SigmaDetection detectionModel, String values) throws InvalidSigmaRuleException {
        String parsedValueString = StringUtils.substringAfter(values, EQUALS);
        parsedValueString = parsedValueString.substring(0,parsedValueString.length()-1);

        // Values can be an array or a single value.  If the operation is a regular expression we will assume this is
        // not an array. Array bracket characters can be found in regular expressions so this current logic would break.
        if ( (detectionModel.getOperator() != null && detectionModel.getOperator() == OperatorType.REGEX) ||
                !StringUtils.containsAny(parsedValueString, OPEN_ARRAY + CLOSE_ARRAY))
        {
            String dValue = StringUtils.deleteWhitespace(parsedValueString);
            detectionModel.addValue(buildStringWithOperator(dValue, detectionModel.getOperator()));
        }
        else {
            List<String> valuesList = Arrays.asList(
                    StringUtils.deleteWhitespace(StringUtils.substringBetween(parsedValueString, OPEN_ARRAY, CLOSE_ARRAY)).split(","));

            for (String dValue : valuesList) {
                detectionModel.addValue(buildStringWithOperator(dValue, detectionModel.getOperator()));
            }
        }
    }

    // TODO We need to handle escaping in sigma
    private String buildStringWithOperator(String value, OperatorType operator) throws InvalidSigmaRuleException {

        // Sigma spec isn't clear on what to do with wildcard characters when they are in vlaues with a "transformation"
        // which we are calling operator
        if (operator != null) {
            switch (operator) {
                case STARTS_WITH:
                case BEGINS_WITH:
                    return sigmaWildcardToRegex(value) + ".*";
                case CONTAINS:
                    return ".*" + sigmaWildcardToRegex(value) + ".*";
                case ENDS_WITH:
                    return ".*" + sigmaWildcardToRegex(value);
                case REGEX:
                    if (!validRegex(value))
                        throw new InvalidSigmaRuleException("Regular expression operator specified " +
                                "but pattern did not compile for value = " + value);
                    return value;
            }
        }

        return sigmaWildcardToRegex(value);
    }

    private boolean validRegex(String regex) {
        try {
            // check if pattern is already a regex and do nothing
            Pattern.compile(regex);
            return true;
          } catch (PatternSyntaxException e) {
           return false;
          }
    }

    /**
     * This function takes a sigma expression which allows the typical search wildcards and converts it into a java regex
     * pattern.  If there are no sigma wildcards then nothing will change
     * @param value sigma pattern value
     * @return java regex pattern
     */
    private String sigmaWildcardToRegex(String value) {
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < value.length(); ++i) {
            final char c = value.charAt(i);
            switch(c) {
                case '*': out.append(".*"); break;
                case '?': out.append('.'); break;
                case '.': out.append("\\."); break;
                case '\\': out.append("\\\\"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    public static void main(String[] args) throws InvalidSigmaRuleException {
        //String theString = "{host|endswith=[1, 2, 3, 4, 5, 6, 7, 8, 9, 0]}";
        //String theString = "{host=^.*1$}";
        String theString = "{host|greater_than=100}";
        
        DetectionParser parser = new DetectionParser();
        SigmaDetection model = parser.parseDetection(theString);

        System.out.println(model.toString());
        System.out.println("match: " + model.matches("101", false));
    }
}
