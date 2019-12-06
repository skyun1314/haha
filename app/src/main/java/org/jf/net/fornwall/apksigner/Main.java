package org.jf.net.fornwall.apksigner;

import com.example.myapplication.hahahaha;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;


/** Sign files from the command line using zipsigner-lib. */
public class Main {



	public static void main(String... argList) throws Exception {





		String keystorePath = argList[0];
		String inputFile =  argList[1];
		String outputFile = argList[2];

		char[] keyPassword= argList[3].toCharArray();;

		File keystoreFile = new File(keystorePath);
		if (!keystoreFile.exists()) {
			String alias = "alias";
			hahahaha.Log.e("Creating new keystore (using '" + new String(keyPassword) + "' as password and '"
					+ alias + "' as the key alias).");
			CertCreator.DistinguishedNameValues nameValues = new CertCreator.DistinguishedNameValues();
			nameValues.setCommonName("APK Signer");
			nameValues.setOrganization("Earth");
			nameValues.setOrganizationalUnit("Earth");
			CertCreator.createKeystoreAndKey(keystorePath, keyPassword, "RSA", 2048, alias, keyPassword, "SHA1withRSA",
					30, nameValues);
		}

		KeyStore keyStore = KeyStoreFileManager.loadKeyStore(keystorePath, null);
		String alias = keyStore.aliases().nextElement();

		X509Certificate publicKey = (X509Certificate) keyStore.getCertificate(alias);
		try {
			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword);
			ZipSigner.signZip(publicKey, privateKey, "SHA1withRSA", inputFile, outputFile);
		} catch (UnrecoverableKeyException e) {
			System.err.println("apksigner: Invalid key password.");
			System.exit(1);
		}
	}

}
