package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

//Bot который загадывает число
//пользователь отгадывает дихотомией
//to do github

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.toString());
    private DatagramSocket datagramSocket;
    private InetAddress address;
    private byte[] dataBuffer;
    private Integer clientPort;
    private boolean flag = true;
    private boolean game = false;
    private int number;
    private Server(int portNumber){
        try {
            datagramSocket = new DatagramSocket(portNumber);
            dataBuffer = new byte[1024];
        }catch (Exception e){
            e.getStackTrace();
        }
    }
    private void listener() {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
            println("Server started!");
            while (exitCycle()) {
                datagramSocket.receive(datagramPacket);
                address = datagramPacket.getAddress();
                clientPort = datagramPacket.getPort();
                String receivedData = new String(datagramPacket.getData(),
                        datagramPacket.getOffset(), datagramPacket.getLength());
                if (game){
                    if (receivedData.contains("@stop")){
                        game = false;
                        sendAnswer("Game is stopped by user!");
                        continue;
                    }
                    String [] tmp = receivedData.split(": ");
                    playGame(tmp[1]);
                }
                if (receivedData.compareTo("@quit ") == 0){
                    flag = false;
                }
                if (receivedData.contains("@startGame")){
                    game = true;
                    Random r = new Random();
                    number = r.nextInt(100);
                    println("Game started. Number is " + number);
                    sendAnswer("Game started! I generated a number from 0 to 100!");
                }
                else {
                    println(receivedData);
                }
            }
        }catch (IOException e) {
            println(e.getMessage());
        }
    }
    private void respond() {
        while (exitCycle()) {
            try {
                Scanner sc = new Scanner(System.in);
                String answ;
                answ = sc.nextLine();
                byte[] sendData = answ.getBytes();
                DatagramPacket outServerPacket = new DatagramPacket(sendData, sendData.length,
                        address, clientPort);
                datagramSocket.send(outServerPacket);
            } catch (IOException e) {
                println(e.getMessage());
            }
        }
    }
    private boolean exitCycle (){
        return flag;
    }
    private void playGame (String input){
        try(Scanner sc = new Scanner(input)) {
            int tmp = -1;
            while (sc.hasNext()){
                if (sc.hasNextInt()){
                    tmp = sc.nextInt();
                }
                else{
                    sc.next();
                }
            }
            if (input.contains("More")) {
                if (number > tmp) {
                    sendAnswer("Yes");
                    return;
                }
                else {
                    sendAnswer("No");
                    return;
                }
            }
            if (input.contains("Less")) {
                if (number < tmp) {
                    sendAnswer("Yes");
                    return;
                }
                else {
                    sendAnswer("No");
                    return;
                }
            }
            if (tmp == number){
                sendAnswer("You won!");
                game = false;
                return;
            }
            if (tmp == -1){
                sendAnswer("Error! Write new condition: More than [number] or " +
                        "Less than [number], or final answer");
                return;
            }
            sendAnswer("No");
        }catch (Exception e){
            println(e.getMessage());
        }
    }
    private void sendAnswer (String input){
        try {
            byte[] sendData;
            DatagramPacket outServerPacket;
            sendData = input.getBytes();
            outServerPacket = new DatagramPacket(sendData, sendData.length,
                    address, clientPort);
            datagramSocket.send(outServerPacket);
        } catch (IOException e) {
            println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(2017);
            new Thread(server::listener).start();
            new Thread(server::respond).start();
        } catch (Exception e) {
            println(e.getMessage());
        }
    }
    static void println (String input){
        String output = input + "\n";
        logger.log(Level.INFO, output);
    }
}