package com.test;

import android.app.Activity;
import android.content.Context;

import com.test.vo.AuthenticateVO;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;


public class EUExFingerPrint extends EUExBase {
    private static final int ERROR_UNREGISTERED = 2;
    private static final int ERROR_DISABLE_HARDWARE = 1;
    private static final int ERROR_DISABLE_FINGERPRINT = 3;
    private static final int ERROR_NO_INIT = 4;
    private static final int ERROR_NOT_MATCH = 5;

    public EUExFingerPrint(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    private String cbAuthenticateFunId;
    private String cbInitFunId;
    private static FingerprintIdentify mFingerprintIdentify;
    private static boolean mIsCalledStartIdentify = false;

    private boolean isInit = false;

    @Override
    protected boolean clean() {
        if(mFingerprintIdentify != null){
            mFingerprintIdentify.cancelIdentify();
        }
        return false;
    }

    public static void onActivityResume(Context context) {
        if(mIsCalledStartIdentify){
            mFingerprintIdentify.resumeIdentify();
        }
    }

    public static void onActivityPause(Context context) {
        mFingerprintIdentify.cancelIdentify();
    }

    public void init(String[] params){
        if(params.length < 1){
            errorCallback(0,0,"error params!");
            return;
        }
        cbInitFunId = params[params.length -1];
        mFingerprintIdentify = new FingerprintIdentify((Activity) mContext, new BaseFingerprint.FingerprintIdentifyExceptionListener() {
            @Override
            public void onCatchException(Throwable exception) {
                callbackInit(EUExCallback.F_C_FAILED, exception.getLocalizedMessage());
            }
        });

        if(!mFingerprintIdentify.isHardwareEnable()){
            callbackInit(ERROR_DISABLE_HARDWARE, "设备硬件不支持指纹识别");
            return;
        }

        if(!mFingerprintIdentify.isRegisteredFingerprint()){
            callbackInit(ERROR_UNREGISTERED, "未注册指纹");
            return;
        }

        if(!mFingerprintIdentify.isFingerprintEnable()){
            callbackInit(ERROR_DISABLE_FINGERPRINT, "指纹识别不可用");
            return;
        }
        mIsCalledStartIdentify = false;
        isInit = true;
        callbackInit(EUExCallback.F_C_SUCCESS);
    }



    public void authenticate(String[] params){
        if(params.length < 1){
            errorCallback(0,0,"error params!");
            return;
        }
        cbAuthenticateFunId = params[params.length -1];
        if(!isInit){
            callbackAuthenticate(ERROR_NO_INIT, "未初始化");
            return;
        }
        AuthenticateVO data = new AuthenticateVO();
        if(params.length > 1){
            data = DataHelper.gson.fromJson(params[0], AuthenticateVO.class);
        }
        mIsCalledStartIdentify = true;
        mFingerprintIdentify.resumeIdentify();
        mFingerprintIdentify.startIdentify(data.getMaxTries(), new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                callbackAuthenticate(EUExCallback.F_C_SUCCESS);
            }

            @Override
            public void onNotMatch(int availableTimes) {
                callbackAuthenticate(ERROR_NOT_MATCH, "指纹不匹配，还剩" + availableTimes + "次尝试机会");
            }

            @Override
            public void onFailed() {
                callbackAuthenticate(EUExCallback.F_C_FAILED, "识别失败");
            }
        });
    }

    private void callbackInit(int error){
        callbackToJs(Integer.parseInt(cbInitFunId), false, error);
    }

    private void callbackInit(int error, String data){
        callbackToJs(Integer.parseInt(cbInitFunId), false, error, data);
    }

    private void callbackAuthenticate(int error){
        callbackToJs(Integer.parseInt(cbAuthenticateFunId), false, error);
    }

    private void callbackAuthenticate(int error, String data){
        callbackToJs(Integer.parseInt(cbAuthenticateFunId), true, error, data);
    }
}
