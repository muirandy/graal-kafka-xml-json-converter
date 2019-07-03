package com.aimyourtechnology.xmljson.converter;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import java.util.Random;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class XmlJsonConverterTest {

    private String orderId = "" + new Random().nextInt();

    @Test
    void convertsXmlToJson() {
        XmlJsonConverter converter = new XmlJsonConverter();

        String jsonString = converter.convertXmlToJson(xmlValue());

        assertJsonEquals(jsonValue(), jsonString);
    }

    @Test
    void convertsJsonToXml() {
        XmlJsonConverter converter = new XmlJsonConverter();

        String xmlString = converter.convertJsonToXml(jsonValue());

        XmlAssert.assertThat(xmlValue()).and(xmlString).ignoreWhitespace().areIdentical();
    }

    private String xmlValue() {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<order>" +
                        "<orderId>%s</orderId>\n" +
                        "</order>", orderId
        );
    }

    private String jsonValue() {
        return String.format(
                "{" +
                        "  \"order\":{" +
                        "    \"orderId\":%s" +
                        "  }" +
                        "}",
                orderId
        );
    }
}
