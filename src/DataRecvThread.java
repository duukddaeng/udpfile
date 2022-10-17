/*
计算机网络程序设计
局域网UDP文件分发设计
2019302110088-杜煜-19广电工3班
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class DataRecvThread extends Thread{
    public AdminGUI window;

    public DataRecvThread(AdminGUI adminGUI) {
        this.window = adminGUI;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[1032];
            window.recvPkt = new DatagramPacket(buf, buf.length);
            RandomAccessFile file = null;
            String sendadmin;
            DatagramPacket pkt;
            int filesize = 0;
            int count = 0;
            byte[] sendbuf = new byte[1032];
            int offset2 = 0;
            int filesize2 = 0;
            int failcount = 0;
            while (true) {
                // 接收消息
                //InetSocketAddress sockAddr = null;
                window.dsocket.receive(window.recvPkt);
                /*while (window.recvadmin!=null) {
                    int recport = window.map.get(window.recvadmin);
                    sockAddr = new InetSocketAddress("127.0.0.1", recport);
                }*/
                String type = new String(window.recvPkt.getData(), 0, 4);
                // 判断消息类型
                if (type.equals("requ")) {
                    // 保存发送方用户名
                    sendadmin = new String(window.recvPkt.getData(), 0, window.recvPkt.getLength()-4);
                    // 窗口显示请求信息
                    window.messageArea1.append(new String(window.recvPkt.getData(), 0, window.recvPkt.getLength()) + "\n");
                } else if (type.equals("info")) {
                    // 读取文件名和大小
                    String filename = new String(window.recvPkt.getData(), 8, window.recvPkt.getLength()-8);
                    String[] name = filename.split("\\\\");
                    for (int i =0;i< name.length;i++){
                        filename=name[i];
                    }

                    for (int i=0; i<4; i++) {
                        filesize += (buf[i+4] & 0xff) << ((3-i) *8);
                    }
                    file = new RandomAccessFile(window.port+"out"+filename, "rw");
                    // 发送应答信息：next+用户名
                    String msg = "nextadmin" + window.port;
                    pkt = new DatagramPacket(msg.getBytes(), msg.length(), window.recvPkt.getSocketAddress());
                    window.dsocket.send(pkt);
                } else if (type.equals("data")) {
                    int offset = 0;
                    for (int i=0; i<4; i++) {
                        offset += (buf[i+4] & 0xff) << ((3-i) *8);
                    }
                    if (file != null) {
                        file.seek(offset);
                        file.write(buf, 8, window.recvPkt.getLength()-8);
                        count += window.recvPkt.getLength()-8;
                        // 发送应答信息：next+用户名
                        String msg = "nextadmin" + window.port;
                        pkt = new DatagramPacket(msg.getBytes(), msg.length(), window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                    }
                } else if (type.equals("over")) {
                    int offset = 0;
                    for (int i=0; i<4; i++) {
                        offset += (buf[i+4] & 0xff) << ((3-i) *8);
                    }
                    if (file != null) {
                        file.seek(offset);
                        file.write(buf, 8, window.recvPkt.getLength()-8);
                        count += window.recvPkt.getLength()-8;
                    }
                    if (filesize == count) {
                        // 发送应答信息：succ+用户名
                        String msg = "succadmin" + window.port;
                        pkt = new DatagramPacket(msg.getBytes(), msg.length(), window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                    } else {
                        // 发送应答信息：fail+用户名
                        String msg = "failadmin" + window.port;
                        pkt = new DatagramPacket(msg.getBytes(), msg.length(), window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                    }
                } else if (type.equals("okok")) {

                    window.messageArea1.append(new String(window.recvPkt.getData(), 0, window.recvPkt.getLength())+"\n");
                    window.file2.seek(0);
                    String filename = window.file.getCanonicalPath();
                    filesize2 = (int) window.file2.length();
                    byte[] size = new byte[8+filename.length()];
                    size[4] = (byte) ((filesize2 >> 24) & 0x0ff);
                    size[5] = (byte) ((filesize2 >> 16) & 0x0ff);
                    size[6] = (byte) ((filesize2 >> 8) & 0x0ff);
                    size[7] = (byte) (filesize2 & 0x0ff);
                    System.arraycopy("info".getBytes(), 0, size, 0, "info".length());
                    System.arraycopy(filename.getBytes(), 0, size, 8, filename.length());
                    pkt = new DatagramPacket(size, size.length, window.recvPkt.getSocketAddress());
                    window.dsocket.send(pkt);
                } else if (type.equals("nono")) {
                    window.messageArea1.append(new String(window.recvPkt.getData(), 0, window.recvPkt.getLength())+"\n");
                    break;
                } else if (type.equals("next")) {
                    window.messageArea1.append(new String(window.recvPkt.getData(), 0, window.recvPkt.getLength())+"\n");
                    if (offset2+1024<filesize2) {
                        window.file2.read(sendbuf, 8, 1024);
                        sendbuf[4] = (byte) ((offset2 >> 24) & 0x0ff);
                        sendbuf[5] = (byte) ((offset2 >> 16) & 0x0ff);
                        sendbuf[6] = (byte) ((offset2 >> 8) & 0x0ff);
                        sendbuf[7] = (byte) (offset2 & 0x0ff);
                        offset2 += 1024;
                        window.file2.seek(offset2);
                        System.arraycopy("data".getBytes(), 0, sendbuf, 0, "data".length());
                        pkt = new DatagramPacket(sendbuf, sendbuf.length, window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                    } else if (offset2+1024>filesize2) {
                        byte[] last = new byte[8+(filesize2%1024)];
                        window.file2.read(last, 8, last.length-8);
                        sendbuf[4] = (byte) ((offset2 >> 24) & 0x0ff);
                        sendbuf[5] = (byte) ((offset2 >> 16) & 0x0ff);
                        sendbuf[6] = (byte) ((offset2 >> 8) & 0x0ff);
                        sendbuf[7] = (byte) (offset2 & 0x0ff);
                        System.arraycopy("over".getBytes(), 0, sendbuf, 0, "data".length());
                        pkt = new DatagramPacket(sendbuf, sendbuf.length, window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                        offset2 = 0;
                        window.file2.seek(offset2);
                    } else if (offset2+1024==filesize2) {
                        byte[] last = new byte[1032];
                        window.file2.read(last, 8, last.length-8);
                        sendbuf[4] = (byte) ((offset2 >> 24) & 0x0ff);
                        sendbuf[5] = (byte) ((offset2 >> 16) & 0x0ff);
                        sendbuf[6] = (byte) ((offset2 >> 8) & 0x0ff);
                        sendbuf[7] = (byte) (offset2 & 0x0ff);
                        System.arraycopy("over".getBytes(), 0, sendbuf, 0, "data".length());
                        pkt = new DatagramPacket(sendbuf, sendbuf.length, window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                        offset2 = 0;
                        window.file2.seek(offset2);
                    }
                } else if (type.equals("succ")) {
                    window.messageArea1.append(new String(window.recvPkt.getData(), 0, window.recvPkt.getLength())+"\n");
                    break;
                } else if (type.equals("fail")) {
                    window.messageArea1.append(new String(window.recvPkt.getData(), 0, window.recvPkt.getLength())+"\n");
                    failcount++;
                    if (failcount<=3) {
                        window.file2.seek(0);
                        String filename = window.file.getCanonicalPath();
                        filesize2 = (int) window.file2.length();
                        byte[] size = new byte[8+filename.length()];
                        size[4] = (byte) ((filesize2 >> 24) & 0x0ff);
                        size[5] = (byte) ((filesize2 >> 16) & 0x0ff);
                        size[6] = (byte) ((filesize2 >> 8) & 0x0ff);
                        size[7] = (byte) (filesize2 & 0x0ff);
                        System.arraycopy("info".getBytes(), 0, size, 0, "info".length());
                        System.arraycopy(filename.getBytes(), 0, size, 8, filename.length());
                        pkt = new DatagramPacket(size, size.length, window.recvPkt.getSocketAddress());
                        window.dsocket.send(pkt);
                    } else {
                        break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
