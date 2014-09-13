/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kriptotest;

import java.security.Key;
import java.security.KeyPair;

/**
 *
 * @author Aleksandar
 */
public class KriptoTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        CryptoJavniKljuc javni = new CryptoJavniKljuc();
        KeyPair keyPair= javni.generateKeyPair();
        
        CryptoTajniKljuc tajni = new CryptoTajniKljuc();
        Key key = tajni.generateKey();
        System.out.println(key.getEncoded().toString());
        byte[] cripto = javni.encrypt(key.getEncoded(), keyPair.getPublic());
        System.out.println(cripto.toString());
        byte[] dec = javni.decrypt(cripto, keyPair.getPrivate());
        System.out.println(dec.toString());
        
    }
    
}
