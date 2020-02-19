/*
 * Copyright 2012 Kulikov Dmitriy
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

package javax.microedition.lcdui;

public interface Choice {
	int EXCLUSIVE = 1;
	int MULTIPLE = 2;
	int IMPLICIT = 3;
	int POPUP = 4;

	int TEXT_WRAP_DEFAULT = 0;
	int TEXT_WRAP_ON = 1;
	int TEXT_WRAP_OFF = 2;

	int append(String stringPart, Image imagePart);

	void delete(int elementNum);

	void deleteAll();

	int getFitPolicy();

	Font getFont(int elementNum);

	Image getImage(int elementNum);

	int getSelectedFlags(boolean[] selectedArray_return);

	int getSelectedIndex();

	String getString(int elementNum);

	void insert(int elementNum, String stringPart, Image imagePart);

	boolean isSelected(int elementNum);

	void set(int elementNum, String stringPart, Image imagePart);

	void setFitPolicy(int fitPolicy);

	void setFont(int elementNum, Font font);

	void setSelectedFlags(boolean[] selectedArray);

	void setSelectedIndex(int elementNum, boolean selected);

	int size();
}