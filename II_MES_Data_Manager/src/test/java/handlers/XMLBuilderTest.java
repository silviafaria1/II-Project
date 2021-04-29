package handlers;

import handlers.XMLBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class XMLBuilderTest {
    XMLBuilder builder;

    @BeforeMethod
    public void init() throws ParserConfigurationException {
        builder = new XMLBuilder();
    }

    @Test
    public void testInitFilteredStockResponse() throws TransformerException {
        builder.buildFilteredStockResponse(1,54);
        String result = builder.getXMLAsString();
        Assert.assertEquals(
                result,
                "<Current_Stores quantity=\"54\" type=\"P1\"/>\r\n"
        );
    }

}