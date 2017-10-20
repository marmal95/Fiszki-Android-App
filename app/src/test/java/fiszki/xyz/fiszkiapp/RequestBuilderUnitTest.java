package fiszki.xyz.fiszkiapp;

import org.junit.Test;

import fiszki.xyz.fiszkiapp.source.RequestBuilder;

import static org.junit.Assert.*;

public class RequestBuilderUnitTest {
    @Test
    public void buildEmptyRequestTest() throws Exception {
        RequestBuilder requestBuilder = new RequestBuilder();
        String actual = requestBuilder.buildRequest();
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void buildRequestTest() throws Exception {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.putParameter("testKey1", "testValue1");
        requestBuilder.putParameter("testKey2", "testValue2");
        requestBuilder.putParameter("testKey3", "testValue3");

        String actual = requestBuilder.buildRequest();
        String expected = "testKey1=testValue1&testKey2=testValue2&testKey3=testValue3";
        assertEquals(expected, actual);
    }

    @Test
    public void buildRequestWithTwoSameKeysTest() {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.putParameter("testKey1", "testValue1");
        requestBuilder.putParameter("testKey2", "testValue2");
        requestBuilder.putParameter("testKey1", "testValue3");

        String actual = requestBuilder.buildRequest();
        String expected = "testKey1=testValue3&testKey2=testValue2";
        assertEquals(expected, actual);
    }

    @Test
    public void buildEncodedUtf8RequestTest() {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.putParameter("testKey1", "testValue!@#$%^&*()-=?[]{}';,./\\|");
        requestBuilder.encodeParameters("UTF-8");

        // https://www.freeformatter.com/url-encoder.html
        String actual = requestBuilder.buildRequest();
        String expected = "testKey1=testValue%21%40%23%24%25%5E%26*%28%29-%3D%3F%5B%5D%7B%7D%27%3B%2C.%2F%5C%7C";
        assertEquals(expected, actual);
    }
}