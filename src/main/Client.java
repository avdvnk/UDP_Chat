package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.InputMismatchException;
import java.util.Scanner;
import static main.Server.println;

public class Client {
    private String clientName = "defaultName";
    private Integer port;
    private DatagramSocket datagramSocket;
    private InetAddress address;
    private byte[] dataBuffer;
    private Client(String address, int port) {
        try {
            this.address = InetAddress.getByName(address);
            this.port = port;
            datagramSocket = new DatagramSocket();
            dataBuffer = new byte[1024];
        } catch (Exception e) {
            println(e.getMessage());
        }
    }
    private void setName(String string) {
        clientName = string;
    }
    private void sendMessage() {
        Scanner sc = new Scanner(System.in);
        while (!datagramSocket.isClosed()) {
            println("Type message: ");
            String message = sc.nextLine();
            if (message.length() > 6) {
                String cmp = new String(message.getBytes(), 0, 6);
                if (cmp.compareTo("@name ") == 0) {
                    this.setName(new String(message.getBytes(), 6, message.length() - 6));
                    println("Your name was changed!");
                    continue;
                }
            }
            if (message.compareTo("@quit") == 0) {
                try{
                    String fullMessage = clientName + message;
                    byte [] data = fullMessage.getBytes();
                    DatagramPacket outPacket = new DatagramPacket(data, data.length, address, port);
                    datagramSocket.send(outPacket);
                    datagramSocket.close();
                    println("Good luck!");
                    System.exit(0);
                }catch (IOException e){
                    println(e.getMessage());
                }
            }
            try {
                String fullMessage = clientName + ": " + message;
                byte[] data = fullMessage.getBytes();
                DatagramPacket outPacket = new DatagramPacket(data, data.length, address, port);
                datagramSocket.send(outPacket);
            } catch (IOException e) {
                println(e.getMessage());
            } catch (IllegalArgumentException e){
                println("Port must be > 0");
                System.exit(0);
            }
        }
    }
    private void recieveMessage() {
        try {
            DatagramPacket datagramPacket = new DatagramPacket
                    (dataBuffer, dataBuffer.length);
            while (!datagramSocket.isClosed()) {
                datagramSocket.receive(datagramPacket);
                String answ = new String(datagramPacket.getData(), datagramPacket.getOffset(),
                        datagramPacket.getLength());
                println("Server response: " + answ);
            }
        } catch (IOException e) {
            datagramSocket.close();
            println(e.getMessage());
        }catch (IllegalArgumentException e){
            println("port must be > 0");
            System.exit(0);
        }
    }
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            String adr;
            Integer port;
            println("Enter serverAdress");
            adr = scanner.next();
            println("Enter portNumber");
            port = scanner.nextInt();
            println("Avaliable options: \n@name [your name] to change your name in chat\n" +
                    "[message] + [enter] to send message\n@quit to close chat" +
                    "\n@startGame to start game this number" +
                    "\n@stop to stop the game");
            Client client = new Client(adr, port);
            new Thread(client::sendMessage).start();
            new Thread(client::recieveMessage).start();
        } catch (InputMismatchException e) {
            println("Try another port address input");
        }
    }
}