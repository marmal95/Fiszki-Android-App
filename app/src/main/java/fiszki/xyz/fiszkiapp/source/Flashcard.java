package fiszki.xyz.fiszkiapp.source;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Keeps all necessary attributes of user's Flashcard
 */
public class Flashcard implements Parcelable {

    private String id;
    private String langFrom;
    private String langTo;
    private String name;
    private String hash;
    private String timeCreated;
    private String timeEdit;
    private String version;
    private String status;
    private String rate;

    /**
     * Initializes all variables
     */
    public Flashcard(){
        this.id = "";
        this.langFrom = "";
        this.langTo = "";
        this.name = "";
        this.hash = "";
        this.timeCreated = "";
        this.timeEdit = "";
        this.version = "";
        this.status = "";
        this.rate = "";
    }

    /**
     * Unpacks Flashcard from Parcel
     * @param in parcel with packed Flashcard
     */
    private Flashcard(Parcel in) {
        this.id = in.readString();
        this.langFrom = in.readString();
        this.langTo = in.readString();
        this.name = in.readString();
        this.hash = in.readString();
        this.timeCreated = in.readString();
        this.timeEdit = in.readString();
        this.version = in.readString();
        this.status = in.readString();
        this.rate = in.readString();
    }

    public static final Creator<Flashcard> CREATOR = new Creator<Flashcard>() {
        /**
         * {@inheritDoc}
         */
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };

    /**
     * {@inheritDoc}
     * @return 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Pack Flashcard to parcel
     * @param dest parcel with packed flashcard
     * @param flags -
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(langFrom);
        dest.writeString(langTo);
        dest.writeString(name);
        dest.writeString(hash);
        dest.writeString(timeCreated);
        dest.writeString(timeEdit);
        dest.writeString(version);
        dest.writeString(status);
        dest.writeString(rate);
    }

    /**
     * Get Flashcard id
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set Flashcard id
     * @param id Flashcard id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get language of words to translate
     * @return language (pl/en/de/es/fe)
     */
    public String getLangFrom() {
        return langFrom;
    }

    /**
     * Set language of words to translate
     * @param langFrom language (pl/en/de/es/fe)
     */
    public void setLangFrom(String langFrom) {
        this.langFrom = langFrom;
    }

    /**
     * Get language of words translated
     * @return language (pl/en/de/es/fe)
     */
    public String getLangTo() {
        return langTo;
    }

    /**
     * Set language of words translated
     * @param langTo language (pl/en/de/es/fe)
     */
    public void setLangTo(String langTo) {
        this.langTo = langTo;
    }

    /**
     * Get Flashcard name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set Flashcard name. Encode it with URLEncode
     * @param name Flashcard name
     */
    public void setName(String name) {
        try {
            name = URLDecoder.decode(name, "UTF-8");
            // name = Html.fromHtml(name).toString();
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.name = name;
    }

    /**
     * Get Flashcard hash
     * @return hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Set Flashcard hash
     * @param hash Flashcard hash
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Get Flashcard created time
     * @return UNIX-time
     */
    public String getTimeCreated() {
        return timeCreated;
    }

    /**
     * Set Flashcard created time
     * @param timeCreated UNIX-time
     */
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * Get Flashcard edit time
     * @return UNIX-time
     */
    public String getTimeEdit() {
        return timeEdit;
    }

    /**
     * Set Flashcard edit time
     * @param timeEdit UNIX-time
     */
    public void setTimeEdit(String timeEdit) {
        this.timeEdit = timeEdit;
    }

    /**
     * Get Flashcard version
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set Flashcard version
     * @param version Flashcard version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get Flashcard status
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set Flashcard status
     * @param status Flashcard status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get Flashcard rate
     * @return rate
     */
    public String getRate() {
        return rate;
    }

    /**
     * SetFlashcard rate
     * @param rate Flashcard rate
     */
    public void setRate(String rate) {
        this.rate = rate;
    }
}
