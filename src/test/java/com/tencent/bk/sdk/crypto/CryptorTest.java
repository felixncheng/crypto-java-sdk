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

import com.tencent.bk.sdk.crypto.cryptor.ASymmetricCryptor;
import com.tencent.bk.sdk.crypto.cryptor.ASymmetricCryptorFactory;
import com.tencent.bk.sdk.crypto.cryptor.SymmetricCryptor;
import com.tencent.bk.sdk.crypto.cryptor.SymmetricCryptorFactory;
import com.tencent.bk.sdk.crypto.cryptor.consts.CryptorNames;
import com.tencent.bk.sdk.crypto.exception.CryptoException;
import com.tencent.bk.sdk.crypto.util.RSAUtil;
import com.tencent.bk.sdk.crypto.util.SM2Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static com.tencent.kona.crypto.CryptoUtils.toHex;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptorTest {

    private static SymmetricCryptor noneCryptor = null;
    private static SymmetricCryptor sm4Cryptor = null;
    private static SymmetricCryptor aesCryptor = null;
    private static ASymmetricCryptor sm2Cryptor = null;
    private static ASymmetricCryptor rsaCryptor = null;

    private static final String EMPTY_KEY = "";
    private static final byte[] EMPTY_KEY_BYTES = EMPTY_KEY.getBytes(StandardCharsets.UTF_8);
    private static final String EMPTY_MESSAGE = "";
    private static final byte[] EMPTY_MESSAGE_BYTES = EMPTY_MESSAGE.getBytes(StandardCharsets.UTF_8);

    private static final String KEY = "中文符号~!@#$%^&*();test";
    private static final byte[] KEY_BYTES = KEY.getBytes(StandardCharsets.UTF_8);
    private static final String MESSAGE = "test中文符号~!@#$%^&*()_+=-0987654321`[]{};:'\"<>?,./";
    private static final byte[] MESSAGE_BYTES = MESSAGE.getBytes(StandardCharsets.UTF_8);

    @BeforeAll
    static void setup() {
        noneCryptor = SymmetricCryptorFactory.getCryptor(CryptorNames.NONE);
        sm4Cryptor = SymmetricCryptorFactory.getCryptor(CryptorNames.SM4);
        aesCryptor = SymmetricCryptorFactory.getCryptor(CryptorNames.AES);
        sm2Cryptor = ASymmetricCryptorFactory.getCryptor(CryptorNames.SM2);
        rsaCryptor = ASymmetricCryptorFactory.getCryptor(CryptorNames.RSA);
    }

    @Test
    void testNoneCryptor() {
        // 空值用例
        // key与message同时为空，字节数组
        byte[] emptyEncryptedMessageBytes = noneCryptor.encrypt(EMPTY_KEY_BYTES, EMPTY_MESSAGE_BYTES);
        System.out.println("emptyEncryptedMessageBytes=" + toHex(emptyEncryptedMessageBytes));
        assertArrayEquals(EMPTY_MESSAGE_BYTES, emptyEncryptedMessageBytes);
        byte[] emptyMessageBytesByEmptyKey = noneCryptor.decrypt(EMPTY_KEY_BYTES, emptyEncryptedMessageBytes);
        assertArrayEquals(emptyEncryptedMessageBytes, emptyMessageBytesByEmptyKey);
        // key与message同时为空，字符串
        String emptyEncryptedMessage = noneCryptor.encrypt(EMPTY_KEY, EMPTY_MESSAGE);
        System.out.println("emptyEncryptedMessage=" + emptyEncryptedMessage);
        assertEquals(EMPTY_MESSAGE, emptyEncryptedMessage);
        String emptyMessageByEmptyKey = noneCryptor.decrypt(EMPTY_KEY, emptyEncryptedMessage);
        assertEquals(emptyEncryptedMessage, emptyMessageByEmptyKey);

        // key为空，message不为空，字节数组
        byte[] emptyKeyEncryptedMessageBytes = noneCryptor.encrypt(EMPTY_KEY_BYTES, MESSAGE_BYTES);
        System.out.println("emptyKeyEncryptedMessageBytes=" + toHex(emptyKeyEncryptedMessageBytes));
        assertArrayEquals(MESSAGE_BYTES, emptyKeyEncryptedMessageBytes);
        byte[] messageBytesByEmptyKey = noneCryptor.decrypt(EMPTY_KEY_BYTES, emptyKeyEncryptedMessageBytes);
        assertArrayEquals(emptyKeyEncryptedMessageBytes, messageBytesByEmptyKey);
        // key为空，message不为空，字符串
        String emptyKeyEncryptedMessage = noneCryptor.encrypt(EMPTY_KEY, MESSAGE);
        System.out.println("emptyKeyEncryptedMessage=" + emptyKeyEncryptedMessage);
        assertEquals(MESSAGE, emptyKeyEncryptedMessage);
        String messageByEmptyKey = noneCryptor.decrypt(EMPTY_KEY, emptyKeyEncryptedMessage);
        assertEquals(emptyKeyEncryptedMessage, messageByEmptyKey);

        // key不为空，message为空，字节数组
        byte[] emptyMessageEncryptedMessageBytes = noneCryptor.encrypt(KEY_BYTES, EMPTY_MESSAGE_BYTES);
        System.out.println("emptyMessageEncryptedMessageBytes=" + toHex(emptyMessageEncryptedMessageBytes));
        assertArrayEquals(EMPTY_MESSAGE_BYTES, emptyMessageEncryptedMessageBytes);
        byte[] emptyMessageBytesByNormalKey = noneCryptor.decrypt(KEY_BYTES, emptyMessageEncryptedMessageBytes);
        assertArrayEquals(emptyMessageEncryptedMessageBytes, emptyMessageBytesByNormalKey);
        // key不为空，message为空，字符串
        String emptyMessageEncryptedMessage = noneCryptor.encrypt(KEY, EMPTY_MESSAGE);
        System.out.println("emptyMessageEncryptedMessage=" + emptyMessageEncryptedMessage);
        assertEquals(EMPTY_MESSAGE, emptyMessageEncryptedMessage);
        String emptyMessageByNormalKey = noneCryptor.decrypt(KEY, emptyMessageEncryptedMessage);
        assertEquals(emptyMessageEncryptedMessage, emptyMessageByNormalKey);

        // 一般用例，字节数组
        byte[] realEncryptedMessageBytes = noneCryptor.encrypt(KEY_BYTES, MESSAGE_BYTES);
        System.out.println("realEncryptedMessageBytes=" + toHex(realEncryptedMessageBytes));
        assertArrayEquals(MESSAGE_BYTES, realEncryptedMessageBytes);
        byte[] normalMessageBytes = noneCryptor.decrypt(KEY_BYTES, realEncryptedMessageBytes);
        assertArrayEquals(realEncryptedMessageBytes, normalMessageBytes);
        // 一般用例，字符串
        String realEncryptedMessage = noneCryptor.encrypt(KEY, MESSAGE);
        System.out.println("realEncryptedMessage=" + realEncryptedMessage);
        assertEquals(MESSAGE, realEncryptedMessage);
        String normalMessage = noneCryptor.decrypt(KEY, realEncryptedMessage);
        assertEquals(realEncryptedMessage, normalMessage);
    }

    @Test
    void testSM4Cryptor() throws Exception {
        // 空值用例
        // key与message同时为空，字节数组
        assertThrows(CryptoException.class, () -> sm4Cryptor.encrypt(EMPTY_KEY_BYTES, EMPTY_MESSAGE_BYTES));
        assertThrows(CryptoException.class, () -> sm4Cryptor.decrypt(EMPTY_KEY_BYTES, EMPTY_MESSAGE_BYTES));

        // key与message同时为空，字符串
        assertThrows(CryptoException.class, () -> sm4Cryptor.encrypt(EMPTY_KEY, EMPTY_MESSAGE));
        assertThrows(CryptoException.class, () -> sm4Cryptor.decrypt(EMPTY_KEY, EMPTY_MESSAGE));

        // key为空，message不为空，字节数组
        assertThrows(CryptoException.class, () -> sm4Cryptor.encrypt(EMPTY_KEY_BYTES, MESSAGE_BYTES));
        assertThrows(CryptoException.class, () -> sm4Cryptor.decrypt(EMPTY_KEY_BYTES, MESSAGE_BYTES));
        // key为空，message不为空，字符串
        assertThrows(CryptoException.class, () -> sm4Cryptor.encrypt(EMPTY_KEY, MESSAGE));
        assertThrows(CryptoException.class, () -> sm4Cryptor.decrypt(EMPTY_KEY, MESSAGE));

        // key不为空，message为空，字节数组
        byte[] emptyMessageEncryptedMessageBytes = sm4Cryptor.encrypt(KEY_BYTES, EMPTY_MESSAGE_BYTES);
        System.out.println("emptyMessageEncryptedMessageBytes=" + toHex(emptyMessageEncryptedMessageBytes));
        byte[] emptyMessageBytesByNormalKey = sm4Cryptor.decrypt(KEY_BYTES, emptyMessageEncryptedMessageBytes);
        assertArrayEquals(EMPTY_MESSAGE_BYTES, emptyMessageBytesByNormalKey);
        // key不为空，message为空，字符串
        String emptyMessageEncryptedMessage = sm4Cryptor.encrypt(KEY, EMPTY_MESSAGE);
        System.out.println("emptyMessageEncryptedMessage=" + emptyMessageEncryptedMessage);
        String emptyMessageByNormalKey = sm4Cryptor.decrypt(KEY, emptyMessageEncryptedMessage);
        assertEquals(EMPTY_MESSAGE, emptyMessageByNormalKey);

        // 一般用例，字节数组
        byte[] realEncryptedMessageBytes = sm4Cryptor.encrypt(KEY_BYTES, MESSAGE_BYTES);
        System.out.println("realEncryptedMessageBytes=" + toHex(realEncryptedMessageBytes));
        byte[] normalMessageBytes = sm4Cryptor.decrypt(KEY_BYTES, realEncryptedMessageBytes);
        assertArrayEquals(MESSAGE_BYTES, normalMessageBytes);
        // 一般用例，字符串
        String realEncryptedMessage = sm4Cryptor.encrypt(KEY, MESSAGE);
        System.out.println("realEncryptedMessage=" + realEncryptedMessage);
        String normalMessage = sm4Cryptor.decrypt(KEY, realEncryptedMessage);
        assertEquals(MESSAGE, normalMessage);

        // 流数据
        // 加密
        InputStream in = AESUtilTest.class.getClassLoader().getResourceAsStream("fileToEncrypt.txt");
        String outFilePath = new File("").getAbsolutePath() + "/out/encryptedFile.sm4Cryptor.encrypt";
        FileOutputStream out = new FileOutputStream(outFilePath);
        sm4Cryptor.encrypt(KEY, in, out);
        if (in != null) {
            in.close();
        }
        out.close();
        // 解密
        String inFilePath = new File("").getAbsolutePath() + "/out/encryptedFile.sm4Cryptor.encrypt";
        in = new FileInputStream(inFilePath);
        String decryptedFilePath = new File("").getAbsolutePath() + "/out/decryptedFile.sm4Cryptor.txt";
        out = new FileOutputStream(decryptedFilePath);
        sm4Cryptor.decrypt(KEY, in, out);
        in.close();
        out.close();
        // 验证
        in = AESUtilTest.class.getClassLoader().getResourceAsStream("fileToEncrypt.txt");
        assert in != null;
        String srcFileMd5 = DigestUtils.md5Hex(in);
        FileInputStream fis = new FileInputStream(decryptedFilePath);
        String decryptedFileMd5 = DigestUtils.md5Hex(fis);
        assertEquals(srcFileMd5, decryptedFileMd5);
        in.close();
        fis.close();
    }

    @Test
    void testAESCryptor() throws Exception {
        // 空值用例
        // key与message同时为空，字节数组
        assertThrows(CryptoException.class, () -> aesCryptor.encrypt(EMPTY_KEY_BYTES, EMPTY_MESSAGE_BYTES));
        assertThrows(CryptoException.class, () -> aesCryptor.decrypt(EMPTY_KEY_BYTES, EMPTY_MESSAGE_BYTES));
        // key与message同时为空，字符串
        assertThrows(CryptoException.class, () -> aesCryptor.encrypt(EMPTY_KEY, EMPTY_MESSAGE));
        assertThrows(CryptoException.class, () -> aesCryptor.decrypt(EMPTY_KEY, EMPTY_MESSAGE));

        // key为空，message不为空，字节数组
        assertThrows(CryptoException.class, () -> aesCryptor.encrypt(EMPTY_KEY_BYTES, MESSAGE_BYTES));
        assertThrows(CryptoException.class, () -> aesCryptor.decrypt(EMPTY_KEY_BYTES, MESSAGE_BYTES));
        // key为空，message不为空，字符串
        assertThrows(CryptoException.class, () -> aesCryptor.encrypt(EMPTY_KEY, MESSAGE));
        assertThrows(CryptoException.class, () -> aesCryptor.decrypt(EMPTY_KEY, MESSAGE));

        // key不为空，message为空，字节数组
        byte[] emptyMessageEncryptedMessageBytes = aesCryptor.encrypt(KEY_BYTES, EMPTY_MESSAGE_BYTES);
        System.out.println("emptyMessageEncryptedMessageBytes=" + toHex(emptyMessageEncryptedMessageBytes));
        byte[] emptyMessageBytesByNormalKey = aesCryptor.decrypt(KEY_BYTES, emptyMessageEncryptedMessageBytes);
        assertArrayEquals(EMPTY_MESSAGE_BYTES, emptyMessageBytesByNormalKey);
        // key不为空，message为空，字符串
        String emptyMessageEncryptedMessage = aesCryptor.encrypt(KEY, EMPTY_MESSAGE);
        System.out.println("emptyMessageEncryptedMessage=" + emptyMessageEncryptedMessage);
        String emptyMessageByNormalKey = aesCryptor.decrypt(KEY, emptyMessageEncryptedMessage);
        assertEquals(EMPTY_MESSAGE, emptyMessageByNormalKey);

        // 一般用例，字节数组
        byte[] realEncryptedMessageBytes = aesCryptor.encrypt(KEY_BYTES, MESSAGE_BYTES);
        System.out.println("realEncryptedMessageBytes=" + toHex(realEncryptedMessageBytes));
        byte[] normalMessageBytes = aesCryptor.decrypt(KEY_BYTES, realEncryptedMessageBytes);
        assertArrayEquals(MESSAGE_BYTES, normalMessageBytes);
        // 一般用例，字符串
        String realEncryptedMessage = aesCryptor.encrypt(KEY, MESSAGE);
        System.out.println("realEncryptedMessage=" + realEncryptedMessage);
        String normalMessage = aesCryptor.decrypt(KEY, realEncryptedMessage);
        assertEquals(MESSAGE, normalMessage);

        // 流数据
        // 加密
        InputStream in = AESUtilTest.class.getClassLoader().getResourceAsStream("fileToEncrypt.txt");
        String outFilePath = new File("").getAbsolutePath() + "/out/encryptedFile.aesCryptor.encrypt";
        FileOutputStream out = new FileOutputStream(outFilePath);
        aesCryptor.encrypt(KEY, in, out);
        if (in != null) {
            in.close();
        }
        out.close();
        // 解密
        String inFilePath = new File("").getAbsolutePath() + "/out/encryptedFile.aesCryptor.encrypt";
        in = new FileInputStream(inFilePath);
        String decryptedFilePath = new File("").getAbsolutePath() + "/out/decryptedFile.aesCryptor.txt";
        out = new FileOutputStream(decryptedFilePath);
        aesCryptor.decrypt(KEY, in, out);
        in.close();
        out.close();
        // 验证
        in = AESUtilTest.class.getClassLoader().getResourceAsStream("fileToEncrypt.txt");
        assert in != null;
        String srcFileMd5 = DigestUtils.md5Hex(in);
        FileInputStream fis = new FileInputStream(decryptedFilePath);
        String decryptedFileMd5 = DigestUtils.md5Hex(fis);
        assertEquals(srcFileMd5, decryptedFileMd5);
        in.close();
        fis.close();
    }

    @Test
    void testSM2Cryptor() {
        KeyPair keyPair = SM2Util.genKeyPair();
        // 仅支持非空message
        // 字节数组
        byte[] encryptedMessageBytes = sm2Cryptor.encrypt(keyPair.getPublic(), MESSAGE_BYTES);
        byte[] decryptedMessageBytes = sm2Cryptor.decrypt(keyPair.getPrivate(), encryptedMessageBytes);
        assertArrayEquals(MESSAGE_BYTES, decryptedMessageBytes);

        // 字符串
        String encryptedMessageStr = sm2Cryptor.encrypt(keyPair.getPublic(), MESSAGE);
        System.out.println("encryptedMessageStr=" + encryptedMessageStr);
        String decryptedMessageStr = sm2Cryptor.decrypt(keyPair.getPrivate(), encryptedMessageStr);
        System.out.println("decryptedMessageStr=" + decryptedMessageStr);
        assertEquals(MESSAGE, decryptedMessageStr);
    }

    @Test
    void testRSACryptor() {
        KeyPair keyPair = RSAUtil.genKeyPair();
        // 仅支持非空message
        // 字节数组
        byte[] encryptedMessageBytes = rsaCryptor.encrypt(keyPair.getPublic(), MESSAGE_BYTES);
        byte[] decryptedMessageBytes = rsaCryptor.decrypt(keyPair.getPrivate(), encryptedMessageBytes);
        assertArrayEquals(MESSAGE_BYTES, decryptedMessageBytes);

        // 字符串
        String encryptedMessageStr = rsaCryptor.encrypt(keyPair.getPublic(), MESSAGE);
        System.out.println("encryptedMessageStr=" + encryptedMessageStr);
        String decryptedMessageStr = rsaCryptor.decrypt(keyPair.getPrivate(), encryptedMessageStr);
        System.out.println("decryptedMessageStr=" + decryptedMessageStr);
        assertEquals(MESSAGE, decryptedMessageStr);
    }

}
