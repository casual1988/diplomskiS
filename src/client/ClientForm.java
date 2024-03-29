/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import kriptotest.CryptoJavniKljuc;
import kriptotest.CryptoTajniKljuc;

/**
 *
 * @author Aleksandar
 */
public class ClientForm extends javax.swing.JFrame {

    public static final int TCP_PORT = 9000;
    public static final int clientPort = 9001;
    private ClientInfo currentClient = new ClientInfo();

    BufferedReader in;
    InetAddress addr;
    Socket sock;
    PrintWriter out;
    String message;
    ArrayList<ClientInfo> onlineUser = new ArrayList<ClientInfo>();
    ObjectOutputStream oos;
    ObjectInputStream ois;
    ClientInfo recipient;

    KeyPair key;
    CryptoJavniKljuc javni;
    CryptoTajniKljuc tajni;
    /**
     * Creates new form ClientForm
     */
    public ClientForm() {
        initComponents();
        javni = new CryptoJavniKljuc();
        this.key = javni.generateKeyPair();
        currentClient.setKey(key.getPublic());

        currentClient.setUsername("Aleksandar");
        // (new Thread(new ClientForm())).start();
        //pokretanje mini servera za prijem poruka
        if (connectToServer()) {
            message = "Uspjesna konekcija na server";
            (new Thread(new ClientServer(this))).start();
        } else {
            message = "konekcija nije uspjela";
        }

    }

    public boolean connectToServer() {
        try {
            addr = InetAddress.getByName("192.168.1.3");
            sock = new Socket(addr, TCP_PORT);
            oos = new ObjectOutputStream(sock.getOutputStream());
            ois = new ObjectInputStream(sock.getInputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login() throws IOException {
        //    out.println("login " + user.getUsername() + " " + user.getPassword());
        if (in.readLine().equals("200")) {
            return true;
        } else {
            return false;
        }
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getOnlineUser() throws IOException, ClassNotFoundException {
        sendPublicKey();
        oos.writeObject("getOnlineUser");
        onlineUser = (ArrayList<ClientInfo>) ois.readObject();
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < onlineUser.size(); i++) {
            model.addElement(onlineUser.get(i));
        }
        jList1.setModel(model);
    }
    
     public void signOut() throws IOException, ClassNotFoundException {
        oos.writeObject("signout");
        String response = (String) ois.readObject();
        if (response.equals("200")) {
            closeConnection();
        }
    }

    public void sendObject() throws IOException {
        ClientInfo client = new ClientInfo();
        client.setUsername("aco");
        oos.writeObject(client);
    }

    //metoda za salnje
    public void sendMessageToClint() {
        try {
            this.recipient = (ClientInfo) jList1.getSelectedValue();
            if (this.recipient.getSock() == null) {
                this.recipient.setSock(getClientSocket(this.recipient));
            }
            String msg = jTextArea2.getText();
            if (this.recipient.getSecretKey() == null) {
                byte[] kez = generateSecretKeyProba(recipient);
                sendSecretKey(recipient, kez);   // napravio novu samo za slanje kljuca
                sendMSG(recipient, msg.getBytes());  // prvo slanje kljuca pa onda poruke
                jTextArea2.setText("");
            } else {
                //  byte[] encript = tajni.encrypt(msg.getBytes(), this.recipient.getSecretKey());
                //sendMSG(recipient, encript); //dodao gdet bytes
                sendMSG(recipient, msg.getBytes());
                jTextArea1.append(currentClient.getUsername() + ":   " + msg + "\n");
                jTextArea2.setText("");
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendPublicKey() throws IOException, ClassNotFoundException {
        System.out.println("slanje pk");
        oos.writeObject(currentClient);
        System.out.println("poslano");
        String response = (String) ois.readObject();
        System.out.println(response);
        System.out.println("primljeno");
        //proba ,slanje kljuca na server za ovog usera,da bi server cuvao i razmjenio kljuceve mejdu korisnicima
    }

    public String generateSecretKey(ClientInfo client) throws UnsupportedEncodingException {
        CryptoTajniKljuc tajni = new CryptoTajniKljuc();
        Key secretKey = tajni.generateKey();
        this.recipient.setSecretKey(secretKey);
        CryptoJavniKljuc javni = new CryptoJavniKljuc();
        byte[] byteText = javni.encrypt(secretKey.getEncoded(), client.getKey());
        String cipherText = new String(byteText, "UTF8");
        System.out.println(cipherText);
        return cipherText;
    }

    public Key recivedSecretKey(byte[] msg, ClientInfo currentClient, KeyPair key) throws UnsupportedEncodingException {
        CryptoJavniKljuc javni = new CryptoJavniKljuc();
        byte[] text = javni.decrypt(msg, key.getPrivate());
        SecretKey spec = new SecretKeySpec(text, "DES");
        Key keyaa = (Key) spec;
        currentClient.setSecretKey(keyaa);
        System.out.println(keyaa.toString());
        return keyaa;
    }
    
     public byte[] generateSecretKeyProba(ClientInfo client) throws UnsupportedEncodingException {
        tajni = new CryptoTajniKljuc();
        Key secretKey = tajni.generateKey();
        this.recipient.setSecretKey(secretKey);
        //CryptoJavniKljuc javni = new CryptoJavniKljuc();
        byte[] byteText = javni.encrypt(secretKey.getEncoded(), client.getKey());
        // String cipherText = new String(byteText,"UTF8");
        // System.out.println(cipherText);
        return byteText;
    }

    private void connectionWithClient(ClientInfo client) throws UnknownHostException, IOException {
        int port = 9001;
        InetAddress addr1 = InetAddress.getByName(client.getIp());
//Socket sock1 = new Socket(addr1, Integer.parseInt(niz[1]));
        Socket sock = new Socket(addr1, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                sock.getInputStream()));
// inicijalizuj izlazni stream
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                sock.getOutputStream())), true);
        MessageData msg = new MessageData();
        out.println("pozdrav,saljem ti poruku");
        //    ReaderThread reader = new ReaderThread(sock, in, msg);
        //  WriterThread writer = new WriterThread(out, msg);
    }

//    private void sendMSG(ClientInfo client, String msg) throws UnknownHostException, IOException {
//        int port = 9001;
//        InetAddress addr1 = InetAddress.getByName(client.getIp());
//Socket sock1 = new Socket(addr1, Integer.parseInt(niz[1]));
//        Socket sock = new Socket(addr1, port);
// inicijalizuj izlazni stream
//        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
//                sock.getOutputStream())), true);
//        //    ReaderThread reader = new ReaderThread(sock, in, msg);
//        //    WriterThread writer = new WriterThread(out, msg);
//        out.println(msg);
//        jTextArea1.append(currentClient.getUsername() + ":   " + msg + "\n");
 //   }
    
 private void sendMSG(ClientInfo client, byte[] msg) throws UnknownHostException, IOException {
        OutputStream out = null;
        out = client.getSock().getOutputStream();

        ByteArrayInputStream serverinput = new ByteArrayInputStream(msg);
        int count;
        byte[] bytearray = new byte[128];
        byte[] cipherbuffer;
        while ((count = serverinput.read(bytearray)) > 0) {
            cipherbuffer = (tajni.encrypt(bytearray, client.getSecretKey()));
            out.write(cipherbuffer, 0, cipherbuffer.length);
        }
        out.flush();
    }

    private void sendSecretKey(ClientInfo client, byte[] msg) throws UnknownHostException, IOException {
        OutputStream out = null;
        out = client.getSock().getOutputStream();
        out.write(msg);
        out.flush();
    }

    public Socket getClientSocket(ClientInfo client) throws IOException {
        InetAddress addr1 = InetAddress.getByName(client.getIp());
        Socket sock = new Socket(addr1, clientPort);
        return sock;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(jList1);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextArea1KeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane3.setViewportView(jTextArea2);

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem1.setText("Start");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(26, 26, 26))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)))
                .addGap(7, 7, 7))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        sendMessageToClint();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        try {
            getOnlineUser();
        } catch (IOException ex) {
            Logger.getLogger(ClientForm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jTextArea1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyPressed
        System.out.println("proba za listiner");
        jList1.setSelectedIndex(0);
    }//GEN-LAST:event_jTextArea1KeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientForm().setVisible(true);
            }
        });
    }

    public JTextArea getjTextArea1() {
        return jTextArea1;
    }

    public void setjTextArea1(JTextArea jTextArea1) {
        this.jTextArea1 = jTextArea1;
    }

    public JList getjList1() {
        return jList1;
    }

    public void setjList1(JList jList1) {
        this.jList1 = jList1;
    }

    public KeyPair getKey() {
        return key;
    }

    public void setKey(KeyPair key) {
        this.key = key;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables
}
