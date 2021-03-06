/*
 *  Copyright 2021 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.microedition.pim;

public interface Event extends PIMItem {
	int ALARM = 100;
	int CLASS = 101;
	int END = 102;
	int LOCATION = 103;
	int NOTE = 104;
	int REVISION = 105;
	int START = 106;
	int SUMMARY = 107;
	int UID = 108;
	int CLASS_CONFIDENTIAL = 200;
	int CLASS_PRIVATE = 201;
	int CLASS_PUBLIC = 202;

	RepeatRule getRepeat();

	void setRepeat(RepeatRule value);
}
