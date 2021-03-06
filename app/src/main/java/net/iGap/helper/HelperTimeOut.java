/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.helper;

import net.iGap.Config;
import net.iGap.G;

public class HelperTimeOut {

    /**
     * @param firstTime if don't fill first time automatically use from currentTimeMillis
     * @param secondTime latest time
     * @param timeout if don't fill time use from Config.DEFAULT_TIME_OUT(10 second)
     */

    public static boolean timeoutChecking(long firstTime, long secondTime, long timeout) {
        long difference;
        if (firstTime == 0) {
            firstTime = System.currentTimeMillis();
        }
        difference = (firstTime - secondTime);

        if (timeout == 0) {
            timeout = Config.DEFAULT_TIME_OUT;
        }

        if (difference >= timeout) {
            return true;
        }

        return false;
    }

    public static boolean heartBeatTimeOut() {

        long difference;

        long currentTime = System.currentTimeMillis();
        difference = (currentTime - G.latestHearBeatTime);

        if (difference >= (G.serverHeartBeatTiming + Config.HEART_BEAT_CHECKING_TIME_OUT)) { // server hearBeat timing and plus with Config.HEART_BEAT_CHECKING_TIME_OUT
            return true;
        }

        return false;
    }
}