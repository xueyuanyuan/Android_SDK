package com.sdk.agent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.game.callback.LoginCallback;
import com.game.callback.LogoutCallback;
import com.game.callback.NetworkSignCallback;
import com.game.callback.NetworkSignProvider;
import com.game.callback.PurchaseCallback;
import com.game.global.Platform;
import com.game.model.PurchaseModel;
import com.game.model.UserEntity;
import com.npc.sdk.view.WebDialog;
import com.sdk.PackInitializeConfig;
import com.sdk.base.StatusCode;
import com.sdk.base.interfaces.NPCExitListener;
import com.sdk.base.interfaces.NPCInitSDKListener;
import com.sdk.base.interfaces.NPCPayResultListener;
import com.sdk.base.interfaces.SdkInterface;
import com.sdk.base.model.NPCPayInfo;
import com.sdk.base.model.NPCRoleInfo;
import com.sdk.base.model.NPCUser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Agent_impl extends SdkInterface {


    public static final String GetUpdateDir = "GetUpdateDir";
    public static final String ShowUrlView = "ShowUrlView";

    public static final String CreateRole = "CreateRole";
    public static final String PaySuccess = "PaySuccess";


    String agent = "";
    String channel = "";

    @Override
    public void doAction(String action, Object[] args) {
    }

    @Override
    public boolean doAction_cover(String action, Object[] args) {

        Log.i(TAG, "agent impl doAction:" + action);

        if (action.equals(GetUpdateDir)) {
            GetUpdateDir(action, args);
            return true;
        } else if (action.equalsIgnoreCase(ShowUrlView)) {
            ShowUrlView(action, args);
            return true;
        } else if (action.equalsIgnoreCase(CreateRole)) {
            CreateRole(action, args);
            return true;
        } else if (action.equalsIgnoreCase(PaySuccess)) {
            PaySuccess(action, args);
            return true;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate init agent_impl");
        Platform.getInstance().initMainActivity(activity);
        Platform.getInstance().setCallbacks(loginCallback, logoutCallback, purchaseCallback, networkSignCallback);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    LoginCallback loginCallback = new LoginCallback() {
        @Override
        public void loginSuccess(UserEntity gameUser) {
            isLogin = false;
            Log.d(TAG, "游戏收到回调 登录成功：" + gameUser.getUid());
            String uid = gameUser.getUid();
            String token = gameUser.getToken();
            userListener.onLoginSuccess(new NPCUser(uid, token), null);
            if (loginHandler != null && loginTimerRunnable != null)
                loginHandler.removeCallbacks(loginTimerRunnable);
        }

        @Override
        public void loginFail(String msg) {
            isLogin = false;
            Log.d(TAG, "游戏收到回调 登录失败：" + msg);
            userListener.onLoginFailed("", null);
            if (loginHandler != null && loginTimerRunnable != null)
                loginHandler.removeCallbacks(loginTimerRunnable);
            toastMsg("登录失败," + msg);
        }
    };
    LogoutCallback logoutCallback = new LogoutCallback() {
        @Override
        public void onLogoutSuccess() {
            Log.d(TAG, "游戏收到回调 注销成功~");

            if (isLogout) {
                isLogout = false;

            }
            userListener.onLogout(null);

        }

        @Override
        public void onLogoutFail() {
            Log.d(TAG, "游戏收到回调 注销失败~");

            userListener.onLogout(null);

        }
    };
    PurchaseCallback purchaseCallback = new PurchaseCallback() {
        @Override
        public void onSuccess(String ordeId, PurchaseModel purchaseModel1) {
            Log.d(TAG, "游戏收到回调 支付成功 ordeId = " + ordeId);
            npcPayResultListener.onPayResult(StatusCode.SUCCESS, null);
            isLogin = false;

        }

        @Override
        public void onFail(String msg) {
            Log.d(TAG, "游戏收到回调 失败支付 : " + msg);
            npcPayResultListener.onPayResult(StatusCode.FAILED, null);
            isLogin = false;
        }
    };
    NetworkSignCallback networkSignCallback = new NetworkSignCallback() {
        @Override
        public void getParams(final String params, final NetworkSignProvider provider) {
            try {
                String sign = RSAUtil.signWithRSA(params);
                provider.setSign(sign);
            } catch (Exception e) {
                e.printStackTrace();
                provider.setSign("");
            }
        }
    };


    public void toastMsg(final String msg) {

        runInThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String[] getGlobalVarStr(PackInitializeConfig config, String agent,
                                    String channel) {
        this.agent = agent;
        this.channel = channel;
        config.setAgent(agent);
        config.setChannel(channel);
        String[] globalVar = platform.getGlobalvar();
        Log.d(TAG, "addGlobalVars:" + Arrays.toString(globalVar));
        config.addArgsFromArray(globalVar);
        return config.toArray();
    }

    @Override
    public void Initialize(boolean isLandscape, boolean isTest,
                           final NPCInitSDKListener initSDKListener) {

        // 注册注销账号时的回调,在用户注销账号前调用都可以
        initSDKListener.onInitFinish(StatusCode.SUCCESS, null);

    }

    boolean isLogin = false;
    Handler loginHandler;
    LoginTimerRunnable loginTimerRunnable;

    @Override
    public void login() {
        isLogin = true;
        //开始倒计时
        if (loginHandler == null) {
            loginHandler = new Handler();
        }
        if (loginTimerRunnable == null) {
            loginTimerRunnable = new LoginTimerRunnable();
        }
        loginHandler.removeCallbacks(loginTimerRunnable);

        Platform.getInstance().login();
        loginHandler.postDelayed(loginTimerRunnable, 2000);
    }

    private class LoginTimerRunnable implements Runnable {

        @Override
        public void run() {
            try {
                com.game.b.b dialogContext = com.game.b.b.a();
                if (dialogContext == null) {
                    return;
                }
                Field field = com.game.b.b.class.getDeclaredField("e");
                if (field == null) return;
                field.setAccessible(true);
                Dialog e = (Dialog) field.get(dialogContext);

                if (e == null &&  isLogin) {
                    Log.i(TAG, "login  e == null");
                    loginHandler.postDelayed(this, 300);
                    return;
                }
//                e.setCancelable(false);
                if (e.isShowing()) {

                    Log.i(TAG, "login is showing");
                } else {
                    Log.i(TAG, "login is not showing");
                    userListener.onLoginFailed("", null);
                    return;
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            loginHandler.postDelayed(this, 300);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void exit(final NPCExitListener exitListener) {
        if (isLogin) {
            isLogin = false;
            userListener.onLoginFailed("", null);
            return;
        }
        exitListener.onNo3rdExiterProvide();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Platform.getInstance().exit();
    }

    NPCPayResultListener npcPayResultListener;

    @Override
    public void pay(NPCPayInfo payInfo, final NPCPayResultListener resultListener) {

        npcPayResultListener = resultListener;
        PurchaseModel purchaseModel = new PurchaseModel();
        purchaseModel.setCoins(Double.parseDouble(payInfo.getMoney()));
        purchaseModel.setRoleid(payInfo.getJiaosePK());
        purchaseModel.setServerid(payInfo.getServerid());
        //请保持唯一，建议传入订单流水号，供游戏接入商查询订单
//		purchaseModel.setDeveloperInfo(payInfo.getPreOrder());
        purchaseModel.setProductName("游戏道具");
        String ext = String.format("%s_%s_%s_%s", payInfo.getPreOrder(), payInfo.getModel(), agent, channel);
        purchaseModel.setDeveloperInfo(ext);

        Platform.getInstance().purchase(purchaseModel);
        Log.i(TAG, "start pay:" + ext);
    }

    /**
     * 弹出内部网页框
     *
     * @param action
     * @param args
     */
    private void ShowUrlView(String action, Object[] args) {

        // TODO Auto-generated method stub
        final String url_ = (String) args[0];
        WebDialog.setDimensions0(args);
        new Handler(activity.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.i(TAG, url_);
                new WebDialog(activity, url_).show();
            }

        });
    }

    public void GetUpdateDir(String action, Object[] args) {

        platform.setStateDoing();
        String name = activity.getString(activity.getResources().getIdentifier(
                "app_name_en", "string", activity.getPackageName()));
        System.err.println("name : " + name);
        // String name = "S80OL4";
        String directory = android.os.Environment.getExternalStorageDirectory()
                + "/" + name + "/update";
        platform.setStateResult(new String[]{directory});
        platform.setStateSuccess();
    }


    public Object[] createRoleArgs = null;

    public void CreateRole(String action, final Object[] args) {
//		doPlatformAction("CreateRole", [mingchen, pk, serverid, serverName]);
        createRoleArgs = args;

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume game");
        Platform.getInstance().floatResume(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause pause");
        Platform.getInstance().hideFloatView();
    }

    @Override
    public void submitGameInfo(int state, NPCRoleInfo roleInfo) {
        isLogin = false;
    }

    public void PaySuccess(String action, final Object[] args) {


    }

    boolean isLogout = false;

    @Override
    public void logout() {
        isLogout = true;
        Platform.getInstance().logout();
    }
}
