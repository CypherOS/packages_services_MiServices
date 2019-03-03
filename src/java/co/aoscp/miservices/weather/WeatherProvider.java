/*
 * Copyright (C) 2018 Pixel Experience
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
package co.aoscp.miservices.weather;

public class WeatherProvider {

    public static final int WEATHER_UPDATE_SUCCESS = 0; // Success
    public static final int WEATHER_UPDATE_RUNNING = 1; // Update running
    public static final int WEATHER_UPDATE_ERROR = 2; // Error

    private int status;
    private String conditions;
    private int temperatureMetric;
    private int temperatureImperial;

    WeatherProvider(int status, String conditions, int temperatureMetric, int temperatureImperial) {
        this.status = status;
        this.conditions = conditions;
        this.temperatureMetric = temperatureMetric;
        this.temperatureImperial = temperatureImperial;
    }

    int getTemperature(boolean metric) {
        return metric ? this.temperatureMetric : this.temperatureImperial;
    }

    int getStatus() {
        return this.status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    String getConditions() {
        return this.conditions;
    }

    @Override
    public String toString() {
        return "WeatherProvider: " +
                "status=" + getStatus() + "," +
                "conditions=" + getConditions() + "," +
                "temperatureMetric=" + getTemperature(true) + "," +
                "temperatureImperial=" + getTemperature(false);
    }
}