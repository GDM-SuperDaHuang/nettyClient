package com.netty.message;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;

public class DHKeyInfo {
    private static DHKeyInfo instance;
    // 私有构造函数
    private DHKeyInfo() {
    }

    // 双重检查锁定获取实例
    public static DHKeyInfo getInstance() {
        if (instance == null) {
            synchronized (DHKeyInfo.class) {
                if (instance == null) {
                    instance = new DHKeyInfo();
                }
            }
        }
        return instance;
    }
    private BigInteger p;  // p

    public void setP(BigInteger p) {
        this.p = p;
    }

    //测试消息
    private BigInteger privateKey;  // 私钥
    private BigInteger publicKey;   // 服务器公钥B
    private BigInteger sharedKey;   // 共享密钥K

    public DHKeyInfo(BigInteger privateKey, BigInteger publicKey, BigInteger sharedKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.sharedKey = sharedKey;
    }


    // 密钥派生函数
    private SecretKey deriveKey(BigInteger sharedKey) throws Exception {
        String password = sharedKey.toString();
        String salt = "randomSalt";
        int iterations = 65536;
        int keyLength = 256;
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterations, keyLength);
        byte[] derivedKeyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(derivedKeyBytes, "AES");
    }

    // 加密函数
    private byte[] encrypt(String plaintext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext.getBytes());
    }

    // 字节数组转十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getSharedKey() {
        return sharedKey;
    }

    public void setPrivateKey(BigInteger privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(BigInteger publicKey) {
        this.publicKey = publicKey;
    }

    public void setSharedKey(BigInteger sharedKey) {
        this.sharedKey = sharedKey;
    }
}
