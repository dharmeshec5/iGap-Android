/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.response;

import io.realm.Realm;
import net.iGap.proto.ProtoError;
import net.iGap.proto.ProtoUserProfileSetSelfRemove;
import net.iGap.realm.RealmUserInfo;

public class UserProfileSetSelfRemoveResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public UserProfileSetSelfRemoveResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override public void handler() {
        super.handler();
        final ProtoUserProfileSetSelfRemove.UserProfileSetSelfRemoveResponse.Builder builder = (ProtoUserProfileSetSelfRemove.UserProfileSetSelfRemoveResponse.Builder) message;

        builder.getSelfRemove();

        Realm realm1 = Realm.getDefaultInstance();
        realm1.executeTransaction(new Realm.Transaction() {
            @Override public void execute(Realm realm) {
                realm.where(RealmUserInfo.class).findFirst().setSelfRemove(builder.getSelfRemove());
            }
        });

        realm1.close();
    }

    @Override public void timeOut() {
        super.timeOut();
    }

    @Override public void error() {
        super.error();

        ProtoError.ErrorResponse.Builder errorResponse = (ProtoError.ErrorResponse.Builder) message;
        int majorCode = errorResponse.getMajorCode();
        int minorCode = errorResponse.getMinorCode();
    }
}


