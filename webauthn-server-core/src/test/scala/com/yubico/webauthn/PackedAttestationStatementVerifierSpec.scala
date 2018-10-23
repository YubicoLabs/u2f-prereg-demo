package com.yubico.webauthn

import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.test.Util
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.util.Success
import scala.util.Try


@RunWith(classOf[JUnitRunner])
class PackedAttestationStatementVerifierSpec extends FunSpec with Matchers {

  val verifier = new PackedAttestationStatementVerifier

  describe("PackedAttestationStatementVerifier") {

    describe("verify the X.509 certificate requirements") {

      it("which pass Klas's attestation certificate.") {

        val cert = Util.importCertFromPem(getClass.getResourceAsStream("klas-cert.pem"))

        val result = Try(verifier.verifyX5cRequirements(cert, ByteArray.fromHex("F8A011F38C0A4D15800617111F9EDC7D")))

        result shouldBe a [Success[_]]
        result.get should be (true)
      }

    }

    describe("supports attestation certificates with the algorithm") {
      it ("ECDSA.") {
        val (cert, key) = TestAuthenticator.generateAttestationCertificate()
        val (credential, _) = TestAuthenticator.createBasicAttestedCredential(
          attestationCertAndKey = Some((cert, key)),
          attestationStatementFormat = "packed"
        )

        val result = verifier.verifyAttestationSignature(
          credential.getResponse.getAttestation,
          new BouncyCastleCrypto().hash(credential.getResponse.getClientDataJSON)
        )

        key.getAlgorithm should be ("ECDSA")
        result should be (true)
      }

      it ("RSA.") {
        val (cert, key) = TestAuthenticator.generateRsaCertificate()
        val (credential, _) = TestAuthenticator.createBasicAttestedCredential(
          attestationCertAndKey = Some((cert, key)),
          attestationStatementFormat = "packed"
        )

        val result = verifier.verifyAttestationSignature(
          credential.getResponse.getAttestation,
          new BouncyCastleCrypto().hash(credential.getResponse.getClientDataJSON)
        )

        key.getAlgorithm should be ("RSA")
        result should be (true)
      }
    }

  }

}
