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

import android.os.Handler;
import android.os.Looper;
import io.realm.Realm;
import net.iGap.G;
import net.iGap.activities.ActivityChat;
import net.iGap.adapter.items.chat.AbstractMessage;
import net.iGap.helper.HelperGetUserInfo;
import net.iGap.helper.HelperLogMessage;
import net.iGap.interfaces.OnGetUserInfo;
import net.iGap.proto.ProtoClientGetRoom;
import net.iGap.proto.ProtoError;
import net.iGap.proto.ProtoGlobal;
import net.iGap.realm.RealmRoom;
import net.iGap.request.RequestClientGetRoom;

import static net.iGap.realm.RealmRoom.putOrUpdate;

public class ClientGetRoomResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public ClientGetRoomResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override public void handler() {
        super.handler();

        final ProtoClientGetRoom.ClientGetRoomResponse.Builder clientGetRoom = (ProtoClientGetRoom.ClientGetRoomResponse.Builder) message;

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override public void execute(final Realm realm) {

                String[] identityParams = identity.split("\\*");
                final String identity = identityParams[0];

                if (identity.equals(RequestClientGetRoom.CreateRoomMode.justInfo.toString())) {
                    RealmRoom realmRoom = RealmRoom.putOrUpdate(clientGetRoom.getRoom());
                    realmRoom.setDeleted(true);
                    realmRoom.setKeepRoom(true);

                    // update log message in realm room message after get room info
                    if (G.logMessageUpdatList.containsKey(clientGetRoom.getRoom().getId())) {

                        G.handler.postDelayed(new Runnable() {
                            @Override public void run() {
                                HelperLogMessage.updateLogMessageAfterGetUserInfo(G.logMessageUpdatList.get(clientGetRoom.getRoom().getId()));
                            }
                        }, 500);
                    }

                    return;
                }

                if (clientGetRoom.getRoom().getType() == ProtoGlobal.Room.Type.CHAT) {

                    new HelperGetUserInfo(new OnGetUserInfo() {
                        @Override public void onGetUserInfo(ProtoGlobal.RegisteredUser registeredUser) {

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override public void run() {
                                    final Realm realm = Realm.getDefaultInstance();

                                    realm.executeTransactionAsync(new Realm.Transaction() {
                                        @Override public void execute(Realm realm) {
                                            putOrUpdate(clientGetRoom.getRoom());
                                        }
                                    }, new Realm.Transaction.OnSuccess() {
                                        @Override public void onSuccess() {

                                            if (G.onClientGetRoomResponse != null) {

                                                G.handler.post(new Runnable() {
                                                    @Override public void run() {
                                                        G.onClientGetRoomResponse.onClientGetRoomResponse(clientGetRoom.getRoom(), clientGetRoom, identity);
                                                    }
                                                });
                                            }

                                            realm.close();
                                        }
                                    }, new Realm.Transaction.OnError() {
                                        @Override public void onError(Throwable error) {
                                            realm.close();
                                        }
                                    });
                                }
                            });
                        }
                    }).getUserInfo(clientGetRoom.getRoom().getChatRoomExtra().getPeer().getId());
                } else {
                    putOrUpdate(clientGetRoom.getRoom());

                    G.handler.postDelayed(new Runnable() {
                        @Override public void run() {
                            if (G.onClientGetRoomResponse != null) {
                                G.onClientGetRoomResponse.onClientGetRoomResponse(clientGetRoom.getRoom(), clientGetRoom, identity);
                            }
                        }
                    }, 500);
                }
            }
        });
        realm.close();

        // update chat message header forward after get user or room info
        if (AbstractMessage.updateForwardInfo != null) {
            if (AbstractMessage.updateForwardInfo.containsKey(clientGetRoom.getRoom().getId())) {
                String messageId = AbstractMessage.updateForwardInfo.get(clientGetRoom.getRoom().getId());
                AbstractMessage.updateForwardInfo.remove(clientGetRoom.getRoom().getId());
                if (ActivityChat.onUpdateUserOrRoomInfo != null) {
                    ActivityChat.onUpdateUserOrRoomInfo.onUpdateUserOrRoomInfo(messageId);
                }
            }
        }
    }

    @Override public void timeOut() {
        super.timeOut();
        if (G.onClientGetRoomResponse != null) {
            G.onClientGetRoomResponse.onTimeOut();
        }
    }

    @Override public void error() {
        super.error();
        ProtoError.ErrorResponse.Builder errorResponse = (ProtoError.ErrorResponse.Builder) message;
        int majorCode = errorResponse.getMajorCode();
        int minorCode = errorResponse.getMinorCode();
        if (majorCode == 614 && minorCode == 1) {
            String[] identityParams = identity.split("\\*");
            final long roomId = Long.parseLong(identityParams[1]);
            RealmRoom.createEmptyRoom(roomId);
        }
        if (G.onClientGetRoomResponse != null) {
            G.onClientGetRoomResponse.onError(majorCode, minorCode);
        }
    }
}


