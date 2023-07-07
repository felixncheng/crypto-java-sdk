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
package com.tencent.bk.sdk.crypto.util;

import com.tencent.bk.sdk.crypto.exception.SM4DecryptException;
import com.tencent.bk.sdk.crypto.exception.SM4EncryptException;
import com.tencent.kona.crypto.KonaCryptoProvider;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import static com.tencent.kona.crypto.CryptoUtils.toHex;

/**
 * 国密对称加密算法SM4相关操作工具类
 */
@Slf4j
public class SM4Util {

    /**
     * Kona加解密算法Provider名称
     */
    private static final String PROVIDER_NAME_KONA_CRYPTO = "KonaCrypto";
    /**
     * 加解密算法
     */
    private static final String ALGORITHM_SM4 = "SM4";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String TRANSFORMATION_SM4_CTR_NO_PADDING = "SM4/CTR/NoPadding";
    /**
     * CTR工作模式下的初始化向量长度
     */
    private static final int CTR_IV_LENGTH = 16;
    /**
     * 随机数发生器，用于生成随机IV
     */
    private static final SecureRandom random = new SecureRandom();


    static {
        KonaCryptoProvider konaCryptoProvider = new KonaCryptoProvider();
        if (null == Security.getProvider(konaCryptoProvider.getName())) {
            Security.addProvider(konaCryptoProvider);
        }
    }

    /**
     * 使用SM4/GCM/NoPadding对明文内容进行加密
     *
     * @param key     密钥字节数组
     * @param message 明文字节数组
     * @return 首部含IV的密文字节数组
     * @throws SM4EncryptException 加密失败异常信息
     */
    public static byte[] encrypt(byte[] key, byte[] message) {
        try {
            byte[] iv = new byte[CTR_IV_LENGTH];
            random.nextBytes(iv);
            byte[] cipherBytes = encryptWithIV(key, iv, message);
            byte[] finalBytes = new byte[cipherBytes.length + CTR_IV_LENGTH];
            System.arraycopy(iv, 0, finalBytes, 0, iv.length);
            System.arraycopy(cipherBytes, 0, finalBytes, iv.length, cipherBytes.length);
            return finalBytes;
        } catch (Exception e) {
            throw new SM4EncryptException("Fail to encrypt message using SM4", e);
        }
    }

    /**
     * 使用SM4/GCM/NoPadding对密文内容进行解密
     *
     * @param key                    密钥字节数组
     * @param encryptedMessageWithIV 首部含IV的密文字节数组
     * @return 明文字节数组
     * @throws SM4DecryptException 解密失败异常信息
     */
    public static byte[] decrypt(byte[] key, byte[] encryptedMessageWithIV) {
        try {
            if (encryptedMessageWithIV.length < CTR_IV_LENGTH) {
                throw new SM4DecryptException(
                    "Unexpected encryptedMessageWithIV length:" + encryptedMessageWithIV.length
                );
            }
            byte[] iv = new byte[CTR_IV_LENGTH];
            System.arraycopy(encryptedMessageWithIV, 0, iv, 0, iv.length);
            int encryptedMessageLength = encryptedMessageWithIV.length - CTR_IV_LENGTH;
            byte[] encryptedMessage = new byte[encryptedMessageLength];
            System.arraycopy(encryptedMessageWithIV, CTR_IV_LENGTH, encryptedMessage, 0, encryptedMessage.length);
            return decryptWithIV(key, iv, encryptedMessage);
        } catch (Exception e) {
            throw new SM4DecryptException("Fail to decrypt encryptedMessageWithIV using SM4", e);
        }
    }

    private static byte[] encryptWithIV(byte[] key, byte[] iv, byte[] message) throws NoSuchPaddingException,
        NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
        InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (log.isDebugEnabled()) {
            log.debug("key=" + toHex(key) + ",iv=" + toHex(iv) + ",message=" + toHex(message));
        }
        SecretKey secretKey = new SecretKeySpec(paddingKey(key), ALGORITHM_SM4);
        IvParameterSpec paramSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_SM4_CTR_NO_PADDING, PROVIDER_NAME_KONA_CRYPTO);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
        return cipher.doFinal(message);
    }

    private static byte[] decryptWithIV(byte[] key, byte[] iv, byte[] encryptedMessage) throws NoSuchPaddingException,
        NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
        InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (log.isDebugEnabled()) {
            log.debug("key=" + toHex(key) + ",iv=" + toHex(iv) + ",encryptedMessage=" + toHex(encryptedMessage));
        }
        SecretKey secretKey = new SecretKeySpec(paddingKey(key), ALGORITHM_SM4);
        IvParameterSpec paramSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_SM4_CTR_NO_PADDING, PROVIDER_NAME_KONA_CRYPTO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
        return cipher.doFinal(encryptedMessage);
    }

    /**
     * 对加解密用的密钥进行16字节补齐，多余字节舍弃，不足字节补0
     *
     * @param key 密钥字节数组
     * @return 补齐为16字节的密钥数组
     */
    private static byte[] paddingKey(byte[] key) {
        byte[] paddedKey = new byte[16];
        int keyLength = key.length;
        for (int i = 0; i < 16; i++) {
            if (i < keyLength) {
                paddedKey[i] = key[i];
            } else {
                paddedKey[i] = 0;
            }
        }
        return paddedKey;
    }
}