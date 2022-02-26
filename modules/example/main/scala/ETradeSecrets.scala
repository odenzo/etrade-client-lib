import io.circe.Codec
import io.circe.generic.AutoDerivation

case class ETradeSecrets(sandbox: ETradeKeys, prod: ETradeKeys) extends AutoDerivation
