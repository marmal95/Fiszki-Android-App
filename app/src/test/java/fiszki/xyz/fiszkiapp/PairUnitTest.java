package fiszki.xyz.fiszkiapp;

import org.junit.Test;

import fiszki.xyz.fiszkiapp.utils.Pair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PairUnitTest {
    @Test
    public void initPairDefaultConstructorTest() throws Exception {
        Pair pair = new Pair();
        assertNull(pair.getLeftValue());
        assertNull(pair.getRightValue());
    }

    @Test
    public void initPairConstructorTest() throws Exception {
        Pair pair = new Pair("testLeftValue", "testRightValue");
        assertEquals("testLeftValue", pair.getLeftValue());
        assertEquals("testRightValue", pair.getRightValue());
    }

    @Test
    public void swapPairValuesTest() throws Exception {
        Pair pair = new Pair("testLeftValue", "testRightValue");
        pair.swapValues();
        assertEquals("testLeftValue", pair.getRightValue());
        assertEquals("testRightValue", pair.getLeftValue());
    }
}