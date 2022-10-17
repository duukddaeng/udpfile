/*
计算机网络程序设计
局域网UDP文件分发设计
2019302110088-杜煜-19广电工3班
 */

import java.io.RandomAccessFile;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SendWay extends Thread{
    AdminGUI window;
    String admin;

    public SendWay(AdminGUI window, String admin) {
        this.window = window;
        this.admin= admin;
    }

    public void run() {
        try {
            int i = window.map.get(admin);
            InetSocketAddress sockAddr = new InetSocketAddress("127.0.0.1", i);
            //byte[] sendbuf = new byte[1032];
            //RandomAccessFile file2 = new RandomAccessFile(window.file, "rw");
            DatagramPacket sendPkt = new DatagramPacket(("requadmin"+window.port).getBytes(StandardCharsets.UTF_8), ("requadmin"+window.port).length(), sockAddr);
            window.dsocket.send(sendPkt);
           /* byte[] recvbuf = new byte[1024];
            DatagramPacket recvPkt = new DatagramPacket(recvbuf, recvbuf.length);

            int offset2 = 0;
            int filesize2 = 0;
            int failcount = 0;
            while (true) {
                window.dsocket.receive(recvPkt);
                String type = new String(recvPkt.getData(), 0, 4);
                window.messageArea1.append(new String(recvPkt.getData(), 0, recvPkt.getLength()));
                if (type.equals("okok")) {
                    file2.seek(0);
                    String filename = window.file.getCanonicalPath();
                    filesize2 = (int) file2.length();
                    byte[] size = new byte[8+filename.length()];
                    size[4] = (byte) ((filesize2 >> 24) & 0x0ff);
                    size[5] = (byte) ((filesize2 >> 16) & 0x0ff);
                    size[6] = (byte) ((filesize2 >> 8) & 0x0ff);
                    size[7] = (byte) (filesize2 & 0x0ff);
                    System.arraycopy("info".getBytes(), 0, size, 0, "info".length());
                    System.arraycopy(filename.getBytes(), 0, size, 8, filename.length());
                    sendPkt = new DatagramPacket(size, size.length, sockAddr);
                    window.dsocket.send(sendPkt);
                } else if (type.equals("nono")) {
                    break;
                } else if (type.equals("next")) {
                    if (offset2+1024<filesize2) {
                        file2.read(sendbuf, 8, 1024);
                        sendbuf[4] = (byte) ((offset2 >> 24) & 0x0ff);
                        sendbuf[5] = (byte) ((offset2 >> 16) & 0x0ff);
                        sendbuf[6] = (byte) ((offset2 >> 8) & 0x0ff);
                        sendbuf[7] = (byte) (offset2 & 0x0ff);
                        offset2 += 1024;
                        file2.seek(offset2);
                        System.arraycopy("data".getBytes(), 0, sendbuf, 0, "data".length());
                        sendPkt = new DatagramPacket(sendbuf, sendbuf.length, sockAddr);
                        window.dsocket.send(sendPkt);
                    } else if (offset2+1024>filesize2) {
                        byte[] last = new byte[8+(filesize2%1024)];
                        file2.read(last, 8, last.length-8);
                        sendbuf[4] = (byte) ((offset2 >> 24) & 0x0ff);
                        sendbuf[5] = (byte) ((offset2 >> 16) & 0x0ff);
                        sendbuf[6] = (byte) ((offset2 >> 8) & 0x0ff);
                        sendbuf[7] = (byte) (offset2 & 0x0ff);
                        System.arraycopy("over".getBytes(), 0, sendbuf, 0, "data".length());
                        sendPkt = new DatagramPacket(sendbuf, sendbuf.length, sockAddr);
                        window.dsocket.send(sendPkt);
                        offset2 = 0;
                        file2.seek(offset2);
                    } else if (offset2+1024==filesize2) {
                        byte[] last = new byte[1032];
                        file2.read(last, 8, last.length-8);
                        sendbuf[4] = (byte) ((offset2 >> 24) & 0x0ff);
                        sendbuf[5] = (byte) ((offset2 >> 16) & 0x0ff);
                        sendbuf[6] = (byte) ((offset2 >> 8) & 0x0ff);
                        sendbuf[7] = (byte) (offset2 & 0x0ff);
                        System.arraycopy("over".getBytes(), 0, sendbuf, 0, "data".length());
                        sendPkt = new DatagramPacket(sendbuf, sendbuf.length, sockAddr);
                        window.dsocket.send(sendPkt);
                        offset2 = 0;
                        file2.seek(offset2);
                    }
                } else if (type.equals("succ")) {
                    break;
                } else if (type.equals("fail")) {
                    failcount++;
                    if (failcount<=3) {
                        file2.seek(0);
                        String filename = window.file.getCanonicalPath();
                        filesize2 = (int) file2.length();
                        byte[] size = new byte[8+filename.length()];
                        size[4] = (byte) ((filesize2 >> 24) & 0x0ff);
                        size[5] = (byte) ((filesize2 >> 16) & 0x0ff);
                        size[6] = (byte) ((filesize2 >> 8) & 0x0ff);
                        size[7] = (byte) (filesize2 & 0x0ff);
                        System.arraycopy("info".getBytes(), 0, size, 0, "info".length());
                        System.arraycopy(filename.getBytes(), 0, size, 8, filename.length());
                        sendPkt = new DatagramPacket(size, size.length, sockAddr);
                        window.dsocket.send(sendPkt);
                    } else {
                        break;
                    }
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
