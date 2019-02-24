package pt.IPG.messenger;

import java.util.Date;

public class ContactModel {
   /* "_id": "5c6a9a619c005b002b5fecc7",
            "updatedAt": "2019-02-18T11:43:29.502Z",
            "createdAt": "2019-02-18T11:43:29.502Z",
            "conversationId": "5c6a9a619c005b002b5fecc6",
            "body": "hello again and againbmnbmn",
            "author"
            */

   private String _id;
   private Date updatedAt;
   private Date createdAt;
   private String  conversationId;


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
