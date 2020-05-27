package io

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Validator {
  import Validator._

  def validate(signature: String, body: String)(implicit lineConfig: LineConfig): Boolean = {
    val mac = Mac.getInstance(SignatureAlgorithm)

    mac.init(new SecretKeySpec(lineConfig.channelSecret.getBytes, SignatureAlgorithm))

    val hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8))

    Base64.getEncoder.encodeToString(hash) == signature
  }
}

object Validator {
  final private val SignatureAlgorithm = "HmacSHA256"
}
