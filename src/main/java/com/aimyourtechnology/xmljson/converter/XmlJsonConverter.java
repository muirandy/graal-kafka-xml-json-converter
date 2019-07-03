package com.aimyourtechnology.xmljson.converter;

import org.json.JSONObject;
import org.json.XML;

class XmlJsonConverter {

    private static final int PRETTY_PRINT_INDENT_FACTOR = 4;

    static String convertXmlToJson(String xmlString) {
        JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
        return xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
    }

    public String convertJsonToXml(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return XML.toString(json);
    }
}
