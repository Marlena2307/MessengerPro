package tn.amin.mpro2.hook.all;

import android.app.AndroidAppHelper;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import tn.amin.mpro2.hook.BaseHook;
import tn.amin.mpro2.hook.HookId;
import tn.amin.mpro2.hook.listener.HookListenerResult;
import tn.amin.mpro2.orca.OrcaGateway;

public class MediaTranscoderHook extends BaseHook {

    @Override
    public HookId getId() {
        return HookId.MEDIA_TRANSCODER;
    }

    @Override

    protected Set<XC_MethodHook.Unhook> injectInternal(OrcaGateway gateway) {
        final Class<?> DefaultMediaTranscoder = XposedHelpers.findClass("com.facebook.msys.mci.transcoder.DefaultMediaTranscoder", gateway.classLoader);

        Set<XC_MethodHook.Unhook> hooks = new HashSet<>();

        for (Method method : DefaultMediaTranscoder.getDeclaredMethods()) {
            if ("transcodeImage".equals(method.getName())) {
                XC_MethodHook.Unhook hook = XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args = null;
                    }
                });

                hooks.add(hook);
            } else if ("transcodeVideo".equals(method.getName())) {
                XC_MethodHook.Unhook hook = XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MessageReceivedHook.class.getName());
//                        logger.warning(Arrays.toString(param.args));
                        if (param.args[0] != null) {
                            //TODO find a way to bypass size limit for videos
                            File file = new File(new URI((String) param.args[0]).getPath());
                            long length = file.length();
                            if (param.args[1] == null && length < 52428800){
                                param.args = null;
                            }
                        }

                    }
                });

                hooks.add(hook);
            } else if ("transcodeGif".equals(method.getName())) {
                XC_MethodHook.Unhook hook = XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args = null;
                    }
                });

                hooks.add(hook);
                notifyListeners((listener) -> ((MediaTranscodeHookListener) listener).onMediaTranscode());
            }
        }

        return hooks;
    }

    public interface MediaTranscodeHookListener {

        HookListenerResult<Boolean> onMediaTranscode();
    }
}
