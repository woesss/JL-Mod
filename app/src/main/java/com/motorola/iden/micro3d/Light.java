/*
 *  Copyright 2022 Yury Kharchenko
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

package com.motorola.iden.micro3d;

public class Light {
	public static final int MAX_AMBIENT_LIGHT_INTENSITY = 4096;
	public static final int MAX_DIRECTIONAL_LIGHT_INTENSITY = 16384;
	public static final int MIN_AMBIENT_LIGHT_INTENSITY = 0;
	public static final int MIN_DIRECTIONAL_LIGHT_INTENSITY = 0;
	int ambientIntensity;
	Vector3D direction;
	int directionalIntensity;


	public Light(int ambientIntensity, Vector3D direction, int directionalIntensity) {
		setDirectionVector(direction);
		setAmbientIntensity(ambientIntensity);
		setDirectionalIntensity(directionalIntensity);
	}

	Light(Light src) {
		ambientIntensity = src.ambientIntensity;
		directionalIntensity = src.directionalIntensity;
		if (src.direction != null) {
			direction = new Vector3D(src.direction);
		}
	}

	public int getAmbientIntensity() {
		return ambientIntensity;
	}

	public int getDirectionalIntensity() {
		return directionalIntensity;
	}

	public Vector3D getDirectionVector() {
		return direction;
	}

	public void setAmbientIntensity(int intensity) {
		ambientIntensity = clamp(intensity, MAX_AMBIENT_LIGHT_INTENSITY);
	}

	public void setDirectionalIntensity(int intensity) {
		directionalIntensity = clamp(intensity, MAX_DIRECTIONAL_LIGHT_INTENSITY);
	}

	public void setDirectionVector(Vector3D direction) {
		if (direction != null && direction.isZero()) {
			throw new IllegalArgumentException();
		}
		this.direction = direction;
	}

	private static int clamp(int intensity, int max) {
		return Math.max(Math.min(intensity, max), 0);
	}
}