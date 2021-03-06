/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.notification.testutils;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.content.Context;
import android.content.ServiceConnection;

import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow class for {@link Car}. Components in car support library expects
 * this class to be available at run time.
 */
@Implements(Car.class)
public class ShadowCar {

    private static Car sMockCar = mock(Car.class);
    private static boolean sIsConnected;
    private static String sServiceName;
    private static Object sCarManager;

    /**
     * Returns a mocked version of a {@link Car} object.
     */
    @Implementation
    protected static Car createCar(Context context, ServiceConnection serviceConnection) {

        if (serviceConnection != null) {
            doAnswer((Answer<Void>) invocation -> {
                serviceConnection.onServiceConnected(null, null);
                return null;
            }).when(sMockCar).connect();
            doAnswer((Answer<Void>) invocation -> {
                serviceConnection.onServiceDisconnected(null);
                return null;
            }).when(sMockCar).disconnect();
        }
        doReturn(sIsConnected).when(sMockCar).isConnected();
        if (sServiceName != null) {
            try {
                doReturn(sCarManager).when(sMockCar).getCarManager(sServiceName);
            } catch (CarNotConnectedException e) {
                // do nothing, have to do this because compiler doesn't understand mock can't throw
                // exception.
            }
        }
        return sMockCar;
    }

    /**
     * Sets the manager returned by {@link Car#getCarManager(String)}.
     *
     * @param serviceName the name for the service request that should return this car manager.
     * @param carManager  the object returned by a call with this service.
     */
    public static void setCarManager(String serviceName, Object carManager) {
        sServiceName = serviceName;
        sCarManager = carManager;
        try {
            doReturn(carManager).when(sMockCar).getCarManager(serviceName);
        } catch (CarNotConnectedException e) {
            // do nothing, have to do this because compiler doesn't understand mock can't throw e.
        }
    }

    /**
     * Resets the shadow state, note this will not remove stubbed behavior on references to older
     * calls to {@link #createCar(Context, ServiceConnection)}.
     */
    @Resetter
    public static void reset() {
        sMockCar = mock(Car.class);
        sServiceName = null;
        sCarManager = null;
        sIsConnected = false;
    }
}

