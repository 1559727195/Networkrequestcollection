package com.massky.networkrequestcollection.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 * 处理从tcp服务端发送过来的数据
 */
public class MyReceiver extends BroadcastReceiver {

    private Context context;
    private String action = "";
    private Timer timer;
    private TimerTask task;

    @Override
    public void onReceive(Context context, Intent intent) {////tcpSocket收到tcpServer异常时，进入到这里
        this.context = context;
        String tcpreceiver = intent.getStringExtra("tcpreceiver");
//        ToastUtil.showToast(context, "tcpreceiver:" + tcpreceiver);
//        //解析json数据
//        final User user = new GsonBuilder().registerTypeAdapterFactory(
//                new NullStringToEmptyAdapterFactory()).create().fromJson(tcpreceiver, User.class);//json字符串转换为对象
//        if (user == null) return;
//
//        switch (user.command) {
//            case "sraum_beat":
//                break;
//            case "sraum_verifySocket":
//                processCustomMessage(context,MESSAGE_SRAUM_VERIFI_SOCKET,tcpreceiver);
//                break;//认证有效的TCP链接（APP->网关）
//            case "sraum_login":
//                //MESSAGE_SRAUM_LOGIN
//                processCustomMessage(context,MESSAGE_SRAUM_LOGIN,tcpreceiver);
//                break;//登录网关 （APP - 》 网关）
//            case "sraum_getGatewayInfo":
//
//                break;//获取网关基本信息 （APP - 》 网关）
//        }
    }


    //send msg to MyReceiver
    private void processCustomMessage(Context context
            , String action,String strcontent) {

        Intent msgIntent = new Intent(action);
//            msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
                msgIntent.putExtra("strcontent",strcontent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
    }
}

