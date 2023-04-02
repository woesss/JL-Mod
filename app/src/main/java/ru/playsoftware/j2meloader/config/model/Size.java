/*
 *  Copyright 2023 Yury Kharchenko
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

package ru.playsoftware.j2meloader.config.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Size implements Comparable<Size> {
   public final int width;
   public final int height;
   private final String string;

   public Size(int width, int height) {
      this.width = width;
      this.height = height;
      string = width + " x " + height;
   }

   @Nullable
   public static Size parse(String string) {
      if (string == null) {
         return null;
      }

      int x = string.indexOf(" x ");
      if (x < 0) {
         return null;
      }
      try {
         return new Size(Integer.parseInt(string.substring(0, x)),
                 Integer.parseInt(string.substring(x + 3)));
      } catch (NumberFormatException e) {
         return null;
      }
   }

   @NonNull
   @Override
   public String toString() {
      return string;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof Size)) {
         return false;
      }
      Size that = (Size) obj;
      return that.width == width && that.height == height;
   }

   @Override
   public int compareTo(Size o) {
      int r = Integer.compare(width, o.width);
      return r != 0 ? r : Integer.compare(height, o.height);
   }
}
