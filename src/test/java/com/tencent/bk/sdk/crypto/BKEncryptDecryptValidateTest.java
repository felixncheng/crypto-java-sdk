/*
 * Tencent is pleased to support the open source community by making 蓝鲸加解密Java SDK（crypto-java-sdk） available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * 蓝鲸加解密Java SDK（crypto-java-sdk） is licensed under the MIT License.
 *
 * License for 蓝鲸加解密Java SDK（crypto-java-sdk）:
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.tencent.bk.sdk.crypto;

import com.tencent.bk.sdk.crypto.util.Base64Util;
import com.tencent.bk.sdk.crypto.util.ProviderUtil;
import com.tencent.bk.sdk.crypto.util.SM2Util;
import com.tencent.bk.sdk.crypto.util.SM4Util;
import com.tencent.kona.crypto.CryptoUtils;
import com.tencent.kona.crypto.spec.SM2SignatureParameterSpec;
import com.tencent.kona.pkix.PKIXUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.List;

import static com.tencent.kona.crypto.CryptoUtils.toBytes;
import static com.tencent.kona.crypto.CryptoUtils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 蓝鲸各语言间加解密互通验证
 */
@SuppressWarnings({"SameParameterValue", "unused"})
class BKEncryptDecryptValidateTest {

    private static ECPrivateKey privateKey = null;
    private static ECPublicKey publicKey = null;
    private static byte[] sm4Key;
    private static byte[] message;
    private static String messageStr;

    private static byte[] tongsuoSM2EncryptedMessage;
    private static byte[] pythonSM2EncryptedMessage;
    private static byte[] cppSM2EncryptedMessage;
    private static byte[] goSM2EncryptedMessage;
    private static byte[] nodeJsSM2EncryptedMessage;

    private static byte[] javaSignMessage;
    private static byte[] tongsuoSignMessage;
    private static byte[] pythonSignMessage;
    private static byte[] cppSignMessage;
    private static byte[] goSignMessage;
    private static byte[] nodeJsSignMessage;

    private static byte[] pythonSM4EncryptedMessage;
    private static byte[] cppSM4EncryptedMessage;
    private static byte[] goSM4EncryptedMessage;
    private static byte[] nodeJsSM4EncryptedMessage;

    static {
        ProviderUtil.addKonaCryptoProviderIfNotExist();
    }

    private static String readContentFromFile(String filePath) throws IOException {
        try {
            InputStream in = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String readContentFromTestResourceFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources").resolve(filePath));
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static String readKeyStrFromTestResourceFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources").resolve(filePath));
        return filterPem(lines, true);
    }

    private static byte[] readRawBytesFromTestResourceFile(String filePath) {
        InputStream in = BKEncryptDecryptValidateTest.class.getClassLoader().getResourceAsStream(filePath);
        if (in == null) {
            throw new RuntimeException("Cannot find resource:" + filePath);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try {
            int l;
            while ((l = in.read(buffer)) != -1) {
                bos.write(buffer, 0, l);
            }
            bos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Fail to readBytesFromClasspathFile", e);
        }
    }

    private static final String BEGIN = "-----BEGIN";
    private static final String END = "-----END";

    private static String filterPem(List<String> lines, boolean keepSeparator) {
        StringBuilder result = new StringBuilder();

        boolean begin = false;
        for (String line : lines) {
            if (line.startsWith(END)) {
                if (keepSeparator) {
                    result.append(line);
                }
                break;
            }

            if (line.startsWith(BEGIN)) {
                begin = true;
                if (keepSeparator) {
                    result.append(line).append("\n");
                }
                continue;
            }

            if (begin) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * 创建文件的父目录
     *
     * @param path 文件路径
     * @return 最终文件父目录是否存在
     */
    private static boolean checkOrCreateParentDirsForFile(String path) {
        File theFile = new File(path);
        File parentDir = theFile.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.out.println("mkdir parent dir fail!dir:" + parentDir.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    /**
     * 将字节数组数据保存至文件
     *
     * @param path         文件路径
     * @param contentBytes 字节数组
     * @return 是否保存成功
     */
    public static boolean saveBytesToFile(String path, byte[] contentBytes) {
        if (!checkOrCreateParentDirsForFile(path)) {
            return false;
        }
        boolean isSuccess = false;
        if (contentBytes == null || contentBytes.length == 0) {
            return false;
        }
        try (FileOutputStream out = new FileOutputStream(path)) {
            out.write(contentBytes);
            out.flush();
            File file = new File(path);
            isSuccess = file.setExecutable(true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    @BeforeAll
    static void setup() throws Exception {
        // SM2公私钥
        String x509PublicKeyStr = readKeyStrFromTestResourceFile("gm-crypto-resources/leaf-pubilc.key");
        publicKey = (ECPublicKey) PKIXUtils.getPublicKey("EC", x509PublicKeyStr);
        String privateKeyStr = readKeyStrFromTestResourceFile("gm-crypto-resources/leaf.key");
        privateKey = (ECPrivateKey) PKIXUtils.getRFC5915PrivateKey(privateKeyStr);
        // SM2签名
        javaSignMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.java.sign");
        tongsuoSignMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sign");
        pythonSignMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.python.sign");
        cppSignMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.sign.cpp");
        goSignMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.go.sign");
        nodeJsSignMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.nodejs.sign");
        // SM2加密数据
        tongsuoSM2EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.encrypt");
        pythonSM2EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.python.encrypt");
        cppSM2EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.encrypt.cpp");
        goSM2EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.go.encrypt");
        nodeJsSM2EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm2.nodejs.encrypt");

        // SM4密钥
        sm4Key = "Tencent".getBytes(StandardCharsets.UTF_8);
        // SM4明文
        // 避免Git将\n自动转为\r\n引起的问题，直接使用hex获取
        // 37045a07-8d63-494e-b2d1-8ded581b675a
        // message = readRawBytesFromTestResourceFile("gm-crypto-resources/text");
        message = toBytes("33373034356130372d386436332d343934652d623264312d3864656435383162363735610a");
        messageStr = readContentFromTestResourceFile("gm-crypto-resources/text").trim();
        // SM4密文
        pythonSM4EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm4.python.encrypt");
        cppSM4EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm4.cpp.encrypt");
        goSM4EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm4.go.encrypt");
        nodeJsSM4EncryptedMessage = readRawBytesFromTestResourceFile("gm-crypto-resources/text.sm4.nodejs.encrypt");
    }

    void testSM2Encrypt(byte[] publicKeyBytes, byte[] sm2EncryptedMessage, byte[] privateKeyBytes) {
        System.out.println("testSM2Encrypt:");
        boolean result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm2.java.encrypt",
            sm2EncryptedMessage
        );
        assertTrue(result);
        System.out.println("messageBytes=" + toHex(message)
            + ",base64=" + Base64Util.encodeContentToStr(message));
        System.out.println("publicKeyBytes=" + toHex(publicKeyBytes)
            + ",base64=" + Base64Util.encodeContentToStr(publicKeyBytes));
        System.out.println("privateKeyBytes=" + toHex(privateKeyBytes)
            + ",base64=" + Base64Util.encodeContentToStr(privateKeyBytes));
    }

    void testSM2Decrypt(byte[] sm2EncryptedMessage, byte[] privateKeyBytes) throws Exception {
        // Java
        byte[] sm2DecryptedMessage = SM2Util.decrypt(privateKeyBytes, sm2EncryptedMessage);
        String currentPath = new File("").getAbsolutePath();
        String path = currentPath + "/out/text.sm2.decrypt.java";
        boolean result = saveBytesToFile(path, sm2DecryptedMessage);
        assertTrue(result);
        assertEquals(readContentFromFile(path).trim(), messageStr);
        // Tongsuo
        sm2DecryptedMessage = SM2Util.decrypt(privateKeyBytes, tongsuoSM2EncryptedMessage);
        path = currentPath + "/out/text.sm2.decrypt";
        result = saveBytesToFile(path, sm2DecryptedMessage);
        assertTrue(result);
        assertEquals(readContentFromFile(path).trim(), messageStr);
        // Python
        sm2DecryptedMessage = SM2Util.decrypt(privateKeyBytes, pythonSM2EncryptedMessage);
        path = currentPath + "/out/text.sm2.decrypt.python";
        result = saveBytesToFile(path, sm2DecryptedMessage);
        assertTrue(result);
        assertEquals(readContentFromFile(path).trim(), messageStr);
        // Cpp
        sm2DecryptedMessage = SM2Util.decrypt(privateKeyBytes, cppSM2EncryptedMessage);
        path = currentPath + "/out/text.sm2.decrypt.cpp";
        result = saveBytesToFile(path, sm2DecryptedMessage);
        assertTrue(result);
        assertEquals(readContentFromFile(path).trim(), messageStr);
        // Go
        sm2DecryptedMessage = SM2Util.decrypt(privateKeyBytes, goSM2EncryptedMessage);
        path = currentPath + "/out/text.sm2.decrypt.go";
        result = saveBytesToFile(path, sm2DecryptedMessage);
        assertTrue(result);
        assertEquals(readContentFromFile(path).trim(), messageStr);
        // NodeJS
        sm2DecryptedMessage = SM2Util.decrypt(privateKeyBytes, nodeJsSM2EncryptedMessage);
        path = currentPath + "/out/text.sm2.decrypt.nodejs";
        result = saveBytesToFile(path, sm2DecryptedMessage);
        assertTrue(result);
        assertEquals(readContentFromFile(path).trim(), messageStr);
    }

    void testSM2Signature() throws Exception {
        System.out.println("testSM2Signature:");
        SM2SignatureParameterSpec paramSpec = new SM2SignatureParameterSpec(publicKey);
        Signature signer = Signature.getInstance("SM2", SM2Util.PROVIDER_NAME_KONA_CRYPTO);
        signer.setParameter(paramSpec);
        signer.initSign(privateKey);
        signer.update(message);
        byte[] signature = signer.sign();
        System.out.println("signature=" + toHex(signature));
        boolean result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm2.java.sign",
            signature
        );
        assertTrue(result);
        // SM2验签
        Signature verifier = Signature.getInstance("SM2", SM2Util.PROVIDER_NAME_KONA_CRYPTO);
        verifier.setParameter(paramSpec);
        System.out.println("publicKey:len=" + publicKey.getEncoded().length + ",hex=" + toHex(publicKey.getEncoded()));
        System.out.println("message:len=" + message.length + ",hex=" + toHex(message));
        // Java
        verifier.initVerify(publicKey);
        verifier.update(message);
        System.out.println("javaSignMessage:len=" + javaSignMessage.length + ",hex=" + toHex(javaSignMessage));
        boolean javaVerified = verifier.verify(javaSignMessage);
        assertTrue(javaVerified);
        // Tongsuo
        verifier.initVerify(publicKey);
        verifier.update(message);
        System.out.println("tongsuoSignMessage:len=" + tongsuoSignMessage.length + ",hex=" + toHex(tongsuoSignMessage));
        boolean tongsuoVerified = verifier.verify(tongsuoSignMessage);
        assertTrue(tongsuoVerified);
        // Python
        verifier.initVerify(publicKey);
        verifier.update(message);
        System.out.println("pythonSignMessage:len=" + pythonSignMessage.length + ",hex=" + toHex(pythonSignMessage));
        boolean pythonVerified = verifier.verify(pythonSignMessage);
        assertTrue(pythonVerified);
        // Cpp
        verifier.initVerify(publicKey);
        verifier.update(message);
        System.out.println("cppSignMessage:len=" + cppSignMessage.length + ",hex=" + toHex(cppSignMessage));
        boolean cppVerified = verifier.verify(cppSignMessage);
        assertTrue(cppVerified);
        // Go
        verifier.initVerify(publicKey);
        verifier.update(message);
        System.out.println("goSignMessage:len=" + goSignMessage.length + ",hex=" + toHex(goSignMessage));
        boolean goVerified = verifier.verify(goSignMessage);
        assertTrue(goVerified);
        // NodeJS
        verifier.initVerify(publicKey);
        verifier.update(message);
        System.out.println("nodeJsSignMessage:len=" + nodeJsSignMessage.length + ",hex=" + toHex(nodeJsSignMessage));
        boolean nodeJsVerified = verifier.verify(nodeJsSignMessage);
        assertTrue(nodeJsVerified);
    }

    void testSM2() throws Exception {
        byte[] publicKeyBytes = CryptoUtils.pubKey(publicKey.getW());
        byte[] sm2EncryptedMessage = SM2Util.encrypt(publicKeyBytes, message);
        byte[] privateKeyBytes = privateKey.getS().toByteArray();
        // SM2加密
        testSM2Encrypt(publicKeyBytes, sm2EncryptedMessage, privateKeyBytes);

        // SM2解密
        testSM2Decrypt(sm2EncryptedMessage, privateKeyBytes);

        // SM2签名
        testSM2Signature();
    }

    void testSM4() {
        // SM4加密
        byte[] sm4EncryptedMessage = SM4Util.encrypt(sm4Key, message);
        boolean result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm4.java.encrypt",
            sm4EncryptedMessage
        );
        assertTrue(result);
        // SM4解密
        // Java
        byte[] sm4DecryptedMessage = SM4Util.decrypt(sm4Key, sm4EncryptedMessage);
        result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm4.java.decrypt",
            sm4DecryptedMessage
        );
        assertTrue(result);
        // Python
        sm4DecryptedMessage = SM4Util.decrypt(sm4Key, pythonSM4EncryptedMessage);
        result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm4.python.decrypt",
            sm4DecryptedMessage
        );
        assertTrue(result);
        // Cpp
        sm4DecryptedMessage = SM4Util.decrypt(sm4Key, cppSM4EncryptedMessage);
        result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm4.cpp.decrypt",
            sm4DecryptedMessage
        );
        assertTrue(result);
        // Go
        sm4DecryptedMessage = SM4Util.decrypt(sm4Key, goSM4EncryptedMessage);
        result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm4.go.decrypt",
            sm4DecryptedMessage
        );
        assertTrue(result);
        // NodeJS
        sm4DecryptedMessage = SM4Util.decrypt(sm4Key, nodeJsSM4EncryptedMessage);
        result = saveBytesToFile(
            new File("").getAbsolutePath() + "/out/text.sm4.nodejs.decrypt",
            sm4DecryptedMessage
        );
        assertTrue(result);
    }

    @Test
    void testEncryptAndDecrypt() throws Exception {
        testSM2();
        testSM4();
    }

}
