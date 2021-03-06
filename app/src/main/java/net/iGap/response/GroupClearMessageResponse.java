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
import net.iGap.G;
import net.iGap.proto.ProtoGroupClearMessage;
import net.iGap.realm.RealmClientCondition;
import net.iGap.realm.RealmClientConditionFields;
import net.iGap.realm.RealmRoom;
import net.iGap.realm.RealmRoomFields;
import net.iGap.realm.RealmRoomMessage;
import net.iGap.realm.RealmRoomMessageFields;

public class GroupClearMessageResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public GroupClearMessageResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override
    public void handler() {
        super.handler();
        final ProtoGroupClearMessage.GroupClearMessageResponse.Builder builder = (ProtoGroupClearMessage.GroupClearMessageResponse.Builder) message;
        builder.getRoomId();
        builder.getClearId();

        Realm realm = Realm.getDefaultInstance();
        if (builder.getResponse().getId().isEmpty()) { // another account cleared message
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    final RealmClientCondition realmClientCondition = realm.where(RealmClientCondition.class).equalTo(RealmClientConditionFields.ROOM_ID, builder.getRoomId()).findFirst();
                    realmClientCondition.setClearId(builder.getClearId());
                }
            });
            G.clearMessagesUtil.onChatClearMessage(builder.getRoomId(), builder.getClearId(), builder.getResponse());
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmRoom realmRoom = realm.where(RealmRoom.class).equalTo(RealmRoomFields.ID, builder.getRoomId()).findFirst();
                if (realmRoom != null && ((realmRoom.getLastMessage() == null) || (realmRoom.getLastMessage().getMessageId() <= builder.getRoomId()))) {
                    realmRoom.setUnreadCount(0);
                    realmRoom.setLastMessage(null);
                }

                realm.where(RealmRoomMessage.class).equalTo(RealmRoomMessageFields.ROOM_ID, builder.getRoomId()).lessThan(RealmRoomMessageFields.MESSAGE_ID, builder.getClearId()).findAll().deleteAllFromRealm();
            }
        });

        realm.close();
    }

    @Override
    public void timeOut() {
        super.timeOut();
    }

    @Override
    public void error() {
        super.error();
    }
}
