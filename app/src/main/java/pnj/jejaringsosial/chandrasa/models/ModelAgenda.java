package pnj.jejaringsosial.chandrasa.models;

public class ModelAgenda {

    String aId, aParticipants, aTimestamp, createdBy, cImage, cName, cEmail, jUid, jEmail, aTitle, aDesc, aPlace, aDate, aDateMillis, message, sender, mTimestamp, type;

    public ModelAgenda() {
    }

    public ModelAgenda(String aId, String aParticipants, String aTimestamp, String createdBy, String cImage, String cName, String cEmail, String jUid, String jEmail, String aTitle, String aDesc, String aPlace, String aDate, String aDateMillis, String message, String sender, String mTimestamp, String type) {
        this.aId = aId;
        this.aParticipants = aParticipants;
        this.aTimestamp = aTimestamp;
        this.createdBy = createdBy;
        this.cImage = cImage;
        this.cName = cName;
        this.cEmail = cEmail;
        this.jUid = jUid;
        this.jEmail = jEmail;
        this.aTitle = aTitle;
        this.aDesc = aDesc;
        this.aPlace = aPlace;
        this.aDate = aDate;
        this.aDateMillis = aDateMillis;
        this.message = message;
        this.sender = sender;
        this.mTimestamp = mTimestamp;
        this.type = type;
    }

    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public String getaParticipants() {
        return aParticipants;
    }

    public void setaParticipants(String aParticipants) {
        this.aParticipants = aParticipants;
    }

    public String getaTimestamp() {
        return aTimestamp;
    }

    public void setaTimestamp(String aTimestamp) {
        this.aTimestamp = aTimestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getcImage() {
        return cImage;
    }

    public void setcImage(String cImage) {
        this.cImage = cImage;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getcEmail() {
        return cEmail;
    }

    public void setcEmail(String cEmail) {
        this.cEmail = cEmail;
    }

    public String getjUid() {
        return jUid;
    }

    public void setjUid(String jUid) {
        this.jUid = jUid;
    }

    public String getjEmail() {
        return jEmail;
    }

    public void setjEmail(String jEmail) {
        this.jEmail = jEmail;
    }

    public String getaTitle() {
        return aTitle;
    }

    public void setaTitle(String aTitle) {
        this.aTitle = aTitle;
    }

    public String getaDesc() {
        return aDesc;
    }

    public void setaDesc(String aDesc) {
        this.aDesc = aDesc;
    }

    public String getaPlace() {
        return aPlace;
    }

    public void setaPlace(String aPlace) {
        this.aPlace = aPlace;
    }

    public String getaDate() {
        return aDate;
    }

    public void setaDate(String aDate) {
        this.aDate = aDate;
    }

    public String getaDateMillis() {
        return aDateMillis;
    }

    public void setaDateMillis(String aDateMillis) {
        this.aDateMillis = aDateMillis;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getmTimestamp() {
        return mTimestamp;
    }

    public void setmTimestamp(String mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
