package com.massky.networkrequestcollection;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.massky.networkrequestcollection.Utils.ApiHelper;
import com.massky.networkrequestcollection.Utils.MyOkHttp;
import com.massky.networkrequestcollection.Utils.Mycallback;
import com.massky.networkrequestcollection.Utils.NullStringToEmptyAdapterFactory;
import com.massky.networkrequestcollection.bean.User;
import com.massky.networkrequestcollection.service.MyService;
import com.massky.networkrequestcollection.util.ICallback;
import com.massky.networkrequestcollection.util.IConnectTcpback;
import com.massky.networkrequestcollection.util.UDPClient;
import java.util.HashMap;
import java.util.Map;
import static com.massky.networkrequestcollection.Utils.AES.Decrypt;
import static com.massky.networkrequestcollection.Utils.AES.Encrypt;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String jsonstr = testHttp();
        test_aes_decryption(jsonstr);
        test_udp_connect();
        test_tcp_connect();


    }

    private void test_tcp_connect() {
        /**
         * 测试TCP
         */
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        init_tcp_connect(32678, "192.168.169.1");
    }

    private void test_udp_connect() {
        /**
         * 测试UDP
         */
        UDPClient.activity_destroy(false);
        udp_searchGateway();
    }

    private void test_aes_decryption(String jsonstr) {
        /**
         * 测试AES 加解密
         */

        //加密
        String DeString = null;
        // 密钥
        String key = "masskysraum-6206";
        try {
            // 解密
            DeString = Encrypt(jsonstr, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //解密
        try {

            DeString = Decrypt(jsonstr, key);
            Log.e("robin debug", "DeString:" + DeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String testHttp() {
        /**
         * 测试HTTP
         */
        String jsonstr = "{\"result\": \"100\",\"count\": \"22\"}";
        MyOkHttp.postMapObject(ApiHelper.testurl, jsonstr,
                new Mycallback(MainActivity.this) {
                    @Override
                    public void onSuccess(User user) {
                        String result = user.result;
                    }
                });
        return jsonstr;
    }

    @Override
    protected void onDestroy() {
        UDPClient.activity_destroy(true);//udp线程被杀死
        super.onDestroy();
    }


    /**
     * command:命令标识 sraum_searchFGateway
     */
    private void udp_searchGateway() {
        Map map = new HashMap();
        map.put("command", "sraum_searchGateway");
//                send_udp(new Gson().toJson(map),"sraum_searchGateway");
        UDPClient.initUdp(new Gson().toJson(map), "sraum_searchGateway", new ICallback() {
            @Override
            public void process(final Object data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        udp_sraum_setGatewayTime();
////                        ToastUtil.showToast(Main_New_Activity.this,"sraum_searchGateway_success!");
//                        Map map = (Map) data;
//                        String command = (String) map.get("command");
//                        String content = (String) map.get("content");
//                        final User user = new GsonBuilder().registerTypeAdapterFactory(
//                                new NullStringToEmptyAdapterFactory()).create().fromJson(content, User.class);//json字符串转换为对象
//                        if (user == null) return;
//                        SharedPreferencesUtil.saveData(Main_New_Activity.this, "tcp_server_ip", user.ip);
//                        init_tcp_connect();
                    }
                });
            }

            @Override
            public void error(String data) {//Socket close
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        ToastUtil.showToast(Main_New_Activity.this, "网关断线或异常");
                    }
                });
            }//
        });
    }


    /**
     * 初始化TCP连接
     */
    private void init_tcp_connect(final int port, final String ip) {
        // final String ip = (String) SharedPreferencesUtil.getData(Main_New_Activity.this, "tcp_server_ip", "");
        new Thread(new Runnable() {
            @Override
            public void run() {

                MyService.getInstance().connectTCP(port, ip, new IConnectTcpback() {//连接tcpServer成功
                    @Override
                    public void process() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //ToastUtil.showToast(Main_New_Activity.this, "连接TCPServer成功");
                            }
                        });
                    }

                    @Override
                    public void error(final int connect_ctp) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (connect_ctp >= 0) {//去主动，网关断线后，每隔10s去连接。
                                    // 收到异常，重连，没收到，心跳之后，第一步，再次发心跳。超时5s，再次收到异常，显示网关断线。去连网关。
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            init_tcp_connect(port, ip);
                                        }
                                    }, 10000);//10s
                                } else {//退出后，界面相应变化，网关异常断线，显示断线，不能直接退出。
                                    //ToastUtil.showToast(Main_New_Activity.this, "显示网关断线。去连网关");
                                }
                            }
                        });
                    }
                }, new ICallback() {

                    @Override
                    public void process(Object data) {

                    }

                    @Override
                    public void error(String data) {
                        //收到tcp服务端断线
                        init_tcp_connect(port,ip); //网关断线后，每隔10s去连接。
                    }
                });
            }
        }).start();
    }

    /**
     * 发送tcp心跳包
     */
    public void send_tcp_heart() {
//        for (int i = 0; i < 2; i++) {
//            try {
//                if (clicksSocket != null)
//                    clicksSocket.sendUrgentData(0xFF);
//            } catch (IOException e) {
//                e.printStackTrace();
//                //tcp服务器端有异常
//            }
//        }

        Map map = new HashMap();
        map.put("command", "sraum_beat");
//                    sraum_send_tcp(map,"sraum_verifySocket");//认证有效的TCP链接
//        TCPClient.sraum_send_tcp(map,"sraum_beat",new ICallback() {
//
//            @Override
//            public void process(Object data) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtil.showToast(Main_New_Activity.this, "发送心跳成功");
//                    }
//                });
//            }
//
//            @Override
//            public void error(String data) {
//
//            }
//        });

        MyService.getInstance().sraum_send_tcp(map, "sraum_beat");
    }
}





