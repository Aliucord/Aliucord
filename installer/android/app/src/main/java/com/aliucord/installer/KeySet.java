/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.installer;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public final class KeySet {
    public final X509Certificate publicKey;
    public final PrivateKey privateKey;
    public KeySet(X509Certificate pubKey, PrivateKey privKey) {
        publicKey = pubKey;
        privateKey = privKey;
    }
}
