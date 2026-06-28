package com.moji.iradremover; // <--- MAKE SURE THIS MATCHES YOUR PACKAGE NAME!

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
        XposedBridge.log("Eitaa Ad Remover: Module loaded into package: " + lpparam.packageName);

        if (!lpparam.packageName.equals("ir.eitaa.messenger")) {
            return;
        }

        XposedBridge.log("Eitaa Ad Remover: Successfully targeted Eitaa!");

        // ==========================================
        // 1. MAIN CHAT LIST (DialogsAdapter)
        // ==========================================
        try {
            XposedHelpers.findAndHookMethod("ir.eitaa.ui.Adapters.DialogsAdapter", lpparam.classLoader, "notifyDataSetChanged", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // Force the adapter to believe there are no ads
                    XposedHelpers.setBooleanField(param.thisObject, "hasDialogAd", false);
                    XposedHelpers.setBooleanField(param.thisObject, "hasMiniAppAd", false);
                    XposedHelpers.setIntField(param.thisObject, "adsCount", 0);
                }
            });
            XposedBridge.log("Eitaa Ad Remover: Hooked DialogsAdapter to remove main chat list ads.");
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook DialogsAdapter: " + t.getMessage());
        }

        // ==========================================
        // 2. SEARCH ADS (MessagesController.searchAd)
        // ==========================================
        try {
            // Hook searchAd to prevent it from fetching ads for DialogsSearchAdapter and FilteredSearchView
            // Note: Eitaa uses desugared Java 8 libraries, so the Consumer class is j$.util.function.Consumer
            XposedHelpers.findAndHookMethod("ir.eitaa.messenger.MessagesController", lpparam.classLoader, "searchAd", 
                String.class, String.class, "j$.util.function.Consumer", "j$.util.function.Consumer", 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // Call the error consumer (param.args[3]) with false to simulate a network failure
                        Object errorConsumer = param.args[3];
                        if (errorConsumer != null) {
                            try {
                                XposedHelpers.callMethod(errorConsumer, "accept", Boolean.FALSE);
                            } catch (Throwable ignore) {}
                        }
                        // Abort the original method so no ads are fetched
                        param.setResult(false);
                        XposedBridge.log("Eitaa Ad Remover: Blocked searchAd fetch!");
                    }
                }
            );
            XposedBridge.log("Eitaa Ad Remover: Hooked MessagesController.searchAd to block search ads.");
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook searchAd: " + t.getMessage());
        }

        // ==========================================
        // 3. SIMPLE ADS LIST (SimpleAdsList)
        // ==========================================
        try {
            XposedHelpers.findAndHookMethod("ir.eitaa.ui.Components.SimpleAdsList", lpparam.classLoader, "showAds", java.util.List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // Abort the method so the ad list remains empty and the "تبلیغات" header stays hidden
                    param.setResult(null);
                    XposedBridge.log("Eitaa Ad Remover: Blocked SimpleAdsList.showAds!");
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook SimpleAdsList.showAds: " + t.getMessage());
        }

        try {
            XposedHelpers.findAndHookConstructor("ir.eitaa.ui.Components.SimpleAdsList", lpparam.classLoader, android.content.Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // Force the view to be invisible and take up 0 space
                    View view = (View) param.thisObject;
                    view.setVisibility(View.GONE);
                    view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                }
            });
            XposedBridge.log("Eitaa Ad Remover: Hidden SimpleAdsList view completely.");
        } catch (Throwable t) {
            XposedBridge.log("Eitaa Ad Remover: Failed to hook SimpleAdsList constructor: " + t.getMessage());
        }
    }
}
