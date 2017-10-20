package fiszki.xyz.fiszkiapp.source;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


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

    public Flashcard(){
        this.id = null;
        this.langFrom = null;
        this.langTo = null;
        this.name = null;
        this.hash = null;
        this.timeCreated = null;
        this.timeEdit = null;
        this.version = null;
        this.status = null;
        this.rate = null;
    }

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

        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLangFrom() {
        return langFrom;
    }

    public void setLangFrom(String langFrom) {
        this.langFrom = langFrom;
    }

    public String getLangTo() {
        return langTo;
    }

    public void setLangTo(String langTo) {
        this.langTo = langTo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        try {
            name = URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getTimeEdit() {
        return timeEdit;
    }

    public void setTimeEdit(String timeEdit) {
        this.timeEdit = timeEdit;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
