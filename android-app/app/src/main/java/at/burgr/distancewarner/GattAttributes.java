/*
 * Copyright (C) 2013 The Android Open Source Project
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

package at.burgr.distancewarner;

import java.util.HashMap;

/**
 * This class includes a subset of standard GATT attributes
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String DISTANCE_SENSOR = "6b6ea109-9067-4a59-8baf-26f37740dc7d";
    public static String DISTANCE_CHARACTERISTIC = "d18b1ec7-022d-423d-8bc6-8c19a57d7242";

    static {
        attributes.put(DISTANCE_SENSOR, "Heart Rate Measurement");
        attributes.put(DISTANCE_CHARACTERISTIC, "Distance Measurement");
    }

    public static String lookup(String uuid) {
        return attributes.get(uuid);
    }
}
