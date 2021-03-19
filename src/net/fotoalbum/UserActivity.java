package net.fotoalbum;

import java.sql.Timestamp;

class UserActivity {

    private final String userName;
    private final Timestamp loginTime;
    private final String actionMessage;
    private final Timestamp actionTime;

    UserActivity(final Timestamp loginTime, final String userName, final String actionMessage, final Timestamp actionTime) {
        this.loginTime = loginTime;
        this.userName = userName;
        this.actionMessage = actionMessage;
        this.actionTime = actionTime;
    }

    Timestamp getLoginTime() {
        return loginTime;
    }

    String getUserName() {
        return userName;
    }

    String getActionMessage() {
        return actionMessage;
    }

    Timestamp getActionTime() {
        return actionTime;
    }
}
