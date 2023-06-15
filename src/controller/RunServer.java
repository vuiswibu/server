package controller;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import view.Host;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author boi09
 */
public class RunServer {
    public static volatile ClientManager clientmanager;
    public static Socket socketOfServer;
    public static int ID_room;
    public static volatile Host host;

    public static void main(String[] args) {
        ServerSocket listener = null;
        clientmanager = new ClientManager();
        System.out.println("Server is waiting to accept user...");
        int clientNumber = 0;
        ID_room = 100;
        try {
            listener = new ServerSocket(7777);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10, // corePoolSize
                100, // maximumPoolSize
                10, // thread timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(8) // queueCapacity
        );
        host= new Host();
        host.run();
        try {
            while (true) {
                // Chấp nhận một yêu cầu kết nối từ phía Client.
                // Đồng thời nhận được một đối tượng Socket tại server.
                socketOfServer = listener.accept();
                System.out.println(socketOfServer.getInetAddress().getHostAddress());
                Client client = new Client(socketOfServer, clientNumber++);
                clientmanager.add(client);
                System.out.println("Số thread đang chạy là: "+clientmanager.getLength());
                executor.execute(client);  
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

