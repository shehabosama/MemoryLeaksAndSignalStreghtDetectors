package com.base.memoryleaksdetectors;

import android.content.Context;

public class SingletonClass {
    private static SingletonClass singletonClassInstance;

    private Context context;

    private SingletonClass(Context context){
        this.context = context;
    }

    public static void singletonClassInstance(Context context){

        if (singletonClassInstance == null){
            singletonClassInstance = new SingletonClass(context);

        }
    }
}