/*
 * Copyright 2017 Nikita Shakarun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.sensor;

public interface SensorInfo {
	int CONN_EMBEDDED = 1;
	int CONN_REMOTE = 2;
	int CONN_SHORT_RANGE_WIRELESS = 4;
	int CONN_WIRED = 8;
	String CONTEXT_TYPE_AMBIENT = "ambient";
	String CONTEXT_TYPE_DEVICE = "device";
	String CONTEXT_TYPE_USER = "user";
	String CONTEXT_TYPE_VEHICLE = "vehicle";
	String PROP_IS_CONTROLLABLE = "controllable";
	String PROP_IS_REPORTING_ERRORS = "errorsReported";
	String PROP_LATITUDE = "latitude";
	String PROP_LOCATION = "location";
	String PROP_LONGITUDE = "longitude";
	String PROP_MAX_RATE = "maxSamplingRate";
	String PROP_VENDOR = "vendor";
	String PROP_VERSION = "version";

	ChannelInfo[] getChannelInfos();

	int getConnectionType();

	String getContextType();

	String getDescription();

	int getMaxBufferSize();

	String getModel();

	Object getProperty(String name);

	String[] getPropertyNames();

	String getQuantity();

	String getUrl();

	boolean isAvailabilityPushSupported();

	boolean isAvailable();

	boolean isConditionPushSupported();
}
