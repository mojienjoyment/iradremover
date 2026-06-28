package com.moji.iradremover; // <--- MAKE SURE THIS MATCHES YOUR PACKAGE NAME!

import android.view.View;
import android.view.ViewGroup;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class EitaaHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Catch-all log to verify the module is loading
        XposedBridge.log("Eitaa Ad Remover: Module loaded into package: " + lpparam.packageName);

        // Only run the rest of the code if the app is Eitaa
        if (!lpparam.packageName.equals("ir.eitaa.messenger")) {
            return; 
        }

        XposedBridge.log("Eitaa Ad Remover: Successfully targeted Eitaa!");

        // ==========================================
        // HOOK 1: Block the showAds method
        // ==========================================
        try {
            XposedHelpers.findAndHookMethod(
                "ir.eitaa.ui.Components.SimpleAdsList", 
                lpparam.classLoader, 
                "showAds", 
                java.util.List.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // Abort the method execution. The ad list will remain empty, 
                        // and the "تبلیغات" title will stay hidden.
                        param.setResult(null); 
                        XposedBridge.log("Eitaa Ad Remover: Blocked showAds method!");
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook showAds: " + t.getMessage());
        }

        // ==========================================
        // HOOK 2: Hide the view completely on creation
        // ==========================================
        try {
            XposedHelpers.findAndHookConstructor(
                "ir.eitaa.ui.Components.SimpleAdsList", 
                lpparam.classLoader, 
                android.content.Context.class, 
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        View view = (View) param.thisObject;
                        // Force the view to be invisible and take up 0 space
                        view.setVisibility(View.GONE);
                        view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                        XposedBridge.log("Eitaa Ad Remover: Hidden SimpleAdsList view!");
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook constructor: " + t.getMessage());
        }
    }
}
