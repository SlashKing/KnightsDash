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


/**
 * GameObject defines any object that resides in the game world (character, background, special
 * effect, enemy, etc).  It is a collection of GameComponents which implement its behavior;
 * GameObjects themselves have no intrinsic behavior.  GameObjects are also "bags of data" that
 * components can use to share state (direct component-to-component communication is discouraged).
 */
public class GameObject extends BaseObject {
    // These fields are managed by components.
    protected Vector2 mPosition;
    public float activationRadius;
    public boolean destroyOnDeactivation;
    
    public int life;
    public int mana;
    
    public Vector2 facingDirection;
    public float width;
    public float height;
    
    private static final int DEFAULT_LIFE = 1;
    
    public enum ActionType {
        INVALID,
        IDLE,
        MOVE,
        RUN,
        JUMP,
        ATTACK,
        JATTACK,
        SLIDE,
        SHOOT,
        HIT_REACT,
        DEATH,
        HIDE,
        FROZEN
    }
    
    protected ActionType mCurrentAction;
    
    public enum Team {
        NONE,
        PLAYER,
        ENEMY
    }
    
    public Team team;
    
    public GameObject() {
        super();

        mPosition = new Vector2();
        
        facingDirection = new Vector2(1, 0);
        
        reset();
    }
    
    @Override
    public void reset() {
        
        mPosition.zero();
        facingDirection.set(1.0f, 1.0f);
        
        mCurrentAction = ActionType.INVALID;
        activationRadius = 0;
        destroyOnDeactivation = false;
        life = DEFAULT_LIFE;
        team = Team.NONE;
        width = 0.0f;
        height = 0.0f;
        
    }
    public final Vector2 getPosition() {
        return mPosition;
    }

    public final void setPosition(Vector2 position) {
        mPosition.set(position);
    }

    
    public final ActionType getCurrentAction() {
        return mCurrentAction;
    }
    
    public final void setCurrentAction(ActionType type) {
        mCurrentAction = type;
    }
}
