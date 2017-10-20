package fiszki.xyz.fiszkiapp;


import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class FlashcardUnitTest {

    @Test
    public void initFlashcardDefaultConstructorTest() throws Exception {
        Flashcard flashcard = new Flashcard();
        assertNull(flashcard.getId());
        assertNull(flashcard.getLangFrom());
        assertNull(flashcard.getLangTo());
        assertNull(flashcard.getName());
        assertNull(flashcard.getHash());
        assertNull(flashcard.getTimeCreated());
        assertNull(flashcard.getTimeEdit());
        assertNull(flashcard.getVersion());
        assertNull(flashcard.getStatus());
        assertNull(flashcard.getRate());
    }

    @Test
    public void flashcardParcelableTest() throws Exception {
        Flashcard flashcard = new Flashcard();
        flashcard.setId("5");
        flashcard.setLangFrom("pl");
        flashcard.setLangTo("en");
        flashcard.setName("testFlashcard");
        flashcard.setHash("fsdjkn54rnfs");
        flashcard.setTimeCreated("5345345345353");
        flashcard.setTimeEdit("2423423423423");
        flashcard.setVersion("3");
        flashcard.setStatus("2");
        flashcard.setRate("10");

        Parcel parcel = Parcel.obtain();
        flashcard.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        Flashcard createdFromParcel = Flashcard.CREATOR.createFromParcel(parcel);

        assertEquals(flashcard.getId(), createdFromParcel.getId());
        assertEquals(flashcard.getLangFrom(), createdFromParcel.getLangFrom());
        assertEquals(flashcard.getLangTo(), createdFromParcel.getLangTo());
        assertEquals(flashcard.getName(), createdFromParcel.getName());
        assertEquals(flashcard.getHash(), createdFromParcel.getHash());
        assertEquals(flashcard.getTimeCreated(), createdFromParcel.getTimeCreated());
        assertEquals(flashcard.getTimeEdit(), createdFromParcel.getTimeEdit());
        assertEquals(flashcard.getVersion(), createdFromParcel.getVersion());
        assertEquals(flashcard.getStatus(), createdFromParcel.getStatus());
        assertEquals(flashcard.getRate(), createdFromParcel.getRate());
    }
}
