/*
 * Copyright (C) 2018 Pixel Experience
 * Copyright (C) 2019 CypherOS
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
package co.aoscp.miservices.quickspace;

public class QuickspaceCard {

    public static final int WEATHER_UPDATE_SUCCESS = 0; // Success
    public static final int WEATHER_UPDATE_RUNNING = 1; // Update running
    public static final int WEATHER_UPDATE_ERROR = 2; // Error

    public static final int EVENT_NONE = 0; // None
    public static final int EVENT_FIRST_TIME = 1; // FirstTime

    private int status;
    private String conditions;
    private int temperatureMetric;
    private int temperatureImperial;

    private int eventType;
    private String eventTitle;
    private String eventAction;

    public QuickspaceCard(int status, String conditions, int temperatureMetric, int temperatureImperial,
                int eventType, String eventTitle, String eventAction) {
        this.status = status;
        this.conditions = conditions;
        this.temperatureMetric = temperatureMetric;
        this.temperatureImperial = temperatureImperial;
        this.eventType = eventType;
        this.eventTitle = eventTitle;
        this.eventAction = eventAction;
    }

    public int getTemperature(boolean metric) {
        return metric ? this.temperatureMetric : this.temperatureImperial;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getConditions() {
        return this.conditions;
    }

    public int getEventType() {
        return this.eventType;
    }

    public String getEventTitle() {
        return this.eventTitle;
    }

    public String getEventAction() {
        return this.eventAction;
    }

    @Override
    public String toString() {
        return "QuickspaceCard: " +
                "status=" + getStatus() + "," +
                "conditions=" + getConditions() + "," +
                "temperatureMetric=" + getTemperature(true) + "," +
                "temperatureImperial=" + getTemperature(false) + "," +
                "eventType=" + getEventType() + "," +
                "eventTitle=" + getEventTitle() + "," +
                "eventAction=" + getEventAction();
    }
}