/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ServerFull;


import client.ClientInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Aleksandar
 */
public class Server extends Thread {
  
private Socket sock;
private int value;
private BufferedReader in;
private PrintWriter out;
public static final int TCP_PORT = 9000;
public final static String OK="200";
public final static String ERROR="400";
public final static String ServerError="500";
private ObjectOutputStream oos;  
private ObjectInputStream  ois;


private static ArrayList<User> users= new ArrayList<User>();
private static ArrayList<ClientInfo> onlienUsers= new ArrayList<ClientInfo>();

public static void main(String[] args) {
    
 //   users.add(new User("aleksandar"));
  //  users.add(new User("marko"));
  //  users.add(new User("pero"));
  //  users.add(new User("simo"));
   
try {
int clientCounter = 0;
// slušaj zahteve na datom portu
ServerSocket ss = new ServerSocket(TCP_PORT);
//System.out.println(ss.getInetAddress() +" "+ ss.getLocalSocketAddress());
System.out.println("Server running...");
while (true) {
Socket sock = ss.accept();
System.out.println("Client prihvacen: " + (++clientCounter));
Server st = new Server(sock, clientCounter);
ClientInfo client= new ClientInfo();
client.setUsername("user"+clientCounter);
client.setIp(sock.getInetAddress().getHostAddress());
User user= new User();
user.setClient(client);
user.setSocket(sock);
users.add(user);
onlienUsers.add(client);
}
} catch (Exception ex) {
ex.printStackTrace();
}}


public Server(Socket sock, int value) {
this.sock = sock;
this.value = value;
try {
    

 ois = new ObjectInputStream(sock.getInputStream());   
 oos = new ObjectOutputStream(sock.getOutputStream());
// inicijalizuj ulazni stream
//in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
// inicijalizuj izlazni stream
//out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);    
} catch (Exception ex) {
ex.printStackTrace();
}
start();}



public void run() {
try {
  Object received = null;
//while((received =ois.readObject())!=null)

    received =ois.readObject();
if(received instanceof ClientInfo){
   ClientInfo user= (ClientInfo)received;
      System.out.println(user.getUsername());
}else if(received instanceof String){
    String command= (String) received;
    if(command.equals("getOnlineUser")){
        System.out.println(command);
        onlienUsers= ServerMethods.getOnlineUser(users, sock);
        oos.writeObject(onlienUsers); 
    }
    else if(command.equals("signout")){
        System.out.println(command);
        ServerMethods.signout(users, sock);
        oos.writeObject(OK); 
        in.close();
        out.close();
        sock.close();
    }
    
//}else if(){


}
    
    
    
    

    
} catch (Exception ex) {
ex.printStackTrace();
}


}

//public void run() {
//try {
//  
//    String request;
//    while ((request = in.readLine()) != null) {
//// procitaj zahtjev
//// odgovori na zahtjev
//if(request.equals("list")){
//  out.println(ServerMethods.listUsers(clientList,this.sock));
//}
//else if(request.contains("user")){
//
//String niz[]= request.split(" ");
//User user= ServerMethods.call(clientList, niz[1]);
//ServerMethods.sendMessage(user, "probna poruka");
//}else if(request.contains("login")){    //saebri 5 10
//if(ServerMethods.login(clientList))
//out.println("OK");
//}else if(request.contains("connect")){    //upis u fajl
// out.println(OK);
//}else if(request.contains("getOnlineUser")){    //upis u fajl
//out.println(ServerMethods.listUsers(clientList,this.sock));
//}
//
//
//else {
//out.println("pogresna komanda");
//}
//
//    }
//in.close();
//out.close();
//sock.close();
//
//} catch (Exception ex) {
//ex.printStackTrace();
//}
//}
    public static ArrayList<User> getUsers() {
        return users;
    }

    public static void setUsers(ArrayList<User> users) {
        Server.users = users;
    }

    public static ArrayList<ClientInfo> getOnlienUsers() {
        return onlienUsers;
    }

    public static void setOnlienUsers(ArrayList<ClientInfo> onlienUsers) {
        Server.onlienUsers = onlienUsers;
    }


}