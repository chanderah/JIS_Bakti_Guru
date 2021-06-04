package pnj.jejaringsosial.chandrasa.models;

public class ModelVideo {
    String pId, pTitle, pTime, videoUrl, pComments, pLikes, uid, uDp, uName, uEmail;

    public ModelVideo() {
    }

    public ModelVideo(String pId, String pTitle, String pTime, String videoUrl, String pComments, String pLikes, String uid, String uDp, String uName, String uEmail) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pTime = pTime;
        this.videoUrl = videoUrl;
        this.pComments = pComments;
        this.pLikes = pLikes;
        this.uid = uid;
        this.uDp = uDp;
        this.uName = uName;
        this.uEmail = uEmail;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }
}

