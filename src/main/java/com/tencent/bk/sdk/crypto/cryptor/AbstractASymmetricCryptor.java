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

package com.tencent.bk.sdk.crypto.cryptor;

import com.tencent.bk.sdk.crypto.util.Base64Util;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AbstractASymmetricCryptor implements ASymmetricCryptor {

    public abstract byte[] encrypt(PublicKey publicKey, byte[] message);

    public abstract byte[] decrypt(PrivateKey privateKey, byte[] encryptedMessage);

    @Override
    public String encrypt(PublicKey publicKey, String message) {
        byte[] encryptedMessage = encrypt(
            publicKey,
            message.getBytes(StandardCharsets.UTF_8)
        );
        return Base64Util.encodeContentToStr(encryptedMessage);
    }

    @Override
    public String decrypt(PrivateKey privateKey, String base64EncodedEncryptedMessage) {
        byte[] rawEncryptedMessage = Base64Util.decodeContentToByte(base64EncodedEncryptedMessage);
        byte[] decryptedMessage = decrypt(
            privateKey,
            rawEncryptedMessage
        );
        return new String(decryptedMessage, StandardCharsets.UTF_8);
    }
}
