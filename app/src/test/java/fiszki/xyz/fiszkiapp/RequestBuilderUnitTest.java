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
}