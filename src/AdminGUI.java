/*
计算机网络程序设计
局域网UDP文件分发设计
2019302110088-杜煜-19广电工3班
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;

public class AdminGUI extends JFrame{
    public ServerSocket serverSocket = null;
    public InetAddress groupAddr;
    public int port;
    public DatagramSocket dsocket;
    public MulticastSocket msocket;
    public JTextField textField;
    public JTextArea messageArea1;
    public JTextArea messageArea2;
    public JTextArea messageArea3;
    public Map<String, Integer> map;
    public String ipaddr = "127.0.0.1";
    public String adminInfo;
    public File file;
    public RandomAccessFile file2;
    public String recvadmin;
    public DatagramPacket recvPkt;
    public AdminGUI() {
        //添加界面部件
        JFrame mainWindow = new JFrame();
        mainWindow.setLayout(null);
        mainWindow.setSize(640,480);
        mainWindow.setResizable(false);
        textField = new JTextField();
        textField.setBounds(180, 10, 400, 30);
        mainWindow.add(textField);
        messageArea1 = new JTextArea();
        JScrollPane scrollPane1 = new JScrollPane(messageArea1);
        scrollPane1.setBounds(180, 50, 400, 300);
        mainWindow.add(scrollPane1);
        messageArea2 = new JTextArea();
        JScrollPane scrollPane2 = new JScrollPane(messageArea2);
        scrollPane2.setBounds(280, 360, 300, 30);
        mainWindow.add(scrollPane2);
        messageArea3 = new JTextArea();
        JScrollPane scrollPane3 = new JScrollPane(messageArea3);
        scrollPane3.setBounds(10, 50, 160, 300);
        mainWindow.add(scrollPane3);
        JButton btn1 = new JButton("发送");
        btn1.setBounds(10, 10, 100, 30);
        btn1.addActionListener(new ButtonListener());
        mainWindow.add(btn1);
        JButton btn2 = new JButton("接收");
        btn2.setBounds(10, 360, 80, 30);
        btn2.addActionListener(new ButtonListener());
        mainWindow.add(btn2);
        JButton btn3 = new JButton("拒绝");
        btn3.setBounds(100, 360, 80, 30);
        btn3.addActionListener(new ButtonListener());
        mainWindow.add(btn3);
        JButton btn4 = new JButton("浏览");
        btn4.setBounds(190, 360, 80, 30);
        btn4.addActionListener(new ButtonListener());
        mainWindow.add(btn4);
        mainWindow.setVisible(true);

        try {
            // 随机生成端口号
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            groupAddr = InetAddress.getByName("224.5.6.7");
            // 指定端口，加入组播
            InetSocketAddress sockAddr =new InetSocketAddress("224.5.6.7", 12345);
            msocket = new MulticastSocket(12345);
            msocket.setReuseAddress(true);
            msocket.joinGroup(groupAddr);
            dsocket = new DatagramSocket(port);
            // 封装ip和port
            adminInfo = port + "";
            map = new HashMap<>();
            // 创建线程
            MulticastRecvThread thread1 = new MulticastRecvThread(this);
            thread1.start();
            DataRecvThread thread2 =new DataRecvThread(this);
            thread2.start();
            // 发送用户信息
            byte[] buf1;
            buf1 = ("join:" + adminInfo).getBytes(Charset.forName("UTF-8"));
            DatagramPacket pkt = new DatagramPacket(buf1, buf1.length, groupAddr, 12345);
            msocket.send(pkt);
            // 等待窗口关闭
            mainWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    byte[] buf2;
                    buf2 = ("leav:" + adminInfo).getBytes(Charset.forName("UTF-8"));
                    DatagramPacket pkt = new DatagramPacket(buf2, buf2.length, groupAddr, 12345);
                    try {
                        msocket.send(pkt);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.exit(0);//退出系统
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        AdminGUI window = new AdminGUI();
    }

    public void sendFile() {
        String[] admin = textField.getText().split("&");
        for (int i = 0;i<admin.length;i++) {
            recvadmin = admin[i];
            SendWay thread = new SendWay(this, admin[i]);
            thread.run();
        }
    }

    public void recFile(){
        String admin = textField.getText();
        int i = map.get(admin);
        InetSocketAddress sockAddr = new InetSocketAddress("127.0.0.1", i);
        String msg = "okokadmin"+port;
        try {
            DatagramPacket pkt = new DatagramPacket(msg.getBytes(StandardCharsets.UTF_8), msg.getBytes().length, sockAddr);
            dsocket.send(pkt);
            messageArea1.append("已接受发送文件请求。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rufFile() {
        String admin = textField.getText();
        int i = map.get(admin);
        InetSocketAddress sockAddr = new InetSocketAddress("127.0.0.1", i);
        String msg = "nonoadmin"+port;
        try {
            DatagramPacket pkt = new DatagramPacket(msg.getBytes(StandardCharsets.UTF_8), msg.getBytes().length, sockAddr);
            dsocket.send(pkt);
            messageArea1.append("已拒绝发送文件请求。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openFile() {
        messageArea2.append("");
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        try {
            int result = fc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                file2 = new RandomAccessFile(file, "rw");
                messageArea2.append("打开文件：" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("发送")) {
                sendFile();
            } else if (e.getActionCommand().equals("接收")) {
                recFile();
            } else if (e.getActionCommand().equals("拒绝")) {
                rufFile();
            } else if (e.getActionCommand().equals("浏览")) {
                openFile();
            }
        }
    }
}
