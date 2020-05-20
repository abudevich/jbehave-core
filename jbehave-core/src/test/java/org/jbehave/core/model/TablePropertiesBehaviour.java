package org.jbehave.core.model;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Valery Yatsynovich
 */
public class TablePropertiesBehaviour {

    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String CONVERTED = "converted";

    @Test
    public void canGetCustomProperties() {
        TableProperties properties = new TableProperties("ignorableSeparator=!--,headerSeparator=!,valueSeparator=!,"
                + "commentSeparator=#,trim=false,metaByRow=true,transformer=CUSTOM_TRANSFORMER", "|", "|",
                "|--");
        assertThat(properties.getRowSeparator(), equalTo("\n"));
        assertThat(properties.getHeaderSeparator(), equalTo("!"));
        assertThat(properties.getValueSeparator(), equalTo("!"));
        assertThat(properties.getIgnorableSeparator(), equalTo("!--"));
        assertThat(properties.getCommentSeparator(), equalTo("#"));
        assertThat(properties.isTrim(), is(false));
        assertThat(properties.isMetaByRow(), is(true));
        assertThat(properties.getTransformer(), equalTo("CUSTOM_TRANSFORMER"));
    }

    @Test
    public void canSetPropertiesWithBackwardSlash() {
        TableProperties properties = new TableProperties("custom=\\", "|", "|", "|--");
        assertThat(properties.getProperties().getProperty("custom"), equalTo("\\"));
    }

    @Test
    public void canSetPropertiesWithSpecialChars() {
        TableProperties properties = new TableProperties("withSpecialChars=/:*$\\", "|", "|", "|--");
        assertThat(properties.getProperties().getProperty("withSpecialChars"), equalTo("/:*$\\"));
    }

    @Test
    public void canGetDefaultProperties() {
        TableProperties properties = new TableProperties(new Properties());
        assertThat(properties.getHeaderSeparator(), equalTo("|"));
        assertThat(properties.getValueSeparator(), equalTo("|"));
        assertThat(properties.getIgnorableSeparator(), equalTo("|--"));
        assertThat(properties.getCommentSeparator(), equalTo("#"));
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), is(nullValue()));
    }

    @Test
    public void canGetAllProperties() {
        Properties properties = new Properties();
        properties.setProperty(KEY, VALUE);
        TableProperties tableProperties = new TableProperties(properties);
        assertThat(tableProperties.getProperties().containsKey("key"), is(true));
    }

    @Test
    public void canGetPropertiesWithNestedTransformersWithoutEscaping() {
        TableProperties properties = new TableProperties("transformer=CUSTOM_TRANSFORMER, " +
                "tables={transformer=CUSTOM_TRANSFORMER\\, parameter1=value1}");
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), equalTo("CUSTOM_TRANSFORMER"));
        assertThat(properties.getProperties().getProperty("tables"),
                equalTo("{transformer=CUSTOM_TRANSFORMER, parameter1=value1}"));
    }

    @Test
    public void shouldApplyConvertersToValues() {
        ParameterConverters converters = Mockito.mock(ParameterConverters.class);
        Mockito.when(converters.convert(VALUE, String.class)).thenReturn(CONVERTED);
        TableProperties properties = new TableProperties(KEY + "=" + VALUE, "|", "|", "|--", converters);
        assertThat(properties.getProperties().getProperty(KEY), equalTo(CONVERTED));
    }

    @Test
    public void canDecoratePropertyValuesToTrimOrKeepVerbatim() {
        TableProperties properties = new TableProperties("{key1|trim}= surroundedWithSpaces, {key2|verbatim}= surroundedWithSpaces ");
        assertThat(properties.getProperties().getProperty("key1"), equalTo("surroundedWithSpaces"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo(" surroundedWithSpaces "));
    }

    @Test
    public void canDecoratePropertyValuesToUpperAndLowerCase() {
        TableProperties properties = new TableProperties("{key1|uppercase}=toUpper, {key2|lowercase}=toLower");
        assertThat(properties.getProperties().getProperty("key1"), equalTo("TOUPPER"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo("tolower"));
    }

    @Test
    public void canTrimPropertyValuesByDefault() {
        TableProperties properties = new TableProperties("key1= surroundedWithSpaces , key2= ");
        assertThat(properties.getProperties().getProperty("key1"), equalTo("surroundedWithSpaces"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo(""));
    }

    @Test
    public void canChainDecoratorsToDecoratePropertyValues() {
        TableProperties properties = new TableProperties("{key1|uppercase|verbatim}= toUpper , {key2|lowercase|trim}= toLower ");
        assertThat(properties.getProperties().getProperty("key1"), equalTo(" TOUPPER "));
        assertThat(properties.getProperties().getProperty("key2"), equalTo("tolower"));
    }

}
