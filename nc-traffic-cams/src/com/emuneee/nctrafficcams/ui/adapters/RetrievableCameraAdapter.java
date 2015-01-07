/*
 * Copyright (C) 2012 http://emuneee.com/blog/apps/
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
package com.emuneee.nctrafficcams.ui.adapters;

import java.util.ArrayList;

import com.emuneee.nctrafficcams.api.Camera;

/**
 * Interface for camera adapters that you can retreive cameras from
 * @author evan
 *
 */
public interface RetrievableCameraAdapter {
	public abstract Camera getCamera(int i);

	public abstract ArrayList<Camera> getCameras();
}
