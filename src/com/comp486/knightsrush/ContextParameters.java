/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.comp486.knightsrush;

import android.content.Context;
import android.content.Intent;

/**
 *  Contains global (but typically constant) parameters about the current operating context 
 *  */
public class ContextParameters extends BaseObject {
    public int viewWidth;
    public int viewHeight;
    public Context context;
    public float mDensity;
	public int gameWidth;
	public int gameHeight;
	public float scaleFactorW;
	public float scaleFactorH;
	public int columns;
	public int rows;
	public int difficulty;
	public int area;
	public int act;
	public int quest;
	public Intent intent;
	public int difficultyCompleted;
	
    
    public ContextParameters() {
        super();
    }
   
    @Override
    public void reset() {
        
    }
}
