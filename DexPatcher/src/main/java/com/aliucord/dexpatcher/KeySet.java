/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.dexpatcher;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class KeySet {
    public final X509Certificate publicKey;
    public final PrivateKey privateKey;
    public KeySet(X509Certificate pubKey, PrivateKey privKey) {
        publicKey = pubKey;
        privateKey = privKey;
    }
}
