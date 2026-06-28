package com.veo.hook.bili.speed;

import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class EitaaHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Only run this code if the app being loaded is Eitaa
        // Note: Check the actual package name of Eitaa. It is usually "ir.eitaa.messenger"
        if (!lpparam.packageName.equals("ir.eitaa.messenger")) {
            return; 
        }

        XposedBridge.log("Eitaa Ad Remover: Loaded into " + lpparam.packageName);

        try {
            // Hook the constructor of SimpleAdsList to prevent it from being created
            XposedHelpers.findAndHookConstructor(
                "ir.eitaa.ui.Components.SimpleAdsList", 
                lpparam.classLoader, 
                android.content.Context.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // By setting the result to null, we stop the object from being created
                        param.setResult(null);
                        XposedBridge.log("Eitaa Ad Remover: Blocked SimpleAdsList creation!");
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook SimpleAdsList constructor: " + t.getMessage());
            
            // Fallback: If constructor hook fails, try to hide it via onAttachedToWindow
            try {
                XposedHelpers.findAndHookMethod(
                    "ir.eitaa.ui.Components.SimpleAdsList", 
                    lpparam.classLoader, 
                    "onAttachedToWindow", 
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            View view = (View) param.thisObject;
                            view.setVisibility(View.GONE);
                            view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                            XposedBridge.log("Eitaa Ad Remover: Hidden SimpleAdsList via onAttachedToWindow!");
                        }
                    }
                );
            } catch (Throwable t2) {
                XposedBridge.log("Eitaa Ad Remover: Fallback hook also failed: " + t2.getMessage());
            }
        }
    }
}
