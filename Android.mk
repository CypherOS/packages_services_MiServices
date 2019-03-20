#
# Copyright (C) 2019 CypherOS
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

include $(LOCAL_PATH)/includes_weather.mk

include $(CLEAR_VARS)
LOCAL_JAVA_LIBRARIES := \
    android-support-annotations

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4

LOCAL_STATIC_JAVA_LIBRARIES := \
    prebuilt-gson \
    prebuilt-jsoup \
    prebuilt-okhttp \
    prebuilt-okio \
    prebuilt-sunrisesunset \
	prebuilt-acrCloud \
	prebuilt-miServices

LOCAL_STATIC_JAVA_AAR_LIBRARIES := \
    prebuilt-play-services-basement \
    prebuilt-play-services-base \
    prebuilt-play-services-location \
    prebuilt-play-services-places-placereport \
    prebuilt-play-services-task

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += $(foreach lib, $(LOCAL_STATIC_JAVA_AAR_LIBRARIES),\
  $(call intermediates-dir-for,JAVA_LIBRARIES,$(lib),,COMMON)/aar/res)

LOCAL_USE_AAPT2 := true
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_JAR_EXCLUDE_FILES := none
LOCAL_PACKAGE_NAME := MiServices
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS := optional
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE := prebuilt-sunrisesunset
LOCAL_SRC_FILES := libs/SunriseSunsetCalculator-1.2.jar
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_SDK_VERSION := current
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuilt-acrCloud
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_SRC_FILES := libs/libAcrCloud.jar
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_SDK_VERSION := 27
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuilt-miServices
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_SRC_FILES := libs/libMiShared.jar
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_SDK_VERSION := 27
include $(BUILD_PREBUILT)