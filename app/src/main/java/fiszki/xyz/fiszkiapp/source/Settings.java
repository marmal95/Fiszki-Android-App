package fiszki.xyz.fiszkiapp.source;

public class Settings {

    private boolean displayInSequence;
    private boolean displayRevSequence;
    private boolean displayRandomly;

    private boolean repeatNotKnown;
    private boolean reverseLanguages;
    private boolean createRevisionList;

    private boolean decisionMode;
    private boolean writeMode;

    private boolean ttsEnabled;
    private int ttsSpeed;
    private boolean skipBrackets;
    private boolean autoReadWords;
    private boolean autoReadTrans;

    private int fontSize;

    public boolean isDisplayInSequence() {
        return displayInSequence;
    }

    public void setDisplayInSequence(boolean displayInSequence) {
        this.displayInSequence = displayInSequence;
    }

    public boolean isDisplayRevSequence() {
        return displayRevSequence;
    }

    public void setDisplayRevSequence(boolean displayRevSequence) {
        this.displayRevSequence = displayRevSequence;
    }

    public boolean isDisplayRandomly() {
        return displayRandomly;
    }

    public void setDisplayRandomly(boolean displayRandomly) {
        this.displayRandomly = displayRandomly;
    }

    public boolean isRepeatNotKnown() {
        return repeatNotKnown;
    }

    public void setRepeatNotKnown(boolean repeatNotKnown) {
        this.repeatNotKnown = repeatNotKnown;
    }

    public boolean isReverseLanguages() {
        return reverseLanguages;
    }

    public void setReverseLanguages(boolean reverseLanguages) {
        this.reverseLanguages = reverseLanguages;
    }

    public boolean isCreateRevisionList() {
        return createRevisionList;
    }

    public void setCreateRevisionList(boolean createRevisionList) {
        this.createRevisionList = createRevisionList;
    }

    public boolean isDecisionMode() {
        return decisionMode;
    }

    public void setDecisionMode(boolean decisionMode) {
        this.decisionMode = decisionMode;
    }

    public boolean isWriteMode() {
        return writeMode;
    }

    public void setWriteMode(boolean writeMode) {
        this.writeMode = writeMode;
    }

    public boolean isTtsEnabled() {
        return ttsEnabled;
    }

    public void setTtsEnabled(boolean ttsEnabled) {
        this.ttsEnabled = ttsEnabled;
    }

    public int getTtsSpeed() {
        return ttsSpeed;
    }

    public void setTtsSpeed(int ttsSpeed) {
        this.ttsSpeed = ttsSpeed;
    }

    public boolean isSkipBrackets() {
        return skipBrackets;
    }

    public void setSkipBrackets(boolean skipBrackets) {
        this.skipBrackets = skipBrackets;
    }

    public boolean isAutoReadTrans() {
        return autoReadTrans;
    }

    public void setAutoReadTrans(boolean autoReadTrans) {
        this.autoReadTrans = autoReadTrans;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isAutoReadWords() {
        return autoReadWords;
    }

    public void setAutoReadWords(boolean autoReadWords) {
        this.autoReadWords = autoReadWords;
    }
}
