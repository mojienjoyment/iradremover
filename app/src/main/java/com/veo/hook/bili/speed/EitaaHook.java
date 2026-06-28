package com.moji.iradremover;

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
        // LOG 1: This will show EVERY app that loads
        XposedBridge.log("=== Eitaa Ad Remover: handleLoadPackage called for: " + lpparam.packageName + " ===");

        // Only run this code if the app being loaded is Eitaa
        if (!lpparam.packageName.equals("ir.eitaa.messenger")) {
            XposedBridge.log("Eitaa Ad Remover: Skipping " + lpparam.packageName + " (not Eitaa)");
            return; 
        }

        XposedBridge.log("Eitaa Ad Remover: SUCCESS - Loaded into Eitaa!");
        XposedBridge.log("Eitaa Ad Remover: ClassLoader: " + lpparam.classLoader);

        try {
            XposedBridge.log("Eitaa Ad Remover: Attempting to hook SimpleAdsList constructor...");
            
            // Hook the constructor of SimpleAdsList
            XposedHelpers.findAndHookConstructor(
                "ir.eitaa.ui.Components.SimpleAdsList", 
                lpparam.classLoader, 
                android.content.Context.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log("Eitaa Ad Remover: BLOCKED SimpleAdsList creation!");
                        param.setResult(null);
                    }
                }
            );
            
            XposedBridge.log("Eitaa Ad Remover: SimpleAdsList constructor hooked successfully!");
            
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook SimpleAdsList constructor!");
            XposedBridge.log("Eitaa Ad Remover: Error: " + t.getMessage());
            t.printStackTrace();
            
            // Fallback
            try {
                XposedBridge.log("Eitaa Ad Remover: Trying fallback onAttachedToWindow hook...");
                XposedHelpers.findAndHookMethod(
                    "ir.eitaa.ui.Components.SimpleAdsList", 
                    lpparam.classLoader, 
                    "onAttachedToWindow", 
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("Eitaa Ad Remover: Hidden SimpleAdsList via onAttachedToWindow!");
                            View view = (View) param.thisObject;
                            view.setVisibility(View.GONE);
                            view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                        }
                    }
                );
            } catch (Throwable t2) {
                XposedBridge.log("Eitaa Ad Remover: Fallback also failed: " + t2.getMessage());
            }
        }
    }
}
