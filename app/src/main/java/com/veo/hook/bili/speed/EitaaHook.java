package com.moji.iradremover; // <--- MAKE SURE THIS MATCHES YOUR PACKAGE NAME!

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class EitaaHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("Eitaa Ad Remover: Module loaded into package: " + lpparam.packageName);

        if (!lpparam.packageName.equals("ir.eitaa.messenger")) {
            return; 
        }

        XposedBridge.log("Eitaa Ad Remover: Successfully targeted Eitaa!");

        // ==========================================
        // DIAGNOSTIC HOOK: Catch ANY TextView being set to "تبلیغات"
        // ==========================================
        try {
            XposedHelpers.findAndHookMethod(
                "android.widget.TextView", 
                lpparam.classLoader, 
                "setText", 
                CharSequence.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        CharSequence text = (CharSequence) param.args[0];
                        // Check if the text being set is the ad banner text
                        if (text != null && text.toString().contains("تبلیغات")) {
                            XposedBridge.log("==================================================");
                            XposedBridge.log("Eitaa Ad Remover: CAUGHT 'تبلیغات' in TextView: " + param.thisObject.getClass().getName());
                            // Print the stack trace to see exactly which class is creating this ad!
                            XposedBridge.log("Eitaa Ad Remover: Stack trace:\n" + android.util.Log.getStackTraceString(new Throwable()));
                            XposedBridge.log("==================================================");
                        }
                    }
                }
            );
            XposedBridge.log("Eitaa Ad Remover: Diagnostic TextView hook installed!");
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook TextView: " + t.getMessage());
        }
    }
}
