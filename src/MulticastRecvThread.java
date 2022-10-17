/*
计算机网络程序设计
局域网UDP文件分发设计
2019302110088-杜煜-19广电工3班
 */

import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class MulticastRecvThread extends Thread{
    public AdminGUI window;

    public MulticastRecvThread(AdminGUI window) {
        this.window = window;
    }

    @Override
    public void run() {
        byte buf[] = new byte[1024];
        DatagramPacket recvPkt = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                // 接收信息
                window.msocket.receive(recvPkt);
                String[] msg = new String(recvPkt.getData(), 0, recvPkt.getLength()).split(":");

                // 判断信息类型
                if(msg[0].equals("join")) {
                    int i = Integer.parseInt(msg[1]);
                    if (window.port != i && !(window.map.containsValue(i))){
                        window.map.put("admin"+msg[1], i);
                        window.messageArea1.append(new String(recvPkt.getData(), 0,recvPkt.getLength()) + "\n");
                        window.messageArea3.append("admin"+msg[1] + "\n");
                        byte buf1[];
                        buf1 = ("join:" + window.adminInfo).getBytes(Charset.forName("UTF-8"));
                        DatagramPacket pkt = new DatagramPacket(buf1, buf1.length, window.groupAddr, 12345);
                        window.msocket.send(pkt);
                    }
                } else if (msg[0].equals("leav")) {
                    window.map.remove("admin"+msg[1]);
                    window.messageArea1.append(new String(recvPkt.getData(), 0,recvPkt.getLength()) + "\n");
                    window.messageArea3.setText("");
                    window.messageArea3.append(window.map.keySet().toString()+ "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
